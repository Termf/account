package com.binance.account.service.certificate.impl;

import com.binance.account.common.query.SearchResult;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.UserChainAddressAudit;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserChainAddressAuditMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.certificate.IUserChainAddress;
import com.binance.account.vo.security.request.ChainAddressAnalyzeRequest;
import com.binance.account.vo.security.request.ChainAddressAuditRequest;
import com.binance.account.vo.user.response.AuditChainResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static javax.management.timer.Timer.ONE_DAY;

/**
 * @author alex 2018/9/12
 */
@Service
@Log4j2
public class UserChainAddressBusiness implements IUserChainAddress {

    @Resource
    private UserChainAddressAuditMapper userChainAddressAuditMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserIndexMapper userIndexMapper;

    @Override
    public APIResponse<?> submitChainAddressAudit(APIRequest<ChainAddressAnalyzeRequest> request) {
        ChainAddressAnalyzeRequest addressAnalyzeRequest = request.getBody();

        if (addressAnalyzeRequest.getUserId() == null) {
            return APIResponse.getErrorJsonResult("用户ID不能为空");
        }

        if (StringUtils.isEmpty(addressAnalyzeRequest.getChainAddress())) {
            return APIResponse.getErrorJsonResult("地址不能为空");
        }

        UserChainAddressAudit userChainAddressAudit = new UserChainAddressAudit();
        userChainAddressAudit.setCoin(addressAnalyzeRequest.getCoin());
        userChainAddressAudit.setUserId(addressAnalyzeRequest.getUserId());
        userChainAddressAudit.setStatus(UserChainAddressAudit.Status.PENDING.ordinal());
        userChainAddressAudit.setType(addressAnalyzeRequest.getDirection() == ChainAddressAnalyzeRequest.Direction.DEPOSIT ? 0 : 1);
        userChainAddressAudit.setAddress(addressAnalyzeRequest.getChainAddress());
        userChainAddressAudit.setChainalysisResult(addressAnalyzeRequest.getAnalyzeResult());
        userChainAddressAudit.setCreateTime(new Date());
        userChainAddressAudit.setUpdateTime(new Date());
        userChainAddressAudit.setCreatedBy(addressAnalyzeRequest.getCreator());
        userChainAddressAudit.setBizId(addressAnalyzeRequest.getBizId());
        userChainAddressAudit.setChannel(addressAnalyzeRequest.getChannel());
        userChainAddressAudit.setStatus(addressAnalyzeRequest.getStatus());

        try {
            if (userChainAddressAuditMapper.insert(userChainAddressAudit) > 0) {
                return APIResponse.getOKJsonResult();
            }
        } catch (Exception ex) {
            log.error("Failed to insert into user_chain_addr_audit, {}", ex);
            return APIResponse.getErrorJsonResult("保存失败");
        }


        return APIResponse.getErrorJsonResult("提交区块链地址人工审核失败");
    }

    @Override
    public APIResponse<?> auditChainAddress(APIRequest<ChainAddressAuditRequest> request) {
        ChainAddressAuditRequest addressAuditRequest = request.getBody();

        if (addressAuditRequest.getId() == null) {
            return APIResponse.getErrorJsonResult("审核ID不能为空");
        }

        if (addressAuditRequest.getUserId() == null) {
            return APIResponse.getErrorJsonResult("用户ID不能为空");
        }

        if (StringUtils.isEmpty(addressAuditRequest.getStatus()) ||
                !StringUtils.isNumeric(addressAuditRequest.getStatus())) {
            return APIResponse.getErrorJsonResult("状态不能为空或者格式不正确");
        }

        UserChainAddressAudit existedAudit = userChainAddressAuditMapper.selectByPrimaryKey(addressAuditRequest.getId());
        if (existedAudit == null || !addressAuditRequest.getUserId().equals(existedAudit.getUserId())) {
            return APIResponse.getErrorJsonResult("未找到原始记录");
        }

        UserChainAddressAudit userChainAddressAudit = new UserChainAddressAudit();
        userChainAddressAudit.setId(addressAuditRequest.getId());
        userChainAddressAudit.setUserId(addressAuditRequest.getUserId());
        userChainAddressAudit.setStatus(Integer.valueOf(addressAuditRequest.getStatus()));
        userChainAddressAudit.setRefundAddress(addressAuditRequest.getRefundAddress());
        userChainAddressAudit.setRefundAddressTag(addressAuditRequest.getRefundAddressTag());
        userChainAddressAudit.setComment(addressAuditRequest.getComment());
        userChainAddressAudit.setUpdateTime(new Date());
        userChainAddressAudit.setUpdatedBy(addressAuditRequest.getAuditor());

        if (userChainAddressAudit.getStatus().equals(existedAudit.getStatus())) {

            return APIResponse.getErrorJsonResult("状态无变化");
        }

        try {
            int affectedRows = 0;
            log.info("change user chain address audit status to {}, userId={}", userChainAddressAudit.getStatus(), userChainAddressAudit.getUserId());
            if (userChainAddressAudit.getStatus().equals(UserChainAddressAudit.Status.PENDING.ordinal())) {
                if (existedAudit.getStatus().equals(UserChainAddressAudit.Status.STOP_SERVICE.ordinal())) {
                    // 从停止服务变更为待处理时，所有记录的状态变为待处理
                    log.info("reset all records to pending, userId={}", userChainAddressAudit.getUserId());
                    affectedRows = userChainAddressAuditMapper.resetToPending(userChainAddressAudit);
                }
                else {
                    // 更新单条记录至待确认
                    affectedRows = userChainAddressAuditMapper.updateByPrimaryKeySelective(userChainAddressAudit);
                }
            }
            else if (userChainAddressAudit.getStatus().equals(UserChainAddressAudit.Status.STOP_SERVICE.ordinal())) {
                // 如果用户有一条记录是停止服务状态，则所有记录都为停止服务状态
                affectedRows = userChainAddressAuditMapper.updateAllRecordStatus(userChainAddressAudit);
            }
            // 更新单条记录到已豁免或者已拒绝或者已退款
            else {
                affectedRows = userChainAddressAuditMapper.updateByPrimaryKeySelective(userChainAddressAudit);
            }
            AuditChainResponse auditChainResponse = null;
            if (affectedRows > 0) {
                // 返回 待处理+豁免 数量，如果大于0，继续锁定用户提币，等于0意味着都是已停止服务，开启提币+禁止交易
                // 强制从主库读，避免读写延迟问题，确保页面展示正确的用户状态
                HintManager hintManager = null;

                try {
                    hintManager = HintManager.getInstance();
                    hintManager.setMasterRouteOnly();
                    //刷新出入金白名单地址缓存
                    refreshWhiteAddrCache();
                    List<Map<String, Object>> auditCnts = userChainAddressAuditMapper.getProcessingCount(userChainAddressAudit.getUserId());
                    long totalCnt = 0;
                    long stopServiceCnt = 0;
                    long exemptCnt = 0;
                    long refuseCnt = 0;
                    long pendingCnt = 0;
                    long refundCnt = 0;
                    for (Map<String, Object> statusCnt : auditCnts) {
                        Integer statusKey = (Integer) statusCnt.get("status");
                        Long num = (Long) statusCnt.getOrDefault("statusCount", 0);
                        // 0 待处理 1 停止服务  2 豁免  3 拒绝  4 已退款
                        if (statusKey == 1) {
                            stopServiceCnt = num;
                        } else if (statusKey == 2) {
                            exemptCnt = num;
                        } else if (statusKey == 3) {
                            refuseCnt = num;
                        } else if (statusKey == 4) {
                            refundCnt = num;
                        } else if (statusKey == 0) {
                            pendingCnt = num;
                        } else {
                            log.error("wrong status found!");
                        }
                        totalCnt += num;
                    }

                    if (totalCnt > 0) {
                        auditChainResponse = new AuditChainResponse();
                        auditChainResponse.setStopServiceCount(stopServiceCnt);
                        auditChainResponse.setExemptCount(exemptCnt);
                        auditChainResponse.setRefuseCount(refuseCnt);
                        auditChainResponse.setPendingCount(pendingCnt);
                        auditChainResponse.setRefundCount(refundCnt);
                        auditChainResponse.setTotalCount(totalCnt);
                    }
                } finally {
                    if (null != hintManager) {
                        hintManager.close();
                    }
                }

                return APIResponse.getOKJsonResult(auditChainResponse);
            }
        } catch (Exception ex) {
            log.error("Failed to update user chainAddress audit status, {}", ex);
            return APIResponse.getErrorJsonResult("区块链地址人工审核失败");
        }

        return APIResponse.getErrorJsonResult("区块链地址人工审核失败");
    }

    @Override
    public APIResponse<SearchResult<UserChainAddressAudit>> getChainAddressAuditPage(APIRequest<ChainAddressAuditRequest> request) {
        ChainAddressAuditRequest addressAuditQueryRequest = request.getBody();

        if (addressAuditQueryRequest != null) {
            if (StringUtils.isNotBlank(addressAuditQueryRequest.getEmail())) {
                final User user = userMapper.queryByEmail(addressAuditQueryRequest.getEmail());
                if (user == null) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                } else {
                    addressAuditQueryRequest.setUserId(user.getUserId());
                }
            }

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("userId", addressAuditQueryRequest.getUserId());
            if (StringUtils.isNotBlank(addressAuditQueryRequest.getStatus())) {
                queryParams.put("status", addressAuditQueryRequest.getStatus());
            }
            if (StringUtils.isNotBlank(addressAuditQueryRequest.getType())) {
                queryParams.put("type", addressAuditQueryRequest.getType());
            }
            if (StringUtils.isNotBlank(addressAuditQueryRequest.getCoin())) {
                queryParams.put("coin", addressAuditQueryRequest.getCoin());
            }
            if (addressAuditQueryRequest.getId()!=null) {
                queryParams.put("id", addressAuditQueryRequest.getId());
            }


            queryParams.put("order", addressAuditQueryRequest.getOrder());
            queryParams.put("sort", addressAuditQueryRequest.getSort());
            queryParams.put("offset", addressAuditQueryRequest.getOffset());
            queryParams.put("limit", addressAuditQueryRequest.getLimit());
            List<UserChainAddressAudit> userChainAddressAuditList = userChainAddressAuditMapper.getAuditPage(queryParams);

            // fullfill email
            if (userChainAddressAuditList != null && !userChainAddressAuditList.isEmpty()) {
                Set<Long> userIds = userChainAddressAuditList.stream().map(UserChainAddressAudit::getUserId).collect(Collectors.toSet());
                List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
                Map<Long, String> userEmailMapping = userIndices.stream().collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));
                for (UserChainAddressAudit audit: userChainAddressAuditList) {
                    audit.setEmail(userEmailMapping.get(audit.getUserId()));
                }
            }

            SearchResult<UserChainAddressAudit> searchResult = new SearchResult<>();
            searchResult.setRows(userChainAddressAuditList);
            searchResult.setTotal(userChainAddressAuditMapper.getAuditCount(queryParams));
            return APIResponse.getOKJsonResult(searchResult);
        }
        return APIResponse.getErrorJsonResult("获取用户地址审核列表失败");

    }

    @Override
    public APIResponse<Boolean> isAddressInWhitelist(String address) {
        String whiteList = RedisCacheUtils.get(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES);
        if(StringUtils.isBlank(whiteList)){
            refreshWhiteAddrCache();
            whiteList = RedisCacheUtils.get(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES);
        }
        if(StringUtils.isBlank(whiteList)){
            log.warn("the whiteList is blank, the address is:{}", address);
            return APIResponse.getOKJsonResult(false);
        }else {
            return APIResponse.getOKJsonResult(whiteList.contains(address));
        }
    }

    @Override
    public String getWhiteAddresses() {
        StringBuilder result = new StringBuilder();
        Integer pageSize = 5000;
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("type", 1);
        queryParams.put("status", 2);
        int total = userChainAddressAuditMapper.getAuditCount(queryParams);
        int circleCount = total % pageSize == 0 ? total / pageSize : (total / pageSize + 1);
        //去重用
        Set<String> whiteAddress = new HashSet<>();
        for (int i = 0; i < circleCount; i++) {
            Integer start = i * pageSize;
            queryParams.put("offset", start);
            queryParams.put("limit", pageSize);
            List<UserChainAddressAudit> userChainAddressAuditList = userChainAddressAuditMapper.getAuditPage(queryParams);
            for (UserChainAddressAudit audit : userChainAddressAuditList) {
                //检测是否有审核时间更晚的拒绝记录, 如果有则不加入白名单
                Boolean hasRejectRec = false;
                List<UserChainAddressAudit> rejectList = userChainAddressAuditMapper.selectRejectRecByAddress(audit.getAddress());
                for (UserChainAddressAudit rejectAudit : rejectList) {
                    if (rejectAudit.getUpdateTime().after(audit.getUpdateTime())) {
                        hasRejectRec = true;
                        break;
                    }
                }
                if (hasRejectRec || whiteAddress.contains(audit.getAddress())) {
                    continue;
                }
                whiteAddress.add(audit.getAddress());
                if (StringUtils.isBlank(result)) {
                    result.append(audit.getAddress());
                } else {
                    result.append("," + audit.getAddress());
                }
            }
        }
        log.info("the size of whiteAddress is:{}, the whiteAddress is:{}, the total is:{}", whiteAddress.size(), result, total);
        return result.toString();
    }

    @Override
    public void refreshWhiteAddrCache() {
        log.info("start refresh white addr cache");
        RedisCacheUtils.del(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES);
        String whiteList = getWhiteAddresses();
        if(StringUtils.isNotBlank(whiteList)) {
            RedisCacheUtils.set(AccountConstants.GLOBAL_RISK_CHECK_WHITE_ADDRESSES, whiteList, ONE_DAY);
        }
    }
}
