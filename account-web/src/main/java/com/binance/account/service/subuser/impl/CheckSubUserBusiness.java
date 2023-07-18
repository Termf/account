package com.binance.account.service.subuser.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.assetservice.AssetApiClient;
import com.binance.account.integration.assetservice.ProductApiClient;
import com.binance.account.integration.assetservice.TranApiClient;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.integration.featureservice.FeatureValueApiApiClient;
import com.binance.account.integration.risk.CommonRiskApiClient;
import com.binance.account.service.withdraw.IUserWithdrawPropertyBusiness;
import com.binance.account.task.GetOtcLoanLockTask;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.withdraw.response.UserWithdrawLockAmountResponse;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.assetservice.vo.response.asset.AssetResponse;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;

import com.binance.platform.common.TrackingUtils;
import com.binance.rule.response.DecisionCommonResponse;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.collect.Lists;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.keygen.KeyGenerator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Fei.Huang on 2018/10/11.
 */
@Log4j2
@Service
public class CheckSubUserBusiness extends BaseSubUserBusiness {

    private static final String REDIS_SUB_USER_TRANSFER_KEY = "account:subuser:transfer:";
    private static final String SUB_ACCOUNT_TRANSFER_LIMIT="sub_account_transfer_limit";

    @Autowired
    private AssetApiClient assetApiClient;
    @Autowired
    private UserAssetApiClient userAssetApiClient;
    @Resource
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Autowired
    protected TranApiClient tranApiClient;

    protected KeyGenerator keyGenerator=new DefaultKeyGenerator();

    @Autowired
    private IUserWithdrawPropertyBusiness iUserWithdrawPropertyBusiness;

    @Autowired
    private ProductApiClient productApiClient;

    @Autowired
    private FeatureValueApiApiClient featureValueApiApiClient;


    @Value("${withdraw.lock.check,switch:false}")
    private boolean needcheckLock;


    @Value("${otcLoan.lock.check,switch:false}")
    private boolean needcheckOtcLoanLock;

    @Value("${broker.transfer.limit:60000}")
    private Integer brokerTransferLimit;

    @Autowired
    private CommonRiskApiClient commonRiskApiClient;

    private ExecutorService subaccountThreadPool = Executors.newFixedThreadPool(5);

    /**
     * 说明：这两个字段，一个是存放母账户修改子账户邮箱的redis前缀
     * 一个是默认的每天的修改极限次数，24小时一个账户
     * */
    private static final String REDIS_SUB_USER_MODIFY_EMAIL_KEY = "account:subuser:modify:email";
    private static final Long  SUB_USER_MODIFY_EMAIL_LIMIT= 20L;
    /**
     * 一个合法邮箱的正则表达式
     * */
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";


    public User checkAndGetUserById(final Long userId) {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user;
    }

    protected User checkAndGetUserByEmail(final String email) {
        if (org.apache.commons.lang.StringUtils.isBlank(email)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final Long userId = userIndexMapper.selectIdByEmail(email);
        if (null == userId) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = userMapper.queryByEmail(email);
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user;
    }

    protected void assertUser2FaAtLeastOneEnabled(final Long status) {
        if (BitUtils.isFalse(status, Constant.USER_MOBILE) && BitUtils.isFalse(status, Constant.USER_GOOGLE)) {

            throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
        }
    }

    protected void assertSubUserFunctionEnabled(final Long status) {
        if (!BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {

            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED);
        }
    }


    protected void assertSubUserFunctionDisabled(final Long status) {
        if (BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {

            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_ALREADY_ENABLED);
        }
    }

    protected boolean checkAssetSubUserFunctionEnabled(final Long status) {
       return BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUBUSER_FUNCTION_ENABLED);
    }

    protected boolean checkAssetSubUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUBUSER);
    }

    protected void assertBrokerSubUserFunctionEnabled(final Long status) {
        if (!BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {

            throw new BusinessException(GeneralCode.BROKER_SUB_UER_FUNCTION_NOT_ENABLED);
        }
    }

    protected void assertBrokerSubUserFunctionDisabled(final Long status) {
        if (BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {

            throw new BusinessException(GeneralCode.BROKER_SUB_UER_FUNCTION_ALREADY_ENABLED);
        }
    }

    public boolean isSubUserFunctionEnabled(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
    }

    protected SubUserBinding assertIsSubUser(final User user) {
        if (!BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)) {
            throw new BusinessException(GeneralCode.NOT_SUB_USER);
        }
        SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(user.getUserId());
        if (null == subUserBinding) {
            throw new BusinessException(GeneralCode.NOT_SUB_USER);
        }
        return subUserBinding;
    }

    protected void assertIsEnabledSubUser(final User user) {
        assertIsSubUser(user);
        boolean isSubUserEnabled = BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUB_USER_ENABLED);
        if (!isSubUserEnabled) {
            throw new BusinessException(GeneralCode.SUB_USER_NOT_ENABLED);
        }
    }

    protected boolean isEnabledSubUser(final User user) {
        return BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)
                && null != subUserBindingMapper.selectBySubUserId(user.getUserId())
                && BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUB_USER_ENABLED);
    }

    protected void assertIsNotSubUser(final User user) {
        if (isSubUser(user) || null != subUserBindingMapper.selectBySubUserId(user.getUserId())) {
            throw new BusinessException(GeneralCode.SUB_USER_ALREADY_BOUND);
        }
    }

    protected void assertIsNotMarginOrFutureOrFiatUser(final User user) {
        if (BitUtils.isEnable(user.getStatus(), Constant.USER_IS_FIAT_USER)||
                BitUtils.isEnable(user.getStatus(), Constant.USER_IS_FUTURE_USER)||
                BitUtils.isEnable(user.getStatus(), Constant.USER_IS_MARGIN_USER)) {
            throw new BusinessException(GeneralCode.SUB_USER_ALREADY_BOUND);
        }
    }

    protected void assertIsNotBrokerSubUser(final User user) {
        if (isBrokerSubUser(user) || isSubUser(user) || null != subUserBindingMapper.selectBySubUserId(user.getUserId())) {
            throw new BusinessException(GeneralCode.SUB_USER_ALREADY_BOUND);
        }
    }

    public boolean isSubUser(final User user) {
        return BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)
                && null != subUserBindingMapper.selectBySubUserId(user.getUserId());
    }

    protected boolean isBrokerSubUser(final User user) {
        return BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)
                && BitUtils.isEnable(user.getStatus(), Constant.USER_IS_BROKER_SUBUSER);
    }

    protected void assertParentSubUserBound(final Long parentUserId, final Long subUserId) {

        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        assertParentSubUserBoundNotCheckParent(parentUserId, subUserId);
    }

    protected User validateParentSubUserBoundAndGetSubUser(final Long parentUserId, final String subUserEmail) {
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());
        User subUser = checkAndGetUserByEmail(subUserEmail);
        assertParentSubUserBoundNotCheckParent(parentUserId, subUser.getUserId());
        return subUser;
    }

    /**
     * 判断是母账号
     * */
    protected void assertIsParentUser(final Long parentUserId) {
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());
    }

    protected User assertParentSubUserBoundNotCheckParent(final Long parentUserId, final Long subUserId) {
        User subUser = checkAndGetUserById(subUserId);
        SubUserBinding subUserBinding = assertIsSubUser(subUser);
        if (!subUserBinding.getParentUserId().equals(parentUserId)) {
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        return subUser;
    }

    protected Map<Long, User> assertParentSubUserUnbound(final Long parentUserId, final Long subUserId) {

        User parentUser = checkAndGetUserById(parentUserId);
        // 确保已开通母子账号功能
        assertSubUserFunctionEnabled(parentUser.getStatus());

        User subUser = checkAndGetUserById(subUserId);
        // 确保待绑定的子账号没有开通母子账号功能
        assertSubUserFunctionDisabled(subUser.getStatus());
        // 确保待绑定的子账号状态且未被绑定
        assertIsNotSubUser(subUser);
        //子账户下面不能有子账户
        List<Long> subUserIdList= subUserBindingMapper.selectSubUserIdsByParent(subUserId);
        if(CollectionUtils.isNotEmpty(subUserIdList)){
            throw new BusinessException(GeneralCode.SUB_USER_ALREADY_BOUND);
        }

        Map<Long, User> userMap = new HashMap<>();
        userMap.put(parentUserId, parentUser);
        userMap.put(subUserId, subUser);
        return userMap;
    }


    protected Boolean checkIfExceededModifyEmailLimit(Long parentUserId){
        long modifyEmailCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_SUB_USER_MODIFY_EMAIL_KEY, 0L);
        if(modifyEmailCount <= SUB_USER_MODIFY_EMAIL_LIMIT) {
            modifyEmailCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_SUB_USER_MODIFY_EMAIL_KEY, 1L, 24L, TimeUnit.HOURS);
        }
        log.info("CheckSubUserBusiness.checkIfExceededModifyEmailLimit, parentUserId:{}, frequencyLimits:{},redisModifyEmailCount:{}", parentUserId, SUB_USER_MODIFY_EMAIL_LIMIT,modifyEmailCount);
        if (modifyEmailCount > SUB_USER_MODIFY_EMAIL_LIMIT) {
            //一天只能修改20次（成功的）
            return true;
        }else{
            return false;
        }
    }


    /**
      * 校验转出方和转入方的userid
     * */
    public void validateSenderAndRecipientUserList(String senderEmail, String recipientEmail){
        //判断email是否存在或者相等
        if(org.apache.commons.lang3.StringUtils.isAnyBlank(senderEmail,recipientEmail)||
                org.apache.commons.lang3.StringUtils.equalsIgnoreCase(senderEmail,recipientEmail)){
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        List<String> emails = Lists.newArrayList(senderEmail,recipientEmail);
        final List<UserIndex> userIndexList = this.userIndexMapper.selectByEmails(emails);
        //判断数据库里面是否能够查到匹配数目的userid
        if(CollectionUtils.isEmpty(userIndexList)||userIndexList.size()<2){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long senderUserId = null;
        Long recipientUserId = null;
        for (UserIndex userIndex : userIndexList) {
            if (senderEmail.equalsIgnoreCase(userIndex.getEmail())) {
                senderUserId = userIndex.getUserId();
            }
            if (recipientEmail.equalsIgnoreCase(userIndex.getEmail())) {
                recipientUserId = userIndex.getUserId();
            }
        }
        if(null==senderUserId||null==recipientUserId){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
    }


    /**
     * 返回转入方和转出方的email
     * return Pair.of(senderUserEmail,recipientUserEmail)
     * */
    public void validateSubAccountTransfer(Long parentUserId,Long senderUserId, Long recipientUserId,String asset,BigDecimal amount, boolean isBroker)throws Exception{
        //常规非空校验
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(parentUserId.toString(), senderUserId.toString(), recipientUserId.toString(), asset)
                || null == amount || amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        // 确保划转未超过1分钟5次频率
        if (transferExceededFrequencyLimits(parentUserId,isBroker)) {
            log.info("SubUserBusiness.exceeded frequency limits, parentUserId:{}", parentUserId);
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        // 确保转出转入方不可同一账户
        Boolean subAccountEqualsFlag=senderUserId.equals(recipientUserId);
        if (subAccountEqualsFlag) {
            throw new BusinessException(AccountErrorCode.SUB_USER_TRANSFER_ACCOUNT_SHOULD_BE_DIFFERENT);
        }
        // 确保转出转入方为子母账户关系或同属当前母账户
        List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);
        List<Long> subUserIds=Lists.newArrayList();
        for(SubUserBinding subUserBinding:subUserBindings){
            subUserIds.add(subUserBinding.getSubUserId());
        }
        if(CollectionUtils.isEmpty(subUserBindings)){
            throw new BusinessException(AccountErrorCode.SUB_USER_IS_NOT_EXIST);
        }
        // 转出方必须是当前登陆用户，或当前登陆用户的子账户
        if (!parentUserId.equals(senderUserId) && !subUserIds.contains(senderUserId)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_ILLEGAL_RELATION);
        }
        // 转入方必须是当前登陆用户，或当前登陆用户的子账户
        if (!parentUserId.equals(recipientUserId) && !subUserIds.contains(recipientUserId)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_ILLEGAL_RELATION);
        }
        //获取转入方和转出方的基本信息
        User senderUser = checkAndGetUserById(senderUserId);
        User recipientUser =checkAndGetUserById(recipientUserId);
        //获取转入方和转出方的安全信息
        UserSecurity senderUserSecurity = this.userSecurityMapper.selectByPrimaryKey(senderUserId);
        UserSecurity recipientUserSecurity = this.userSecurityMapper.selectByPrimaryKey(recipientUserId);
        if(null==senderUser||null==recipientUser||null==senderUserSecurity||null==recipientUserSecurity){
            throw new BusinessException(AccountErrorCode.MISS_USER_BASE_DETAIL);
        }
        // 确保转出方未被禁止提币（若转出方为母账户则不校验）
        if(!parentUserId.equals(senderUserId)){
            if (Integer.valueOf(1).equals(senderUserSecurity.getWithdrawSecurityStatus()) || Integer.valueOf(1).equals(senderUserSecurity.getWithdrawSecurityAutoStatus())) {
                throw new BusinessException(AccountErrorCode.WITHDRAW_SECURITY_BAN);
            }
        }
        // 确保转出转入方账号已激活
        if (!BitUtils.isEnable(senderUser.getStatus(), Constant.USER_ACTIVE)||!BitUtils.isEnable(recipientUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED);
        }
        //获取当前资产信息是否存在
        AssetResponse assetResponse=assetApiClient.getAssetByCode(asset);
        if(null==assetResponse){
            throw new BusinessException(AccountErrorCode.ASSET_IS_NOT_EXIST);
        }
        //查询当前用户的资产是否存在
        UserAssetResponse userAssetResponse=userAssetApiClient.getPrivateUserAsset(senderUserId.toString(),asset);
        if(null==userAssetResponse||CollectionUtils.isEmpty(userAssetResponse.getUserAssetList())){
            throw new BusinessException(AccountErrorCode.USER_HAVE_NO_ASSET);
        }
        //当前用户的资产
        if(amount.compareTo(userAssetResponse.getUserAssetList().get(0).getFree()) > 0){
            throw new BusinessException(AccountErrorCode.USER_HAVE_NO_AVALIABLE_AMOUNT);
        }
        //判断当前用户有没有负资产
        if(userAssetApiClient.queryNegativeAssetByUserId(senderUserId)>0){
            throw new BusinessException(AccountErrorCode.USER_AMOUNT_LESS_THAN_ZERO);
        }
        UserWithdrawLockAmountResponse  senderUserWithdrawLockAmountResponse=iUserWithdrawPropertyBusiness.getLockAmount(senderUserId);

        boolean isParentUserSender=parentUserId.equals(senderUserId);
        log.info("isParentUserSender={}",isParentUserSender);
        //otc loan被锁定的btc金额
        BigDecimal otcLoanLockBtc=BigDecimal.ZERO;
        if(needcheckOtcLoanLock){
            try{
                GetOtcLoanLockTask getOtcLoanLockTask=new GetOtcLoanLockTask(featureValueApiApiClient,senderUserId);
                FutureTask futureTask=new FutureTask(getOtcLoanLockTask);
                Thread getOtcLoanThread=new Thread(futureTask);
                getOtcLoanThread.start();
                otcLoanLockBtc=(BigDecimal)futureTask.get(500,TimeUnit.MILLISECONDS);
            }catch (Exception e){
                log.info("getOtcLoanLockTask error:",e);
            }
        }
        //入金充值被锁定的btc额度
        BigDecimal depositLockBtc=BigDecimal.ZERO;
        if(!isParentUserSender && needcheckLock && null!=senderUserWithdrawLockAmountResponse && BigDecimal.ZERO.compareTo(senderUserWithdrawLockAmountResponse.getTotalAmount())<0){
            depositLockBtc=senderUserWithdrawLockAmountResponse.getTotalAmount();
        }
        log.info("otcLoanLockBtc={},depositLockBtc={}",otcLoanLockBtc,depositLockBtc);
        //只要任何一个业务有被锁定的金额那么就要走到锁定逻辑
        if(otcLoanLockBtc.compareTo(BigDecimal.ZERO)>0 || depositLockBtc.compareTo(BigDecimal.ZERO)>0 ){
            //将当前提币资产转换成等值btc
            UserAssetTransferBtcResponse userAssetTransferBtcResponse=userAssetApiClient.getUserAssetTransferBtc(senderUserId);
            BigDecimal currentTransferBtc=productApiClient.getEqualBtcAmount(asset,amount);
            if(null==userAssetTransferBtcResponse || null==currentTransferBtc){
                throw new BusinessException(AccountErrorCode.ASSET_IS_NOT_EXIST);
            }
            log.info("userAssetTransferBtcResponse={},currentTransferBtc={},senderUserWithdrawLockAmountResponse={}",JsonUtils.toJsonNotNullKey(userAssetTransferBtcResponse),
                    currentTransferBtc,JsonUtils.toJsonHasNullKey(senderUserWithdrawLockAmountResponse));

            BigDecimal currentAlivalableAmount=userAssetTransferBtcResponse.getTotalTransferBtc().subtract(otcLoanLockBtc).subtract(depositLockBtc);
            log.info("currentAlivalableAmount={},currentTransferBtc={}",currentAlivalableAmount, currentTransferBtc);
            if(currentAlivalableAmount.compareTo(currentTransferBtc)<0){
                log.info("check failed");
                throw new BusinessException(AccountErrorCode.UNCONFIRMED_RESTRICTED_TRANSFER,new Object[] { senderUserWithdrawLockAmountResponse.getTotalAmount().toPlainString() });
            }
            log.info("check pass");
        }

        boolean isParentUserRecipient=parentUserId.equals(recipientUserId);
        log.info("isParentUserSender={},isParentUserRecipient={}",isParentUserSender,isParentUserRecipient);
        if(!isParentUserSender && isParentUserRecipient && isBroker){
            DecisionCommonResponse decisionCommonResponse= commonRiskApiClient.commonRuleForSubTransferToParent(senderUserId.toString(),amount.doubleValue(),asset,parentUserId.toString());
            if(null!=decisionCommonResponse && decisionCommonResponse.getIsHit().booleanValue()){
                log.info("hint decision rule parentUserId={}",parentUserId);
                throw new BusinessException(AccountErrorCode.BROKER_TRANSFER_HINT_DECISION_RULE);
            }

        }
        //到此结束，需要验证的东西都验证完毕
    }


    /**
     * 返回转入方和转出方的email
     * return Pair.of(senderUserEmail,recipientUserEmail)
     * */
    public void validateSubMarginMainAccountTransfer(Long parentUserId,Long senderUserId, Long recipientUserId,String asset,BigDecimal amount, Integer type)throws Exception{
        //常规非空校验
        if (org.apache.commons.lang3.StringUtils.isAnyBlank(parentUserId.toString(), senderUserId.toString(), recipientUserId.toString(), asset)
                || null == amount || amount.compareTo(BigDecimal.ZERO) < 1) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        // 确保划转未超过1分钟5次频率
//        if (transferExceededFrequencyLimits(parentUserId)) {
//            log.info("SubUserBusiness.exceeded frequency limits, parentUserId:{}", parentUserId);
//            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
//        }
        // 确保转出转入方不可同一账户
        Boolean subAccountEqualsFlag=senderUserId.equals(recipientUserId);
        if (subAccountEqualsFlag) {
            throw new BusinessException(AccountErrorCode.SUB_USER_TRANSFER_ACCOUNT_SHOULD_BE_DIFFERENT);
        }
        //转入转出方的margin、main关系
        //获取转入方和转出方的基本信息
//        User senderUser = checkAndGetUserById(senderUserId);
//        User recipientUser =checkAndGetUserById(recipientUserId);
        //获取转入方和转出方的安全信息
//        UserSecurity senderUserSecurity = this.userSecurityMapper.selectByPrimaryKey(senderUserId);
//        UserSecurity recipientUserSecurity = this.userSecurityMapper.selectByPrimaryKey(recipientUserId);
//        if(null==senderUser||null==recipientUser||null==senderUserSecurity||null==recipientUserSecurity){
//            throw new BusinessException(AccountErrorCode.MISS_USER_BASE_DETAIL);
//        }
        //获取当前资产信息是否存在
        AssetResponse assetResponse=assetApiClient.getAssetByCode(asset);
        if(null==assetResponse){
            throw new BusinessException(AccountErrorCode.ASSET_IS_NOT_EXIST);
        }
        //如果是从现货转到margin则需要检验，相反不需要
        if (type == 1){
            //查询当前用户的资产是否存在
            UserAssetResponse userAssetResponse=userAssetApiClient.getPrivateUserAsset(senderUserId.toString(),asset);
            if(null==userAssetResponse||CollectionUtils.isEmpty(userAssetResponse.getUserAssetList())){
                throw new BusinessException(AccountErrorCode.USER_HAVE_NO_ASSET);
            }
            //当前用户的资产
            if(amount.compareTo(userAssetResponse.getUserAssetList().get(0).getFree()) > 0){
                throw new BusinessException(AccountErrorCode.USER_HAVE_NO_AVALIABLE_AMOUNT);
            }
            //判断当前用户有没有负资产
            if(userAssetApiClient.queryNegativeAssetByUserId(senderUserId)>0){
                throw new BusinessException(AccountErrorCode.USER_AMOUNT_LESS_THAN_ZERO);
            }
        }

        //到此结束，需要验证的东西都验证完毕
    }



    /**
     * 转账频率限制,超过频率是true，没有超过就是false
     */
    private Boolean transferExceededFrequencyLimits(Long parentUserId, boolean isBroker){
        String subAccountTransferLimit = isBroker?String.valueOf(brokerTransferLimit):sysConfigVarCacheService.getValue(SUB_ACCOUNT_TRANSFER_LIMIT);
        int frequencyLimits= 5;
        try {
            //默认就是5，这边硬编码，如果有配置那么以配置为主
            if (org.apache.commons.lang3.StringUtils.isNotBlank(subAccountTransferLimit)) {
                frequencyLimits = Integer.parseInt(subAccountTransferLimit);
            }
        } catch (Exception e) {
            log.warn("setting frequencyLimits error.", e);
        }
        //1分钟50次(不论成功失败)
        long transferCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_SUB_USER_TRANSFER_KEY, 0L);
        if(transferCount <= frequencyLimits) {
            transferCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_SUB_USER_TRANSFER_KEY, 1L, 1L, TimeUnit.MINUTES);
        }
        log.info("SubUserBusiness.transferExceededFrequencyLimits, parentUserId:{}, frequencyLimits:{},redisTransferCount:{}", parentUserId, frequencyLimits,transferCount);
        if (transferCount > frequencyLimits) {
            //请求次数过多,一分钟就50次，能命中真的牛逼
            return true;
        }else{
            return false;
        }
    }

    /**
     * 转账频率限制,超过频率是true，没有超过就是false
     */
    public Boolean transferExceededFrequencyFutureLimits(Long parentUserId,String key,Integer brokerTransferLimit){
        Integer frequencyLimits = brokerTransferLimit!=null?brokerTransferLimit:5000;
        //1分钟50次(不论成功失败)
        long transferCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, key, 0L);
        if(transferCount <= frequencyLimits) {
            transferCount = RedisCacheUtils.increment(parentUserId.toString(), key, 1L, 1L, TimeUnit.MINUTES);
        }
        log.info("SubUserBusiness.transferExceededFrequencyFutureLimits, parentUserId:{}, frequencyLimits:{},redisTransferCount:{}", parentUserId, frequencyLimits,transferCount);
        if (transferCount > frequencyLimits) {
            //请求次数过多,一分钟就50次，能命中真的牛逼
            return true;
        }else{
            return false;
        }
    }


    /**
     * 内部转账
     * */
    public Long internalTransfer(Long parentUserId, Long senderUserId, String senderEmail, Long recipientUserId, String recipientEmail, String asset, BigDecimal amount, String thirdTranId)throws Exception{
        try{
            // 1.创建流水号
            Long transactionId=tranApiClient.getTransIdForSubAccountTransfer(recipientUserId.toString());
            // 2.增加asset_sub_account_transfer记录，并标记为 开始(start)
            tranApiClient.addSubAccountTransferRecord(String.valueOf(parentUserId), senderUserId.toString(), senderEmail, recipientUserId.toString(), recipientEmail, asset, amount, transactionId, "start",thirdTranId);

            // 3.调用划转接口
            userAssetApiClient.assetTransfer(senderUserId.toString(),recipientUserId.toString(),asset,amount,transactionId, AccountConstants.SUBUSER_ASSET_TRANSFER);
            // 4.记录主流币汇率
            try {
                tranApiClient.recordBaseRates( asset,transactionId);
            } catch (Exception e) {
                log.warn(String.format("recordBaseRates error, transactionId:%s, transferAsset:%s.", transactionId, asset), e);
            }
            // 5.将asset_sub_account_transfer记录标记为 正常结束(done)
            try {
                tranApiClient.updateSubAccountTransferRecordStatus(transactionId, "done");
            } catch (Exception e) {
                log.error(String.format("updateSubAccountTransferRecordStatus error, transactionId:%s", transactionId), e);
            }
            //异步通知风控
            AsyncTaskExecutor.execute(() -> {
                log.info("AsyncTaskExecutor parentUserId={} start",parentUserId);
                String traceId = StringUtils.isBlank(TrackingUtils.getTrace()) ? TrackingUtils.generateUUID() : TrackingUtils.getTrace();
                TrackingUtils.saveTrace(traceId);
                boolean isParentUserSender=parentUserId.equals(senderUserId);
                boolean isParentUserRecipient=parentUserId.equals(recipientUserId);
                log.info("AsyncTaskExecutor parentUserId={},isParentUserSender={},isParentUserRecipient={}",parentUserId,isParentUserSender,isParentUserRecipient);
                if(!isParentUserSender && isParentUserRecipient){
                    try{
                        User parentUser = checkAndGetUserById(parentUserId);
                        UserStatusEx userStatusEx=new UserStatusEx(parentUser.getStatus());
                        if(userStatusEx.getIsBrokerSubUserFunctionEnabled().booleanValue()){
                            log.info("skip broker parentId={}",parentUserId);
                            return;
                        }
                        commonRiskApiClient.commonRuleForSubTransferToParent(senderUserId.toString(),amount.doubleValue(),asset,parentUserId.toString());
                    }catch (Exception e){
                        log.error("submit to common risk failed:",e);
                    }

                }
                TrackingUtils.clearTrace();
            });
            return transactionId;
        }catch (Exception e){
            log.error(String.format("internalTransfer error, senderUserId:%s", senderUserId), e);
            throw new BusinessException("transatcion.failed");
        }

    }


    protected void assertBrokerParentSubUserBound(final Long parentUserId, final Long brokerSubAccountId) {

        User parentUser = checkAndGetUserById(parentUserId);
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());

        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,brokerSubAccountId);
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
    }


}
