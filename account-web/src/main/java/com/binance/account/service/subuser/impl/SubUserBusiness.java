package com.binance.account.service.subuser.impl;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.constants.enums.AccountTypeEnum;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.integration.futureservice.DeliveryFutureTransferApiClient;
import com.binance.account.integration.futureservice.FutureTransferApiClient;
import com.binance.account.service.async.UserInfoAsyncTask;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.service.datamigration.impl.MsgNotificationToC2CHelper;
import com.binance.account.service.security.impl.UserSecurityBusiness;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.utils.InvitationCodeUtil;
import com.binance.account.vo.subuser.SubUserBindingVo;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToMasterResponse;
import com.binance.account.vo.security.response.SubAccountTransferVersionForSubToSubResponse;
import com.binance.account.vo.subuser.CreateNoEmailSubUserReq;
import com.binance.account.vo.subuser.enums.SubAccountTransferTypeVersionForSapi;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.assetservice.enums.SubAccountTransferEnum;
import com.binance.future.api.request.AssetTransferRequest;
import com.binance.master.utils.*;
import com.binance.master.utils.JsonUtils;
import com.google.common.base.Function;
import com.binance.account.vo.subuser.response.BindingParentSubUserEmailResp;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.utils.MapUtil;
import com.binance.account.vo.subuser.SubAccountTransferHistoryInfoVo;
import com.binance.account.vo.subuser.SubUserAssetBtcVo;
import com.binance.account.vo.subuser.request.ResendSubUserRegisterMailReq;
import com.binance.account.vo.subuser.request.SubAccountTransHistoryInfoReq;
import com.binance.account.vo.subuser.request.SubUserAssetBtcRequest;
import com.binance.account.vo.subuser.request.SubUserCurrencyBalanceReq;
import com.binance.account.vo.subuser.request.SubUserTransferByTranIdReq;
import com.binance.account.vo.subuser.response.SubAccountTransferHistoryInfoResp;
import com.binance.account.vo.subuser.response.SubUserAssetBtcResponse;
import com.binance.account.vo.subuser.response.SubUserCurrencyBalanceResp;
import com.binance.account.vo.user.request.GetUserRequest;
import com.binance.account.vo.user.request.ResendSendActiveCodeRequest;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import com.binance.assetservice.vo.response.GetSubAccountTransferHistoryResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.assetservice.vo.response.asset.SelectUserAssetResponse;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.apache.commons.lang3.time.StopWatch;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.SecurityLog;
import com.binance.account.common.constant.UserConst;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.subuser.ISubUser;
import com.binance.account.service.user.IUser;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.GetUserSecurityLogResponse;
import com.binance.account.vo.security.response.SubAccountTransferResponse;
import com.binance.account.vo.subuser.SubUserEmailVo;
import com.binance.account.vo.subuser.SubUserInfoVo;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.CreateSubUserReq;
import com.binance.account.vo.subuser.request.ModifySubAccountRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.QuerySubUserRequest;
import com.binance.account.vo.subuser.request.ResetSecondValidationRequest;
import com.binance.account.vo.subuser.request.SubAccountTransHisReq;
import com.binance.account.vo.subuser.request.SubAccountTransferRequest;
import com.binance.account.vo.subuser.request.SubUserSearchReq;
import com.binance.account.vo.subuser.request.SubUserSecurityLogReq;
import com.binance.account.vo.subuser.request.UpdatePassWordRequest;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.subuser.response.CreateSubUserResp;
import com.binance.account.vo.subuser.response.SubAccountResp;
import com.binance.account.vo.subuser.response.SubAccountTransferResp;
import com.binance.account.vo.subuser.response.SubUserEmailVoResp;
import com.binance.account.vo.subuser.response.SubUserInfoResp;
import com.binance.account.vo.subuser.response.SubUserTypeResponse;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.assetservice.vo.request.GetSubAccountTransferHistoryRequest;
import com.binance.assetservice.vo.response.AssetSubAccountTrasnferVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.SysType;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Resource;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@Log4j2
@Service
public class SubUserBusiness extends CheckSubUserBusiness implements ISubUser {

    private static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    private static final Integer MAX_EMAIL_LENGTH = 200;
    private static final String SUB_FUTURE_TRANSFER_LIMIT="sub_future_transfer_limit";
    private static final Pattern COMPATIBLE_EMAIL_PATTERN =
            Pattern.compile("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");

    private static final String GOOGLE = "google";
    private static final String SMS = "sms";
    private static final String SUCCESS = "success";
    private static final String MSG = "msg";
    private static final int DEFAULT_LIMIT = 500;
    private static final int MAX_LIMIT = 1000;
    private static final int DEFAULT_QUERY_DAYS = -100;
    private static final long LEGAL_FIRST_DATE = 946656000000L;

    @Value("${sub.account.max.create.nums}")
    private Long MAX_NUM_OF_SUB_USERS_TOTAL;

    @Value("${sub.account.max.create.perday}")
    private Long MAX_NUM_OF_SUB_USERS_PER_DAY;

    @Value("${sub.account.history.switch:false}")
    private Boolean subHistorySwitch;

    @Value("${sub.furure.transfer.limit:500}")
    private Integer subAccountFutureTransferLimit;

    private static final String REDIS_SUB_USER_CREATE_KEY = "account:subuser:create:";
    private static final String REDIS_SUB_USER_REGISTER_MAIL_KEY = "account:subuser:registerMail:";



    @Autowired
    private IUserSecurity userSecurityBusiness;

    @Autowired
    private UserAgentLogMapper userAgentLogMapper;

    @Autowired
    private IUser userService;

    @Autowired
    private UserAssetApiClient userAssetApiClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserInfoAsyncTask userInfoAsyncTask;
    @Autowired
    private FutureTransferApiClient futureTransferApiClient;
    @Autowired
    private DeliveryFutureTransferApiClient deliveryFutureAssetTransfer;

    @Autowired
    private MatchboxApiClient matchboxApiClient;

    @Autowired
    private UserTradingAccountMapper userTradingAccountMapper;

    @Resource
    protected IUserSecurity iUserSecurity;

    @Autowired
    private MsgNotificationToC2CHelper notificationToC2CHelper;

    @Override
    public APIResponse<Boolean> isSubUserFunctionEnabled(APIRequest<ParentUserIdReq> request) throws Exception {
        ParentUserIdReq requestBody = request.getBody();
        final User parentUser = checkAndGetUserById(requestBody.getParentUserId());
        return APIResponse.getOKJsonResult(isSubUserFunctionEnabled(parentUser.getStatus()));
    }

    @Monitored
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
//    @UserPermissionValidate(userId = "#request.body.parentUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_SUB_ACCOUNT)
    public APIResponse<CreateSubUserResp> createSubUser(APIRequest<CreateSubUserReq> request) throws Exception {

        final CreateSubUserReq requestBody = request.getBody();

        final Long parentUserId = requestBody.getParentUserId();
        final User parentUser = checkAndGetUserById(parentUserId);
        log.info("createSubUser start parentUserId:{}, subUserEmail:{}", parentUserId, requestBody.getEmail());
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 已经开通broker账户的，不能再创建子账户
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());

        // 24小时最多可以创建50次子账户(不论成功失败)
        long tryToCreateCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_SUB_USER_CREATE_KEY, 0L);
        if(tryToCreateCount <= MAX_NUM_OF_SUB_USERS_PER_DAY) {
            tryToCreateCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_SUB_USER_CREATE_KEY, 1L, 24L, TimeUnit.HOURS);
        }
        if (tryToCreateCount > MAX_NUM_OF_SUB_USERS_PER_DAY) {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }

        // 每个母账户最多可创建200个子账户
        long subUserCount = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);
        if (subUserCount > MAX_NUM_OF_SUB_USERS_TOTAL) {
            throw new BusinessException(GeneralCode.SUB_USER_MAX_TOTAL, new Object[] {MAX_NUM_OF_SUB_USERS_TOTAL});
        }

        final String subUserEmail = requestBody.getEmail().trim().toLowerCase();

        if (!Pattern.matches(REGEX_EMAIL, subUserEmail) || subUserEmail.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        if (null != userMapper.queryByEmail(subUserEmail)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }

        final Long agentId;
        final UserInfo parentUserInfo = this.userInfoMapper.selectByPrimaryKey(parentUserId);
        if (null == parentUserInfo || null == parentUserInfo.getAgentId()) {
            log.warn("parentUserInfo is null, parentUserId:{}", parentUserId);
            agentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
        } else {
            agentId = parentUserInfo.getAgentId();
        }

        final String traceSource = requestBody.getTrackSource();

        // 创建子账号
        User subUser =  ((SubUserBusiness) AopContext.currentProxy()).createUser(subUserEmail, requestBody.getPassword(), true);

        // 绑定主账号、子账号关系
        ((SubUserBusiness) AopContext.currentProxy()).createParentSubUserBinding(parentUserId, subUser.getUserId(), requestBody.getRemark());

        // 创建子账号Security信息
        ((SubUserBusiness) AopContext.currentProxy()).createUserSecurity(subUser.getUserId(), subUser.getEmail());

        // 创建子账号用户信息
        ((SubUserBusiness) AopContext.currentProxy()).createSubUserInfo(parentUserId, subUser.getUserId(), agentId, traceSource);

        // 发送激活邮件
        String[] activeEmailParams = sendActiveEmail(subUser, request.getTerminal(), requestBody.getCustomEmailLink());

        // 添加设备信息、IP信息、日志信息
        ((SubUserBusiness) AopContext.currentProxy()).addDeviceInfoAndLogs(subUser.getUserId(), subUser.getEmail(), request.getTerminal(),
                requestBody.getDeviceInfo());

        // 发送用户注册MQ消息至PNK同步数据
        sendRegisterMqMsg(subUser, agentId, traceSource, activeEmailParams);
        // 发送用户市场手续费的消息给pnk同步数据
        sendUserProductFeeMsg(parentUserId,subUser.getUserId());
        AsyncTaskExecutor.execute(() -> {
            try {
                //加入推荐记录表
                insertToAgentLog(parentUserId,subUser.getUserId(),subUser.getEmail());
            }catch (Exception e){
                log.error("insertToAgentLog exception", e);
            }
        });

        notificationToC2CHelper.sendUserTypeChangesMsgAsync(null, Lists.newArrayList(subUser.getUserId()), true);

        // 构建ResponseBody
        CreateSubUserResp response = new CreateSubUserResp();
        response.setParentUserId(parentUserId);
        response.setUserId(subUser.getUserId());
        response.setEmail(subUser.getEmail());
        /*response.setSalt(subUser.getSalt());
        response.setPassword(subUser.getPassword());*/
        response.setAgentId(agentId);
       /* response.setRegisterToken(activeEmailParams[0]);
        response.setCode(activeEmailParams[1]);*/
        response.setCurrentDeviceId(null);

        log.info("createSubUser done parentUserId:{}, subUserEmail:{}", parentUserId, requestBody.getEmail());

        return APIResponse.getOKJsonResult(response);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<CreateNoEmailSubUserResp> createNoEmailSubUser(APIRequest<CreateNoEmailSubUserReq> request) throws Exception {
        final CreateNoEmailSubUserReq requestBody = request.getBody();

        if(requestBody.getUserName().length()>20){
            throw new BusinessException(AccountErrorCode.USER_NAME_LENGTH_EXCEED);
        }

        if (!StringUtils.equals(requestBody.getPassword(), requestBody.getConfirmPassword())) {
            return APIResponse.getErrorJsonResult("Entered passwords differ");
        }

        final Long parentUserId = requestBody.getParentUserId();
        final User parentUser = checkAndGetUserById(parentUserId);
        log.info("createNoEmailSubUser start parentUserId:{}, subUserName:{}", parentUserId, requestBody.getUserName());
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 已经开通broker账户的，不能再创建子账户
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());

        // 24小时最多可以创建50次子账户(不论成功失败)
        long tryToCreateCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_SUB_USER_CREATE_KEY, 0L);
        if(tryToCreateCount <= MAX_NUM_OF_SUB_USERS_PER_DAY) {
            tryToCreateCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_SUB_USER_CREATE_KEY, 1L, 24L, TimeUnit.HOURS);
        }
        if (tryToCreateCount > MAX_NUM_OF_SUB_USERS_PER_DAY) {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }

        // 每个母账户最多可创建200个子账户
        long subUserCount = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);
        if (subUserCount > MAX_NUM_OF_SUB_USERS_TOTAL) {
            throw new BusinessException(GeneralCode.SUB_USER_MAX_TOTAL, new Object[] {MAX_NUM_OF_SUB_USERS_TOTAL});
        }

        final String subUserEmail = generateFakeEmail(requestBody.getUserName().trim().toLowerCase(), 0);
        final Long agentId;
        final UserInfo parentUserInfo = this.userInfoMapper.selectByPrimaryKey(parentUserId);
        if (null == parentUserInfo || null == parentUserInfo.getAgentId()) {
            log.warn("parentUserInfo is null, parentUserId:{}", parentUserId);
            agentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
        } else {
            agentId = parentUserInfo.getAgentId();
        }

        final String traceSource = requestBody.getTrackSource();

        // 创建子账号
        User subUser = ((SubUserBusiness) AopContext.currentProxy()).createNoEmailSubUser(subUserEmail, requestBody.getPassword());

        // 绑定主账号、子账号关系
        ((SubUserBusiness) AopContext.currentProxy()).createParentSubUserBinding(parentUserId, subUser.getUserId(), requestBody.getRemark());

        // 创建子账号Security信息
        ((SubUserBusiness) AopContext.currentProxy()).createUserSecurity(subUser.getUserId(), subUser.getEmail());

        // 创建子账号用户信息
        UserInfo subUserInfo=((SubUserBusiness) AopContext.currentProxy()).createSubUserInfo(parentUserId, subUser.getUserId(), agentId, traceSource);
//        subUserInfo.setUserType(com.binance.account.constants.enums.UserTypeEnum.NORMAL.name());

        // 添加设备信息、IP信息、日志信息
        ((SubUserBusiness) AopContext.currentProxy()).addDeviceInfoAndLogs(subUser.getUserId(), subUser.getEmail(), request.getTerminal(),
                requestBody.getDeviceInfo());
        //直接激活账户
        //这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long tradingAccount = matchboxApiClient.postAccount(subUserInfo, MatchBoxAccountTypeEnum.SPOT);
        subUserInfo.setTradingAccount(tradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(subUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(tradingAccount);
        userTradingAccount.setUserId(subUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        log.info("createNoEmailSubUser.postAccount insert:{}", JSON.toJSONString(userTradingAccount));

        User updateUser = new User();
        updateUser.setEmail(subUser.getEmail());
        updateUser.setStatus(subUser.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE);// 默认禁用app交易
        this.userMapper.updateByEmail(updateUser);
        // 发送用户注册MQ消息至PNK同步数据
        sendRegisterMqMsgForNoEmailSubUser(subUser, subUserInfo);
        // 发送用户市场手续费的消息给pnk同步数据
        sendUserProductFeeMsg(parentUserId,subUser.getUserId());
        AsyncTaskExecutor.execute(() -> {
            try {
                //加入推荐记录表
                insertToAgentLog(parentUserId,subUser.getUserId(),subUser.getEmail());
            }catch (Exception e){
                log.error("insertToAgentLog exception", e);
            }
        });

        notificationToC2CHelper.sendUserTypeChangesMsgAsync(null, Lists.newArrayList(subUser.getUserId()), true);
        
        // 构建ResponseBody
        CreateNoEmailSubUserResp response = new CreateNoEmailSubUserResp();
        response.setParentUserId(parentUserId);
        response.setUserId(subUser.getUserId());
        response.setEmail(subUser.getEmail());
        response.setAgentId(agentId);
        log.info("createNoEmailSubUser done parentUserId:{}, subUserEmail:{}", parentUserId, requestBody.getUserName());
        return APIResponse.getOKJsonResult(response);
    }

    private String generateFakeEmail(String subUserName, int repeatTimes) {
        log.info("generateFakeEmail invoked, subUserName={} repeatTimes={}", subUserName, repeatTimes);
        String randomCode = InvitationCodeUtil.generateRandomCode(8).toLowerCase();
        String subUserEmail = subUserName+"_virtual@"+randomCode+"noemail.com";//拼接虚拟邮箱
        
        if (!Pattern.matches(REGEX_EMAIL, subUserEmail) || subUserEmail.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        if (null != userMapper.queryByEmail(subUserEmail)) {
            // 如果随机创建的邮箱重复，重试2次。第三次仍然重复，抛异常
            if (repeatTimes >= 2) {
                throw new BusinessException(GeneralCode.USER_EXIST);    
            }
            return generateFakeEmail(subUserName, ++repeatTimes);
        } else {
            return subUserEmail;
        }
    }


    @Monitored
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean createAssetManagerSubUser(CreateAssetManagerSubUserReq createAssetManagerSubUserReq) throws Exception {
        Long parentUserId = createAssetManagerSubUserReq.getParentUserId();
        Long assetSubUserId = createAssetManagerSubUserReq.getAssetSubUserId();
        log.info("createAssetManagerSubUser start parentUserId:{}, assetSubUserId:{}", parentUserId, assetSubUserId);
        //1.检验母账户，是否为母账户，如果非资管母账户、添加资管母账户
        //  检验是否是子账户、非资管子账户
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());
        assertBrokerSubUserFunctionDisabled(parentUser.getStatus());
        User subUser = checkAndGetUserById(assetSubUserId);
        if (!AccountTypeEnum.getAccountType(subUser.getStatus()).equals(AccountTypeEnum.NORMAL.getAccountType())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 24小时最多可以创建50次子账户(不论成功失败)
        long tryToCreateCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_SUB_USER_CREATE_KEY, 0L);
        if(tryToCreateCount <= MAX_NUM_OF_SUB_USERS_PER_DAY) {
            tryToCreateCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_SUB_USER_CREATE_KEY, 1L, 24L, TimeUnit.HOURS);
        }
        if (tryToCreateCount > MAX_NUM_OF_SUB_USERS_PER_DAY) {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        // 每个母账户最多可创建200个子账户
        long subUserCount = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);
        if (subUserCount > MAX_NUM_OF_SUB_USERS_TOTAL) {
            throw new BusinessException(GeneralCode.SUB_USER_MAX_TOTAL, new Object[] {MAX_NUM_OF_SUB_USERS_TOTAL});
        }
        //2.enable资管母账户 enable资管子账户
        //资产母账户，如果未开通，则开通
        if(!checkAssetSubUserFunctionEnabled(parentUser.getStatus())){
            parentUser.setStatus(BitUtils.enable(parentUser.getStatus(),Constant.USER_IS_ASSET_SUBUSER_FUNCTION_ENABLED));
            userMapper.updateByEmail(parentUser);
        }
        subUser.setStatus(BitUtils.enable(BitUtils.enable(BitUtils.enable(BitUtils.enable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER),Constant.USER_IS_ASSET_SUB_USER_ENABLED),Constant.USER_IS_SUBUSER),Constant.USER_IS_SUB_USER_ENABLED));
        userMapper.updateByEmail(subUser);
        //更新subUserInfo
        UserInfo assetSubUserInfo = new UserInfo();
        assetSubUserInfo.setUserId(assetSubUserId);
        assetSubUserInfo.setParent(parentUserId);
        userInfoMapper.updateByPrimaryKeySelective(assetSubUserInfo);
        //3.保存到subuserbinding
        ((SubUserBusiness) AopContext.currentProxy()).createParentSubUserBinding(parentUserId, assetSubUserId, null);

        // 4.从母账号同步交易等级和手续费给子账号
        userInfoAsyncTask.setOneSubUserTradeLevelAndCommission(parentUserId, assetSubUserId);
        // 发送用户市场手续费的消息给pnk同步数据
        sendUserProductFeeMsg(parentUserId,subUser.getUserId());
        return true;
    }

    private void insertToAgentLog(Long parentUserId, Long currentUserId, String currentEmail) {
        UserAgentLog existParent = userAgentLogMapper.selectByReferralUserId(parentUserId);
        if (existParent == null || currentUserId == null || StringUtils.isBlank(currentEmail)){
            return;
        }
        UserAgentLog userAgentLog = new UserAgentLog();
        userAgentLog.setAgentCode(existParent.getAgentCode());
        userAgentLog.setUserId(existParent.getUserId());
        userAgentLog.setReferralUser(currentUserId);
        userAgentLog.setReferralEmail(currentEmail);
        User user = userMapper.queryByEmail(currentEmail);
        if (user != null){
            userAgentLog.setUserType(UserTypeEnum.getAccountType(user.getStatus()));
        }
        userAgentLogMapper.insertSelective(userAgentLog);
    }


    @Override
    public APIResponse<SubUserTypeResponse> checkRelationByUserId(APIRequest<UserIdReq> request) throws Exception {

        UserIdReq requestBody = request.getBody();
        final Long userId = requestBody.getUserId();

        final User user = checkAndGetUserById(userId);

        SubUserTypeResponse response = new SubUserTypeResponse();

        if (isSubUserFunctionEnabled(user.getStatus())) {
            response.setUserType(SubUserTypeResponse.UserType.PARENT);
        } else if (isSubUser(user)) {
            response.setUserType(SubUserTypeResponse.UserType.SUB);
            SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(userId);
            response.setParentUserId(subUserBinding.getParentUserId());
        } else {
            response.setUserType(SubUserTypeResponse.UserType.NORMAL);
        }

        if (response.getUserType() != SubUserTypeResponse.UserType.SUB) {
            List<Long> subUserIds = new ArrayList<>();
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByParentUserId(userId);
            for (SubUserBinding subUserBinding : subUserBindings) {
                subUserIds.add(subUserBinding.getSubUserId());
            }
            if (CollectionUtils.isNotEmpty(subUserIds)) {
                response.setSubUserIds(subUserIds);
                log.info("This user used to be a parent-user, userId:{}", userId);

                List<SubUserEmailVo> subUserIdEmails = new ArrayList<>();
                List<UserIndex> userIndexList = userIndexMapper.selectByUserIds(subUserIds);
                for (UserIndex userIndex : userIndexList) {
                    SubUserEmailVo subUserEmailVo = new SubUserEmailVo();
                    subUserEmailVo.setUserId(userIndex.getUserId());
                    subUserEmailVo.setEmail(userIndex.getEmail());
                    subUserIdEmails.add(subUserEmailVo);
                }
                if (CollectionUtils.isNotEmpty(subUserIdEmails)) {
                    response.setSubUserIdEmails(subUserIdEmails);
                }
            }
        }

        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Boolean> checkRelationByParentSubUserIds(APIRequest<BindingParentSubUserReq> request)
            throws Exception {
        final BindingParentSubUserReq requestBody = request.getBody();
        final Long parentUserId = requestBody.getParentUserId();
        final Long subUserId = requestBody.getSubUserId();
        try {
            assertParentSubUserBound(parentUserId, subUserId);
        } catch (Exception e) {
            log.info("not in parent-sub relation, parentUserId:{}, subUserId:{}", parentUserId, subUserId);
            return APIResponse.getOKJsonResult(false);
        }
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public BindingParentSubUserEmailResp checkRelationByParentSubUserEmail(BindingParentSubUserEmailReq request) throws Exception {
        Long parentUserId = request.getParentUserId();
        String subUserEmail = request.getSubUserEmail();
        User subUser=checkAndGetUserByEmail(subUserEmail);
        Long subUserId=subUser.getUserId();
        assertParentSubUserBound(parentUserId, subUserId);
        BindingParentSubUserEmailResp resp=new BindingParentSubUserEmailResp();
        resp.setSubUserId(subUserId);
        return resp;
    }

    @Override
    public APIResponse<Boolean> notSubUserOrIsEnabledSubUser(APIRequest<UserIdReq> request) throws Exception {
        final UserIdReq requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        User user = checkAndGetUserById(userId);
        boolean isSubUser = BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER);
        boolean isSubUserEnabled = BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUB_USER_ENABLED);
        log.info("notSubUserOrIsEnabledSubUser userId:{}, isSubUser:{}, isSubUserEnabled:{}", userId, isSubUser,
                isSubUserEnabled);
        if (!isSubUser) {
            return APIResponse.getOKJsonResult(true);
        }
        if (isSubUserEnabled) {
            return APIResponse.getOKJsonResult(true);
        }
        return APIResponse.getOKJsonResult(false);
    }

    @Override
    public APIResponse<Integer> resetSecondValidation(APIRequest<ResetSecondValidationRequest> request)
            throws Exception {
        ResetSecondValidationRequest requestBody = request.getBody();
        Long subUserId = requestBody.getSubUserId();

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 确保至少一项2FA开打
        assertUser2FaAtLeastOneEnabled(parentUser.getStatus());

        // 母子关系验证
        User subUser = assertParentSubUserBoundNotCheckParent(parentUserId, subUserId);
        //资管子账户不可被修改2fa
        if (checkAssetSubUser(subUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 母账户2fa验证
        AuthTypeEnum authType = requestBody.getParentAuthType();
        iUserSecurity.verificationsTwo(parentUserId, authType, requestBody.getParentCode(), true);

        log.info("resetSecondValidation parentUserId:{}, subUserId:{}, type:{}", parentUserId, subUserId,
                requestBody.getSubType().getCode());

        final UserSecurityLog securityLog = new UserSecurityLog();
        securityLog.setDescription("重置子账户2fa,子账户userId:" + subUserId);

        // 重置2fa状态
        if (GOOGLE.equalsIgnoreCase(requestBody.getSubType().getCode())) {
            if (BitUtils.isEnable(subUser.getStatus(), Constant.USER_GOOGLE)) {
                resetGoogleVerify(subUserId, subUser.getStatus());
                securityLog.setOperateType(Constant.SECURITY_OPERATE_RESET_GOOGLE);
            } else {
                throw new BusinessException(GeneralCode.USER_GOOGLE_VERIFY_NO);
            }

        } else if (SMS.equalsIgnoreCase(requestBody.getSubType().getCode())) {
            if (BitUtils.isEnable(subUser.getStatus(), Constant.USER_MOBILE)) {
                unbindMobile(subUserId, subUser.getStatus());
                securityLog.setOperateType(Constant.SECURITY_OPERATE_RESET_MOBILE);
            } else {
                throw new BusinessException(GeneralCode.USER_MOBILE_VERIFY_NO);
            }
        }

        // 添加安全日志
        addSecurityLog(securityLog, parentUserId, "resetSecondValidation");

        // 同步修改pnk
        Map<String, Object> dataMsg = MapUtil.beanToMap(request.getBody());
        dataMsg.put(UserConst.USER_ID, parentUserId);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.RESET_SUBUSER_SECOND_VALIDATION, dataMsg);
        log.info("iMsgNotification resetSecondValidation:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        return APIResponse.getOKJsonResult(1);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Integer> updateSubUserPwd(APIRequest<UpdatePassWordRequest> request) throws Exception {
        UpdatePassWordRequest requestBody = request.getBody();
        Long subUserId = requestBody.getSubUserId();
        String password = requestBody.getPassword();

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        User subUser = checkAndGetUserById(subUserId);

        // 没有激活不允许操作
        if (!BitUtils.isEnable(subUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        //无邮箱子账户不可修改密码
        if (BitUtils.isEnable(subUser.getStatus(), AccountCommonConstant.USER_IS_NO_EMAIL_SUB_USER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 参数校验
        if (!StringUtils.equals(password, requestBody.getConfirmPassword())) {
            return APIResponse.getErrorJsonResult("Entered passwords differ");
        }

        // 确保至少一项2FA开打
        assertUser2FaAtLeastOneEnabled(parentUser.getStatus());

        // 母子关系验证
        assertParentSubUserBoundNotCheckParent(parentUserId, subUserId);
        if(checkAssetSubUser(subUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 母账户2fa验证
        AuthTypeEnum authType = requestBody.getParentAuthType();
        this.iUserSecurity.verificationsTwo(parentUserId, authType, requestBody.getParentCode(), true);

        log.info("updateSubUserPwd parentUserId:{}, subUserId:{}", parentUserId, subUserId);

        // 修改子账户密码
        updatePwd(subUserId, password);

        // 添加安全日志
        final UserSecurityLog securityLog = new UserSecurityLog();
        securityLog.setDescription("修改子账户密码,子账户userId:" + subUserId);
        securityLog.setOperateType(Constant.SECURITY_OPERATE_UPDATE_SUBUSER_PSW);
        addSecurityLog(securityLog, parentUserId, "updateSubUserPwd");

        // 同步修改pnk
        Map<String, Object> dataMsg = MapUtil.beanToMap(request.getBody());
        dataMsg.put(UserConst.USER_ID, parentUserId);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.UPDATE_SUBUSER_PWD, dataMsg);
        log.info("iMsgNotification updateSubUserPwd:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        return APIResponse.getOKJsonResult(1);
    }

    @Monitored
    @Override
    public APIResponse<SubUserInfoResp> selectSubUserInfo(APIRequest<QuerySubUserRequest> request) {
        QuerySubUserRequest requestBody = request.getBody();

        int limit = requestBody.getLimit();
        int offset = requestBody.getOffset();
        String email = requestBody.getEmail();

        if (StringUtils.isNotBlank(email)) {
            email = email.replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
        }

        Integer isSubUserEnabled = requestBody.getIsSubUserEnabled();

        if (limit < 0 || offset < 0) {
            return APIResponse.getErrorJsonResult("param error");
        }

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        List<SubUserBinding> subUserBindingList = subUserBindingMapper.getSubUserBindingsByParentUserId(parentUserId);


        SubUserInfoResp resp = new SubUserInfoResp();
        List<SubUserInfoVo> respVo = new ArrayList<>();
        if (subUserBindingList == null || subUserBindingList.size() <= 0) {
            log.info("该母账户下没有有效的子账户:{}", parentUserId);
            resp.setResult(respVo);
            resp.setCount(0L);
            return APIResponse.getOKJsonResult(resp);
        }
        List<Long> subUserIds = Lists.transform(subUserBindingList, new Function<SubUserBinding, Long>() {
            @Override
            public Long apply(@Nullable SubUserBinding subUserBinding) {
                return subUserBinding.getSubUserId();
            }
        });
        Map<Long,SubUserBinding> subUserBindingMap=Maps.uniqueIndex(subUserBindingList, new Function<SubUserBinding, Long>() {
            @Override
            public Long apply(@Nullable SubUserBinding subUserBinding) {
                return subUserBinding.getSubUserId();
            }
        });


        // 查询总条数
        Long count = userMapper.selectCountSubUserIds(subUserIds, email, isSubUserEnabled);

        // 批量查询用户信息
        List<User> userList = userMapper.selectUserByUserIds(subUserIds, email, isSubUserEnabled, limit, offset);
        userList.forEach(user -> {
            SubUserInfoVo subVo = new SubUserInfoVo();
            UserStatusEx statusEx = new UserStatusEx(user.getStatus());
            subVo.setSubUserId(user.getUserId());
            subVo.setEmail(user.getEmail());
            subVo.setIsSubUserEnabled(statusEx.getIsSubUserEnabled());
            subVo.setIsAssetSubUser(statusEx.getIsAssetSubUser());
            subVo.setIsAssetSubUserEnabled(statusEx.getIsAssetSubUserEnabled());
            subVo.setIsUserActive(statusEx.getIsUserActive());
            subVo.setIsUserGoogle(statusEx.getIsUserGoogle());
            subVo.setInsertTime(user.getInsertTime());
            if (statusEx.getIsUserMobile()) {
                subVo.setMobile(userSecurityMapper.selectMobileByUserId(user.getUserId()));
            }
            subVo.setIsMarginEnabled(statusEx.getIsExistMarginAccount());
            subVo.setIsFutureEnabled(statusEx.getIsExistFutureAccount());
            subVo.setIsNoEmailSubUser(statusEx.getIsNoEmailSubUser());
            SubUserBinding subUserBinding=subUserBindingMap.get(user.getUserId());
            subVo.setRemark(subUserBinding.getRemark());
            respVo.add(subVo);
        });

        resp.setResult(respVo);
        resp.setCount(count);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<List<SubAccountResp>> getSubAccountList(APIRequest<SubUserSearchReq> request){
        SubUserSearchReq subUserSearchReq = request.getBody();
        //1.确保是母账户
        Long parentUserId = subUserSearchReq.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        //组装请求参数
        APIRequest<QuerySubUserRequest> originRequest = new APIRequest<QuerySubUserRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        QuerySubUserRequest querySubUserRequest = new QuerySubUserRequest();
        Integer page=0;
        Integer limit=0;
        if (null == subUserSearchReq.getPage()) {
            page = 1;
        }
        if (null == subUserSearchReq.getLimit()) {
            limit = DEFAULT_LIMIT;
        }
        if (limit.compareTo(MAX_LIMIT) == 1) {
            limit = MAX_LIMIT;
        }
        querySubUserRequest.setOffset((page - 1) * limit);
        querySubUserRequest.setLimit(limit);
        querySubUserRequest.setParentUserId(subUserSearchReq.getParentUserId());
        String email = subUserSearchReq.getEmail();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(email)) {
            email = email.replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                return APIResponse.getErrorJsonResult("vc.mail.eror");
            }
        }
        querySubUserRequest.setEmail(email);
        String status = subUserSearchReq.getStatus();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(status)) {
            //子账户开启状态 1:开启;0:未开启
            if (SubAccountResp.SubAccountStatus.enabled.name().equalsIgnoreCase(status)) {
                querySubUserRequest.setIsSubUserEnabled(1);
            } else {
                querySubUserRequest.setIsSubUserEnabled(0);
            }
        }

        //2.查询子账户
        APIResponse<SubUserInfoResp> resp = this.selectSubUserInfo(APIRequest.instance(originRequest, querySubUserRequest));
        if (!isOk(resp)) {
            return APIResponse.getErrorJsonResult(getErrorMsg(resp));
        }
        //3.组装返回结果
        List<SubAccountResp> resFromData = getResFromData(resp);
        return APIResponse.getOKJsonResult(resFromData);
    }

    @Override
    public APIResponse<List<SubAccountTransferResp>> getSubAccountTransferHistory(APIRequest<SubAccountTransHisReq> request) throws Exception {
        //1.确保是母账户
        SubAccountTransHisReq subAccountTransHisReq = request.getBody();
        Long parentUserId = subAccountTransHisReq.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        //2.校验email
        String email = subAccountTransHisReq.getEmail();
        Long subUserId = null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(email)) {
            email = subAccountTransHisReq.getEmail().replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                return APIResponse.getErrorJsonResult("vc.mail.eror");
            }
            //3.确保为子母账户关系
            User subUser = this.userMapper.queryByEmail(email);
            if (subUser == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
            }
            subUserId = subUser.getUserId();
            assertParentSubUserBound(parentUserId, subUserId);
        }
        //4.获取userIds
        GetSubAccountTransferHistoryRequest getSubAccountTransferHistoryRequest = new GetSubAccountTransferHistoryRequest();
        if (subUserId != null){
            getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(subUserId));
        }else{
            //直接通过parentUserId查询
            getSubAccountTransferHistoryRequest.setParentUserId(parentUserId);
            getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(parentUserId));
            if (!subHistorySwitch){
                APIRequest<UserIdReq> userIdReqAPI = new APIRequest<>();
                UserIdReq userIdReq = new UserIdReq();
                userIdReq.setUserId(subAccountTransHisReq.getParentUserId());
                userIdReqAPI.setBody(userIdReq);
                APIResponse<SubUserTypeResponse> subUserTypeResponseAPIResponse = this.checkRelationByUserId(userIdReqAPI);
                log.info("selectTransferLogByUserId response userId:{}, resp:{}", userIdReqAPI, subUserTypeResponseAPIResponse);
                if (!isOk(subUserTypeResponseAPIResponse)){
                    return APIResponse.getErrorJsonResult("sub.user.illegal.relation");
                }
                if (subUserTypeResponseAPIResponse.getData() == null || CollectionUtils.isEmpty(subUserTypeResponseAPIResponse.getData().getSubUserIds())){
                    return APIResponse.getErrorJsonResult("sub.user.is empty");
                }
                List<Long> subUserIds = subUserTypeResponseAPIResponse.getData().getSubUserIds();
                subUserIds.add(subAccountTransHisReq.getParentUserId());
                getSubAccountTransferHistoryRequest.setUserIds(subUserIds);
            }

        }
        getSubAccountTransferHistoryRequest.setStartCreateTime(checkAndGetStartTime(subAccountTransHisReq.getStartTime(), subAccountTransHisReq.getEndTime()));
        getSubAccountTransferHistoryRequest.setEndCreateTime(checkAndGetEndTime(subAccountTransHisReq.getEndTime()));
        addPageParam(subAccountTransHisReq.getPage(),subAccountTransHisReq.getLimit(),getSubAccountTransferHistoryRequest);

        GetSubAccountTransferHistoryResponse transferHistoryResponse = tranApiClient.getAccountTransferHistory(getSubAccountTransferHistoryRequest);
        log.info("tranApiClient.getAccountTransferHistory,param:{},result:{}",getSubAccountTransferHistoryRequest,transferHistoryResponse);
        List<AssetSubAccountTrasnferVo> accountTransferHistory = transferHistoryResponse.getAssetSubAccountTrasnferVoList();
        if (CollectionUtils.isEmpty(accountTransferHistory)){
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<SubAccountTransferResp> result = new ArrayList<>(accountTransferHistory.size());
        for (AssetSubAccountTrasnferVo vo:accountTransferHistory){
            SubAccountTransferResp resp = new SubAccountTransferResp();
            resp.setAsset(vo.getAsset());
            resp.setFrom(vo.getSenderEmail());
            resp.setQty(vo.getAmount().toPlainString());
            resp.setTime(vo.getCreateTime().getTime());
            resp.setTo(vo.getRecipientEmail());
            result.add(resp);
        }
        return APIResponse.getOKJsonResult(result);
    }
    /**
     * 查询母账号和子账号的绑定关系并且返回子账号的信息
     * */
    @Override
    public User checkParentAndSubUserBinding(Long parentUserId, String subUserEmail) throws Exception {
       return validateParentSubUserBoundAndGetSubUser(parentUserId,subUserEmail);
    }

    @Override
    public List<UserInfo> checkParentAndGetSubUserInfoList(Long parentUserId,String email,Integer isSubUserEnabled) throws Exception {
        if (StringUtils.isNotBlank(email)) {
            email = email.replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
        }
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
        if(CollectionUtils.isEmpty(subUserIds)){
            return Lists.newArrayList();
        }
        List<User> userList = userMapper.selectUserByUserIds(subUserIds, email, isSubUserEnabled, 0, 0);
        if(CollectionUtils.isEmpty(userList)){
            return Lists.newArrayList();
        }
        List<Long> finalSubUserIds=Lists.transform(userList, new Function<User, Long>() {
            @Override
            public Long apply(@Nullable User user) {
                return user.getUserId();
            }
        });
        List<UserInfo> subUserInfoList=userInfoMapper.selectUserInfoList(finalSubUserIds);
        return subUserInfoList;
    }

    @Override
    public UserInfo checkParentAndGetUserInfo(Long parentUserId) throws Exception {
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());
        UserInfo parentUserInfo=userInfoMapper.selectByPrimaryKey(parentUserId);
        if(Objects.isNull(parentUserInfo)){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return parentUserInfo;
    }

    @Override
    public APIResponse<SubAccountTransferHistoryInfoResp> getSubAccountTransferHistoryInfo(APIRequest<SubAccountTransHistoryInfoReq> request) throws Exception {
        SubAccountTransferHistoryInfoResp response = new SubAccountTransferHistoryInfoResp();
        // 确保是母账户
        final SubAccountTransHistoryInfoReq requestBody = request.getBody();
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 校验userId，可以是子账号id或者母账号id
        Long userId = requestBody.getUserId();
        if (userId != null && !userId.equals(parentUserId)) {
            // 确保为子母账户关系
            assertParentSubUserBound(parentUserId, userId);
        }

        // 获取userIds
        GetSubAccountTransferHistoryRequest getSubAccountTransferHistoryRequest = new GetSubAccountTransferHistoryRequest();
        if (userId != null){
            getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(userId));
        }else{
            getSubAccountTransferHistoryRequest.setParentUserId(parentUserId);
            getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(parentUserId));
            if (!subHistorySwitch) {
                APIRequest<UserIdReq> userIdReqAPI = new APIRequest<>();
                UserIdReq userIdReq = new UserIdReq();
                userIdReq.setUserId(parentUserId);
                userIdReqAPI.setBody(userIdReq);
                APIResponse<SubUserTypeResponse> subUserTypeResponseAPIResponse = this.checkRelationByUserId(userIdReqAPI);
                log.info("selectTransferLogByUserId response userId:{}, resp:{}", userIdReqAPI, subUserTypeResponseAPIResponse);
                if (!isOk(subUserTypeResponseAPIResponse)) {
                    return APIResponse.getErrorJsonResult("sub.user.illegal.relation");
                }
                if (subUserTypeResponseAPIResponse.getData() == null || CollectionUtils.isEmpty(subUserTypeResponseAPIResponse.getData().getSubUserIds())) {
                    return APIResponse.getErrorJsonResult("sub.user.is empty");
                }
                List<Long> subUserIds = subUserTypeResponseAPIResponse.getData().getSubUserIds();
                subUserIds.add(parentUserId);
                getSubAccountTransferHistoryRequest.setUserIds(subUserIds);
            }
        }
        getSubAccountTransferHistoryRequest.setStartCreateTime(checkAndGetStartTime(requestBody.getStartTime(), requestBody.getEndTime()));
        getSubAccountTransferHistoryRequest.setEndCreateTime(checkAndGetEndTime(requestBody.getEndTime()));
        addPageParam(requestBody.getPage(),requestBody.getLimit(),getSubAccountTransferHistoryRequest);

        // 如果transfers参数不为空，校验transfers
        String transfers = requestBody.getTransfers();
        if (StringUtils.isNotBlank(transfers) && (SubAccountTransferEnum.TO.name().equalsIgnoreCase(transfers) ||
                SubAccountTransferEnum.FROM.name().equalsIgnoreCase(transfers))) {
            SubAccountTransferEnum transferEnum = SubAccountTransferEnum.valueOf(transfers.toUpperCase());
            getSubAccountTransferHistoryRequest.setTransfers(transferEnum);
        }

        GetSubAccountTransferHistoryResponse transferHistoryResponse = tranApiClient.getAccountTransferHistory(getSubAccountTransferHistoryRequest);
        log.info("tranApiClient.getAccountTransferHistory,param:{},result:{}",getSubAccountTransferHistoryRequest,transferHistoryResponse);
        List<AssetSubAccountTrasnferVo> accountTransferHistory = transferHistoryResponse.getAssetSubAccountTrasnferVoList();
        if (CollectionUtils.isEmpty(accountTransferHistory)){
            response.setCount(0L);
            response.setResult(Lists.newArrayList());
            return APIResponse.getOKJsonResult(response);
        }

        // 组装返回参数
        List<SubAccountTransferHistoryInfoVo> result = accountTransferHistory.stream().map(x -> {
            SubAccountTransferHistoryInfoVo historyVo = new SubAccountTransferHistoryInfoVo();
            historyVo.setId(x.getId());
            historyVo.setTransactionId(x.getTransactionId());
            historyVo.setFromUser(x.getSenderUserId());
            historyVo.setFromEmail(x.getSenderEmail());
            historyVo.setToUser(x.getRecipientUserId());
            historyVo.setToEmail(x.getRecipientEmail());
            historyVo.setAsset(x.getAsset());
            historyVo.setAmount(x.getAmount());
            historyVo.setCreateTime(x.getCreateTime());
            historyVo.setCreateTimeStamp(x.getCreateTime().getTime());
            return historyVo;
        }).collect(Collectors.toList());
        response.setResult(result);
        response.setCount(transferHistoryResponse.getTotal());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<ResendSendActiveCodeResponse> resendSubUserRegisterMail(APIRequest<ResendSubUserRegisterMailReq> request) throws Exception {
        final ResendSubUserRegisterMailReq requestBody = request.getBody();
        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 验证邮箱
        String email = requestBody.getSubUserEmail().replaceAll(" ", "+");
        if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
            return APIResponse.getErrorJsonResult("vc.mail.eror");
        }

        // 确保为子母账户关系
        User subUser = this.userMapper.queryByEmail(email);
        if (subUser == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        //资管子账户不可修改
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        Long subUserId = subUser.getUserId();
        assertParentSubUserBound(parentUserId, subUserId);

        // 10秒钟内不允许再次发送
        long times = RedisCacheUtils.get(subUserId.toString(), Long.class, REDIS_SUB_USER_REGISTER_MAIL_KEY, 0L);
        if(times < 1) {
            RedisCacheUtils.increment(subUserId.toString(), REDIS_SUB_USER_REGISTER_MAIL_KEY, 1L, 10L, TimeUnit.SECONDS);
        } else {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        APIRequest<ResendSendActiveCodeRequest> originRequest = new APIRequest<ResendSendActiveCodeRequest>();
        originRequest.setLanguage(request.getLanguage());
        originRequest.setTerminal(request.getTerminal());
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        ResendSendActiveCodeRequest sendActiveCodeRequest = new ResendSendActiveCodeRequest();
        sendActiveCodeRequest.setEmail(email);
        sendActiveCodeRequest.setCustomEmailLink(requestBody.getCustomEmailLink());

        APIResponse<ResendSendActiveCodeResponse> apiResponse = userService.resendSendActiveCode(APIRequest.instance(originRequest,sendActiveCodeRequest));
        if (apiResponse != null && apiResponse.getStatus() != APIResponse.Status.OK) {
            log.error("resendSubUserRegisterMail resendSendActiveCode error{}", LogMaskUtils.maskJsonString(JSONObject.toJSONString(apiResponse)));
        }
        return apiResponse;
    }

    @Override
    public APIResponse<SubUserAssetBtcResponse> subUserAssetBtcList(APIRequest<SubUserAssetBtcRequest> request) throws Exception {
        SubUserAssetBtcResponse response = new SubUserAssetBtcResponse();
        // 确保是母账户
        final SubUserAssetBtcRequest requestBody = request.getBody();
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        if (StringUtils.isNotBlank(requestBody.getEmail())) {
            // 验证邮箱
            String email = requestBody.getEmail().replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                return APIResponse.getErrorJsonResult("vc.mail.eror");
            }
        }

        // 分页参数
        Integer page = requestBody.getPage();
        Integer limit = requestBody.getLimit();
        if (page == null || page < 1) {
            page = 1;
        }
        if (null == limit) {
            limit = DEFAULT_LIMIT;
        }
        if (limit.compareTo(MAX_LIMIT) == 1) {
            limit = MAX_LIMIT;
        }

        // 查询子账号列表
        QuerySubUserRequest req = new QuerySubUserRequest();
        req.setParentUserId(parentUserId);
        if (StringUtils.isNotBlank(requestBody.getEmail())) {
            req.setEmail(requestBody.getEmail());
        }
        if (StringUtils.isNotBlank(requestBody.getIsSubUserEnabled()) && StringUtils.isNumeric(requestBody.getIsSubUserEnabled())) {
            req.setIsSubUserEnabled(Integer.valueOf(requestBody.getIsSubUserEnabled()));
        }
        req.setLimit(limit);
        req.setOffset((page - 1) * limit);
        APIResponse<SubUserInfoResp> subUserInfoResponse = applicationContext.getBean(this.getClass()).selectSubUserInfo(APIRequest.instance(req));
        if (subUserInfoResponse != null && subUserInfoResponse.getStatus() != APIResponse.Status.OK) {
            log.error("subUserAssetInfoList selectSubUserInfo error{}", JSONObject.toJSONString(subUserInfoResponse));
        }

        List<SubUserInfoVo> subUserList = subUserInfoResponse.getData().getResult();
        if (CollectionUtils.isEmpty(subUserList)) {
            response.setCount(0L);
            response.setResult(Lists.newArrayList());
            return APIResponse.getOKJsonResult(response);
        }

        // 查询子账号BTC资产总值
        List<SubUserAssetBtcVo> result = new ArrayList<>();
        List<User> users = userMapper.selectByEmails(subUserList.stream().map(SubUserInfoVo::getEmail).collect(Collectors.toList()));
        Map<Long, Long> userStatusList = users.stream().collect(Collectors.toMap(User::getUserId, User::getStatus, (k1, k2) -> k1));
        for (SubUserInfoVo subUserInfo : subUserList) {
            SubUserAssetBtcVo subUserAssetBtc = new SubUserAssetBtcVo();
            subUserAssetBtc.setUserId(subUserInfo.getSubUserId());
            subUserAssetBtc.setEmail(subUserInfo.getEmail());
            subUserAssetBtc.setIsSubUserEnabled(subUserInfo.getIsSubUserEnabled());
            if (userStatusList != null && userStatusList.containsKey(subUserInfo.getSubUserId())){
                Long status = userStatusList.get(subUserInfo.getSubUserId());
                subUserAssetBtc.setIsAssetSubUser(BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUBUSER));
                subUserAssetBtc.setIsAssetSubUserEnabled(BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUB_USER_ENABLED));
            }
            BigDecimal totalAsset = new BigDecimal(0);
            try {
                UserAssetTransferBtcResponse assetResponse = userAssetApiClient.getUserAssetTransferBtc(subUserInfo.getSubUserId());
                if (assetResponse != null) {
                     totalAsset = assetResponse.getTotalTransferBtc();
                }
            } catch (Exception e) {
                log.error("getUserAssetTransferBtc error, subUserId={} error={}", subUserInfo.getSubUserId(), e.getMessage());
            }
            subUserAssetBtc.setTotalAsset(totalAsset);
            result.add(subUserAssetBtc);
        }
        response.setResult(result);
        response.setCount(subUserInfoResponse.getData().getCount());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<BigDecimal> parentUserAssetBtc(APIRequest<ParentUserIdReq> request) throws Exception {
        // 确保是母账户
        Long parentUserId = request.getBody().getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        BigDecimal assetBtc = new BigDecimal(0);
        try {
            UserAssetTransferBtcResponse assetResponse = userAssetApiClient.getUserAssetTransferBtc(parentUserId);
            if (assetResponse != null) {
                assetBtc = assetResponse.getTotalTransferBtc();
            }
        } catch (Exception e) {
            log.error("getUserAssetTransferBtc error, parentUserId={} error={}", parentUserId, e.getMessage());
        }

        return APIResponse.getOKJsonResult(assetBtc);
    }

    @Override
    public APIResponse<BigDecimal> allSubUserAssetBtc(APIRequest<ParentUserIdReq> request) throws Exception {
        // 确保是母账户
        Long parentUserId = request.getBody().getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 查询所有子账号
        APIRequest<UserIdReq> userIdReqAPI = new APIRequest<>();
        UserIdReq userIdReq = new UserIdReq();
        userIdReq.setUserId(request.getBody().getParentUserId());
        userIdReqAPI.setBody(userIdReq);
        APIResponse<SubUserTypeResponse> subUserResponse = this.checkRelationByUserId(userIdReqAPI);
        log.info("checkRelationByUserId response userId:{}, resp:{}", userIdReqAPI, subUserResponse);
        if (!isOk(subUserResponse)){
            return APIResponse.getErrorJsonResult("sub.user.illegal.relation");
        }
        if (subUserResponse.getData() == null || CollectionUtils.isEmpty(subUserResponse.getData().getSubUserIds())){
            return APIResponse.getErrorJsonResult("sub.user.is empty");
        }
        List<Long> subUserIds = subUserResponse.getData().getSubUserIds();

        // 遍历查询子账号btc资产
        StopWatch sw = new StopWatch();
        sw.start();
        BigDecimal totalBtc = new BigDecimal(0);
        for (Long subUserId : subUserIds) {
            try {
                UserAssetTransferBtcResponse assetResponse = userAssetApiClient.getUserAssetTransferBtc(subUserId);
                if (assetResponse != null) {
                    totalBtc = totalBtc.add(assetResponse.getTotalTransferBtc());
                }
            } catch (Exception e) {
                log.error("getUserAssetTransferBtc error, subUserId={} error={}", subUserId, e.getMessage());
            }
        }
        sw.stop();
        log.info("allSubUserAssetBtc parentUserId={} has {} subUsers, sum assetBtc cost {} ms", parentUserId, subUserIds.size(), sw.getTime());
        return APIResponse.getOKJsonResult(totalBtc);
    }

    @Override
    public APIResponse<List<SubUserCurrencyBalanceResp>> subUserCurrencyBalance(APIRequest<SubUserCurrencyBalanceReq> request) throws Exception {
        // 确保是母账户
        final SubUserCurrencyBalanceReq requestBody = request.getBody();
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 验证参数
        if (StringUtils.isBlank(requestBody.getCoin())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }

        List<Long> subUserIds = Lists.newArrayList();
        final Map<Long, String> idEmailMap = new HashMap<>();
        if (StringUtils.isNotBlank(requestBody.getEmail())) {
            // 验证邮箱
            String email = requestBody.getEmail().replaceAll(" ", "+");
            if (!COMPATIBLE_EMAIL_PATTERN.matcher(email).matches()) {
                return APIResponse.getErrorJsonResult("vc.mail.eror");
            }

            APIRequest<GetUserRequest> getUserAPIRequest = new APIRequest<>();
            GetUserRequest getUserRequest = new GetUserRequest();
            getUserRequest.setEmail(email);
            getUserAPIRequest.setBody(getUserRequest);
            APIResponse<Long> userResponse = userService.getUserIdByEmail(getUserAPIRequest);
            if (!isOk(userResponse)){
                return APIResponse.getErrorJsonResult("user.is empty");
            }
            Long userId = userResponse.getData();
            subUserIds.add(userId);
            idEmailMap.put(userId, email);
        } else {
            // 查询所有子账号
            APIRequest<UserIdReq> userIdReqAPI = new APIRequest<>();
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(requestBody.getParentUserId());
            userIdReqAPI.setBody(userIdReq);
            APIResponse<SubUserTypeResponse> subUserResponse = this.checkRelationByUserId(userIdReqAPI);
            log.info("checkRelationByUserId response userId:{}, resp:{}", userIdReqAPI, subUserResponse);
            if (!isOk(subUserResponse)){
                return APIResponse.getErrorJsonResult("sub.user.illegal.relation");
            }
            if (subUserResponse.getData() == null || CollectionUtils.isEmpty(subUserResponse.getData().getSubUserIds())){
                return APIResponse.getErrorJsonResult("sub.user.is empty");
            }
            subUserIds = subUserResponse.getData().getSubUserIds();
            subUserIds.add(parentUserId);
            idEmailMap.putAll(subUserResponse.getData().getSubUserIdEmails().stream().collect(Collectors.toMap(SubUserEmailVo::getUserId, SubUserEmailVo::getEmail)));
            idEmailMap.put(parentUserId, parentUser.getEmail());
        }

        // 查询子账号资产
        List<SelectUserAssetResponse> userAssetResponses = userAssetApiClient.getUserAssetByUserIdsCode(subUserIds, requestBody.getCoin());
        Map<Long, BigDecimal> idFreeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userAssetResponses)) {
            userAssetResponses.stream().forEach(x -> {
                idFreeMap.put(x.getUserId(), x.getFree());
            });
        }

        List<SubUserCurrencyBalanceResp> result = new ArrayList<>();
        for (Long userId : subUserIds) {
            SubUserCurrencyBalanceResp resp = new SubUserCurrencyBalanceResp();
            resp.setUserId(userId);
            String email = idEmailMap.get(userId);
            resp.setEmail(email);
            resp.setAsset(requestBody.getCoin());

            BigDecimal free = idFreeMap.get(userId);
            resp.setFree(free != null ? free : new BigDecimal(0));
            result.add(resp);
        }

        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<SubAccountTransferHistoryInfoVo> getSubUserTransferByTranId(APIRequest<SubUserTransferByTranIdReq> request) throws Exception {
        // 确保是母账户
        final SubUserTransferByTranIdReq requestBody = request.getBody();
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        // 验证参数
        if (requestBody.getTranId() == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        AssetSubAccountTrasnferVo transferInfoVo = tranApiClient.getTransferByTranId(requestBody.getTranId());
        if (transferInfoVo == null) {
            return APIResponse.getErrorJsonResult("sub.transfer.is empty");
        }

        // 验证当前用户的数据权限。母账号操作的划转，可以是母->子，子->母，子->子。
        List<Long> subUserIds = getSubUserIds(parentUserId);
        Long senderUserId = Long.valueOf(transferInfoVo.getSenderUserId());
        Long recipientUserId = Long.valueOf(transferInfoVo.getRecipientUserId());

        // sender、recipient，要么=parentUserId，要么属于subUserIds
        if ((parentUserId.compareTo(senderUserId) == 0 || subUserIds.contains(senderUserId)) && (parentUserId.compareTo(recipientUserId) == 0 || subUserIds.contains(recipientUserId))) {
            SubAccountTransferHistoryInfoVo historyVo = new SubAccountTransferHistoryInfoVo();
            BeanUtils.copyProperties(transferInfoVo, historyVo);
            historyVo.setFromEmail(transferInfoVo.getSenderEmail());
            historyVo.setFromUser(transferInfoVo.getSenderUserId());
            historyVo.setToEmail(transferInfoVo.getRecipientEmail());
            historyVo.setToUser(transferInfoVo.getRecipientUserId());
            historyVo.setCreateTimeStamp(transferInfoVo.getCreateTime().getTime());
            return APIResponse.getOKJsonResult(historyVo);
        } else {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
    }

    @Override
    public APIResponse<Integer> updateSubUserRemark(APIRequest<UpdateSubUserRemarkRequest> request) throws Exception {
        final UpdateSubUserRemarkRequest requestBody = request.getBody();
        Long parentUserId = requestBody.getParentUserId();
        String subUserEmail=requestBody.getSubUserEmail();
        // 验证子母账号关系
        User subUser=validateParentSubUserBoundAndGetSubUser(parentUserId,subUserEmail);
        SubUserBinding subUserBinding=new SubUserBinding();
        subUserBinding.setParentUserId(parentUserId);
        subUserBinding.setSubUserId(subUser.getUserId());
        subUserBinding.setRemark(requestBody.getRemark());
        Integer result=subUserBindingMapper.updateRemarkByParentIdAndSubUserId(subUserBinding);
        log.info("updateSubUserRemark parentId={},subUserId={},result={}",parentUserId,subUser.getUserId(),result);
        if(result.intValue()!=1){
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        return APIResponse.getOKJsonResult(result);
    }

    private List<Long> getSubUserIds(Long parentUserId) throws Exception {
        // 查询所有子账号
        APIRequest<UserIdReq> userIdReqAPI = new APIRequest<>();
        UserIdReq userIdReq = new UserIdReq();
        userIdReq.setUserId(parentUserId);
        userIdReqAPI.setBody(userIdReq);
        APIResponse<SubUserTypeResponse> subUserResponse = this.checkRelationByUserId(userIdReqAPI);
        log.info("checkRelationByUserId response userId:{}, resp:{}", userIdReqAPI, subUserResponse);
        if (!isOk(subUserResponse)){
            throw new BusinessException("checkRelationByUserId error");
        }
        if (subUserResponse.getData() == null || CollectionUtils.isEmpty(subUserResponse.getData().getSubUserIds())){
            throw new BusinessException(GeneralCode.SYS_NOT_EXIST);
        }
        List<Long> subUserIds = subUserResponse.getData().getSubUserIds();
        return subUserIds;
    }

    @Override
    public APIResponse<SubAccountTransferVersionForSubToSubResponse> subAccountTransferVersionForSubToSub(APIRequest<SubAccountTransferVersionForSubToSubRequest> request) throws Exception {
        SubAccountTransferVersionForSubToSubRequest requestBody=request.getBody();
        //1 验证子账号
        User subUser = checkAndGetUserById(requestBody.getUserId());
        isEnabledSubUser(subUser);
        SubUserBinding subUserBinding=assertIsSubUser(subUser);
        Long parentUserId=subUserBinding.getParentUserId();
        User toUser=checkAndGetUserByEmail(requestBody.getToEmail());
        if(parentUserId.longValue()==toUser.getUserId()){
            throw new BusinessException(AccountErrorCode.CANNOT_TRASFER_TO_PARENT_ACCOUNT);
        }

        //资管子账户不可修改
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER) ||
                com.binance.account.util.BitUtils.isEnable(toUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //2 组装请求参数调用划转接口
        APIRequest<SubAccountTransferRequest> originRequest = new APIRequest<SubAccountTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        SubAccountTransferRequest subAccountTransferRequest = new SubAccountTransferRequest();
        subAccountTransferRequest.setParentUserId(parentUserId);
        subAccountTransferRequest.setSenderUserId(subUser.getUserId());
        subAccountTransferRequest.setRecipientUserId(toUser.getUserId());
        subAccountTransferRequest.setAsset(requestBody.getAsset());
        subAccountTransferRequest.setAmount(requestBody.getAmount());
        log.info("call subAccountTransferVersionForSubToSub:"+ LogMaskUtils.maskJsonString(JsonUtils.toJsonNotNullKey(subAccountTransferRequest)));
        APIResponse<SubAccountTransferResponse> apiResponse=subAccountTransfer(APIRequest.instance(originRequest, subAccountTransferRequest));
        log.info("resp subAccountTransferVersionForSubToSub:"+ LogMaskUtils.maskJsonString(JsonUtils.toJsonNotNullKey(apiResponse)));
        SubAccountTransferVersionForSubToSubResponse subAccountTransferVersionForSubToSubResponse=new SubAccountTransferVersionForSubToSubResponse();
        subAccountTransferVersionForSubToSubResponse.setTransactionId(apiResponse.getData().getTransactionId());
        return APIResponse.getOKJsonResult(subAccountTransferVersionForSubToSubResponse);
    }

    @Override
    public APIResponse<SubAccountTransferVersionForSubToMasterResponse> subAccountTransferVersionForSubToMaster(APIRequest<SubAccountTransferVersionForSubToMasterRequest> request) throws Exception {
        SubAccountTransferVersionForSubToMasterRequest requestBody=request.getBody();
        //1 验证子账号
        User subUser = checkAndGetUserById(requestBody.getUserId());
        isEnabledSubUser(subUser);
        SubUserBinding subUserBinding=assertIsSubUser(subUser);
        Long parentUserId=subUserBinding.getParentUserId();
        //资管子账户不可修改
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //2 组装请求参数调用划转接口
        APIRequest<SubAccountTransferRequest> originRequest = new APIRequest<SubAccountTransferRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        SubAccountTransferRequest subAccountTransferRequest = new SubAccountTransferRequest();
        subAccountTransferRequest.setParentUserId(parentUserId);
        subAccountTransferRequest.setSenderUserId(subUser.getUserId());
        subAccountTransferRequest.setRecipientUserId(parentUserId);
        subAccountTransferRequest.setAsset(requestBody.getAsset());
        subAccountTransferRequest.setAmount(requestBody.getAmount());
        log.info("call subAccountTransferVersionForSubToMaster:"+ LogMaskUtils.maskJsonString(JsonUtils.toJsonNotNullKey(subAccountTransferRequest)));
        APIResponse<SubAccountTransferResponse> apiResponse=subAccountTransfer(APIRequest.instance(originRequest, subAccountTransferRequest));
        log.info("resp subAccountTransferVersionForSubToMaster:"+ LogMaskUtils.maskJsonString(JsonUtils.toJsonNotNullKey(apiResponse)));
        SubAccountTransferVersionForSubToMasterResponse subAccountTransferVersionForSubToMasterResponse=new SubAccountTransferVersionForSubToMasterResponse();
        subAccountTransferVersionForSubToMasterResponse.setTransactionId(apiResponse.getData().getTransactionId());
        return APIResponse.getOKJsonResult(subAccountTransferVersionForSubToMasterResponse);
    }

    @Override
    public APIResponse<List<SubAccountTranHisResForSapiVersion>> subUserHistoryVersionForSapi(APIRequest<SubUserHistoryVersionForSapiRequest> request) throws Exception {
        SubUserHistoryVersionForSapiRequest requestBody=request.getBody();
        //1 验证子账号
        User subUser = checkAndGetUserById(requestBody.getUserId());
        isEnabledSubUser(subUser);
        SubUserBinding subUserBinding=assertIsSubUser(subUser);
        Long parentUserId=subUserBinding.getParentUserId();
        GetSubAccountTransferHistoryRequest getSubAccountTransferHistoryRequest = new GetSubAccountTransferHistoryRequest();
        getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(requestBody.getUserId()));
        if(null!=requestBody.getEndTime()){
            getSubAccountTransferHistoryRequest.setEndCreateTime(new Date(requestBody.getEndTime()));
        }
        if(null!=requestBody.getStartTime()){
            getSubAccountTransferHistoryRequest.setStartCreateTime(new Date(requestBody.getStartTime()));
        }
        if(null!=requestBody.getLimit()){
            getSubAccountTransferHistoryRequest.setOffset(0);// start
            getSubAccountTransferHistoryRequest.setLimit(requestBody.getLimit());
        }
        if (StringUtils.isNotBlank(requestBody.getAsset())) {
            getSubAccountTransferHistoryRequest.setAsset(requestBody.getAsset());
        }

        if(null!=requestBody.getSubAccountTransferTypeVersionForSapi()){
            int transderTypeCode=requestBody.getSubAccountTransferTypeVersionForSapi().getCode();
            getSubAccountTransferHistoryRequest.setTransfers(transderTypeCode==1? SubAccountTransferEnum.FROM:SubAccountTransferEnum.TO);// start
        }
        log.info("call subUserHistoryVersionForSapi:"+ LogMaskUtils.maskJsonString(JsonUtils.toJsonNotNullKey(getSubAccountTransferHistoryRequest)));
        List<AssetSubAccountTrasnferVo> accountTransferHistoryList = tranApiClient.getAccountTransferHistory(getSubAccountTransferHistoryRequest).getAssetSubAccountTrasnferVoList();
        if (CollectionUtils.isEmpty(accountTransferHistoryList)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<SubAccountTranHisResForSapiVersion> result = new ArrayList<>(accountTransferHistoryList.size());
        for (AssetSubAccountTrasnferVo vo : accountTransferHistoryList) {
            SubAccountTranHisResForSapiVersion res = new SubAccountTranHisResForSapiVersion();
            Long senderUserId=Long.valueOf(vo.getSenderUserId());
            Long recipientUserId=Long.valueOf(vo.getRecipientUserId());
            String countparty="subAccount";
            String countpartyEmail="";
            String countpartyUserId=null;
            int transtype=0;
            if(senderUserId.longValue()==requestBody.getUserId().longValue()){
               /// email
                countpartyEmail=vo.getRecipientEmail();
                countpartyUserId=vo.getRecipientUserId();
                transtype=SubAccountTransferTypeVersionForSapi.TRANSFER_OUT.getCode();
            }else{
                countpartyEmail=vo.getSenderEmail();
                countpartyUserId=vo.getSenderUserId();
                transtype=SubAccountTransferTypeVersionForSapi.TRANSFER_IN.getCode();
            }
            if(parentUserId.toString().equals(countpartyUserId)){
                countparty="master";
            }
            res.setCounterParty(countparty);
            res.setEmail(countpartyEmail);
            res.setType(transtype);
            res.setAsset(vo.getAsset());
            res.setQty(vo.getAmount().toPlainString());
            res.setTime(vo.getCreateTime().getTime());
            result.add(res);
        }
        return APIResponse.getOKJsonResult(result);
    }

    private void addPageParam(Integer page, Integer limit, GetSubAccountTransferHistoryRequest request) {
        if (null == page) {
            page = 1;
        }
        if (null == limit) {
            limit = DEFAULT_LIMIT;
        }
        if (limit.compareTo(MAX_LIMIT) == 1) {
            limit = MAX_LIMIT;
        }
        request.setOffset((page - 1) * limit);
        request.setLimit(limit);
    }
    private Date checkAndGetStartTime(Long startTime, Long endTime) {
        Date to = checkAndGetEndTime(endTime);
        Date from = null != startTime ? new Date(startTime) : org.apache.commons.lang3.time.DateUtils.addDays(to, DEFAULT_QUERY_DAYS);
        // 查询大于3个月的数据，使用默认查询100天内数据
        if (startTime != null && startTime < org.apache.commons.lang3.time.DateUtils.addDays(to, DEFAULT_QUERY_DAYS).getTime()) {
            from = org.apache.commons.lang3.time.DateUtils.addDays(to, DEFAULT_QUERY_DAYS);
        }
        return from;
    }

    private Date checkAndGetEndTime(Long endTime) {
        Date to = null != endTime ? new Date(endTime) : org.apache.commons.lang3.time.DateUtils.addDays(new Date(), 1);
        // 防止传入一个很久的结束日期
        if (to.getTime() < LEGAL_FIRST_DATE) {
            to = org.apache.commons.lang3.time.DateUtils.addDays(new Date(), 1);
        }
        return to;
    }

    private List<SubAccountResp> getResFromData(APIResponse<SubUserInfoResp> resp) {
        SubUserInfoResp subUserInfoResp = resp.getData();
        List<SubUserInfoVo> subUserInfoVos = subUserInfoResp.getResult();
        List<SubAccountResp> subAccountResps = new ArrayList<>();
        for (SubUserInfoVo subUserInfoVo : subUserInfoVos) {
            SubAccountResp subAccountResp = new SubAccountResp();
            subAccountResp.setEmail(subUserInfoVo.getEmail());
            subAccountResp.setStatus(subUserInfoVo.getIsSubUserEnabled() ? SubAccountResp.SubAccountStatus.enabled : SubAccountResp.SubAccountStatus.disabled);
            subAccountResp.setActivated(subUserInfoVo.getIsUserActive());
            subAccountResp.setMobile(subUserInfoVo.getMobile());
            subAccountResp.setgAuth(subUserInfoVo.getIsUserGoogle());
            subAccountResp.setCreateTime(subUserInfoVo.getInsertTime().getTime());
            subAccountResps.add(subAccountResp);
        }
        return subAccountResps;
    }


    private APIRequest<UserIdRequest> getUserIdRequestAPIRequest(Long parendUserId) {
        APIRequest<UserIdRequest> request = new APIRequest<>();
        UserIdRequest userIdRequest = new UserIdRequest();
        userIdRequest.setUserId(parendUserId);
        request.setBody(userIdRequest);
        return request;
    }


    /**
     * 重置谷歌验证
     *
     * @param subUserId:子账户id
     * @param subUserStatus:子账户状态
     */
    private void resetGoogleVerify(Long subUserId, Long subUserStatus) {
        // 更新user状态
        User user = new User();
        user.setStatus(BitUtils.disable(subUserStatus, Constant.USER_GOOGLE));
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        user.setEmail(userIndex.getEmail());
        this.userMapper.updateByEmailSelective(user);

        // 解绑谷歌验证
        UserSecurity security = new UserSecurity();
        security.setUserId(subUserId);
        security.setAuthKey(null);
        this.userSecurityMapper.updateAuthKeyByEmail(security);
    }

    /**
     * 重置手机验证
     *
     * @param subUserId：子账户id
     * @param subUserStatus：子账户状态
     */
    private void unbindMobile(Long subUserId, Long subUserStatus) {
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(subUserId);
        if (StringUtils.isBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }

        // 删除索引
        userMobileIndexMapper.deleteByPrimaryKey(userSecurity.getMobile(), userSecurity.getMobileCode());

        // 更新user状态
        User user = new User();
        user.setStatus(BitUtils.disable(subUserStatus, Constant.USER_MOBILE));
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(subUserId);
        user.setEmail(userIndex.getEmail());
        this.userMapper.updateByEmailSelective(user);

        // 更新手机号
        UserSecurity security = new UserSecurity();
        security.setUserId(subUserId);
        security.setMobile(null);
        security.setMobileCode(null);
        this.userSecurityMapper.updateMobileByUserId(security);
    }

    /**
     * 添加安全日志
     *
     * @param securityLog
     * @param parentUserId:母账户userId
     * @param wayName:方法名
     */
    private void addSecurityLog(UserSecurityLog securityLog, Long parentUserId, String wayName) {
        try {
            String ip = WebUtils.getRequestIp();
            final APIRequestHeader header = WebUtils.getAPIRequestHeader();

            securityLog.setUserId(parentUserId);
            securityLog.setIp(ip);

            if (header != null) {
                securityLog.setClientType(header.getTerminal().getCode());
            } else {
                securityLog.setClientType(TerminalEnum.OTHER.getCode());
            }
            securityLog.setIpLocation(IP2LocationUtils.getCountryCity(ip));
            securityLog.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error("add {} securityLog failed, userId:{}, exception:{}", wayName, parentUserId, e);
        }
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     */
    private int updatePwd(final Long userId, final String password) throws NoSuchAlgorithmException {
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final String salt = StringUtils.uuid();
        return iUserSecurity.updateUserPassword(userIndex.getEmail(), salt, PasswordUtils.encode(password, salt,
                RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true)), false, UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX);
    }

    // 弃用，使用checkRelationByUserId替换
    @Deprecated
    @Monitored
    @Override
    public APIResponse<SubUserEmailVoResp> selectSubUserEmailList(APIRequest<ParentUserIdReq> request) {
        ParentUserIdReq requestBody = request.getBody();

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);

        SubUserEmailVoResp resp = new SubUserEmailVoResp();
        List<SubUserEmailVo> respVo = new ArrayList<>();
        if (subUserIds == null || subUserIds.size() <= 0) {
            log.info("该母账户下没有有效的子账户:{}", parentUserId);
            return APIResponse.getOKJsonResult(resp);
        }
        // 批量查询用户信息
        List<User> userList = userMapper.selectByUserIds(subUserIds);
        if (userList != null && userList.size() > 0) {
            userList.forEach(user -> {
                SubUserEmailVo vo = new SubUserEmailVo();
                BeanUtils.copyProperties(user, vo);
                respVo.add(vo);
            });
        }
        resp.setResult(respVo);
        return APIResponse.getOKJsonResult(resp);
    }

    @Monitored
    @Override
    public APIResponse<GetUserSecurityLogResponse> loginHistoryList(APIRequest<SubUserSecurityLogReq> request) {
        SubUserSecurityLogReq requestBody = request.getBody();

        // 确保是母账户
        Long parentUserId = requestBody.getParentUserId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertSubUserFunctionEnabled(parentUser.getStatus());

        Long subUserId = requestBody.getSubUserId();
        String operateType = requestBody.getOperateType();
        Date startOperateTime = requestBody.getStartOperateTime();
        Date endOperateTime = requestBody.getEndOperateTime();
        int offset = requestBody.getOffset();
        int limit = requestBody.getLimit();

        GetUserSecurityLogResponse resp = new GetUserSecurityLogResponse();
        // 查该子账户登录历史列表
        if (subUserId != null) {
            // 验证是否母子关系
            assertParentSubUserBoundNotCheckParent(parentUserId, subUserId);
            List<Long> userIdList = new ArrayList<>();
            userIdList.add(subUserId);
            return getLoginHistoryList(userIdList, operateType, startOperateTime, endOperateTime, resp, offset, limit);
        }

        // 根据parentId查subUserId集合 ,查该母账户下的所有子账户登录历史
        List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);

        if (subUserIds == null || subUserIds.size() <= 0) {
            log.info("该母账户下没有有效的子账户:{}", parentUserId);
            resp.setCount(0L);
            return APIResponse.getOKJsonResult(resp);
        }
        return getLoginHistoryList(subUserIds, operateType, startOperateTime, endOperateTime, resp, offset, limit);
    }

    @Override
//    @UserPermissionValidate(userId = "#request.body.senderUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_SUB_TRANSFER)
    public APIResponse<SubAccountTransferResponse> subAccountTransfer(APIRequest<SubAccountTransferRequest> request) throws Exception{
        SubAccountTransferRequest requestBody = request.getBody();
        requestBody.setAmount(requestBody.getAmount().setScale(8,BigDecimal.ROUND_DOWN));
        log.info("subAccountTransfer.formatRequest={}",JsonUtils.toJsonNotNullKey(requestBody));
        Long parentUserId=requestBody.getParentUserId();//母账号userId
        Long senderUserId=requestBody.getSenderUserId();//转出方的邮箱
        Long recipientUserId=requestBody.getRecipientUserId();//转入方的邮箱
        User senderUser = checkAndGetUserById(senderUserId);
        User recipientUser =checkAndGetUserById(recipientUserId);
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(senderUser.getStatus()) || checkAssetSubUser(recipientUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        if (com.binance.account.util.BitUtils.isEnable(senderUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER)||com.binance.account.util.BitUtils.isEnable(recipientUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        String asset=requestBody.getAsset();//资产名字
        BigDecimal amount=requestBody.getAmount();//划转数量
        //1 判断是否是母账户
        assertIsParentUser(parentUserId);
        //2 转出方和转入方的userid
        validateSenderAndRecipientUserList(senderUser.getEmail(),recipientUser.getEmail());
        //3 验证转账参数之间关系的合法性
        validateSubAccountTransfer(parentUserId,senderUserId,recipientUserId,asset,amount,false);
        //4 开始正式进行转账操作
        Long transactionId=internalTransfer(parentUserId, senderUserId,senderUser.getEmail(),recipientUserId,recipientUser.getEmail(),asset,amount, null);
        SubAccountTransferResponse resp=new SubAccountTransferResponse();
        resp.setTransactionId(transactionId);
        return APIResponse.getOKJsonResult(resp);

    }

    @Override
    public APIResponse<Boolean> subAccountFutureAssetTransfer(APIRequest<SubAccountFutureTransferReq> request)throws Exception{
        log.info("subAccountFutureAssetTransfer.brokerFutureAssetTransfer.request:{}",JsonUtils.toJsonHasNullKey(request));
        SubAccountFutureTransferReq body = request.getBody();
        Long parentUserId = body.getParentUserId();
        String fromEmail = body.getFromEmail();
        String toEmail = body.getToEmail();
        User fromUser = null;
        User toUser = null;
        User parentUser = checkAndGetUserById(parentUserId);
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        if (StringUtils.isBlank(fromEmail)){
            fromUser = checkAndGetUserById(parentUserId);
            toUser = checkAndGetUserByEmail(toEmail);
        }else if (StringUtils.isBlank(toEmail)){
            toUser = checkAndGetUserById(parentUserId);
            fromUser = checkAndGetUserByEmail(fromEmail);
        }else if (fromEmail.equals(toEmail)){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }else {
            fromUser= checkAndGetUserByEmail(fromEmail);
            toUser= checkAndGetUserByEmail(toEmail);
            Map<String,Object> param = new HashMap<>();
            param.put("parentUserId", parentUserId);
            param.put("userIds", Lists.newArrayList(fromUser.getUserId(),toUser.getUserId()));
            List<SubUserBinding> subUserBindings=  subUserBindingMapper.selectByParentUserIdAndSubUserIds(param);
            if(CollectionUtils.isEmpty(subUserBindings) || subUserBindings.size() != 2){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
        }
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(fromUser.getStatus()) || checkAssetSubUser(toUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 确保转出转入方账号已激活
        if (!BitUtils.isEnable(fromUser.getStatus(), Constant.USER_ACTIVE)||!BitUtils.isEnable(toUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED);
        }
        if(!transferExceededFrequencyFutureLimits(parentUserId,SUB_FUTURE_TRANSFER_LIMIT,subAccountFutureTransferLimit)){
            log.info("SubUserFutureBusiness.exceeded frequency limits, parentUserId:{}", parentUserId);
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        UserInfo fromUserInfo = userInfoMapper.selectByPrimaryKey(fromUser.getUserId());
        UserInfo toUserInfo = userInfoMapper.selectByPrimaryKey(toUser.getUserId());
        if (fromUserInfo == null || fromUserInfo.getFutureUserId() == null || toUserInfo == null || toUserInfo.getFutureUserId() == null){
            log.info("broker sub user has not future:{}", parentUserId);
            throw new BusinessException(AccountErrorCode.SUB_ACCOUNT_ACCOUNT_NOT_EXIST);
        }
        //检查用户关系
        if (body.getFuturesType() == 1){
            Long transId = futureTransferApiClient.getTransIdForFutureTransfer(String.valueOf(toUser.getUserId()), AccountConstants.FUTURE_INNER_TRANSFER);
            log.info("subAccountFutureAssetTransfer.getTransIdForFutureTransfer.transId:{}",transId);
            AssetTransferRequest assetTransferRequest = new AssetTransferRequest();
            assetTransferRequest.setAmount(body.getAmount());
            assetTransferRequest.setAsset(body.getAsset());
            assetTransferRequest.setFromUserId(fromUserInfo.getFutureUserId());
            assetTransferRequest.setToUserId(toUserInfo.getFutureUserId());
            assetTransferRequest.setTranId(transId);
            futureTransferApiClient.futureAssetTransfer(assetTransferRequest);
        }else if (body.getFuturesType() == 2){
            Long transId = deliveryFutureAssetTransfer.getDeliveryTransIdForFutureTransfer(String.valueOf(toUser.getUserId()), AccountConstants.DELIVERY_INNER_TRANSFER);
            log.info("subAccountFutureAssetTransfer.getTransIdForFutureTransfer.transId:{}",transId);
            com.binance.delivery.periphery.api.request.core.AssetTransferRequest assetTransferRequest = new com.binance.delivery.periphery.api.request.core.AssetTransferRequest();
            assetTransferRequest.setAmount(body.getAmount());
            assetTransferRequest.setAsset(body.getAsset());
            assetTransferRequest.setFromUserId(fromUserInfo.getFutureUserId());
            assetTransferRequest.setToUserId(toUserInfo.getFutureUserId());
            assetTransferRequest.setTranId(transId);
            deliveryFutureAssetTransfer.deliveryFutureAssetTransfer(assetTransferRequest);
        }
        log.info("subAccountFutureAssetTransfer.brokerFutureAssetTransfer.end");
        return APIResponse.getOKJsonResult(true);
    }

    /**
     * 母账户修改子账户邮箱(出现异常事务需要回滚)
     * */
    @Override
    @SecurityLog(name = "母账户修改子账户邮箱", operateType = AccountConstants.SECURITY_OPERATE_TYPE_MODIFY_SUBUSER,
            userId = "#request.body.parentUserId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<Integer> modifySubAccount(APIRequest<ModifySubAccountRequest> request) throws Exception {
        ModifySubAccountRequest requestBody = request.getBody();
        Long parentUserId=requestBody.getParentUserId();//母账号userId
        Long subAccountUserId=requestBody.getSubAccountUserId();//需要修改的子账户userid
        String modifyEmail=requestBody.getModifyEmail().toLowerCase();//需要修改的邮箱
        AuthTypeEnum authType=requestBody.getAuthType();//认证类型
        String authCode=requestBody.getCode();//2次验证码
        //账号要存在，母账号和子账号
        User parentUser=checkAndGetUserById(parentUserId);
        User originSubUser=checkAndGetUserById(subAccountUserId);
        //资管子账户不可修改邮箱
        if (com.binance.account.util.BitUtils.isEnable(originSubUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //无邮箱子账户不可修改邮箱
        if (BitUtils.isEnable(originSubUser.getStatus(), AccountCommonConstant.USER_IS_NO_EMAIL_SUB_USER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //资管子账户不可修改邮箱
        if(checkAssetSubUser(originSubUser.getStatus())){
            log.info("SubUserBusiness.modifySubAccount:subUserAssetAccount is not allowed, parentUserId:{},subUserId:{}", parentUserId,subAccountUserId);
            throw new BusinessException(GeneralCode.SYS_VALID);
        }
        // 没有激活不允许操作
        if (!BitUtils.isEnable(originSubUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        //单个母账户24小时只能修改20次子账号的邮箱(不管成功失败，反正会被别人撞库，有安全隐患)
        Boolean isExceededLimit=checkIfExceededModifyEmailLimit(parentUserId);
        if(isExceededLimit){
            log.info("SubUserBusiness.modifySubAccount: exceeded frequency limits, parentUserId:{}", parentUserId);
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        //2fa或者手机至少要绑定一样
        assertUser2FaAtLeastOneEnabled(parentUser.getStatus());
        //确保是母账户，并且传进来的subUserid是绑定关系
        assertParentSubUserBound(parentUserId,subAccountUserId);
        userSecurityBusiness.verificationsTwo(parentUserId, authType, authCode, true);
        //验证email合法性
        if (!Pattern.matches(REGEX_EMAIL, modifyEmail)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        //不能用别人用过的邮箱
        User existedUserByEmail = userMapper.queryByEmail(modifyEmail);
        if (null != existedUserByEmail) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }
        //验证逻辑完毕，开始修改邮箱
        int num = userMapper.deleteByEmail(originSubUser.getEmail());
        if (num > 0) {
            //user表使用email做分表条件的所以需要删除
            User newSubUser = new User();
            BeanUtils.copyProperties(originSubUser, newSubUser);
            newSubUser.setEmail(modifyEmail);
            userMapper.insert(newSubUser);
            //userindex和usersecurity是根据userid做分表条件的所以只要update
            UserIndex updateUserIndex = new UserIndex();
            updateUserIndex.setUserId(subAccountUserId);
            updateUserIndex.setEmail(modifyEmail);
            userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setUserId(subAccountUserId);
            userSecurity.setEmail(modifyEmail);
            userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
            log.info("SubUserBusiness.modifySubAccount: success parentUserId:{},subUserId={}, modifyEmail:{} success", parentUserId,subAccountUserId,modifyEmail);
        } else {
            log.error("SubUserBusiness.modifySubAccount:Delete subUser failed, userId:{}, email:{}", originSubUser.getUserId(),
                    originSubUser.getEmail());
        }
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = Maps.newHashMap();
        dataMsg.put(UserConst.USER_ID, subAccountUserId);
        dataMsg.put(UserConst.EMAIL, modifyEmail);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.MODIFY_SUBACCOUNT_EMAIL, dataMsg);
        log.info("iMsgNotification modifySubAccount:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
        return APIResponse.getOKJsonResult(1);
    }






    /**
     * 获取登录历史
     *
     * @param subUserIds
     * @param operateType
     * @param startOperateTime
     * @param endOperateTime
     * @param resp
     * @param offset
     * @param limit
     * @return
     */
    @Monitored
    private APIResponse<GetUserSecurityLogResponse> getLoginHistoryList(List<Long> subUserIds, String operateType,
            Date startOperateTime, Date endOperateTime, GetUserSecurityLogResponse resp, int offset, int limit) {
        // 查询总条数
        Long count = this.userSecurityLogMapper.getSecurityCountByUserIds(subUserIds, operateType, startOperateTime,
                endOperateTime);
        if (count <= 0) {
            resp.setCount(0L);
            return APIResponse.getOKJsonResult(resp);
        }
        List<UserSecurityLog> result = this.userSecurityLogMapper.getSecurityByUserIds(subUserIds, operateType,
                startOperateTime, endOperateTime, offset, limit);


        // email需要单独查询
        Map<Long, String> userIdEmailMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(subUserIds)) {
            final List<UserIndex> userIndexList = this.userIndexMapper.selectByUserIds(subUserIds);
            userIdEmailMap = userIndexList.stream().collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));
        }

        List<UserSecurityLogVo> userSecurityLogVos = new ArrayList<>();
        for (UserSecurityLog userSecurityLog : result) {
            UserSecurityLogVo userSecurityLogVo = new UserSecurityLogVo();
            BeanUtils.copyProperties(userSecurityLog, userSecurityLogVo);

            String email = userIdEmailMap.get(userSecurityLog.getUserId());
            userSecurityLogVo.setEmail(email);
            userSecurityLogVos.add(userSecurityLogVo);
        }
        resp.setCount(count);
        resp.setResult(userSecurityLogVos);
        return APIResponse.getOKJsonResult(resp);
    }


    public static String getErrorMsg(APIResponse resp) {
        if (resp.getStatus() == APIResponse.Status.ERROR) {
            if (resp.getType() == APIResponse.Type.VALID) {
                Map<String, Object> objMap = (Map<String, Object>) resp.getErrorData();
                return Joiner.on("&").join(objMap.values().iterator());
            } else {
                return resp.getErrorData().toString();
            }
        }
        return null;
    }

    public static boolean isOk(APIResponse resp) {
        return resp !=null && Objects.equals(resp.getStatus().name(), Status.OK.name());
    }

    @Override
    public APIResponse<List<SubUserBindingVo>> queryFutureSubUserBinding(APIRequest<IdRequest> request) {
        final Long userId = request.getBody().getUserId();
        assertIsParentUser(userId);
        List<SubUserBinding> subUserBindings = subUserBindingMapper.selectFutureSubUserByParent(userId);
        if (CollectionUtils.isEmpty(subUserBindings)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());    
        }

        List<SubUserBindingVo> subUserBindingVos = subUserBindings.stream().map(x -> {
            SubUserBindingVo subUserBindingVo = new SubUserBindingVo();
            BeanUtils.copyProperties(x, subUserBindingVo);
            return subUserBindingVo;
        }).collect(Collectors.toList());

        return APIResponse.getOKJsonResult(subUserBindingVos);
    }


    public enum Status {
        /**
         * 正常
         */
        OK,
        /**
         * 异常
         */
        ERROR
    }
}
