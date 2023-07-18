package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.UserSecurityResetAnswerResult;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.security.UserSecurityResetAnswerLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.security.UserSecurityResetAnswerLogMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.face.handler.SecurityResetFaceHandler;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.vo.reset.request.ResetAnswerArg;
import com.binance.account.vo.security.request.ResetSecurityRequest;
import com.binance.account.vo.security.request.SecurityStatusRequest;
import com.binance.assetservice.api.IAssetApi;
import com.binance.assetservice.api.IUserAssetApi;
import com.binance.assetservice.vo.request.GetAssetByCodeRequest;
import com.binance.assetservice.vo.request.GetOneUserAssetRequest;
import com.binance.assetservice.vo.request.UserAssetTransferBtcRequest;
import com.binance.assetservice.vo.response.UserAssetItemResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.assetservice.vo.response.asset.AssetResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.old.data.charge.OldUserChargeMapper;
import com.binance.master.old.data.withdraw.OldWithdrawDailyLimitModifyMapper;
import com.binance.master.old.models.charge.OldUserCharge;
import com.binance.master.old.models.withdraw.OldWithdrawDailyLimitModify;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.WebUtils;
import com.binance.risk.api.RiskSecurityApi;
import com.binance.risk.vo.UserIdRequestVo;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主要用作一些帮助计算信息
 * @author liliang1
 * @date 2019-01-15 19:50
 */
@Log4j2
@Component
public class UserSecurityResetHelper {

    // 得分数组
    public static final int[] SCORES = {5, 5, 2, 4, 2, 2};
    // 通过得分
    public static final int APPROVE_SCORE = 10;
    //常用IP答题加分
    public static final int COMMON_IP_SCORE = 5;

    private static final String REQ_PARAM_AMOUNT = "amount";

    @Resource
    private UserIpMapper userIpMapper;
    @Resource
    private UserSecurityResetAnswerLogMapper userSecurityResetAnswerLogMapper;
    @Resource
    private IAssetApi iAssetApi;
    @Resource
    private IUserAssetApi iUserAssetApi;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private IUserCertificate iUserCertificate;
    @Resource
    private IUserSecurity iUserSecurity;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private RiskSecurityApi riskSecurityApi;
    @Resource
    private ApolloCommonConfig apolloCommonConfig;
    @Resource
    private UserCertificateIndexMapper userCertificateIndexMapper;
    @Resource
    private OldWithdrawDailyLimitModifyMapper oldWithdrawDailyLimitModifyMapper;
    @Resource
    private OldUserChargeMapper oldUserChargeMapper;
    @Resource
    private IFace iFace;
    @Resource
    private SecurityResetFaceHandler securityResetFaceHandler;

    /**
     * 用于发送邮件时使用
     *
     * @param userId
     * @return
     */
    public User getUserByUserId(Long userId) {
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            log.info("get user index fail by userId:{} ", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = userMapper.queryByEmail(userIndex.getEmail());
        if (user == null) {
            log.info("get user fail by userId:{}", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 根据Email获取用户ID
     * @param email
     * @return
     */
    public Long getUserIdByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        User user = userMapper.queryByEmail(email);
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user.getUserId();
    }

    /**
     * 计算用户重置流程答题过程中常用IP答题加分
     * @param userId
     * @param id
     * @param reset
     */
    public void answerIpScore(Long userId, String id, UserSecurityReset reset) {
        String ip = WebUtils.getRequestIp();
        int ipCount = userIpMapper.getIpCount(userId, ip);
        if (ipCount > 0) {
            log.info("答题的IP存在于常用IP中. userId:{} id:{}", userId, id);
            reset.setQuestionScore(COMMON_IP_SCORE);
            // 记录答题日志
            UserSecurityResetAnswerLog answerLog = new UserSecurityResetAnswerLog();
            answerLog.setResetId(id);
            answerLog.setUserId(userId);
            answerLog.setCreateTime(DateUtils.getNewUTCDate());
            answerLog.setUpdateTime(DateUtils.getNewUTCDate());
            answerLog.setQuestionSeq(-1);
            answerLog.setQuestionScore(COMMON_IP_SCORE);
            answerLog.setTotalScore(COMMON_IP_SCORE);
            answerLog.setResult(UserSecurityResetAnswerResult.RIGHT);
            answerLog.setAnswer("常用IP答题加分");
            userSecurityResetAnswerLogMapper.insert(answerLog);
        }
    }

    /**
     * 重置流程答题是否正确
     * @param userId
     * @param id
     * @param reset
     * @param answerArg
     * @return
     */
    public boolean answerValidate(Long userId, String id, UserSecurityReset reset, ResetAnswerArg answerArg) {
        if (answerArg.getQuestion() == null) {
            return false;
        }
        boolean result = false;
        int question = answerArg.getQuestion();
        Map<String, Object> answerMap = Maps.newHashMap();
        switch (question) {
            case 0:
                log.info("验证最后一次充值地址. userId:{} id:{}", userId, id);
                result = verifyLastCharge(userId, answerArg.getAddress(), answerMap);
                break;
            case 1:
                log.info("验证最后一次充值币种的持有数量. userId:{} id:{}", userId, id);
                result = verifyLastAssetAmount(userId, answerArg.getAmount(), answerMap);
                break;
            case 2:
                log.info("验证最后一次充值数量. userId:{} id:{}", userId, id);
                result = verifyLastChargeAmount(userId, answerArg.getAmount(), answerMap);
                break;
            case 3:
                log.info("验证总资产BTC估值. userId:{} id:{}", userId, id);
                result = verifyTotalBTC(userId, answerArg.getAmount(), answerMap);
                break;
            case 4:
                log.info("验证最后一次充值日期. userId:{} id:{}", userId, id);
                result = verifyLastChargeDate(userId, answerArg.getDate(), answerMap);
                break;
            case 5:
                log.info("验证创建账号的日期. userId:{} id:{}", userId, id);
                result = verifyCreateAccountDate(userId, answerArg.getDate(), answerMap);
                break;
            default:
                break;
        }
        log.info("答题验证结果：userId:{} id:{} question:{} result:{}", userId, id, question, result);
        // 记录答题日志
        UserSecurityResetAnswerLog answerLog = new UserSecurityResetAnswerLog();
        answerLog.setResetId(reset.getId());
        answerLog.setUserId(userId);
        answerLog.setCreateTime(DateUtils.getNewUTCDate());
        answerLog.setUpdateTime(DateUtils.getNewUTCDate());
        answerLog.setQuestionSeq(question);
        answerLog.setQuestionScore(SCORES[question]);
        answerLog.setTotalScore(reset.getQuestionScore() + (result ? SCORES[question] : 0));
        answerLog.setResult(result ? UserSecurityResetAnswerResult.RIGHT : UserSecurityResetAnswerResult.WRONG);
        answerLog.setAnswer(JSON.toJSONString(answerMap));
        userSecurityResetAnswerLogMapper.insert(answerLog);
        return result;
    }

    /**
     * 验证最后一次充值地址是否正确
     * @param userId
     * @param address
     * @param answerMap
     * @return
     */
    private boolean verifyLastCharge(Long userId, String address, Map<String, Object> answerMap) {
        answerMap.put("address", address);
        if (StringUtils.isBlank(address)) {
            return false;
        }
        try {
            OldUserCharge charge = getLastUserCharge(userId);
            if (charge != null) {
                String chargeTargetAddress = charge.getTargetAddress();
                String chargeCoin = charge.getCoin();
                answerMap.put("coin", chargeCoin);
                if (StringUtils.equalsIgnoreCase(address, chargeTargetAddress) || (StringUtils.equalsIgnoreCase("IOTA", chargeCoin)
                        && StringUtils.equalsIgnoreCase(StringUtils.substring(address, 0, 81), chargeTargetAddress))) {
                    // IOTA 特殊处理
                    return true;
                }
            }
            return false;
        }catch (Exception e) {
            log.error("验证最后一次充值地址信息异常. userId:{}", userId, e);
            return false;
        }
    }

    /**
     * 验证最后一次充值币种的持有数量
     * @param userId
     * @param amount
     * @param answerMap
     * @return
     */
    private boolean verifyLastAssetAmount(Long userId, String amount, Map<String, Object> answerMap) {
        answerMap.put(REQ_PARAM_AMOUNT, amount);
        if (StringUtils.isBlank(amount) || !NumberUtils.isCreatable(amount)) {
            return false;
        }
        try {
            BigDecimal amountValue = new BigDecimal(amount);
            amountValue = amountValue.setScale(8, BigDecimal.ROUND_HALF_UP);
            String coin = getLastUserChargeCoin(userId);
            if (coin == null) {
                return false;
            }
            AssetResponse assetResponse = getAssetByCoin(userId, coin);
            if (assetResponse == null) {
                return false;
            }
            answerMap.put("coin", coin);
            UserAssetItemResponse userAsset = getUserOneAssetDetail(userId, coin);
            if (userAsset != null) {
                BigDecimal free = userAsset.getFree() == null ? BigDecimal.ZERO : userAsset.getFree();
                BigDecimal locked = userAsset.getLocked() == null ? BigDecimal.ZERO : userAsset.getLocked();
                BigDecimal freeze = userAsset.getFreeze() == null ? BigDecimal.ZERO : userAsset.getFreeze();
                BigDecimal total = free.add(locked).add(freeze);
                if (total.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (amountValue.divide(total, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("1.02")) <= 0
                        && amountValue.divide(total, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("0.98")) >= 0) {
                    return true;
                }
            }
            return false;
        }catch (Exception e) {
            log.error("验证最后一次充值币种的持有数量异常. userId:{}", userId, e);
            return false;
        }
    }

    /**
     * 验证最后一次充值数量
     * @param userId
     * @param amount
     * @param answerMap
     * @return
     */
    private boolean verifyLastChargeAmount(Long userId, String amount, Map<String, Object> answerMap) {
        answerMap.put(REQ_PARAM_AMOUNT, amount);
        if (StringUtils.isBlank(amount) || !NumberUtils.isCreatable(amount)) {
            return false;
        }
        try {
            BigDecimal amountValue = new BigDecimal(amount);
            amountValue = amountValue.setScale(8, BigDecimal.ROUND_HALF_UP);
            OldUserCharge userCharge = getLastUserCharge(userId);
            if (userCharge != null) {
                String coin = userCharge.getCoin();
                answerMap.put("coin", coin);
                BigDecimal chargeAmount = userCharge.getTransferAmount();
                if (chargeAmount == null || chargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
                if (amountValue.divide(chargeAmount, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("1.02")) <= 0
                        && amountValue.divide(chargeAmount, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("0.98")) >= 0) {
                    return true;
                }
            }
            return false;
        }catch (Exception e) {
            log.error("验证最后一次充值数量一次. userId:{}", userId, e);
            return false;
        }
    }

    /**
     * 验证总资产BTC估值
     * @param userId
     * @param amount
     * @param answerMap
     * @return
     */
    private boolean verifyTotalBTC(Long userId, String amount, Map<String, Object> answerMap) {
        answerMap.put(REQ_PARAM_AMOUNT, amount);
        if (StringUtils.isBlank(amount) || !NumberUtils.isCreatable(amount)) {
            return false;
        }
        try {
            BigDecimal amountValue = new BigDecimal(amount);
            amountValue = amountValue.setScale(8, BigDecimal.ROUND_HALF_UP);
            UserAssetTransferBtcResponse btcResponse = getUserAssetTransferBtc(userId);
            if (btcResponse == null || btcResponse.getTotalTransferBtc() == null) {
                return false;
            }
            BigDecimal totalBtc = btcResponse.getTotalTransferBtc();
            if (totalBtc.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
            return  (amountValue.divide(totalBtc, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("1.1")) <= 0
                    && amountValue.divide(totalBtc, 8, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal("0.9")) >= 0);
        }catch (Exception e) {
            log.error("验证总资产BTC估值. userId:{}", userId, e);
            return false;
        }
    }

    /**
     * 验证最后一次充值时间
     * @param userId
     * @param requestDate
     * @param answerMap
     * @return
     */
    private boolean verifyLastChargeDate(Long userId, Long requestDate, Map<String, Object> answerMap) {
        if (requestDate != null) {
            answerMap.put("date", DateUtils.formatterUTC(new Date(requestDate), DateUtils.DETAILED_NUMBER_PATTERN));
        }else {
            answerMap.put("date", null);
            return false;
        }
        OldUserCharge userCharge = getLastUserCharge(userId);
        if (userCharge == null) {
            return false;
        }
        String coin = userCharge.getCoin();
        Date chargeDate = userCharge.getInsertTime();
        answerMap.put("coin", coin);
        long startTime = DateUtils.addDays(chargeDate, -3).getTime();
        long endTime = DateUtils.addDays(chargeDate, 3).getTime();
        return (requestDate.longValue() >= startTime && requestDate.longValue() <= endTime);
    }

    /**
     * 验证账户注册时间
     * @return
     */
    private boolean verifyCreateAccountDate(Long userId, Long requestDate, Map<String, Object> answerMap) {
        if (requestDate != null) {
            answerMap.put("date", DateUtils.formatterUTC(new Date(requestDate), DateUtils.DETAILED_NUMBER_PATTERN));
        }else {
            answerMap.put("date", null);
            return false;
        }
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            log.info("get user index fail by userId:{} ", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = userMapper.queryByEmail(userIndex.getEmail());
        if (user == null) {
            log.info("get user fail by userId:{}", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Date createTime = user.getInsertTime();
        if (createTime == null) {
            return false;
        }
        long startTime = DateUtils.addDays(createTime, -3).getTime();
        long endTime = DateUtils.addDays(createTime, 3).getTime();
        return (requestDate.longValue() >= startTime && requestDate.longValue() <= endTime);
    }

    /**
     * 获取最后一次的充值记录的币种
     * @param userId
     * @return
     */
    public String getLastUserChargeCoin(Long userId) {
        OldUserCharge oldUserCharge = getLastUserCharge(userId);
        return oldUserCharge == null ? null : oldUserCharge.getCoin();
    }

    /**
     * 获取最后一次充值记录信息
     * @param userId
     * @return
     */
    private OldUserCharge getLastUserCharge(Long userId) {
        return this.getLastOldUserCharge(userId);
    }

    /**
     * 最后一次充值币种的入金地址是否相同
     * @param userId
     * @param coin
     * @return
     */
    public boolean isAssetSameAddress(Long userId, String coin) {
        AssetResponse assetResponse = getAssetByCoin(userId, coin);
        return (assetResponse != null && assetResponse.getSameAddress() != null && assetResponse.getSameAddress());
    }

    /**
     * 获取最后一次的入金信息
     * @param userId
     * @param coin
     * @return
     */
    private AssetResponse getAssetByCoin(Long userId, String coin) {
        if (StringUtils.isBlank(coin)) {
            return null;
        }
        try {
            GetAssetByCodeRequest request = new GetAssetByCodeRequest();
            request.setAsset(coin);
            // 前端处理
            request.setBackend(false);
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            APIResponse<AssetResponse> response = iAssetApi.getAssetByCode(APIRequest.instance(header, request));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                log.info("请求获取币种充值地址信息失败. userId:{} coin:{} errorData:{} ", userId, coin, JSON.toJSONString(response));
                return null;
            }
            return response.getData();
        }catch (Exception e) {
            log.error("获取币种的充值地址信息失败. userId:{} coin:{}", userId, coin, e);
            return null;
        }
    }

    /**
     * 获取用户的特定币种的资产信息
     * @param userId
     * @param coin
     * @return
     */
    private UserAssetItemResponse getUserOneAssetDetail(Long userId, String coin) {
        if (userId == null || StringUtils.isBlank(coin)) {
            return null;
        }
        try {
            GetOneUserAssetRequest assetRequest = new GetOneUserAssetRequest();
            assetRequest.setUserId(userId.toString());
            assetRequest.setAsset(coin);
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            APIResponse<UserAssetItemResponse> response = iUserAssetApi.getOneUserAsset(APIRequest.instance(header, assetRequest));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                log.info("获取用户特定资产信息失败. userId:{} coin: message:{}", userId, coin, JSON.toJSONString(response));
                return null;
            }
            UserAssetItemResponse assetResponse = response.getData();
            if (assetResponse == null) {
                log.info("获取不到用户的特定资产信息. userId:{} coin:{}", userId, coin);
                return null;
            }
            // 逻辑上一个币种只有一个资产信息，直接获取第一个返回
            return assetResponse;
        }catch (Exception e) {
            log.error("获取用户特定资产信息异常. userId:{} coin:{}", userId, coin, e);
            return null;
        }
    }

    /**
     * 获取用户的BTC资产估值
     * @param userId
     * @return
     */
    private UserAssetTransferBtcResponse getUserAssetTransferBtc(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            UserAssetTransferBtcRequest btcRequest = new UserAssetTransferBtcRequest();
            btcRequest.setUserId(userId.toString());
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            APIResponse<UserAssetTransferBtcResponse> response = iUserAssetApi.userAssetTransferBtc(APIRequest.instance(header, btcRequest));
            if (response == null || response.getStatus() != APIResponse.Status.OK) {
                log.info("获取用户的BTC资产估值失败. userId:{} message:{}", userId, JSON.toJSONString(response));
                return null;
            }
            return response.getData();
        }catch (Exception e) {
            log.error("获取用户的BTC资产估值异常. userId:{}", userId, e);
            return null;
        }
    }



    /**
     * 通过重置流程的后续处理信息
     * @param userId
     * @param resetId
     * @param reset
     */
    public void resetPassHandler(Long userId, String resetId, UserSecurityReset reset, String ip, TerminalEnum terminal) throws Exception {
        if (StringUtils.isNotBlank(reset.getScanReference())) {
            // 如果是存在有jumio 认证的，不是直接根据kyc跳过jumio的， 需要先验证下 证件号是否在已经被占用，如果已经被占用，不能添加成功
            String idNumber = reset.getIdNumber();
            String documentType = reset.getDocumentType();
            String country = reset.getIssuingCountry();
            if (StringUtils.isAnyBlank(idNumber, country)) {
                log.info("重置流程的JUMIO认证的证件和国籍信息丢失，不能审核通过. userId:{} resetId:{}", userId, resetId);
                throw new BusinessException(GeneralCode.SYS_ERROR, "JUMIO认证的证件和国籍信息丢失");
            }
            boolean haveUsed = iUserCertificate.isJumioIdNumberUseByOtherUser(userId, idNumber, country, documentType);
            if (haveUsed) {
                log.info("重置流程中的证件号信息已经被别的用户使用. userId:{} resetId:{}", userId, resetId);
                throw new BusinessException(GeneralCode.SYS_ERROR, "证件号信息已经被别的用户使用");
            }
        }
        log.info("重置2FA/解禁用户--> 证件号信息验证通过, 开始处理审核通过的处理逻辑, userId:{} resetId:{}", userId, resetId);
        //调用重置流程通过后的安全信息重置的的处理逻辑
        User user = this.getUserByUserId(userId);
        try {
            ResetSecurityRequest.ResetType requestType = getSecurityRequestType(reset.getType());
            iUserSecurity.resetSecurity(user, requestType, ip, terminal);
            log.info("安全信息重置成功, 继续处理后续逻辑. userId:{} resetId:{}", userId, resetId);
            updateSecurityAndTrading(user, resetId, reset.getType(), ip, terminal);
        }catch (BusinessException e) {
            if (GeneralCode.USER_NOT_MOBILE.equals(e.getErrorCode())) {
                log.info("重置的是手机类型，但是出现未绑定手机信息，忽略这种错误继续处理后续逻辑, userId:{} resetId:{}", userId, resetId);
                updateSecurityAndTrading(user, resetId, reset.getType(), ip, terminal);
            }else {
                log.error("重置流程审核通过后处理安全信息异常. userId:{} resetId:{}", userId, resetId, e);
                throw e;
            }
        }catch (Exception e) {
            log.error("重置安全信息处理未知异常失败. userId:{} resetId:{}", userId, resetId, e);
            throw e;
        }
    }

    private ResetSecurityRequest.ResetType getSecurityRequestType(UserSecurityResetType type) {
        switch (type) {
            case enable:
                return ResetSecurityRequest.ResetType.ENABLE;
            case mobile:
                return ResetSecurityRequest.ResetType.MOBILE;
            case google:
                return ResetSecurityRequest.ResetType.GOOGLE;
        }
        return null;
    }


    private void updateSecurityAndTrading(User user, String resetId, UserSecurityResetType type, String ip, TerminalEnum terminal) throws Exception {
        Long userId = user.getUserId();
        if (UserSecurityResetType.enable == type) {
            log.info("解禁用户-->一键启动功能变更: userId:{} resetId:{} activeResult:{}", userId, resetId);
            // risk 解禁用户API 交易
            riskUnbanByUserId(userId, resetId);
        }
    }

    /**
     * 禁止48h内交易
     *
     * @param userId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void resetDisableTrading(Long userId) {
        log.info("重置2FA/解禁用户--> 开始处理禁用用户提币48小时: userId:{}", userId);
        getUserByUserId(userId);
        // 查询 pnk库 withdraw_daily_limit_modify forbid_restore_time 字段和 withdraw_daliy_limit_last 字段
        OldWithdrawDailyLimitModify limitModify = this.getOldWithdrawDailyLimitModify(userId);
        Date oldForbidRestoreTime = limitModify == null ? null : limitModify.getForbidRestoreTime();
        BigDecimal oldWithdrawDailyLimitLast = limitModify == null ? null : limitModify.getWithdrawDaliyLimitLast();
        int resetTradeForbidHour = apolloCommonConfig.getResetTradeForbidHour();
        Date newForbidRestoreTime = DateUtils.addHours(DateUtils.getNewUTCDate(), resetTradeForbidHour);

        OldWithdrawDailyLimitModify newLimitModify = new OldWithdrawDailyLimitModify();
        newLimitModify.setUserId(userId.toString());
        Map<String, Object> param = Maps.newHashMap();
        newLimitModify.setForbidRestoreTime(newForbidRestoreTime);
        short autoRestore = 1;
        newLimitModify.setForbidAutoRestore(autoRestore);
        newLimitModify.setForbidReason("重置二次验证，48小时禁提币");
        if (oldWithdrawDailyLimitLast != null) {
            newLimitModify.setWithdrawDaliyLimitLast(oldWithdrawDailyLimitLast);
        }else {
            param.put("withdrawDaliyLimitLast", null);
        }
        //防止禁用时间被提前
        if (oldForbidRestoreTime != null && oldForbidRestoreTime.compareTo(newForbidRestoreTime) >= 0) {
            log.info("交易禁用时间已经超出当前需要设置的时间，不再进行设置, userId:{}", userId);
            return;
        }
        // 防止下永久禁用被自动恢复
        final UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            log.warn("获取安全信息失败. userId:{}", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (userSecurity.getWithdrawSecurityStatus() != null && userSecurity.getWithdrawSecurityStatus() == 1) {
            log.info("用户的安全信息提现交易已经被禁用，不需要再次修改防止永久禁用被自动恢复. userId:{}", userId);
            return;
        }
        // 保存 pnk 库 withdraw_daily_limit_modify 中的数据
        this.saveOldWithdrawDailyLimitModify(newLimitModify);
        SecurityStatusRequest statusRequest = new SecurityStatusRequest();
        statusRequest.setUserId(userId);
        statusRequest.setWithdrawSecurityStatus(1);
        APIResponse<Integer> statusRsp = iUserSecurity.updateStatusByUserId(APIRequest.instance(statusRequest));
        log.info("重置2FA/解禁账户--> 修改用户安全信息-新增禁用标识状态结果: userId:{} result:{}", userId, JSON.toJSONString(statusRsp));
    }

    private void riskUnbanByUserId(Long userId, String resetId) {
        // 从风控那边解禁用户的API交易
        try {
            UserIdRequestVo requestVo = new UserIdRequestVo();
            requestVo.setUserId(userId);
            APIResponse<Boolean> response = riskSecurityApi.unbanByUserId(APIRequest.instance(requestVo));
            log.info("禁用用户--> 风控解禁用户API信息结果：userId:{} resetId:{} response:{}", userId, resetId, JSON.toJSONString(response));
        }catch (Exception e) {
            log.error("解禁用户--> 风控解禁用户API异常失败. userId:{} resetId:{}", userId, resetId, e);
        }
    }


    /**
     * 查询单日提现限额信息
     * @param userId
     * @return
     */
    public OldWithdrawDailyLimitModify getOldWithdrawDailyLimitModify(Long userId) {
        if (userId == null) {
            return null;
        }
        return oldWithdrawDailyLimitModifyMapper.selectByPrimaryKey(userId.toString());
    }

    /**
     * 保存更新单日提现限额信息
     * @param newLimitModify
     * @return
     */
    public int saveOldWithdrawDailyLimitModify(OldWithdrawDailyLimitModify newLimitModify) {
        if (newLimitModify == null || StringUtils.isBlank(newLimitModify.getUserId())) {
            return 0;
        }
        log.info("保存更新单日提现限额信息: {}", JSON.toJSONString(newLimitModify));
        try {
            return oldWithdrawDailyLimitModifyMapper.insertSelective(newLimitModify);
        }catch (DuplicateKeyException e) {
            return oldWithdrawDailyLimitModifyMapper.updateByPrimaryKeySelective(newLimitModify);
        }
    }

    /**
     * 用户的提现备注信息
     * @param userIds
     * @return
     */
    public Map<Long, String> oldWithdrawDailyLimitModifyCause(List<Long> userIds) {
        Map<Long, String> result = Maps.newHashMap();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        List<String> userIdStrList = userIds.stream().map(item -> item.toString()).collect(Collectors.toList());
        List<OldWithdrawDailyLimitModify> modifies = oldWithdrawDailyLimitModifyMapper.getListByUserId(userIdStrList);
        if (modifies == null || modifies.isEmpty()) {
            return result;
        }
        modifies.stream().forEach(item -> {
            Long userId = Long.valueOf(item.getUserId());
            String cause = item.getModifyCause();
            result.put(userId, cause);
        });
        return result;
    }

    /**
     * 获取用户的最后一次充值记录信息
     * @param userId
     * @return
     */
    public OldUserCharge getLastOldUserCharge(Long userId) {
        return oldUserChargeMapper.getLastUserCharge(userId.toString(), null, null);
    }

    /**
     * 直接初始化一笔重置人脸识别
     * @param userId
     * @param reset
     * @return
     */
    public TransactionFaceLog directInitResetFaceFlow(Long userId, UserSecurityReset reset) {
        // 先检查是否能做人脸识别，只检查UserFaceReference, （在域检查的时候已经发起了预建立对比照片，循环检查是否能做，循环次数满了还没检查到则认为不能做）
        UserFaceReference faceReference = iFace.getUserFaceByMasterBD(userId);
        if (faceReference == null || StringUtils.isBlank(faceReference.getRefImage())) {
            int count = apolloCommonConfig.getResetWaitFaceCheckCount();
            while (count > 0) {
                count --;
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e) {
                    // do nothing
                }
                faceReference = iFace.getUserFaceByMasterBD(userId);
                if (faceReference != null && StringUtils.isNotBlank(faceReference.getRefImage())) {
                    break;
                }
            }
        }
        //新kyc流程，faceReference为空，代表当前kyc状态为UPLOAD，直接抛出异常，暂停reset流程。等待kyc的jumio结果通知。
        if(reset.isNewVersion() && (faceReference == null ||
        		StringUtils.isAllBlank(faceReference.getCheckImage(),faceReference.getRefImage()))) {
        	log.warn("当前用户人脸识别不存在.存在正在进行中的认证了流程 userId:{}", userId);
        	throw new BusinessException(AccountErrorCode.AUTH_FACE_REFERENCE_PROCESSING);
        }


        if (faceReference == null ||
        		StringUtils.isAllBlank(faceReference.getCheckImage(),faceReference.getRefImage())) {
            log.info("当前用户无法做人脸识别. userId:{} resetId:{}", userId, reset.getId());
            return null;
        }
        log.info("检查到用户能做人脸识别的情况下直接发起人脸识别流程: userId:{} resetId:{}", userId, reset.getId());
        try {
            FaceTransType transType = FaceTransType.getByCode(reset.getType().name());
            TransactionFaceLog faceLog = securityResetFaceHandler.directGenerateFaceLog(userId, reset.getId(), transType, false);
            return faceLog;
        }catch (Exception e) {
            log.error("初始化人脸识别异常. userId:{} resetId:{} ", userId, reset.getId(), e);
            return null;
        }
    }
}
