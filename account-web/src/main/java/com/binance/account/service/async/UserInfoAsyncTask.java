package com.binance.account.service.async;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserAgentReward;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.margin.IsolatedMarginUserBindingMapper;
import com.binance.account.data.mapper.user.UserAgentRewardMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.integration.futureservice.CommissionApiClient;
import com.binance.account.integration.isolatedMargin.VipBridgeApiClient;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.InboxUtils;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.CouplingCalculationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.binance.account.integration.margin.VipApiClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.vo.user.request.UpdateUserAgentRewardRequest;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * Created by Fei.Huang on 2018/12/12.
 */
@Slf4j
@Component
public class UserInfoAsyncTask {

    @Autowired
    private SubUserBindingMapper subUserBindingMapper;
    @Autowired
    protected UserInfoMapper userInfoMapper;
    @Resource
    private UserAgentRewardMapper userAgentRewardMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Resource
    private AccountApi accountApi;
    @Resource
    private UserMapper userMapper;
    @Resource
    private IMsgNotification iMsgNotification;
    @Autowired
    private InboxMessageTextApi inboxMessageTextApi;
    @Autowired
    private ISubUserAdmin subUserAdminBusiness;
    @Value("${autoEnable.subUserFunction.tradeLevel:false}")
    private boolean autoEnableSubUserFunctionDueToTradeLevel;
    @Autowired
    private VipApiClient vipApiClient;
    @Autowired
    private VipBridgeApiClient vipBridgeApiClient;
    @Autowired
    private IsolatedMarginUserBindingMapper isolatedMarginUserBindingMapper;
    @Autowired
    private AccountApiClient accountApiClient;
    @Autowired
    private CommissionApiClient commissionApiClient;

    @Async("userInfoAsync")
    public void updateSubUserAgentId(final Long parentUserId, final Long agentId) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    int subResult = this.updateAgentId(subUserId, agentId);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("updateSubUserAgentId error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("updateSubUserAgentId for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId, total,
                    success);
        }
    }

    @Async("userInfoAsync")
    public void batchUpdateSubUsersAgentId(final Long parentUserId, final UpdateUserAgentRewardRequest userInfo,
            final String nextBatchId) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    UpdateUserAgentRewardRequest newRequest = new UpdateUserAgentRewardRequest();
                    BeanUtils.copyProperties(userInfo, newRequest);
                    newRequest.setUserId(subUserId);
                    int subResult = this.batchUpdateAgentId(newRequest, nextBatchId);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("batchUpdateSubUsersAgentId error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("batchUpdateSubUsersAgentId for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId,
                    total, success);
        }
    }

    @Async("userInfoAsync")
    public void updateSubUsersAgentRatio(final Long parentUserId, final UpdateUserAgentRewardRequest requestBody) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    UpdateUserAgentRewardRequest newRequest = new UpdateUserAgentRewardRequest();
                    BeanUtils.copyProperties(requestBody, newRequest);
                    newRequest.setUserId(subUserId);
                    int subResult = this.updateUserAgentRatio(newRequest);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("updateSubUsersAgentRatio error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("updateSubUsersAgentRatio for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId,
                    total, success);
        }
    }

    @Async("userInfoAsync")
    public void updateSubUserAgentRewardRatio(final Long parentUserId, final BigDecimal agentRewardRatio) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    int subResult = this.updateAgentRewardRatio(subUserId, agentRewardRatio);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("updateSubUserAgentRewardRatio error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("updateAgentRewardRatio for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId, total,
                    success);
        }
    }

    @Async("userInfoAsync")
    public void setSubUsersCommission(final Long parentUserId, final BigDecimal buyerCommission,
            final BigDecimal sellerCommission, final BigDecimal takerCommission, final BigDecimal makerCommission,
            final String modifyReason, final String expectedRestoreTime) {

        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    int subResult = this.setCommission(subUserId, buyerCommission, sellerCommission,
                            takerCommission, makerCommission, modifyReason, expectedRestoreTime);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("setSubUsersCommission error, subUserId:%s.", subUserId), e);
                }

                try {
                    setMarginUserCommission(subUserId, buyerCommission, sellerCommission,
                            takerCommission, makerCommission, modifyReason, expectedRestoreTime);
                } catch (Exception e) {
                    log.error(String.format("setMarginUserCommission error, subUserId:%s.", subUserId), e);
                }


                try {
                    setIsolatedMarginUserCommission(subUserId, buyerCommission, sellerCommission,
                            takerCommission, makerCommission, modifyReason, expectedRestoreTime);
                } catch (Exception e) {
                    log.error(String.format("setIsolatedMarginUserCommission error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("setSubUsersCommission for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId, total,
                    success);
        }

    }

    @Async("userInfoAsync")
    public void setMarginUserCommission(final Long rootUserId, final BigDecimal buyerCommission,
                                      final BigDecimal sellerCommission, final BigDecimal takerCommission, final BigDecimal makerCommission,
                                      final String modifyReason, final String expectedRestoreTime) {
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
        if(null==userInfo||null==userInfo.getMarginUserId()){
            log.info("setMarginUserCommission: userInfo is null or marginUserId is null  rootUserId={} ",rootUserId);
            return;
        }
        try {
            int result = this.setCommission(userInfo.getMarginUserId(), buyerCommission, sellerCommission,
                    takerCommission, makerCommission, modifyReason, expectedRestoreTime);
            log.info("setMarginUserCommission: finish result={},rootUserId={},marginUserId={} ",result,rootUserId,userInfo.getMarginUserId());
        } catch (Exception e) {
            log.error(String.format("setMarginUserCommission error, rootUserId:%s.", rootUserId), e);
        }

    }


    @Async("userInfoAsync")
    public void setIsolatedMarginUserCommission(final Long rootUserId, final BigDecimal buyerCommission,
                                        final BigDecimal sellerCommission, final BigDecimal takerCommission, final BigDecimal makerCommission,
                                        final String modifyReason, final String expectedRestoreTime) {

        List<Long> isolatedMarginUserIdList=isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(rootUserId);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(isolatedMarginUserIdList)){
            log.info("setIsolatedMarginUserCommission: isolatedMarginUserIdList is null  rootUserId={} ",rootUserId);
            return;
        }
        for(Long isolatedMarginUserId:isolatedMarginUserIdList){
            try {
                int result = this.setCommission(isolatedMarginUserId, buyerCommission, sellerCommission,
                        takerCommission, makerCommission, modifyReason, expectedRestoreTime);
                log.info("setIsolatedMarginUserCommission: finish result={},rootUserId={},isolatedMarginUserId={} ",result,rootUserId,isolatedMarginUserId);
            } catch (Exception e) {
                log.error(String.format("setIsolatedMarginUserCommission error, rootUserId:%s ,isolatedMarginUserId:%s", rootUserId,isolatedMarginUserId), e);
            }
        }
    }


    @Async("userInfoAsync")
    public void setSubUserTradeLevel(final Long parentUserId, final Integer tradeLevel) {
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            int total = subUserIds.size();
            int success = 0;
            for (Long subUserId : subUserIds) {
                try {
                    int subResult = this.setTradeLevel(subUserId, tradeLevel);
                    if (1 == subResult) {
                        success++;
                    }
                } catch (Exception e) {
                    log.error(String.format("setSubUserTradeLevel error, subUserId:%s.", subUserId), e);
                }

                try {
                    setMarginUserTradeLevel(subUserId, tradeLevel);
                } catch (Exception e) {
                    log.error(String.format("setMarginUserTradeLevel error, subUserId:%s.", subUserId), e);
                }

                try {
                    setFutureUserTradeLevel(subUserId, tradeLevel);
                } catch (Exception e) {
                    log.error(String.format("setFutureUserTradeLevel error, subUserId:%s.", subUserId), e);
                }

                try {
                    setIsolatedMarginUserTradeLevel(subUserId, tradeLevel);
                } catch (Exception e) {
                    log.error(String.format("setIsolatedMarginUserTradeLevel error, subUserId:%s.", subUserId), e);
                }
            }
            log.info("setTradeLevel for subUsers, parentUserId:{}, total:{}, success:{}", parentUserId, total, success);
        }
    }

    @Async("userInfoAsync")
    public void setMarginUserTradeLevel(final Long rootUserId, final Integer tradeLevel) {
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
        if(null==userInfo||null==userInfo.getMarginUserId()){
            log.info("setMarginUserTradeLevel: userInfo is null or marginUserId is null  rootUserId={} ",rootUserId);
            return;
        }
        try {
            int result = this.setTradeLevel(userInfo.getMarginUserId(), tradeLevel);
            log.info("setMarginUserTradeLevel: finish result={},rootUserId={},marginUserId={} ",result,rootUserId,userInfo.getMarginUserId());
        } catch (Exception e) {
            log.error(String.format("setMarginUserTradeLevel error, rootUserId:%s,marginUserId:%s", rootUserId,userInfo.getMarginUserId()), e);
        }
        try {
            //同步给margin vip等级
            vipApiClient.updateVipLevel(rootUserId, tradeLevel);
        } catch (Exception e) {
            log.error(String.format("vipApiClient error, rootUserId:%s,marginUserId:%s", rootUserId,userInfo.getMarginUserId()), e);
        }
    }



    @Async("userInfoAsync")
    public void setIsolatedMarginUserTradeLevel(final Long rootUserId, final Integer tradeLevel) {
        List<Long> isolatedMarginUserIdList=isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(rootUserId);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(isolatedMarginUserIdList)){
            log.info("setIsolatedMarginUserTradeLevel: isolatedMarginUserIdList is null  rootUserId={} ",rootUserId);
            return;
        }
        for(Long isolatedMarginUserId:isolatedMarginUserIdList){
            try {
                int result = this.setTradeLevel(isolatedMarginUserId, tradeLevel);
                log.info("setIsolatedMarginUserTradeLevel: finish result={},rootUserId={},isolatedMarginUserId={} ",result,rootUserId,isolatedMarginUserId);
            } catch (Exception e) {
                log.error(String.format("setIsolatedMarginUserTradeLevel error, rootUserId:%s,isolatedMarginUserId:%s", rootUserId,isolatedMarginUserId), e);
            }
        }
        try {
            //同步给isolated margin vip等级
            vipBridgeApiClient.updateVipLevel(rootUserId, tradeLevel);
        } catch (Exception e) {
            log.error(String.format("vipBridgeApiClient error, rootUserId:%s", rootUserId), e);
        }
    }

    @Async("userInfoAsync")
    public void setFutureUserTradeLevel(final Long rootUserId, final Integer tradeLevel) {
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
        if(null==userInfo||null==userInfo.getFutureUserId()){
            log.info("setFutureUserTradeLevel: userInfo is null or futureUserId is null  rootUserId={} ",rootUserId);
            return;
        }
        try {
            int result = userInfoMapper.updateByTradeLevel(userInfo.getFutureUserId(), tradeLevel);
            log.info("setFutureUserTradeLevel: finish result={},rootUserId={},futureUserId={} ",result,rootUserId,userInfo.getFutureUserId());
        } catch (Exception e) {
            log.error(String.format("setFutureUserTradeLevel error, rootUserId:%s,futureUserId:%s", rootUserId,userInfo.getFutureUserId()), e);
        }
    }

    /**
     * 从母账号获取交易等级和手续费，同步给某个子账号
     * @param parentUserId
     * @param subUserId
     */
    @Async("userInfoAsync")
    public void setOneSubUserTradeLevelAndCommission(final Long parentUserId, final Long subUserId) {
        log.info("setOneSubUserTradeLevelAndCommission start, parentUserId={} subUserId={}", parentUserId, subUserId);
        final UserInfo parentUserInfo = this.userInfoMapper.selectByPrimaryKey(parentUserId);
        final UserInfo subUserInfo = this.userInfoMapper.selectByPrimaryKey(subUserId);
        if (parentUserInfo == null || subUserInfo == null) {
            log.error("parentUserInfo or subUserInfo is null, parentUserId:{} subUserId;{}", parentUserId, subUserId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        
        Integer tradeLevel = parentUserInfo.getTradeLevel();
        BigDecimal buyerCommission = parentUserInfo.getBuyerCommission();
        BigDecimal sellerCommission = parentUserInfo.getSellerCommission();
        BigDecimal makerCommission = parentUserInfo.getMakerCommission();
        BigDecimal takerCommission = parentUserInfo.getTakerCommission();
        try {
            int subResult = this.setCommission(subUserId, buyerCommission, sellerCommission,
                    takerCommission, makerCommission, "", null);
            log.info("setOneSubUserTradeLevelAndCommission setCommission success, updateNum={} subUserId={}", subResult, subUserId);
        } catch (Exception e) {
            log.error(String.format("setOneSubUserTradeLevelAndCommission setCommission error, subUserId:%s.", subUserId), e);
        }
        
        // 使用原setTradeLevel()可能会出现从库subUser状态更新不及时，部分逻辑可能有问题，故部分代码再写一遍
        log.info("setOneSubUserTradeLevelAndCommission setTradeLevel start, subUserId={} fromTradeLevel={} toTradeLevel={}", subUserId, subUserInfo.getTradeLevel(), parentUserInfo.getTradeLevel());
        if (null == subUserInfo.getTradeLevel() || subUserInfo.getTradeLevel().compareTo(tradeLevel) != 0) {
            int subResult = userInfoMapper.updateByTradeLevel(subUserInfo.getUserId(), tradeLevel);
            //升级才推送
            if (null == subUserInfo.getTradeLevel() || subUserInfo.getTradeLevel().compareTo(tradeLevel) < 0){
                Map<String,Object> data = Maps.newHashMap();
                data.put("viplevel",tradeLevel);
                APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(subUserId, data, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web","VIP_levelup");
                AsyncTaskExecutor.execute(() -> {
                    try {
                        inboxMessageTextApi.pushInbox(apiRequest);
                    }catch (Exception e){
                        log.warn("send inbox ip update error",e);
                    }
                });
            }
            log.info("setOneSubUserTradeLevelAndCommission setTradeLevel success, updateNum={} subUserId={}", subResult, subUserId);
        }
    }

    public int updateAgentId(final Long userId, final Long agentId) {
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui == null) {// 该用户不存在
            return 0;
        }
        // 1.update user_agent_reward的agentId
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, userId);
        paramMap.put("agentId", agentId);
        paramMap.put(UserConst.UPDATE_TIME, new Date());// 修改时间
        this.userAgentRewardMapper.updateAgentRewardByUserId(paramMap);

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo.getMarginUserId() != null){
            UserInfo marginUserInfo = new UserInfo();
            marginUserInfo.setAgentId(agentId);
            marginUserInfo.setUserId(userInfo.getMarginUserId());
            this.userInfoMapper.updateByPrimaryKeySelective(marginUserInfo);
        }
        if (userInfo.getFutureUserId() != null){
            UserInfo futureAgentUserInfo = userInfoMapper.selectByPrimaryKey(agentId);
            if (futureAgentUserInfo.getFutureUserId() != null){
                UserInfo futureUserInfo = new UserInfo();
                futureUserInfo.setFutureAgentId(futureAgentUserInfo.getFutureUserId());
                futureUserInfo.setUserId(userInfo.getFutureUserId());
                this.userInfoMapper.updateByPrimaryKeySelective(futureUserInfo);
                commissionApiClient.updateFutureAgent(userInfo.getFutureUserId(),futureAgentUserInfo.getFutureUserId());

            }
        }

        List<Long> isoLatedMarginUserIds = isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(userId);
        for (Long isoLatedMargin:isoLatedMarginUserIds){
            UserInfo isoLatedMarginUserInfo = new UserInfo();
            isoLatedMarginUserInfo.setAgentId(agentId);
            isoLatedMarginUserInfo.setUserId(isoLatedMargin);
            this.userInfoMapper.updateByPrimaryKeySelective(isoLatedMarginUserInfo);
        }

        // 2.update user_info的agentId
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setAgentId(agentId);
        updateUserInfo.setUserId(userId);
        return this.userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);
    }


    public int batchUpdateAgentId(final UpdateUserAgentRewardRequest userInfo, final String nextBatchId) {
        int total = 0;
        Map<String, Object> params = Maps.newHashMap();
        Long userId = userInfo.getUserId();
        params.put(UserConst.USER_ID, userId);
        params.put("agentRewardRatio", userInfo.getAgentRewardRatio());
        params.put(UserConst.IS_RESTORE, userInfo.getIsRestore().getCode());
        params.put("expectRestoreTime", userInfo.getExpectRestoreTime());
        params.put("reason", userInfo.getReason());
        params.put("applyId", userInfo.getApplyId());
        params.put("applyName", userInfo.getApplyName());
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui != null) {// 该用户不存在
            if (!StringUtils.isBlank(ui.getTrackSource())) {
                params.put("trackSource", ui.getTrackSource());// 注册来源
            }
            if (ui.getAgentRewardRatio() != null) {
                params.put("oldAgentRewardRatio", ui.getAgentRewardRatio());// 旧值
            }
            params.put("newAgentRewardRatio", userInfo.getAgentRewardRatio());// 新值
            log.info("updateUserAgentRatio params-->{}", new Gson().toJson(params));

            // 查待审核的UserAgentReward记录
            UserAgentReward uar = this.userInfoMapper.selectUserAgentRewardByUserId(userInfo.getUserId());
            if (uar != null) {// 查到，update user_agent_reward
                Date newDate = new Date();
                params.put("applyTime", newDate);// 申请时间
                params.put(UserConst.UPDATE_TIME, newDate);// 修改时间
                params.put(UserConst.BATCH_ID, nextBatchId);// 批次号
                // update 待审核的UserAgentReward记录
                total += this.userAgentRewardMapper.updateAgentRewardByUserId(params);

            } else {// 没查到，insert user_agent_reward
                params.put(UserConst.BATCH_ID, nextBatchId);// 批次号
                final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
                if (!StringUtils.isBlank(userIndex.getEmail())) {
                    params.put(UserConst.EMAIL, userIndex.getEmail());// 邮箱
                }
                Date newDate = new Date();
                params.put("applyTime", newDate);// 申请时间
                params.put(UserConst.CREATE_TIME, newDate);// 创建时间
                // insert 一条UserAgentReward记录
                total += this.userAgentRewardMapper.saveUserAgentReward(params);
            }
        }

        // 若为母账户，则更新子账户
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            if (userCommonBusiness.isParentUser(user.getStatus())) {
                batchUpdateSubUsersAgentId(userId, userInfo, nextBatchId);
            }
        } catch (Exception e) {
            log.warn("batchUpdateSubUsersAgentId error", e);
        }

        return total;
    }


    public int updateUserAgentRatio(UpdateUserAgentRewardRequest requestBody) {
        // 根据user_id查user_agent_reward表，查到比较该表的新值字段是否和当前值一样，一样不做操作，不一样则update，取新值作为旧值，
        // 没查到insert,insert旧值为user_info表修改之前的分佣比例，当前传入的分佣比例为新值
        Map<String, Object> params = Maps.newHashMap();
        Long userId = requestBody.getUserId();
        params.put("id", requestBody.getId());
        params.put("agentRewardRatio", requestBody.getAgentRewardRatio());
        params.put(UserConst.IS_RESTORE, requestBody.getIsRestore().getCode());
        params.put("expectRestoreTime", requestBody.getExpectRestoreTime());
        params.put("reason", requestBody.getReason());
        params.put("applyId", requestBody.getApplyId());
        params.put("applyName", requestBody.getApplyName());
        UserInfo ui = this.userInfoMapper.selectByPrimaryKey(userId);
        if (ui == null) {// 该用户不存在
            return 0;
        }
        if (!StringUtils.isBlank(ui.getTrackSource())) {
            params.put("trackSource", ui.getTrackSource());// 注册来源
        }
        if (ui.getAgentRewardRatio() != null) {
            params.put("oldAgentRewardRatio", ui.getAgentRewardRatio());// 旧值
        }
        params.put("newAgentRewardRatio", requestBody.getAgentRewardRatio());// 新值
        log.info("updateUserAgentRatio params-->{}", new Gson().toJson(params));

        // 查UserAgentReward记录
        UserAgentReward uar = this.userInfoMapper.selectUserAgentRewardByUserId(requestBody.getUserId());
        if (uar != null && requestBody.getId() == null) {// 修改必须有id
            log.warn("userId为{}的用户有待审核的记录，但审核id为null,无法修改", userId);
            return 0;
        }
        if (uar != null) {// 查到，update user_agent_reward
            Date newDate = new Date();
            params.put("applyTime", newDate);// 申请时间
            params.put(UserConst.UPDATE_TIME, newDate);// 修改时间
            // update UserAgentReward记录并返回
            return this.userAgentRewardMapper.updateByPrimaryKeySelective(params);
        }
        // 没查到，insert user_agent_reward
        params.put(UserConst.BATCH_ID, nextBatchId());// 批次号
        params.put(UserConst.USER_ID, userId);

        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (!StringUtils.isBlank(userIndex.getEmail())) {
            params.put(UserConst.EMAIL, userIndex.getEmail());// 邮箱
        }
        Date newDate = new Date();
        params.put("applyTime", newDate);// 申请时间
        params.put(UserConst.CREATE_TIME, newDate);// 创建时间
        // insert 一条UserAgentReward记录并返回
        return this.userAgentRewardMapper.saveUserAgentReward(params);
    }

    private String nextBatchId() {
        String currentBatchId = this.userAgentRewardMapper.getLastBatchId();
        log.info("currentBatchId-->{}", currentBatchId);
        String nextBatchId = "1";
        if (!StringUtils.isBlank(currentBatchId)) {
            nextBatchId = (Integer.parseInt(currentBatchId) + 1) + "";
        }
        return nextBatchId;
    }

    public int updateAgentRewardRatio(final Long userId, final BigDecimal agentRewardRatio) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setAgentRewardRatio(agentRewardRatio);
        this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return 1;
    }


    public int setCommission(final Long userId, final BigDecimal buyerCommission, final BigDecimal sellerCommission,
                             final BigDecimal takerCommission, final BigDecimal makerCommission, final String modifyReason,
                             final String expectedRestoreTime) {

        int num = 0;
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo) {

            if (null != userInfo.getTradingAccount()) {
                TradingAccountDetails tradingAccountDetails= accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
                log.info("getDetailsByTradingAccountId userId={},tradingAccountDetails={},userInfo={}",userId,JsonUtils.toJsonNotNullKey(tradingAccountDetails),
                        JsonUtils.toJsonNotNullKey(userInfo));
                boolean isEqual=tradingAccountDetails.getBuyerCommission().longValue()==CouplingCalculationUtils.feeLong(buyerCommission)&&
                        tradingAccountDetails.getSellerCommission().longValue()==CouplingCalculationUtils.feeLong(sellerCommission)&&
                        tradingAccountDetails.getTakerCommission().longValue()==CouplingCalculationUtils.feeLong(takerCommission)&&
                        tradingAccountDetails.getMakerCommission().longValue()==CouplingCalculationUtils.feeLong(makerCommission);
                if(!isEqual){
                    accountApi.setCommission(userInfo.getTradingAccount(),
                            CouplingCalculationUtils.feeLong(buyerCommission),
                            CouplingCalculationUtils.feeLong(sellerCommission),
                            CouplingCalculationUtils.feeLong(takerCommission),
                            CouplingCalculationUtils.feeLong(makerCommission));
                }
            } else {
                UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
                User user = userMapper.queryByEmail(userIndex.getEmail());
                if (BitUtils.isTrue(user.getStatus(), Constant.USER_ACTIVE)) {
                    log.error("setCommission user activated, but tradingAccount is null, userId:{}", userId);
                } else {
                    log.warn("setCommission user not activated and tradingAccount is null, userId:{}", userId);
                }
            }

            // 读写延迟
            UserInfo updateUserInfo = new UserInfo();
            updateUserInfo.setUserId(userId);
            updateUserInfo.setBuyerCommission(buyerCommission);
            updateUserInfo.setSellerCommission(sellerCommission);
            updateUserInfo.setTakerCommission(takerCommission);
            updateUserInfo.setMakerCommission(makerCommission);
            num = userInfoMapper.updateByPrimaryKeySelective(updateUserInfo);

        } else {
            log.error("userInfo is null, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        try {
            // 临时代码
            Map<String, Object> dataMsg = new HashMap<>();
            dataMsg.put(UserConst.USER_ID, userId);
            dataMsg.put("buyerCommission", buyerCommission);
            dataMsg.put("sellerCommission", sellerCommission);
            dataMsg.put("takerCommission", takerCommission);
            dataMsg.put("makerCommission", makerCommission);
            dataMsg.put("modifyReason", modifyReason);
            dataMsg.put("expectedRestoreTime", expectedRestoreTime);
            MsgNotification msg =
                    new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.SET_COMMISSION, dataMsg);
            log.info("iMsgNotification setCommission:{}", JSON.toJSONString(msg));
            this.iMsgNotification.send(msg);
        } catch (Exception e) {
            log.error("iMsgNotification.send failed:", e);
        }
        return num;
    }


    public int setTradeLevel(final Long userId, final Integer tradeLevel) {
        int num = 0;
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo) {
            if (null == userInfo.getTradeLevel() || userInfo.getTradeLevel().compareTo(tradeLevel) != 0) {
                num = userInfoMapper.updateByTradeLevel(userInfo.getUserId(), tradeLevel);
                //升级才推送
                if (null == userInfo.getTradeLevel() || userInfo.getTradeLevel().compareTo(tradeLevel) < 0){
                    Map<String,Object> data = Maps.newHashMap();
                    data.put("viplevel",tradeLevel);
                    APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, data, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web","VIP_levelup");
                    AsyncTaskExecutor.execute(() -> {
                        try {
                            inboxMessageTextApi.pushInbox(apiRequest);
                        }catch (Exception e){
                            log.warn("send inbox ip update error",e);
                        }
                    });
                }


                // 将tradeLevel>=1的普通用户自动设置为母账户
                if (autoEnableSubUserFunctionDueToTradeLevel) {
                    try {
                        if (tradeLevel.compareTo(1) >= 0) {
                            User user = userCommonBusiness.checkAndGetUserById(userId);
                            // 通过实名认证，且为普通用户
                            if (BitUtils.isTrue(user.getStatus(), Constant.USER_CERTIFICATION)
                                    && userCommonBusiness.isNormalUser(user.getStatus())) {
                                APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
                                ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
                                parentUserIdReq.setParentUserId(userId);
                                subUserReq.setBody(parentUserIdReq);
                                APIResponse<Boolean> result = subUserAdminBusiness.enableSubUserFunction(subUserReq);
                                log.info("autoEnableSubUserFunction for tradeLevel>=1 parentUserId:{}, result:{}", userId, result);
                            }
                        }
                    } catch (Exception e) {
                        log.error("enableSubUserFunction error for tradeLevel>=1", e);
                    }
                }

            }
        } else {
            log.error("userInfo is null, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return num;
    }


    @Async("userInfoAsync")
    public void setIsolatedMarginUserBnbBurn(Long rootUserId, Boolean enableBnbFlag) {
        List<Long> isolatedMarginUserIdList=isolatedMarginUserBindingMapper.selectisolatedMarginUserIdsByRootUserId(rootUserId);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(isolatedMarginUserIdList)){
            log.info("setIsolatedMarginUserTradeLevel: isolatedMarginUserIdList is null  rootUserId={} ",rootUserId);
            return;
        }
        for(Long isolatedMarginUserId:isolatedMarginUserIdList){
            try {
                UserIndex userIndex = userIndexMapper.selectByPrimaryKey(isolatedMarginUserId);
                User user = userMapper.queryByEmail(userIndex.getEmail());
                UserInfo userInfo=userInfoMapper.selectByPrimaryKey(isolatedMarginUserId);
                if (null==userInfo.getTradingAccount()) {
                    log.error("setIsolatedMarginUserBnbBurn tradingAccount is null, userId:{}", isolatedMarginUserId);
                }
                int updateResult = 0;
                if(enableBnbFlag){
                    user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_FEE));
                    updateResult = this.userMapper.updateByEmailSelective(user);
                }else{
                    user.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_FEE));
                    updateResult = this.userMapper.updateByEmailSelective(user);
                }
                //同步像撮合修改燃烧bnb的状态
                accountApiClient.setGas(user.getUserId().toString(),enableBnbFlag);
                log.info("setIsolatedMarginUserBnbBurn finish: rootUserId={},isolatedMarginUserId={}, enableBnbFlag={}，updateResult={}",rootUserId,isolatedMarginUserId,enableBnbFlag,updateResult);
            } catch (Exception e) {
                log.error(String.format("setIsolatedMarginUserTradeLevel error, rootUserId:%s,isolatedMarginUserId:%s", rootUserId,isolatedMarginUserId), e);
            }
        }

    }

}
