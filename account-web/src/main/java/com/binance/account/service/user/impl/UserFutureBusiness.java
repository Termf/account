package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.aop.FrontTask;
import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.constants.enums.AccountTypeEnum;
import com.binance.account.constants.enums.UserTypeEnum;
import com.binance.account.data.entity.agent.UserAgentLog;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.futureagent.FutureUserAgent;
import com.binance.account.data.entity.log.UserOperationLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.FutureInvitationLog;
import com.binance.account.data.entity.user.FutureUserDeliveryTradingAccount;
import com.binance.account.data.entity.user.FutureUserTradingAccount;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.agent.UserAgentLogMapper;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.data.mapper.futureagent.FutureUserAgentMapper;
import com.binance.account.data.mapper.user.FutureInvitationLogMapper;
import com.binance.account.data.mapper.user.FutureUserDeliveryTradingAccountMapper;
import com.binance.account.data.mapper.user.FutureUserTradingAccountMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.FrontPushEventType;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.domain.bo.FrontPushEventType;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.integration.futureengine.FutureDeliveryAccountApiClient;
import com.binance.account.integration.futureservice.CommissionApiClient;
import com.binance.account.integration.futureservice.DeliveryCommissionApiClient;
import com.binance.account.integration.futureservice.DeliveryUserSettingApiClient;
import com.binance.account.integration.futureservice.RiskApiClient;
import com.binance.account.integration.futureservice.UserInfoApiClient;
import com.binance.account.integration.margin.MarginAccountApiClient;
import com.binance.account.integration.margin.MarginAdminApiClient;
import com.binance.account.integration.message.MsgApiClient;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.service.subuser.impl.CheckSubUserBusiness;
import com.binance.account.service.user.IUserFuture;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.utils.InboxUtils;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.futures.enums.FutureEmailTypeEnum;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.FastCreateFutureAccountRequest;
import com.binance.account.vo.subuser.FutureAccountSummaryInfoVo;
import com.binance.account.vo.subuser.FutureAssetRiskInfoVo;
import com.binance.account.vo.subuser.FuturePositionRiskVO;
import com.binance.account.vo.subuser.MarginAccountSummaryInfoVo;
import com.binance.account.vo.subuser.MarginProfitVo;
import com.binance.account.vo.subuser.MarginTradeCoeffVo;
import com.binance.account.vo.subuser.MarginUserAssetVo;
import com.binance.account.vo.subuser.enums.MarginPeriodType;
import com.binance.account.vo.subuser.enums.SubAccountSummaryQueryType;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountFutureAccountSummaryResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountResp;
import com.binance.account.vo.subuser.response.QuerySubAccountMarginAccountSummaryResp;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.CheckFutureAgentCodeExistReq;
import com.binance.account.vo.user.request.CheckIfOpenFutureAccountRequest;
import com.binance.account.vo.user.request.FutureUserAgentReq;
import com.binance.account.vo.user.request.GetUserIdByTradingAccountRequest;
import com.binance.account.vo.user.request.IdRequest;
import com.binance.account.vo.user.request.SendFutureCallRequest;
import com.binance.account.vo.user.request.SendFutureClosePositionMsgRequest;
import com.binance.account.vo.user.request.SendFutureFundingRateMsgRequest;
import com.binance.account.vo.user.request.SendFutureMarginCallRequest;
import com.binance.account.vo.user.response.FutureUserAgentResponse;
import com.binance.delivery.memgmt.api.client.domain.apiKey.ApiKeyRule;
import com.binance.delivery.memgmt.api.client.domain.apiKey.ApiKeySyncRequest;
import com.binance.future.api.vo.AccountRiskVO;
import com.binance.future.api.vo.BalanceRiskVO;
import com.binance.future.api.vo.PositionRiskVO;
import com.binance.future.api.vo.UserTierVO;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.margin.api.admin.response.MarginTradeCoeffResponse;
import com.binance.margin.api.bookkeeper.response.AccountInfoResponse;
import com.binance.margin.api.bookkeeper.response.AccountSimpleResponse;
import com.binance.margin.api.bookkeeper.response.AccountSummaryResponse;
import com.binance.margin.api.profit.enums.PeriodType;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.*;
import com.binance.memgmt.api.client.domain.apiKey.ApiKeyResponse;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.push.api.IPushApi;
import com.binance.push.vo.request.SinglePushRequest;
import com.binance.push.vo.request.enums.MsgTypeEnum;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.ThreadContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Log4j2
@Service
@Monitored
public class UserFutureBusiness extends CheckSubUserBusiness implements IUserFuture {

    @Value("${deliveryAccount.createWhenCreateFuture:true}")
    private Boolean createDeliveryWhenCreateFuture;

    @Value("${deliveryAccount.createWhenCreateIfNotExist:true}")
    private Boolean createDeliveryWhenCreateIfNotExist;

    @Value("${fixDeliveryAccountJob.stopFixDelivery:false}")
    private Boolean stopFixDelivery;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserCommonValidateService userCommonValidateService;

    @Autowired
    private FutureAccountApiClient futureAccountApiClient;

    @Autowired
    private FutureDeliveryAccountApiClient futureDeliveryAccountApiClient;

    @Autowired
    private FutureUserTradingAccountMapper futureUserTradingAccountMapper;

    @Autowired
    private FutureUserDeliveryTradingAccountMapper futureUserDeliveryTradingAccountMapper;

    @Autowired
    private CommissionApiClient commissionApiClient;

    @Autowired
    private DeliveryCommissionApiClient deliveryCommissionApiClient;

    @Autowired
    private FutureInvitationLogMapper futureInvitationLogMapper;

    @Autowired
    private UserAgentLogMapper userAgentLogMapper;

    @Autowired
    private KycApiTransferAdapter kycApiTransferAdapter;

    @Autowired
    private MsgApiClient msgApiClient;

    @Autowired
    private RiskApiClient riskApiClient;

    @Autowired
    private MarginAccountApiClient marginAccountApiClient;

    @Autowired
    private FutureUserAgentMapper futureUserAgentMapper;

    @Autowired
    private MarginAdminApiClient marginAdminApiClient;

    @Resource
    private ICountry iCountry;

    @Autowired
    private InboxMessageTextApi inboxMessageTextApi;

    @Resource
    private IPushApi iPushApi;

    @Autowired
    private UserOperationLogMapper userOperationLogMapper;
    
    @Autowired
    private UserInfoApiClient userInfoApiClient;
    @Autowired
    private DeliveryUserSettingApiClient deliveryUserSettingApiClient;


    @Value("${marginCountryBlackList:}")
    private String marginCountryBlackList;

    @Value("${futureIpCountryBlackList:}")
    private String futureIpCountryBlackList;

    @Value("${futureBlackListFlag:false}")
    private Boolean futureBlackListFlag;

    @Value("${futureClosePositionFlag:false}")
    private Boolean futureClosePositionFlag;
    //6-12
    @Value("${rebateStrategyChangeTime:1591946295000}")
    private Long rebateStrategyChangeTime;




    private static final ExecutorService executorServiceForPositionMsg = Executors.newFixedThreadPool(2);

    private static final ExecutorService executorServiceForMarginCall = Executors.newFixedThreadPool(2);

    private static final ExecutorService executorServiceForFutureCall = Executors.newFixedThreadPool(2);

    private static final String ACCOUNT_SEND_POSITION_MSG = "account:sendposition:msg:user";
    private static final String FUTURE_USER_AGEMTCODE_REGEX = "(?=.*[a-zA-Z])[a-zA-Z0-9]{3,16}";


    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.OPEN_ACCOUNT_COMPLETE_ROUTING,userId = "#request.body.userId",eventType = FrontPushEventType.OPEN_ACCOUNT_COMPLETE,accountType = FrontPushEventType.OPEN_ACCOUNT_COMPLETE_FUTURES)
//    @UserPermissionValidate(userId = "#request.body.userId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_FUTURES)
    public APIResponse<CreateFutureUserResponse> createFutureAccount(APIRequest<CreateFutureAccountRequest> request) throws Exception {
        final CreateFutureAccountRequest requestBody = request.getBody();
        log.info("Get creating future account request with uid-{}", requestBody.getUserId());
        //判断用户的kyc国家是否在黑名单
        checkKycCountryBackList(requestBody.getUserId());
        //校验并且获取主账户信息  ,rootuser这里解释为主账户
        Pair<User, UserInfo> rootTuple = checkAndGetUserByIdForFutureVersion(requestBody.getUserId(),requestBody.getParentUserId());
        //获取主账号相关信息
        User rootUser = rootTuple.getLeft();
        UserInfo rootUserInfo = rootTuple.getRight();

        //开始创建future账号相关信息
        String vritualEmail = createVirtualEmailForFuture(rootUser.getEmail(), rootUser.getUserId(),requestBody.getInvitationCode());
        User futureUser = ((UserFutureBusiness) AopContext.currentProxy()).createFutureUser(vritualEmail);

        // 创建future账号Security信息
        ((UserFutureBusiness) AopContext.currentProxy()).createFutureUserSecurity(futureUser.getUserId(), futureUser.getEmail());

        // 创建future账号info信息
        Long refferFutureUserId = getRefferFutureUserId(requestBody, rootUserInfo, rootUser.getInsertTime().getTime());
        UserInfo futureUserInfo = ((UserFutureBusiness) AopContext.currentProxy()).createFutureUserInfo(rootUserInfo, futureUser.getUserId(),refferFutureUserId);

        // 更新root表中的futureuserid （这步操作是幂等的）
        rootUserInfo.setFutureUserId(futureUser.getUserId());
        userInfoMapper.updateByPrimaryKeySelective(rootUserInfo);

        //更新主账户的状态(这步操作也是幂等的),这是用来标明这个账户是否拥有margin账户
        rootUser.setStatus(BitUtils.enable(rootUser.getStatus(), AccountConstants.USER_IS_EXIST_FUTURE_ACCOUNT));
        userMapper.updateByEmailSelective(rootUser);

        // 创建future交易账户
        Long futureTradingAccount= createFutureTradingAccount(futureUserInfo);


        //同步通知future-service 账户信息
        if(null!=futureTradingAccount){
            commissionApiClient.initUser(rootUser.getUserId(), futureUserInfo.getUserId(), futureTradingAccount,rootUser.getEmail(),refferFutureUserId, selectParentBrokerFutureUserId(requestBody.getUserId()));
        }

        Long futureDeliveryTradingAccount = null;
        if (createDeliveryWhenCreateFuture) {
            // 创建期货交割合约交易账户
            futureDeliveryTradingAccount = createFutureDeliveryTradingAccount(futureUserInfo);
            // 同步通知delivery-service账户信息
            if (null != futureDeliveryTradingAccount) {
                deliveryCommissionApiClient.initUser(rootUser.getUserId(), futureUserInfo.getUserId(), futureDeliveryTradingAccount, rootUser.getEmail(), refferFutureUserId);
            }
        }

        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        if(isSubUser){
            SubUserBinding updateSubUserBinding=new SubUserBinding();
            updateSubUserBinding.setParentUserId(rootUserInfo.getParent());
            updateSubUserBinding.setSubUserId(rootUserInfo.getUserId());
            updateSubUserBinding.setFutureUserId(futureUser.getUserId());
            log.info("updateSelectiveBySubUserIdAndParentUserId={}",JsonUtils.toJsonNotNullKey(updateSubUserBinding));
            subUserBindingMapper.updateSelectiveBySubUserIdAndParentUserId(updateSubUserBinding);
        }

        LanguageEnum languageEnum = WebUtils.getAPIRequestHeader().getLanguage();
        String terminalCode = request.getTerminal()==null?"web":request.getTerminal().getCode();
        String requestIp = WebUtils.getRequestIp();
        AsyncTaskExecutor.execute(() -> {
            try {
                // 加入推荐记录表
                insertToAgentLog(requestBody.getUserId(), futureUserInfo.getUserId(), futureUser.getEmail());
                if (!BitUtils.isTrue(rootUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER)){
                    sendPushAppEmailInbox(rootUser.getUserId(),rootUser.getEmail(),AccountConstants.CREATE_FUTURE_ACCOUNT_APP,AccountConstants.CREATE_FUTURE_ACCOUNT_EMAIL,AccountConstants.CREATE_FUTURE_ACCOUNT_INBOX,terminalCode,languageEnum,requestIp);
                }
            } catch (Exception e) {
                log.error("insertToAgentLog exception", e);
            }
            try {
                userCommonBusiness.insertInfoRootUserIndex(requestBody.getUserId(),futureUser.getUserId(), UserTypeEnum.FUTURE.name());
            }catch (Exception e){
                log.error("insertInfoRootUserIndex future exception", e);
            }
        });

        final Long deliveryTradingAccount = futureDeliveryTradingAccount;
        String trackingChain = com.binance.platform.common.TrackingUtils.getTrace();
        AsyncTaskExecutor.execute(() -> {
            try {
                // 手动获取tracking，并传递
                com.binance.platform.common.TrackingUtils.saveTrace(trackingChain);
                log.info("future subUser syncNewSubAccount start, userId={}", rootUser.getUserId());
                // 如果是子账号，且不是broker，如果其母账号也是期货账号，通知期货初始化子账号的费率
                if (BitUtils.isTrue(rootUser.getStatus(), AccountCommonConstant.USER_IS_SUBUSER) && BitUtils.isFalse(rootUser.getStatus(), AccountCommonConstant.USER_IS_BROKER_SUBUSER)) {
                    Long parentUserId = rootUserInfo.getParent();
                    if (parentUserId == null) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);                       
                    }

                    final UserIndex parentUserIndex = userIndexMapper.selectByPrimaryKey(parentUserId);
                    if (null == parentUserIndex || StringUtils.isBlank(parentUserIndex.getEmail())) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    final User parentUser = userMapper.queryByEmail(parentUserIndex.getEmail());
                    if (null == parentUser) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    
                    if (BitUtils.isTrue(parentUser.getStatus(), AccountCommonConstant.USER_IS_EXIST_FUTURE_ACCOUNT)) {
                        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(parentUserId);
                        Long parentFutureUserId = parentUserInfo.getFutureUserId();
                        UserInfo parentFutureUserInfo = userInfoMapper.selectByPrimaryKey(parentFutureUserId);

                        // 永续账号
                        if (futureTradingAccount != null && parentFutureUserInfo.getMeTradingAccount() != null) {
                            userInfoApiClient.syncNewSubAccount(futureUser.getUserId(), parentFutureUserId);
                        }
                        
                        // 交割账号
                        if (deliveryTradingAccount != null && parentFutureUserInfo.getDeliveryTradingAccount() != null) {
                            deliveryUserSettingApiClient.syncNewSubAccount(futureUser.getUserId(), parentFutureUserId);    
                        }
                        log.info("future subUser syncNewSubAccount success");
                    }
                }
            } catch (Exception e) {
                log.error("future subUser syncNewSubAccount exception", e);
            }
        });
        CreateFutureUserResponse createFutureUserResponse = new CreateFutureUserResponse();
        createFutureUserResponse.setRootUserId(rootUserInfo.getUserId());
        createFutureUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createFutureUserResponse.setFutureUserId(futureUserInfo.getUserId());
        createFutureUserResponse.setFutureTradingAccount(futureTradingAccount);
        createFutureUserResponse.setFutureDeliveryTradingAccount(futureDeliveryTradingAccount);

        log.info("Create account for uid-{} succeed with result-{}", requestBody.getUserId(), createFutureUserResponse);

        return APIResponse.getOKJsonResult(createFutureUserResponse);
    }
    
    public void sendPushAppEmailInbox(Long userId,String email, String pushCode, String emailCode,String inboxCode, String terminalCode, LanguageEnum languageEnum,String ip){
        try {
            SinglePushRequest singlePushRequest = buildSinglePushRequest(userId, pushCode, languageEnum,ip);
            APIResponse response = iPushApi.asyncSinglePush(APIRequest.instance(singlePushRequest));
            log.info("iPushApi.asyncSinglePush.response:{}",JsonUtils.toJsonHasNullKey(response));
            msgApiClient.sendCreateFuturePushEmail(userId,email,languageEnum.getCode(),terminalCode,emailCode);
            APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, Maps.newHashMap(), languageEnum.getLang(), terminalCode,inboxCode);
            inboxMessageTextApi.pushInbox(apiRequest);
        }catch (Exception e){
            log.error("sendPushAppEmailInbox error",e);
        }

    }

    private SinglePushRequest buildSinglePushRequest(Long userId, String code, LanguageEnum langEnum, String ip) {
        SinglePushRequest singlePushRequest = new SinglePushRequest();
        singlePushRequest.setLanguage(langEnum.getCode());
        singlePushRequest.setBizScenarioCode(code);
        singlePushRequest.setMsgType(MsgTypeEnum.APP);
        singlePushRequest.setSource("future-" + ip);
        singlePushRequest.setPushId("future-" + UUID.randomUUID().toString());
        singlePushRequest.setUserId(String.valueOf(userId));
        singlePushRequest.setParams(Collections.emptyMap());
        return singlePushRequest;
    }

    public Long selectParentBrokerFutureUserId(Long userId){
        if (userId == null){
            return null;
        }
        SubUserBinding subUserBinding = subUserBindingMapper.selectBySubUserId(userId);
        if (subUserBinding == null){
            return null;
        }
        User user = userCommonBusiness.getUserByUseryId(subUserBinding.getParentUserId());
        if (user == null || !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {
            return null;
        }
        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if(parentUserInfo != null && parentUserInfo.getFutureUserId() != null){
            return parentUserInfo.getFutureUserId();
        }
        return null;
    }

    private Long getRefferFutureUserId(CreateFutureAccountRequest requestBody, UserInfo rootUserInfo, Long rootUserInsertTime) {
        log.info("getRefferFutureUserId.rebateStrategyChangeTime:{}",rebateStrategyChangeTime);
        //时间点之前--建议直接使用开关
        if (rebateStrategyChangeTime >= rootUserInsertTime){
            return getByAgentCodeFirst(requestBody, rootUserInfo,true);
        }else{
            //时间点之后
            //合约链接、一键注册
            if (requestBody.getIsFastCreatFuturesAccountProcess()){
                if (StringUtils.isNotBlank(requestBody.getAgentCode())){
                    return getByAgentCodeFirst(requestBody, rootUserInfo,true);
                }
                //如果agentCode空，则获取到主账户的agentId对应的futureUserId，如果为null，则null
                return getByAgentCodeFirst(requestBody, rootUserInfo,false);
            }else{
                return getByAgentCodeFirst(requestBody, rootUserInfo,false);
            }
        }

    }

    private Long getByAgentCodeFirst(CreateFutureAccountRequest requestBody, UserInfo rootUserInfo, boolean useAgentCode) {
        Long refferFutureUserId = null;
        //不忽视agentCode,使用agentCode
        if (StringUtils.isNumeric(requestBody.getAgentCode()) && useAgentCode){
            //存在亿分之一的概率，自己推荐自己
            UserInfo userInfo = userInfoMapper.selectRootUserInfoByFutureUserId(Long.parseLong(requestBody.getAgentCode().trim()));
            if (userInfo == null || userInfo.getFutureUserId() == null){
                throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_IS_NOT_EXIST);
            }
            User user = userCommonBusiness.checkAndGetUserById(userInfo.getUserId());
            if (AccountTypeEnum.getAccountType(user.getStatus()) != 1  && AccountTypeEnum.getAccountType(user.getStatus()) != 6 && AccountTypeEnum.getAccountType(user.getStatus()) != 8){
                throw new BusinessException(AccountErrorCode.ACCOUNT_TYPE_AS_FUTURE_AGENT_CODE_ERROR);
            }
            refferFutureUserId = userInfo.getFutureUserId();
        }else if (StringUtils.isNotBlank(requestBody.getAgentCode()) &&  useAgentCode){
            FutureUserAgent futureUserAgent = futureUserAgentMapper.selectByAgentCode(requestBody.getAgentCode());
            if (futureUserAgent == null || futureUserAgent.getFutureUserId() == null){
                throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_IS_NOT_EXIST);
            }
            refferFutureUserId = futureUserAgent.getFutureUserId();
        }else{
            if (rootUserInfo.getParent() != null ){
                UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(rootUserInfo.getParent());
                if (parentUserInfo != null && parentUserInfo.getFutureUserId() != null){
                    UserInfo parentFutureUserInfo = userInfoMapper.selectByPrimaryKey(parentUserInfo.getFutureUserId());
                    if (parentFutureUserInfo != null &&  parentFutureUserInfo.getFutureAgentId() != null){
                        return parentFutureUserInfo.getFutureAgentId();

                    }
                }
            }
            if (rootUserInfo.getAgentId() != null){
                UserInfo agentUserInfo = userInfoMapper.selectByPrimaryKey(rootUserInfo.getAgentId());
                if (agentUserInfo != null && agentUserInfo.getFutureUserId() != null){
                    refferFutureUserId = agentUserInfo.getFutureUserId();
                }
            }
        }
        return refferFutureUserId;
    }

    @Override
    public APIResponse<Long> getFutureUserIdByFutureTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception {
        Long tradingAccount = request.getBody().getTradingAccount();
        Long futureUserId = futureUserTradingAccountMapper.queryUserIdByTradingAccount(tradingAccount);
        return APIResponse.getOKJsonResult(futureUserId);
    }

    @Override
    public APIResponse<Long> getFutureUserIdByFutureDeliveryTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) throws Exception {
        Long tradingAccount = request.getBody().getTradingAccount();
        Long futureUserId = futureUserDeliveryTradingAccountMapper.queryUserIdByDeliveryTradingAccount(tradingAccount);
        return APIResponse.getOKJsonResult(futureUserId);
    }

    @Override
    public Boolean checkIfOpenFutureAccount(CheckIfOpenFutureAccountRequest request) throws Exception {
        Long rootUserId=request.getRootUserId();
        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(rootUserId);
        if (null == rootUserInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Boolean flag=Objects.nonNull(rootUserInfo.getFutureUserId());
        return flag;
    }

    @Override
    public Boolean sendFutureClosePositionMsg(SendFutureClosePositionMsgRequest request) throws Exception {
        log.info("sendFutureClosePositionMsg start futureUserId={}", request.getFutureUserId());
        Long total = RedisCacheUtils.get(String.valueOf(request.getFutureUserId()+request.getSymbol()), Long.class,
                ACCOUNT_SEND_POSITION_MSG);
        if (null!=total && total.longValue() > 0L) {
            log.warn("overlimit sendFutureClosePositionMsg");
            return Boolean.TRUE;
        }
        RedisCacheUtils.set(String.valueOf(request.getFutureUserId()+request.getSymbol()), 1L, 300L, ACCOUNT_SEND_POSITION_MSG);
        executorServiceForPositionMsg.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadContext.put("traceId","sendFutureClosePositionEmail-"+ UUID.randomUUID().toString());
                    Long futureUserId=request.getFutureUserId();
                    String symbol=request.getSymbol();
                    FutureEmailTypeEnum futureEmailTypeEnum=request.getFutureEmailTypeEnum();
                    UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
                    if (null == rootUserInfo) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    msgApiClient.sendFutureClosePositionEmail(rootUserInfo.getUserId(),symbol,futureEmailTypeEnum,request.getMakePrice(),request.getTotalMarginBalance());
                    if(futureClosePositionFlag) {
                        //发送短信
                        sendFutureClosePositionSms(rootUserInfo.getUserId(), symbol, futureEmailTypeEnum, request.getMakePrice(),request.getTotalMarginBalance());
                        //发送站内信
                        sendFutureClosePositionInbox(rootUserInfo.getUserId(), symbol, futureEmailTypeEnum, request.getMakePrice(),request.getTotalMarginBalance());
                    }
                } catch (Exception e) {
                    log.error("sendFutureClosePositionMsg exception", e);
                }finally {
                    TrackingUtils.removeTraceId();
                }
            }
        });
        return Boolean.TRUE;
    }

    @Override
    public QuerySubAccountFutureAccountResp queryFuturesAccount(Long userId) throws Exception {
        Long futureAccountId=getFutureAccountIdByRootUserId(userId);
        Long futureUserId=getFutureUserIdByRootUserId(userId);
        AccountRiskVO accountRiskVO=riskApiClient.getBalanceRisk(futureAccountId);
        UserTierVO userTierVO= commissionApiClient.getCommission(futureUserId);
        if(null== userTierVO){
            log.info("queryFuturesAccount:userTierVO or accountRiskVO is null");
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        QuerySubAccountFutureAccountResp resp=new QuerySubAccountFutureAccountResp();
        //设置基础信息
        resp.setEmail(userTierVO.getEmail());
        resp.setCanTrade(userTierVO.getAccountStatus().isCanTrade());
        resp.setCanDeposit(userTierVO.getAccountStatus().isCanDeposit());
        resp.setCanWithdraw(userTierVO.getAccountStatus().isCanWithdraw());
        resp.setFeeTier(userTierVO.getTier());
        resp.setUpdateTime(userTierVO.getUpdateTime().getTime());
        //写死目前只有USDT
        resp.setAsset("USDT");
        //设置统计信息
        if(null!=accountRiskVO){
            BeanUtils.copyProperties(accountRiskVO,resp);
        }else{
            resp.setTotalInitialMargin(BigDecimal.ZERO.toPlainString());
            resp.setTotalMaintenanceMargin(BigDecimal.ZERO.toPlainString());
            resp.setTotalWalletBalance(BigDecimal.ZERO.toPlainString());
            resp.setTotalUnrealizedProfit(BigDecimal.ZERO.toPlainString());
            resp.setTotalMarginBalance(BigDecimal.ZERO.toPlainString());
            resp.setTotalPositionInitialMargin(BigDecimal.ZERO.toPlainString());
            resp.setTotalOpenOrderInitialMargin(BigDecimal.ZERO.toPlainString());
            resp.setMaxWithdrawAmount(BigDecimal.ZERO.toPlainString());
        }
        //设置每个币的统计信息
        List<FutureAssetRiskInfoVo> assets=Lists.newArrayList();
        List<BalanceRiskVO> balanceRiskAssets=accountRiskVO.getAssets();
        if(CollectionUtils.isNotEmpty(balanceRiskAssets)){
            for(BalanceRiskVO balanceRiskVO:balanceRiskAssets){
                FutureAssetRiskInfoVo vo=new FutureAssetRiskInfoVo();
                BeanUtils.copyProperties(balanceRiskVO,vo);
                assets.add(vo);
            }

        }
        resp.setAssets(assets);
        return resp;
    }

    @Override
    public QuerySubAccountFutureAccountSummaryResp queryFuturesAccountSummary(UserInfo parentUserInfo, List<UserInfo> subUserInfoList, SubAccountSummaryQueryType subAccountSummaryQueryType,Integer page,Integer rows) throws Exception {
        QuerySubAccountFutureAccountSummaryResp resp=new QuerySubAccountFutureAccountSummaryResp();
        BigDecimal totalInitialMargin=BigDecimal.ZERO;
        BigDecimal totalMaintenanceMargin=BigDecimal.ZERO;
        BigDecimal totalWalletBalance=BigDecimal.ZERO;
        BigDecimal totalUnrealizedProfit=BigDecimal.ZERO;
        BigDecimal totalMarginBalance=BigDecimal.ZERO;
        BigDecimal totalPositionInitialMargin=BigDecimal.ZERO;
        BigDecimal totalOpenOrderInitialMargin=BigDecimal.ZERO;
        if(SubAccountSummaryQueryType.ONLY_PARENT_ACCOUNT==subAccountSummaryQueryType){
            FutureAccountSummaryInfoVo parentAccountSummaryVo=new FutureAccountSummaryInfoVo();
            User parentUser=checkAndGetUserById(parentUserInfo.getUserId());
            parentAccountSummaryVo.setEmail(parentUser.getEmail());
            parentAccountSummaryVo.setUserStatusEx(new UserStatusEx(parentUser.getStatus()));
            parentAccountSummaryVo.setTotalInitialMargin(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalMaintenanceMargin(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalWalletBalance(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalUnrealizedProfit(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalMarginBalance(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalPositionInitialMargin(BigDecimal.ZERO.toPlainString());
            parentAccountSummaryVo.setTotalOpenOrderInitialMargin(BigDecimal.ZERO.toPlainString());
            resp.setParentAccount(parentAccountSummaryVo);
            if(null!=parentUserInfo.getFutureUserId()){
                Map<Long,AccountRiskVO> accountRiskVoMap= riskApiClient.batchGetBalanceRisks(Sets.newHashSet(parentUserInfo.getFutureUserId()));
                AccountRiskVO accountRiskVO=accountRiskVoMap.get(parentUserInfo.getFutureUserId());
                if(null!=accountRiskVO){
                    BeanUtils.copyProperties(accountRiskVO,parentAccountSummaryVo);
                    resp.setParentAccount(parentAccountSummaryVo);
                }
            }
        }else if(SubAccountSummaryQueryType.ONLY_SUB_ACCOUNT==subAccountSummaryQueryType && CollectionUtils.isNotEmpty(subUserInfoList)){
            List<Long> futureUserIdList=Lists.newArrayList();
            Map<Long,Long> futureUserIToRootUserMap=Maps.newConcurrentMap();
            for(UserInfo userInfo:subUserInfoList){
                if(null!=userInfo.getFutureUserId()){
                    futureUserIdList.add(userInfo.getFutureUserId());
                    futureUserIToRootUserMap.put(userInfo.getFutureUserId(),userInfo.getUserId());
                }
            }
            int futureListSize=futureUserIdList.size();
            if (null != page && null != rows && page.intValue() > 0 && rows.intValue() > 0 && futureListSize>0) {
                if(page * rows>futureListSize){
                    futureUserIdList = futureUserIdList.subList((page - 1) * rows, futureListSize);
                }else{
                    futureUserIdList = futureUserIdList.subList((page - 1) * rows, page * rows);
                }
            }
            if(futureListSize==0){
                resp.setSubAccountList(Lists.newArrayList());
                resp.setTotalSubAccountSize(0L);
            }else{
                Map<Long,AccountRiskVO> accountRiskVoMap= riskApiClient.batchGetBalanceRisks(Sets.newHashSet(futureUserIdList));
                List<Long> pageSubUerIdList=Lists.newArrayList();//当前分页需要的主账户useridlist
                for(Long futureUserId:futureUserIdList){
                    pageSubUerIdList.add(futureUserIToRootUserMap.get(futureUserId));
                }
                List<User> subUserList=userMapper.selectByUserIds(pageSubUerIdList);
                Map<Long,User> userIdToUserMap=Maps.uniqueIndex(subUserList, new Function<User, Long>() {
                    @Override
                    public Long apply(@Nullable User user) {
                        return user.getUserId();
                    }
                });
                List<FutureAccountSummaryInfoVo> subAccountList=Lists.newArrayList();
                for(Long futureUserId:futureUserIdList){
                    FutureAccountSummaryInfoVo subUserSummaryInfo=new FutureAccountSummaryInfoVo();
                    Long subUserId=futureUserIToRootUserMap.get(futureUserId);
                    User subUser=userIdToUserMap.get(subUserId);
                    subUserSummaryInfo.setEmail(subUser.getEmail());
                    subUserSummaryInfo.setUserStatusEx(new UserStatusEx(subUser.getStatus()));
                    AccountRiskVO accountRiskVO= accountRiskVoMap.get(futureUserId);
                    if(null!=accountRiskVO){
                        BeanUtils.copyProperties(accountRiskVO,subUserSummaryInfo);
                    }else{
                        subUserSummaryInfo.setTotalInitialMargin(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalMaintenanceMargin(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalWalletBalance(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalUnrealizedProfit(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalMarginBalance(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalPositionInitialMargin(BigDecimal.ZERO.toPlainString());
                        subUserSummaryInfo.setTotalOpenOrderInitialMargin(BigDecimal.ZERO.toPlainString());
                    }
                    subAccountList.add(subUserSummaryInfo);
                }
                resp.setSubAccountList(subAccountList);
                resp.setTotalSubAccountSize(Long.valueOf(String.valueOf(futureListSize)));
                for(FutureAccountSummaryInfoVo vo:subAccountList){
                    totalInitialMargin=totalInitialMargin.add(new BigDecimal(vo.getTotalInitialMargin()));
                    totalMaintenanceMargin=totalMaintenanceMargin.add(new BigDecimal(vo.getTotalMaintenanceMargin()));
                    totalWalletBalance=totalWalletBalance.add(new BigDecimal(vo.getTotalWalletBalance()));
                    totalUnrealizedProfit=totalUnrealizedProfit.add(new BigDecimal(vo.getTotalUnrealizedProfit()));
                    totalMarginBalance=totalMarginBalance.add(new BigDecimal(vo.getTotalMarginBalance()));
                    totalPositionInitialMargin=totalPositionInitialMargin.add(new BigDecimal(vo.getTotalPositionInitialMargin()));
                    totalOpenOrderInitialMargin=totalOpenOrderInitialMargin.add(new BigDecimal(vo.getTotalOpenOrderInitialMargin()));
                }
            }
        }else if(SubAccountSummaryQueryType.ONLY_SUB_ACCOUNT==subAccountSummaryQueryType && CollectionUtils.isEmpty(subUserInfoList)) {
            resp.setSubAccountList(Lists.newArrayList());
            resp.setTotalSubAccountSize(0L);
        }else if(SubAccountSummaryQueryType.QUERY_ALL==subAccountSummaryQueryType){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //设置统计信息
        resp.setTotalInitialMargin(totalInitialMargin.toPlainString());
        resp.setTotalMaintenanceMargin(totalMaintenanceMargin.toPlainString());
        resp.setTotalWalletBalance(totalWalletBalance.toPlainString());
        resp.setTotalUnrealizedProfit(totalUnrealizedProfit.toPlainString());
        resp.setTotalMarginBalance(totalMarginBalance.toPlainString());
        resp.setTotalPositionInitialMargin(totalPositionInitialMargin.toPlainString());
        resp.setTotalOpenOrderInitialMargin(totalOpenOrderInitialMargin.toPlainString());
        return resp;
    }

    @Override
    public List<FuturePositionRiskVO> queryFuturesPositionRisk(Long userId) throws Exception {
        Long futureAccountId=getFutureAccountIdByRootUserId(userId);
        List<PositionRiskVO> positionRiskVOList=riskApiClient.getPositionRisk(futureAccountId);
        if(CollectionUtils.isEmpty(positionRiskVOList)){
            log.info("queryFuturesPositionRisk:positionRiskVOList  is null");
            return Lists.newArrayList();
        }
        List<FuturePositionRiskVO> resultList=Lists.newArrayList();
        for(PositionRiskVO positionRiskVO:positionRiskVOList){
            FuturePositionRiskVO futurePositionRiskVO=new FuturePositionRiskVO();
            BeanUtils.copyProperties(positionRiskVO,futurePositionRiskVO);
            resultList.add(futurePositionRiskVO);
        }
        return resultList;
    }

    @Override
    public QuerySubAccountMarginAccountSummaryResp queryMarginAccountSummary(UserInfo parentUserInfo, List<UserInfo> subUserInfoList, Integer page, Integer rows) throws Exception {
        QuerySubAccountMarginAccountSummaryResp resp=new QuerySubAccountMarginAccountSummaryResp();
        List<Long> marginUserIdList=Lists.newArrayList();
        Long parentMarginUserId=parentUserInfo.getMarginUserId();
        for(UserInfo userInfo:subUserInfoList){
            if(null!=userInfo.getMarginUserId()){
                marginUserIdList.add(userInfo.getMarginUserId());
            }
        }
        List<Long> originMarginUserIdList=Lists.newCopyOnWriteArrayList(marginUserIdList);
        int marginListSize=marginUserIdList.size();
        if (null != page && null != rows && page.intValue() > 0 && rows.intValue() > 0 && marginListSize>0) {
            if(page * rows>marginListSize){
                marginUserIdList = marginUserIdList.subList((page - 1) * rows, marginListSize);
            }else{
                marginUserIdList = marginUserIdList.subList((page - 1) * rows, page * rows);
            }
        }
        AccountSummaryResponse accountSummaryResponse= marginAccountApiClient.subAccountSummary(parentMarginUserId,originMarginUserIdList,marginUserIdList);
        //设置统计信息
        resp.setMasterAccountNetAssetOfBtc(accountSummaryResponse.getMasterAccountNetAssetOfBtc().toPlainString());
        resp.setTotalAssetOfBtc(accountSummaryResponse.getTotalAssetOfBtc().toPlainString());
        resp.setTotalLiabilityOfBtc(accountSummaryResponse.getTotalLiabilityOfBtc().toPlainString());
        resp.setTotalNetAssetOfBtc(accountSummaryResponse.getTotalNetAssetOfBtc().toPlainString());
        if(marginListSize==0){
            resp.setSubAccountList(Lists.newArrayList());
            resp.setTotalSubAccountSize(Long.parseLong(String.valueOf(marginListSize)));
            return resp;
        }

        List<Long> subUserIdList=Lists.transform(subUserInfoList, new Function<UserInfo, Long>() {
            @Override
            public Long apply(@Nullable UserInfo userInfo) {
                return userInfo.getUserId();
            }
        });
        Map<Long,UserInfo> userInfoMap= Maps.uniqueIndex(subUserInfoList, new Function<UserInfo, Long>() {
            @Override
            public Long apply(@Nullable UserInfo userInfo) {
                return userInfo.getUserId();
            }
        });
        List<User> subUserList=userMapper.selectByUserIds(subUserIdList);
        List<AccountSummaryResponse.AccountSummary> marginAccountSummaryList=accountSummaryResponse.getSubAccounts();
        Map<Long,AccountSummaryResponse.AccountSummary> marginAccountSummaryMap=Maps.uniqueIndex(marginAccountSummaryList, new Function<AccountSummaryResponse.AccountSummary, Long>() {
            @Override
            public Long apply(AccountSummaryResponse.@Nullable AccountSummary accountSummary) {
                return accountSummary.getUid();
            }
        });
        List<MarginAccountSummaryInfoVo> subAccountList=Lists.newArrayList();
        for(User subUser:subUserList){
            MarginAccountSummaryInfoVo subUserSummaryInfo=new MarginAccountSummaryInfoVo();
            subUserSummaryInfo.setEmail(subUser.getEmail());
            subUserSummaryInfo.setUserStatusEx(new UserStatusEx(subUser.getStatus()));
            Long marginUserId=userInfoMap.get(subUser.getUserId()).getMarginUserId();
            Boolean isContainMargin=marginUserIdList.contains(marginUserId);
            if(null==marginUserId || !isContainMargin.booleanValue()){
                continue;
            }
            AccountSummaryResponse.AccountSummary accountSummary= marginAccountSummaryMap.get(marginUserId);
            if(null==accountSummary){
                continue;
            }
            subUserSummaryInfo.setMarginLevel(accountSummary.getMarginLevel().toPlainString());
            subUserSummaryInfo.setTotalAssetOfBtc(accountSummary.getTotalAssetOfBtc().toPlainString());
            subUserSummaryInfo.setTotalLiabilityOfBtc(accountSummary.getTotalLiabilityOfBtc().toPlainString());
            subUserSummaryInfo.setTotalNetAssetOfBtc(accountSummary.getTotalNetAssetOfBtc().toPlainString());
            subAccountList.add(subUserSummaryInfo);
        }
        resp.setSubAccountList(subAccountList);
        resp.setTotalSubAccountSize(Long.parseLong(String.valueOf(marginListSize)));
        return resp;
    }

    @Override
    public QuerySubAccountMarginAccountResp queryMarginAccount(User subUser,MarginPeriodType marginPeriodType) throws Exception {
        Long userId=subUser.getUserId();
        Long marginUserId=getMarginUserIdByRootUserId(userId);
        AccountInfoResponse accountInfoResponse=marginAccountApiClient.subAccountInfo(marginUserId,PeriodType.valueOf(marginPeriodType.name()));
        QuerySubAccountMarginAccountResp resp=new QuerySubAccountMarginAccountResp();
        resp.setEmail(subUser.getEmail());
        resp.setMarginLevel(accountInfoResponse.getMarginLevel().toPlainString());
        resp.setMarginLevelStatus(accountInfoResponse.getMarginLevelStatus().name());
        resp.setTotalAssetOfBtc(accountInfoResponse.getTotalAssetOfBtc().toPlainString());
        resp.setTotalLiabilityOfBtc(accountInfoResponse.getTotalLiabilityOfBtc().toPlainString());
        resp.setTotalNetAssetOfBtc(accountInfoResponse.getTotalNetAssetOfBtc().toPlainString());
        List<MarginProfitVo> marginProfitVoList=Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(accountInfoResponse.getProfits())){
            for(AccountInfoResponse.Profit profit: accountInfoResponse.getProfits()){
                MarginProfitVo marginProfitVo=new MarginProfitVo();
                marginProfitVo.setAsset(profit.getAsset());
                marginProfitVo.setProfit(profit.getProfit().toPlainString());
                marginProfitVo.setProfitRate(profit.getProfitRate().toPlainString());
                marginProfitVo.setBeginTime(profit.getBeginTime());
                marginProfitVo.setCalcTime(profit.getCalcTime());
                marginProfitVoList.add(marginProfitVo);
            }
        }
        resp.setMarginProfitVoList(marginProfitVoList);
        List<MarginUserAssetVo> marginUserAssetVoList=Lists.newArrayList();
        if(CollectionUtils.isNotEmpty(accountInfoResponse.getUserAssets())){
            for(AccountInfoResponse.UserAsset userAsset: accountInfoResponse.getUserAssets()){
                MarginUserAssetVo marginUserAssetVo=new MarginUserAssetVo();
                marginUserAssetVo.setTotal(userAsset.getTotal().toPlainString());
                marginUserAssetVo.setAsset(userAsset.getAsset());
                marginUserAssetVo.setBorrowed(userAsset.getBorrowed().toPlainString());
                marginUserAssetVo.setFree(userAsset.getFree().toPlainString());
                marginUserAssetVo.setInterest(userAsset.getInterest().toPlainString());
                marginUserAssetVo.setLocked(userAsset.getLocked().toPlainString());
                marginUserAssetVo.setNetAsset(userAsset.getNetAsset().toPlainString());
                marginUserAssetVo.setNetAssetOfBtc(userAsset.getNetAssetOfBtc().toPlainString());
                marginUserAssetVoList.add(marginUserAssetVo);
            }
        }
        resp.setMarginUserAssetVoList(marginUserAssetVoList);
        //查询margin交易系数
        MarginTradeCoeffResponse marginTradeCoeffResponse = marginAdminApiClient.marginTradeCoeff(userId);
        MarginTradeCoeffVo marginTradeCoeffVo = new MarginTradeCoeffVo();
        if (null != marginTradeCoeffResponse) {
            BeanUtils.copyProperties(marginTradeCoeffResponse, marginTradeCoeffVo);
        }
        //查询margin是否能交易
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        AccountSimpleResponse accountSimpleResponse = marginAccountApiClient.accountSimpleList(userInfo.getMarginUserId());
        if (accountSimpleResponse != null){
            marginTradeCoeffVo.setCanTrade(accountSimpleResponse.getTradeStatus());
        }
        resp.setMarginTradeCoeffVo(marginTradeCoeffVo);
        return resp;
    }

    @Override
    public Boolean sendFutureMarginCall(SendFutureMarginCallRequest request) throws Exception {
        log.info("sendFutureMarginCall start futureUserId={},symbol = {}", request.getFutureUserId(),request.getSymbol());
        executorServiceForMarginCall.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadContext.put("traceId","sendFutureMarginCall-"+ UUID.randomUUID().toString());
                    Long futureUserId=request.getFutureUserId();
                    UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
                    if (null == rootUserInfo) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }

                    log.info("start send margin call email,futureUserId={},time = {}",request.getFutureUserId(),System.currentTimeMillis());
                    //发送email
                    msgApiClient.sendFutureMarginCall(rootUserInfo.getUserId(),request.getSymbol(), org.apache.commons.lang3.StringUtils.isBlank(request.getSymbol()) ?
                            (request.isDelivery() ? AccountConstants.DELIVERY_MARGIN_CALL : AccountConstants.FUTURE_MARGIN_CALL) :
                            (request.isDelivery() ? AccountConstants.DELIVERY_ISOLATED_MARGIN_CALL : AccountConstants.FUTURE_ISOLATED_MARGIN_CALL));

                    log.info("start send margin call sms,futureUserId={},time = {}",request.getFutureUserId(),System.currentTimeMillis());
                    //发送短信
                    sendMarginCallSms(rootUserInfo.getUserId(),request.getSymbol(),org.apache.commons.lang3.StringUtils.isBlank(request.getSymbol()) ?
                            (request.isDelivery() ? AccountConstants.DELIVERY_MARGIN_CALL_SMS : AccountConstants.FUTURE_MARGIN_CALL_SMS) :
                            (request.isDelivery() ? AccountConstants.DELIVERY_ISOLATED_MARGIN_CALL_SMS : AccountConstants.FUTURE_ISOLATED_MARGIN_CALL_SMS));

                    log.info("start send margin call inbox,futureUserId={},time = {}",request.getFutureUserId(),System.currentTimeMillis());
                    //发送站内信
                    sendMarginCallInbox(rootUserInfo.getUserId(),request.getSymbol(),org.apache.commons.lang3.StringUtils.isBlank(request.getSymbol()) ?
                            (request.isDelivery() ? AccountConstants.DELIVERY_MARGIN_CALL_INBOX : AccountConstants.FUTURE_MARGIN_CALL_INBOX) :
                            (request.isDelivery() ? AccountConstants.DELIVERY_ISOLATED_MARGIN_CALL_INBOX :AccountConstants.FUTURE_ISOLATED_MARGIN_CALL_INBOX));

                    log.info("end send margin call ,futureUserId={},time = {}",request.getFutureUserId(),System.currentTimeMillis());
                } catch (Exception e) {
                    log.error("sendFutureMarginCall exception", e);
                }finally {
                    TrackingUtils.removeTraceId();
                }
            }
        });
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendFutureFundingRateMsg(SendFutureFundingRateMsgRequest request) throws Exception {
        log.info("sendFutureFundingRateMsg start futureUserId={},symbol = {}", request.getFutureUserId(),request.getSymbol());
        executorServiceForMarginCall.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadContext.put("traceId","sendFutureFundingRateMsg-"+ UUID.randomUUID().toString());
                    Long futureUserId=request.getFutureUserId();
                    UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
                    if (null == rootUserInfo) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    //发送email
                    msgApiClient.sendFutureFundingRate(rootUserInfo.getUserId(),request.getSymbol(), request.getRate(),request.getAmount(),request.getTime(),AccountConstants.FUTURE_FUNDING_FEE_NOTIFICATION);
                    //发送短信
                    sendFundingRateSms(rootUserInfo.getUserId(),request.getSymbol(),request.getRate(),request.getAmount(),request.getTime(),AccountConstants.FUTURE_FUNDING_FEE_SMS);
                    //发送站内信
                    sendFundingRateInbox(rootUserInfo.getUserId(),request.getSymbol(),request.getRate(),request.getAmount(),request.getTime(),AccountConstants.FUTURE_FUNDING_FEE_INBOX);
                } catch (Exception e) {
                    log.error("sendFutureFundingRateMsg exception", e);
                }finally {
                    TrackingUtils.removeTraceId();
                }
            }
        });
        return Boolean.TRUE;
    }

    /**
     * 发送保证金短信信息
     * @param userId
     * @param smsTemplate
     */
    private void sendMarginCallSms(Long userId, String symbol,String smsTemplate) {
        try {
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败. userId:{}");
            }
            String mobile = userSecurity.getMobile();
            String countryCode = userSecurity.getMobileCode();
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(mobile, countryCode)) {
                log.info("用户没有绑定手机号码或者手机号码的国家码不存在不发送短信: userId:{}", userId);
                return;
            }
            Country country = iCountry.getCountryByCode(countryCode);
            String mobileCode = country != null ? country.getMobileCode() : null;
            if (org.apache.commons.lang3.StringUtils.isBlank(mobileCode)) {
                log.info("国家短信码获取失败，不需要发送短信通知: userId:{}", userId);
                return;
            }
            LanguageEnum languageEnum = LanguageEnum.EN_US;
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("cn", userSecurity.getMobileCode())) {
                languageEnum = LanguageEnum.ZH_CN;
            }
            SendMsgRequest msgRequest = new SendMsgRequest();
            msgRequest.setTplCode(smsTemplate);
            msgRequest.setUserId(String.valueOf(userId));
            msgRequest.setMobileCode(mobileCode);
            msgRequest.setRecipient(mobile);
            msgRequest.setNeedIpCheck(false);
            msgRequest.setNeedSendTimesCheck(false);
            Map<String, Object> params = Maps.newHashMap();
            params.put("symbol", symbol);
            msgRequest.setData(params);
            log.info("发送保证金短信信息: userId:{} smsTemplate:{} language:{}", userId, smsTemplate, languageEnum);
            userCommonBusiness.sendMsg(msgRequest, languageEnum, TerminalEnum.OTHER);
        }catch (Exception e) {
            log.error("保证金短信信息发送出现异常: userId:{} smsTemplate:{}", userId, smsTemplate, e);
        }
    }

    /**
     * 发送资金费率短信信息
     * @param userId
     * @param smsTemplate
     */
    private void sendFundingRateSms(Long userId, String symbol,String rate,String amount,String time,String smsTemplate) {
        try {
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败. userId:{}");
            }
            String mobile = userSecurity.getMobile();
            String countryCode = userSecurity.getMobileCode();
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(mobile, countryCode)) {
                log.info("用户没有绑定手机号码或者手机号码的国家码不存在不发送短信: userId:{}", userId);
                return;
            }
            Country country = iCountry.getCountryByCode(countryCode);
            String mobileCode = country != null ? country.getMobileCode() : null;
            if (org.apache.commons.lang3.StringUtils.isBlank(mobileCode)) {
                log.info("国家短信码获取失败，不需要发送短信通知: userId:{}", userId);
                return;
            }
            LanguageEnum languageEnum = LanguageEnum.EN_US;
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("cn", userSecurity.getMobileCode())) {
                languageEnum = LanguageEnum.ZH_CN;
            }
            SendMsgRequest msgRequest = new SendMsgRequest();
            msgRequest.setTplCode(smsTemplate);
            msgRequest.setUserId(String.valueOf(userId));
            msgRequest.setMobileCode(mobileCode);
            msgRequest.setRecipient(mobile);
            msgRequest.setNeedIpCheck(false);
            msgRequest.setNeedSendTimesCheck(false);
            Map<String, Object> params = Maps.newHashMap();
            params.put("symbols", symbol);
            params.put("rates", rate);
            params.put("amounts", amount);
            params.put("time", time);
            msgRequest.setData(params);
            log.info("发送资金费率短信信息: userId:{} smsTemplate:{} language:{}", userId, smsTemplate, languageEnum);
            userCommonBusiness.sendMsg(msgRequest, languageEnum, TerminalEnum.OTHER);
        }catch (Exception e) {
            log.error("资金费率短信信息发送出现异常: userId:{} smsTemplate:{}", userId, smsTemplate, e);
        }
    }

    /**
     * 发送保证金站内信
     * @param userId
     * @param inboxTemplate
     */
    private void sendMarginCallInbox(Long userId, String symbol,String inboxTemplate) {
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            Map<String,Object> data = Maps.newHashMap();
            data.put("email", user.getEmail());
            data.put("symbol", symbol);
            APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, data, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web",inboxTemplate);
            inboxMessageTextApi.pushInbox(apiRequest);
        }catch (Exception e) {
            log.error("保证金站内信发送出现异常: userId:{} smsTemplate:{}", userId, inboxTemplate, e);
        }
    }

    /**
     * 发送资金费率站内信
     * @param userId
     * @param inboxTemplate
     */
    private void sendFundingRateInbox(Long userId, String symbol,String rate,String amount,String time,String inboxTemplate) {
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            Map<String,Object> data = Maps.newHashMap();
            data.put("email", user.getEmail());
            data.put("symbols", symbol);
            data.put("rates", rate);
            data.put("amounts", amount);
            data.put("time", time);
            APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, data, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web",inboxTemplate);
            inboxMessageTextApi.pushInbox(apiRequest);
        }catch (Exception e) {
            log.error("保证金站内信发送出现异常: userId:{} smsTemplate:{}", userId, inboxTemplate, e);
        }
    }

    /**
     * 发送平仓短信信息
     * @param userId
     * @param
     */
    private void sendFutureClosePositionSms(Long userId, String symbol, FutureEmailTypeEnum futureEmailTypeEnum, BigDecimal makePrice,BigDecimal totalMarginBalance) {
        try {
            String tplCode = null;
            Map<String, Object> params = Maps.newHashMap();
            if(FutureEmailTypeEnum.LIQUIDATION==futureEmailTypeEnum){
                tplCode = AccountConstants.FUTURE_LIQUIDATION_SMS;
                params.put("symbol", symbol);
                params.put("mark_price", makePrice);
                params.put("balance", totalMarginBalance);
            }
            if(FutureEmailTypeEnum.ADL==futureEmailTypeEnum){
                tplCode = AccountConstants.FUTURE_ADL_SMS;
                params.put("symbol", symbol);
            }
            if(FutureEmailTypeEnum.DELIVERY_LIQUIDATION==futureEmailTypeEnum){
                tplCode = AccountConstants.DELIVERY_LIQUIDATION_SMS;
                params.put("symbol", symbol);
                params.put("mark_price", makePrice);
                params.put("balance", totalMarginBalance);
                params.put("asset", (StringUtils.isNotBlank(symbol) && symbol.contains("USD_")) ? symbol.substring(0,symbol.indexOf("USD_")) : "BTC");
            }
            if(FutureEmailTypeEnum.DELIVERY_ADL==futureEmailTypeEnum){
                tplCode = AccountConstants.DELIVERY_ADL_SMS;
                params.put("symbol", symbol);
            }
            if(StringUtils.isBlank(tplCode)){
                log.info("sendFutureClosePositionSms:skip because of empty tplcode userId={}",userId);
                return;
            }
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败. userId:{}");
            }
            String mobile = userSecurity.getMobile();
            String countryCode = userSecurity.getMobileCode();
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(mobile, countryCode)) {
                log.info("用户没有绑定手机号码或者手机号码的国家码不存在不发送短信: userId:{}", userId);
                return;
            }
            Country country = iCountry.getCountryByCode(countryCode);
            String mobileCode = country != null ? country.getMobileCode() : null;
            if (org.apache.commons.lang3.StringUtils.isBlank(mobileCode)) {
                log.info("国家短信码获取失败，不需要发送短信通知: userId:{}", userId);
                return;
            }
            LanguageEnum languageEnum = LanguageEnum.EN_US;
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("cn", userSecurity.getMobileCode())) {
                languageEnum = LanguageEnum.ZH_CN;
            }
            SendMsgRequest msgRequest = new SendMsgRequest();
            msgRequest.setTplCode(tplCode);
            msgRequest.setUserId(String.valueOf(userId));
            msgRequest.setMobileCode(mobileCode);
            msgRequest.setRecipient(mobile);
            msgRequest.setNeedIpCheck(false);
            msgRequest.setNeedSendTimesCheck(false);
            msgRequest.setData(params);
            log.info("发送平仓短信信息: userId:{} smsTemplate:{} language:{}", userId, tplCode, languageEnum);
            userCommonBusiness.sendMsg(msgRequest, languageEnum, TerminalEnum.OTHER);
        }catch (Exception e) {
            log.error("平仓短信信息发送出现异常: userId:{}  msg:{}", userId, e);
        }
    }

    /**
     * 发送平仓站内信
     * @param userId
     * @param
     */
    private void sendFutureClosePositionInbox(Long userId, String symbol, FutureEmailTypeEnum futureEmailTypeEnum, BigDecimal makePrice,BigDecimal totalMarginBalance) {
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            String tplCode = null;
            Map<String, Object> params = Maps.newHashMap();
            if(FutureEmailTypeEnum.LIQUIDATION==futureEmailTypeEnum){
                tplCode = AccountConstants.FUTURE_LIQUIDATION_INBOX;
                params.put("symbol", symbol);
                params.put("mark_price", makePrice);
                params.put("email", user.getEmail());
                params.put("balance", totalMarginBalance);
            }
            if(FutureEmailTypeEnum.ADL==futureEmailTypeEnum){
                tplCode = AccountConstants.FUTURE_ADL_INBOX;
                params.put("symbol", symbol);
                params.put("email", user.getEmail());
            }
            if(FutureEmailTypeEnum.DELIVERY_LIQUIDATION==futureEmailTypeEnum){
                tplCode = AccountConstants.DELIVERY_LIQUIDATION_INBOX;
                params.put("symbol", symbol);
                params.put("mark_price", makePrice);
                params.put("email", user.getEmail());
                params.put("balance", totalMarginBalance);
                params.put("asset", (StringUtils.isNotBlank(symbol) && symbol.contains("USD_")) ? symbol.substring(0,symbol.indexOf("USD_")) : "BTC");
            }
            if(FutureEmailTypeEnum.DELIVERY_ADL==futureEmailTypeEnum){
                tplCode = AccountConstants.DELIVERY_ADL_INBOX;
                params.put("symbol", symbol);
                params.put("email", user.getEmail());
            }
            if(StringUtils.isBlank(tplCode)){
                log.info("sendFutureClosePositionSms:skip because of empty tplcode userId={}",userId);
                return;
            }

            APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, params, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web",tplCode);
            inboxMessageTextApi.pushInbox(apiRequest);
        }catch (Exception e) {
            log.error("平仓站内信发送出现异常: userId:{} msg:{}", userId, e);
        }
    }


    public Long getFutureUserIdByRootUserId(Long rootUserId) throws Exception {
        UserInfo rootUserInfo=userInfoMapper.selectByPrimaryKey(rootUserId);
        if(Objects.isNull(rootUserInfo)||Objects.isNull(rootUserInfo.getTradingAccount())){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Long futureUserId = rootUserInfo.getFutureUserId();
        if (Objects.isNull(futureUserId)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return futureUserId;
    }

    public Long getMarginUserIdByRootUserId(Long rootUserId) throws Exception {
        UserInfo rootUserInfo=userInfoMapper.selectByPrimaryKey(rootUserId);
        if(Objects.isNull(rootUserInfo)||Objects.isNull(rootUserInfo.getTradingAccount())){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Long marginUserId = rootUserInfo.getMarginUserId();
        if (Objects.isNull(marginUserId)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return marginUserId;
    }



    public Long getFutureAccountIdByRootUserId(Long rootUserId) throws Exception {
        UserInfo rootUserInfo=userInfoMapper.selectByPrimaryKey(rootUserId);
        if(Objects.isNull(rootUserInfo)||Objects.isNull(rootUserInfo.getTradingAccount())){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Long futureUserId = rootUserInfo.getFutureUserId();
        if (Objects.isNull(futureUserId)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return futureUserInfo.getMeTradingAccount();
    }


    /**
     * 校验用户信息（Future version）
     *
     * @return Pair 返回的是一个元组，主要是不想再单独包个对象了，为了简单
     */
    protected Pair<User, UserInfo> checkAndGetUserByIdForFutureVersion(Long userId,Long parentUserId) throws Exception {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User rootUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == rootUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //检查传进来的userid是否是future的userid
        //当前账号不能是future 账号
        if (BitUtils.isEnable(rootUser.getStatus(), AccountConstants.USER_IS_FUTURE_USER)) {
            throw new BusinessException(AccountErrorCode.FUTURE_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }
        //当前账号没有激活
        if (!BitUtils.isEnable(rootUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(AccountErrorCode.ACTIVE_FUTURE_ACCOUNT_FAILED);
        }
        //当前账号是否是子账号
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        Boolean needCheckSubUserValidate= isSubUser && Objects.isNull(parentUserId);
        if(needCheckSubUserValidate){
            checkCountryBackListByIp(userId);
        }
        //当前账号不能是margin
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_MARGIN_USER)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }

        //当前账号不能是future
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_FUTURE_USER)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }

        //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
        //2fa 限制取消
        /*Boolean isPass2FA = BitUtils.isEnable(rootUser.getStatus(), Constant.USER_MOBILE) || BitUtils.isEnable(rootUser.getStatus(), Constant.USER_GOOGLE);
        if (!isPass2FA && !isSubUser) {
            throw new BusinessException(AccountErrorCode.PLEASE_PASS_ZFA_BEFORE_CREATE_FUTURE_ACCOUNT);
        }*/

        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null == rootUserInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //已经创建过future账号的就别再反复创建了
        if (null != rootUserInfo.getFutureUserId()) {
            throw new BusinessException(AccountErrorCode.DONT_CREATE_FUTURE_ACCOUNT_AGAIN);
        }
        Pair<User, UserInfo> twoTuple = Pair.of(rootUser, rootUserInfo);
        return twoTuple;
    }

    protected void checkInvitationCode(String invitationCode) throws Exception {
        if (StringUtils.isBlank(invitationCode)) {
            throw new BusinessException(AccountErrorCode.FUTURE_INVITATION_CODE_EMPTY);
        }
        final FutureInvitationLog futureInvitationLog = futureInvitationLogMapper.selectByPrimaryKey(invitationCode);
        if (null == futureInvitationLog || futureInvitationLog.getStatus().intValue()==1) {
            throw new BusinessException(AccountErrorCode.FUTURE_INVITATION_CODE_INVALID);
        }
        futureInvitationLogMapper.enableInvitationCode(invitationCode);
    }



    /**
     * 创建一个虚拟邮箱（幂等）
     */
    protected String createVirtualEmailForFuture(String email, Long userId,String invitationCode) {
        String[] emailArray = email.split("@");
        String virtualEmail = emailArray[0] + "_" + String.valueOf(userId)+ "_future@" + emailArray[1];
        return virtualEmail;
    }

    /**
     * 创建用户（不幂等，但是会回滚）
     *
     * @param vritualEmail
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createFutureUser(final String vritualEmail) throws NoSuchAlgorithmException {
        //这里加了事务回滚，所以如果报错数据直接回滚
        User futureUser = null;
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(vritualEmail);
        futureUser = onlyCreateFutureUser(userIndex);
        return futureUser;
    }

    /**
     * 创建future账户逻辑
     */
    protected User onlyCreateFutureUser(UserIndex userIndex) throws NoSuchAlgorithmException {
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, "123456", cipherCode);
        //实际上future账号的user并不需要密码和salt所以设置为空字符串
        user.setPassword("");
        user.setSalt("");
        //因为是future账号所以只有交易功能，还有需要标志成future
        Long status = user.getStatus();
        status = BitUtils.enable(status, AccountConstants.USER_IS_FUTURE_USER);
        user.setStatus(status);
        userMapper.insert(user);
        return user;
    }

    /**
     * 创建Future用户Security信息（不幂等，但是会回滚）
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createFutureUserSecurity(final Long userId, final String userEmail) {
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(userId);
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");
        userSecurity.setSecurityLevel(1);
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(1);
        userSecurity.setWithdrawSecurityAutoStatus(1);
        userSecurityMapper.insert(userSecurity);
    }

    /**
     * 创建Future用户信息（不幂等，但是会回滚）
     *
     * @param rootUserInfo
     * @param futureUserId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createFutureUserInfo(UserInfo rootUserInfo, Long futureUserId, Long refferFutureUserId) {
        //逻辑很简单，从主账号的userinfo把信息都copy过来就完事了
        UserInfo futureUserInfo = new UserInfo();
        futureUserInfo.setUserId(futureUserId);
        // 被推荐人返佣比例
        futureUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        // 经纪人返佣比例
        futureUserInfo.setAgentRewardRatio(rootUserInfo.getAgentRewardRatio());
        // 用户交易账号 激活时创建
        futureUserInfo.setTradingAccount(null);
        // 被动方手续费
        futureUserInfo.setMakerCommission(null);
        // 主动方手续费
        futureUserInfo.setTakerCommission(null);
        // 买方交易手续费
        futureUserInfo.setBuyerCommission(null);
        // 卖方交易手续费
        futureUserInfo.setSellerCommission(null);
        // 单日最大出金总金额
        futureUserInfo.setDailyWithdrawCap(rootUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        futureUserInfo.setDailyWithdrawCountLimit(rootUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        futureUserInfo.setAutoWithdrawAuditThreshold(rootUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        futureUserInfo.setTradeLevel(rootUserInfo.getTradeLevel());
        futureUserInfo.setNickName("");
        futureUserInfo.setRemark("");
        futureUserInfo.setTrackSource(rootUserInfo.getTrackSource());
        futureUserInfo.setInsertTime(DateUtils.getNewDate());
        futureUserInfo.setUpdateTime(DateUtils.getNewDate());
        // 推荐人
        futureUserInfo.setAgentId(rootUserInfo.getAgentId());
        //future的推荐人id
        futureUserInfo.setFutureAgentId(refferFutureUserId);
        // 返佣开关关闭的话，无视推荐人
        futureUserInfo.setAccountType(UserTypeEnum.FUTURE.name());
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            futureUserInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId = userCommonValidateService.isValidateAgentId(rootUserInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            futureUserInfo.setAgentId(null);
        }
        if (futureUserInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            futureUserInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insert(futureUserInfo);
        return futureUserInfo;
    }


    /**
     * 创建交易账户（这个方法有一定的风险，当撮合不可用时，调用可能报错会导致激活失败，合理的做法是
     * 上层业务记录事务日志，判断业务执行到哪儿一步，然后走补偿job继续执行）
     *
     * @return
     */
    public Long createFutureTradingAccount(UserInfo futureUserInfo) {// 创建交易账号不回滚 防止重复创建
        try {
            Long futureTradingAccount = futureAccountApiClient.createAccount(futureUserInfo.getUserId(),futureUserInfo.getTradeLevel());
            log.info("createTradingAccount futureTradingAccount：{}", futureTradingAccount);
            if (futureTradingAccount != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(futureUserInfo.getUserId());
                userInfo.setMeTradingAccount(futureTradingAccount);
                this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
                FutureUserTradingAccount futureUserTradingAccount = new FutureUserTradingAccount();// 插入交易账户索引
                futureUserTradingAccount.setTradingAccount(futureTradingAccount);
                futureUserTradingAccount.setUserId(futureUserInfo.getUserId());
                log.info("createTradingAccount insert:{}", JSON.toJSONString(futureUserTradingAccount));
                this.futureUserTradingAccountMapper.insert(futureUserTradingAccount);// 交易账户索引 激活时创建交易账户
            }
            return futureTradingAccount;
        } catch (Exception e) {
            log.error(String.format("createFutureTradingAccount failed, userId:%s, exception:", futureUserInfo.getUserId()), e);
            throw new BusinessException("createFutureTradingAccount failed");
        }
    }

    /**
     * 创建期货交割合约撮合系统的交易账户
     * @param futureUserInfo
     * @return
     */
    public Long createFutureDeliveryTradingAccount(UserInfo futureUserInfo) {// 创建交易账号不回滚 防止重复创建
        try {
            Long futureDeliveryTradingAccount = futureDeliveryAccountApiClient.createAccount(futureUserInfo.getUserId(),futureUserInfo.getTradeLevel());
            log.info("createTradingAccount futureDeliveryTradingAccount：{}", futureDeliveryTradingAccount);
            if (futureDeliveryTradingAccount != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(futureUserInfo.getUserId());
                userInfo.setDeliveryTradingAccount(futureDeliveryTradingAccount);
                this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
                FutureUserDeliveryTradingAccount deliveryTradingAccount = new FutureUserDeliveryTradingAccount();// 插入交易账户索引
                deliveryTradingAccount.setDeliveryTradingAccount(futureDeliveryTradingAccount);
                deliveryTradingAccount.setUserId(futureUserInfo.getUserId());
                log.info("createTradingAccount insert:{}", JSON.toJSONString(deliveryTradingAccount));
                this.futureUserDeliveryTradingAccountMapper.insert(deliveryTradingAccount);// 交易账户索引 激活时创建交易账户
            }
            return futureDeliveryTradingAccount;
        } catch (Exception e) {
            log.error(String.format("createFutureDeliveryTradingAccount failed, userId:%s, exception:", futureUserInfo.getUserId()), e);
            throw new BusinessException("createFutureDeliveryTradingAccount failed");
        }
    }

    private void insertToAgentLog(Long parentUserId, Long currentUserId, String currentEmail) {
        UserAgentLog existParent = userAgentLogMapper.selectByReferralUserId(parentUserId);
        if (existParent == null) {
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

    /**
     * 判断当前用户的ip是否在国家黑名单
     */
    protected void checkCountryBackListByIp(Long userId) throws Exception {
        final String ip = WebUtils.getRequestIp();
        log.info("future checkCountryBackListByIp userId={},ip={}", userId, ip);
        if (org.apache.commons.lang3.StringUtils.isBlank(futureIpCountryBlackList)) {
            return;
        }
        String[] countryBlackArry = futureIpCountryBlackList.split(",");
        List<String> countryBlackList=Lists.newArrayList(countryBlackArry);
        String countryCode = null;
        try {
            countryCode = IP2LocationUtils.getCountryShort(ip);
        } catch (Exception e) {
            log.error("future checkCountryBackListByIp.getCountryShort error:", e);
            return;
        }
        if(countryBlackList.contains(countryCode)){
            throw new BusinessException(AccountErrorCode.FUTURE_IP_COUNTRY_NOT_SPPORT);
        }
    }

    /**
     * 判断当前用户的国家是否在国家黑名单
     */
    protected void checkKycCountryBackList(Long userId) throws Exception {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User rootUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == rootUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        //子账号不验证
        if(isSubUser){
            return;
        }
        UserKycCountryResponse userKycCountryResponse= kycApiTransferAdapter.getKycCountry(userId);
        String countryCode=null;
        if(null!=userKycCountryResponse && org.apache.commons.lang3.StringUtils.isNotBlank(userKycCountryResponse.getCountryCode())){
            countryCode=userKycCountryResponse.getCountryCode();
        }
        if(org.apache.commons.lang3.StringUtils.isBlank(countryCode)){
            checkCountryBackListByIp(userId);
            return;
        }
        if(org.apache.commons.lang3.StringUtils.isAnyBlank(countryCode,marginCountryBlackList)){
            return;
        }
        String[] countryBlackArry = marginCountryBlackList.split(",");
        List<String> countryBlackList=Lists.newArrayList(countryBlackArry);
        if(countryBlackList.contains(countryCode)){
            throw new BusinessException(AccountErrorCode.COUNTRY_KYC_NOT_SPPORT);
        }
        return ;
    }

    public String createFutureUserAgent(FutureUserAgentReq futureUserAgentReq){
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(futureUserAgentReq.getUserId());
        if (null == userInfo || userInfo.getFutureUserId() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User user = userCommonBusiness.checkAndGetUserById(futureUserAgentReq.getUserId());
        if (AccountTypeEnum.getAccountType(user.getStatus()) != 1  && AccountTypeEnum.getAccountType(user.getStatus()) != 6 && AccountTypeEnum.getAccountType(user.getStatus()) != 8){
            throw new BusinessException(AccountErrorCode.ACCOUNT_TYPE_AS_FUTURE_AGENT_CODE_ERROR);
        }
        if (!Pattern.matches(FUTURE_USER_AGEMTCODE_REGEX, futureUserAgentReq.getFutureAgentCode()) && !StringUtils.isNumeric(futureUserAgentReq.getFutureAgentCode())) {
            throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_ERROR);
        }
        //用户只能用自己的futureUserId作为agentCode
        if (StringUtils.isNumeric(futureUserAgentReq.getFutureAgentCode()) && userInfo.getFutureUserId() != Long.parseLong(futureUserAgentReq.getFutureAgentCode())){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        FutureUserAgent exist = futureUserAgentMapper.selectByAgentCode(futureUserAgentReq.getFutureAgentCode());
        if (exist != null){
            throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_EXIST);
        }
        FutureUserAgent futureUserAgent = new FutureUserAgent();
        futureUserAgent.setFutureUserId(userInfo.getFutureUserId());
        futureUserAgent.setAgentCode(futureUserAgentReq.getFutureAgentCode());
        futureUserAgent.setUserId(userInfo.getUserId());
        futureUserAgentMapper.insertSelective(futureUserAgent);
        return futureUserAgent.getAgentCode();
    }

    public FutureUserAgentResponse selectFutureUserAgent(CheckFutureAgentCodeExistReq checkFutureAgentCodeExistReq){
        if (StringUtils.isNotBlank(checkFutureAgentCodeExistReq.getFutureAgentCode()) && !StringUtils.isNumeric(checkFutureAgentCodeExistReq.getFutureAgentCode()) && !Pattern.matches(FUTURE_USER_AGEMTCODE_REGEX, checkFutureAgentCodeExistReq.getFutureAgentCode())) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        //用户只能用自己的futureUserId作为agentCode
        if (checkFutureAgentCodeExistReq.getFutureAgentCode() != null){
            return getFutureUserAgentRes(futureUserAgentMapper.selectByAgentCode(checkFutureAgentCodeExistReq.getFutureAgentCode()));
        }else if (checkFutureAgentCodeExistReq.getUserId() != null){
            FutureUserAgentResponse futureUserAgentRes = getFutureUserAgentRes(futureUserAgentMapper.selectByUserId(checkFutureAgentCodeExistReq.getUserId()));
            if (futureUserAgentRes == null){
                futureUserAgentRes = new FutureUserAgentResponse();
                futureUserAgentRes.setUserId(checkFutureAgentCodeExistReq.getUserId());
                UserInfo userInfo = userInfoMapper.selectByPrimaryKey(checkFutureAgentCodeExistReq.getUserId());
                if (userInfo != null && userInfo.getFutureUserId() != null){
                    futureUserAgentRes.setFutureUserId(userInfo.getFutureUserId());
                    futureUserAgentRes.setAgentCode(String.valueOf(userInfo.getFutureUserId()));
                }
            }
            return futureUserAgentRes;
        }else if (checkFutureAgentCodeExistReq.getFutureUserId() != null){
            return getFutureUserAgentRes(futureUserAgentMapper.selectByFutureUserId(checkFutureAgentCodeExistReq.getFutureUserId()));
        }
        return null;
    }

    @Override
    public void validateCretaeFutureAccount(FastCreateFutureAccountRequest request) throws Exception {
        log.info("validateCretaeFutureAccount email={}", request.getEmail());
        //判断用户的kyc国家是否在黑名单
        checkCountryBackListByIp(null);
        //检查期货推荐吗
        if (StringUtils.isNumeric(request.getFuturesReferalCode())){
            //存在亿分之一的概率，自己推荐自己
            UserInfo userInfo = userInfoMapper.selectRootUserInfoByFutureUserId(Long.parseLong(request.getFuturesReferalCode().trim()));
            if (userInfo == null || userInfo.getFutureUserId() == null){
                throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_IS_NOT_EXIST);
            }
            User user = userCommonBusiness.checkAndGetUserById(userInfo.getUserId());
            if (AccountTypeEnum.getAccountType(user.getStatus()) != 1  && AccountTypeEnum.getAccountType(user.getStatus()) != 6 && AccountTypeEnum.getAccountType(user.getStatus()) != 8){
                throw new BusinessException(AccountErrorCode.ACCOUNT_TYPE_AS_FUTURE_AGENT_CODE_ERROR);
            }
        }else if (StringUtils.isNotBlank(request.getFuturesReferalCode())){
            FutureUserAgent futureUserAgent = futureUserAgentMapper.selectByAgentCode(request.getFuturesReferalCode());
            if (futureUserAgent == null || futureUserAgent.getFutureUserId() == null){
                throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_IS_NOT_EXIST);
            }
        }

    }


    private FutureUserAgentResponse getFutureUserAgentRes(FutureUserAgent futureUserAgent){
        if (futureUserAgent == null){
            return null;
        }
        FutureUserAgentResponse response = new FutureUserAgentResponse();
        response.setAgentCode(futureUserAgent.getAgentCode());
        response.setFutureUserId(futureUserAgent.getFutureUserId());
        response.setUserId(futureUserAgent.getUserId());
        return response;
    }

    public Map<Long,String> selectFutureAgentCodes(List<Long> futureUserIds){
        log.info("UserFutureBusiness.selectFutureAgentCodes.param:{}",futureUserIds);
        if(CollectionUtils.isEmpty(futureUserIds) || futureUserIds.size() == 0  || futureUserIds.size() > 100){
            return Maps.newHashMap();
        }
        List<FutureUserAgent> futureUserAgents = futureUserAgentMapper.selectByFutureUserIds(futureUserIds);
        Map<Long,String> defaultValueMap = futureUserIds.stream().collect(Collectors.toMap(x->x, Object::toString, (k1, k2)->k2));
        if (CollectionUtils.isEmpty(futureUserAgents)){
            return defaultValueMap;
        }
        for (FutureUserAgent fua:futureUserAgents){
            if (defaultValueMap.containsKey(fua.getFutureUserId())){
                defaultValueMap.put(fua.getFutureUserId(),fua.getAgentCode());
            }
        }
        return defaultValueMap;
    }

    @Override
    public Boolean sendFutureCall(SendFutureCallRequest request) throws Exception {
        log.info("sendFutureCall start futureUserId={},info = {}", request.getFutureUserId(),request.getInfo());
        executorServiceForFutureCall.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadContext.put("traceId","sendFutureCall-"+ UUID.randomUUID().toString());
                    Long futureUserId=request.getFutureUserId();
                    UserInfo rootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(futureUserId);
                    if (null == rootUserInfo) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    //发送email
                    if(StringUtils.isNotBlank(request.getTemplateCodeEmail())) {
                        msgApiClient.sendFutureCall(rootUserInfo.getUserId(), request.getData(), request.getTemplateCodeEmail());
                    }
                    //发送短信
                    if(StringUtils.isNotBlank(request.getTemplateCodeSms())) {
                        sendCallSms(rootUserInfo.getUserId(), request.getData(), request.getTemplateCodeSms());
                    }
                    //发送站内信
                    if(StringUtils.isNotBlank(request.getTemplateCodeInbox())) {
                        sendCallInbox(rootUserInfo.getUserId(), request.getData(), request.getTemplateCodeInbox());
                    }
                } catch (Exception e) {
                    log.error("sendFutureCall exception", e);
                }finally {
                    TrackingUtils.removeTraceId();
                }
            }
        });
        return Boolean.TRUE;
    }

    @Override
    public APIResponse<CreateFutureUserResponse> createDeliveryAccountIfNotExist(APIRequest<IdRequest> request) throws Exception {
        final IdRequest requestBody = request.getBody();
        final Long rootUserId = requestBody.getUserId();
        log.info("createDeliveryAccountIfNotExist start, rootUserId={}", rootUserId);

        // 查询user信息
        final UserIndex rootUserIndex = this.userIndexMapper.selectByPrimaryKey(rootUserId);
        if (rootUserIndex == null || StringUtils.isBlank(rootUserIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final UserInfo rootUserInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
        if (rootUserInfo == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (rootUserInfo.getFutureUserId() == null) {
            log.error("createDeliveryAccountIfNotExist failed, user doesn't have futureUserId");
            throw new BusinessException("createDeliveryAccountIfNotExist failed, user doesn't have futureUserId");
        }

        Long futureUserId = rootUserInfo.getFutureUserId();
        final UserInfo futureUserInfo = this.userInfoMapper.selectByPrimaryKey(futureUserId);
        if (futureUserInfo == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        CreateFutureUserResponse createFutureUserResponse = new CreateFutureUserResponse();
        createFutureUserResponse.setRootUserId(rootUserId);
        createFutureUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createFutureUserResponse.setFutureUserId(futureUserId);
        createFutureUserResponse.setFutureTradingAccount(futureUserInfo.getMeTradingAccount());
        createFutureUserResponse.setFutureDeliveryTradingAccount(futureUserInfo.getDeliveryTradingAccount());

        // 如果DeliveryTradingAccount为空，且开关打开，则创建
        if (futureUserInfo.getDeliveryTradingAccount() == null && createDeliveryWhenCreateIfNotExist) {
            // 创建期货交割合约交易账户
            log.info("createDeliveryAccountIfNotExist createFutureDeliveryTradingAccount");
            Long futureDeliveryTradingAccount = createFutureDeliveryTradingAccount(futureUserInfo);
            // 同步通知delivery-service账户信息
            if (null != futureDeliveryTradingAccount) {
                deliveryCommissionApiClient.initUser(rootUserId, futureUserInfo.getUserId(), futureDeliveryTradingAccount,rootUserIndex.getEmail(), futureUserInfo.getFutureAgentId());
            }

            // 同步交割账户的apikey。将future mbx的apikey，同步到新交割mbx
            try {
                syncApiKeysFromFuture(futureUserInfo.getMeTradingAccount(), futureDeliveryTradingAccount);
            } catch (Exception e) {
                log.error("syncApiKeysFromFuture error", e);
            }

            createFutureUserResponse.setFutureDeliveryTradingAccount(futureDeliveryTradingAccount);
        }

        return APIResponse.getOKJsonResult(createFutureUserResponse);
    }

    private void syncApiKeysFromFuture(Long meTradingAccount, Long futureDeliveryTradingAccount) {
        log.info("syncApiKeysFromFuture start, meTradingAccount={} deliveryTradingAccount={}", meTradingAccount, futureDeliveryTradingAccount);
        if (meTradingAccount == null) {
            return;
        }
        List<ApiKeyResponse> futureApiKeys = futureAccountApiClient.getApiKeys(meTradingAccount);
        if (CollectionUtils.isEmpty(futureApiKeys)) {
            return;
        }

        // 拼装同步apiKey数据
        List<ApiKeySyncRequest> syncRequests = new ArrayList<>();
        for (ApiKeyResponse apiKey : futureApiKeys) {

            List<ApiKeyRule> syncApiKeyRules = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(apiKey.getRules())) {
                for (com.binance.memgmt.api.client.domain.apiKey.ApiKeyRule futureApiKeyRule : apiKey.getRules()) {
                    ApiKeyRule syncApiKeyRule = ApiKeyRule.builder()
                            .ip(futureApiKeyRule.getIp())
                            .time(futureApiKeyRule.getTime())
                            .build();
                    syncApiKeyRules.add(syncApiKeyRule);
                }
            }
            ApiKeySyncRequest syncRequest = ApiKeySyncRequest.builder()
                    .keyId(apiKey.getKeyId())
                    .accountId(futureDeliveryTradingAccount.intValue())
                    .keyType(apiKey.getApiKeyType())
                    .key(apiKey.getKey())
                    .secretKey(apiKey.getSecretKey())
                    .desc(apiKey.getDesc())
                    .rules(syncApiKeyRules)
                    .permissions(apiKey.getPermissions())
                    .build();

            syncRequests.add(syncRequest);
        }
        futureDeliveryAccountApiClient.syncApiKeyFromFutures(syncRequests);
    }

    /**
     *
     * @param rootUserId
     * @return 是否需要创建 deliveryAccount
     * @throws Exception
     */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean fixDeliveryAccount(Long rootUserId) throws Exception {
        if (stopFixDelivery) {
            log.info("fixDeliveryAccount stop fix, rootUserId={}", rootUserId);
            return Boolean.FALSE;
        }

        final UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(rootUserId);
        final Long futureUserId = rootUserInfo.getFutureUserId();
        if (futureUserId == null) {
            return Boolean.FALSE;
        }
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (futureUserInfo.getMeTradingAccount() == null || futureUserInfo.getDeliveryTradingAccount() != null) {
            return Boolean.FALSE;
        }

        // 用户上一次活跃，是否在1个月之内
        Boolean matchLastRequestCondition = true;
        UserOperationLog userOperationLog = userOperationLogMapper.queryUserLastLog(rootUserId);
        if (userOperationLog == null || userOperationLog.getRequestTime() == null || userOperationLog.getRequestTime().compareTo(DateUtils.addMonths(new Date(), -1)) < 0) {
            matchLastRequestCondition = false;
        }

        // 用户是否有future apikey
        Boolean matchApiKeyCondition = true;
        List<ApiKeyResponse> futureApiKeys = futureAccountApiClient.getApiKeys(futureUserInfo.getMeTradingAccount());
        if (CollectionUtils.isEmpty(futureApiKeys)) {
            matchApiKeyCondition = false;
        }


        // 有永续账号 && (30天内活跃过 || 有apikey)
        if (!matchLastRequestCondition && !matchApiKeyCondition) {
            log.info("fixDeliveryAccount lastLoginTime or apikey 不满足条件 rootUserId={}", rootUserId);
            return Boolean.FALSE;
        }

        // 开始创建delivery账号
        log.info("fixDeliveryAccount createFutureDeliveryTradingAccount rootUserId={} matchLastRequestCondition={} matchApiKeyCondition={}", rootUserId, matchLastRequestCondition, matchApiKeyCondition);
        final UserIndex rootUserIndex = userIndexMapper.selectByPrimaryKey(rootUserId);
        if (rootUserIndex == null) return Boolean.FALSE;
        Long futureDeliveryTradingAccount = createFutureDeliveryTradingAccount(futureUserInfo);
        // 同步通知delivery-service账户信息
        if (null != futureDeliveryTradingAccount) {
            deliveryCommissionApiClient.initUser(rootUserId, futureUserId, futureDeliveryTradingAccount, rootUserIndex.getEmail(), futureUserInfo.getFutureAgentId());
        }

        // 同步交割账户的apikey。将future mbx的apikey，同步到新交割mbx
        try {
            syncApiKeysFromFuture(futureUserInfo.getMeTradingAccount(), futureDeliveryTradingAccount);
        } catch (Exception e) {
            log.error("fixDeliveryAccount syncApiKeysFromFuture error", e);
        }
        log.info("fixDeliveryAccount success,rootUserId={}", rootUserId);
        return Boolean.TRUE;
    }

    /**
     * 发送短信信息
     * @param userId
     * @param tplCode
     */
    private void sendCallSms(Long userId ,Map params,String tplCode) {
        try {
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败. userId:{}");
            }
            String mobile = userSecurity.getMobile();
            String countryCode = userSecurity.getMobileCode();
            if (org.apache.commons.lang3.StringUtils.isAnyBlank(mobile, countryCode)) {
                log.info("用户没有绑定手机号码或者手机号码的国家码不存在不发送短信: userId:{}", userId);
                return;
            }
            Country country = iCountry.getCountryByCode(countryCode);
            String mobileCode = country != null ? country.getMobileCode() : null;
            if (org.apache.commons.lang3.StringUtils.isBlank(mobileCode)) {
                log.info("国家短信码获取失败，不需要发送短信通知: userId:{}", userId);
                return;
            }
            LanguageEnum languageEnum = LanguageEnum.EN_US;
            if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase("cn", userSecurity.getMobileCode())) {
                languageEnum = LanguageEnum.ZH_CN;
            }
            SendMsgRequest msgRequest = new SendMsgRequest();
            msgRequest.setTplCode(tplCode);
            msgRequest.setUserId(String.valueOf(userId));
            msgRequest.setMobileCode(mobileCode);
            msgRequest.setRecipient(mobile);
            msgRequest.setNeedIpCheck(false);
            msgRequest.setNeedSendTimesCheck(false);
            msgRequest.setData(params);
            log.info("发送短信信息: userId:{} smsTemplate:{} language:{}", userId, tplCode, languageEnum);
            userCommonBusiness.sendMsg(msgRequest, languageEnum, TerminalEnum.OTHER);
        }catch (Exception e) {
            log.error("短信信息发送出现异常: userId:{} smsTemplate:{}", userId, tplCode, e);
        }
    }

    /**
     * 发送站内信
     * @param userId
     * @param inboxTemplate
     */
    private void sendCallInbox(Long userId, Map params,String inboxTemplate) {
        try {
            User user = userCommonBusiness.checkAndGetUserById(userId);
            params.put("email", user.getEmail());
            APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(userId, params, WebUtils.getAPIRequestHeader().getLanguage().getLang(), "web",inboxTemplate);
            inboxMessageTextApi.pushInbox(apiRequest);
        }catch (Exception e) {
            log.error("站内信发送出现异常: userId:{} smsTemplate:{}", userId, inboxTemplate, e);
        }
    }

    public Boolean checkIfCanUseFutreAgentCode(Long userId)throws Exception{
        log.info("UserFutureBusiness.checkIfCanUseFutreAgentCode.userId:{}",userId);
        if (userId == null){
            return false;
        }
        User user = checkAndGetUserById(userId);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (user.getInsertTime().getTime() <= rebateStrategyChangeTime){
            return true;
        }else if (userInfo != null && Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode()).equals(userInfo.getAgentId())){
            return true;
        } else{
            return false;
        }
    }

    @Override
    public APIResponse<Void> syncApiKeyToDelivery(APIRequest<IdRequest> request) {
        final IdRequest requestBody = request.getBody();
        final Long rootUserId = requestBody.getUserId();
        log.info("syncApiKeyToDelivery start, rootUserId={}", rootUserId);

        final UserInfo rootUserInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
        if (rootUserInfo == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (rootUserInfo.getFutureUserId() == null) {
            log.error("syncApiKeyToDelivery failed, user doesn't have futureUserId");
            throw new BusinessException("syncApiKeyToDelivery failed, user doesn't have futureUserId");
        }

        Long futureUserId = rootUserInfo.getFutureUserId();
        final UserInfo futureUserInfo = this.userInfoMapper.selectByPrimaryKey(futureUserId);
        if (futureUserInfo == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (futureUserInfo.getMeTradingAccount() == null || futureUserInfo.getDeliveryTradingAccount() == null) {
            throw new BusinessException("future user meTradingAccount or deliveryTradingAccount is null");
        }

        syncApiKeysFromFuture(futureUserInfo.getMeTradingAccount(), futureUserInfo.getDeliveryTradingAccount());
        log.info("syncApiKeyToDelivery finish, rootUserId={}", rootUserId);
        return APIResponse.getOKJsonResult();
    }

}
