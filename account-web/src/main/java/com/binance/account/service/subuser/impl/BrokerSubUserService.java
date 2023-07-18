package com.binance.account.service.subuser.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.SecurityLog;
import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.agent.ApiAgentRewardConfig;
import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.account.data.entity.broker.ApiAgentUserAlias;
import com.binance.account.data.entity.broker.BrokerCommissionWhite;
import com.binance.account.data.entity.broker.BrokerUserCommisssion;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.agent.ApiAgentRewardConfigMapper;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.data.mapper.broker.ApiAgentUserAliasMapper;
import com.binance.account.data.mapper.broker.BrokerCommissionWhiteMapper;
import com.binance.account.data.mapper.broker.BrokerUserCommisssionMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.assetservice.TranApiClient;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.integration.futureengine.FutureDeliveryAccountApiClient;
import com.binance.account.integration.futureservice.DeliveryFutureTransferApiClient;
import com.binance.account.integration.futureservice.FutureTransferApiClient;
import com.binance.account.integration.futureservice.RiskApiClient;
import com.binance.account.integration.margin.MarginAccountApiClient;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.integration.report.ReportApiClient;
import com.binance.account.integration.streamer.StreamerOrderApiClient;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.service.apimanage.impl.ApiManageServiceImpl;
import com.binance.account.service.security.impl.UserSecurityBusiness;
import com.binance.account.service.subuser.IBrokerSubUserService;
import com.binance.account.service.user.IUserFuture;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.service.user.impl.UserInfoBusiness;
import com.binance.account.utils.MaskUtils;
import com.binance.account.vo.apimanage.request.DeleteApiKeyRequest;
import com.binance.account.vo.apimanage.request.GetApiListRequest;
import com.binance.account.vo.apimanage.request.SaveApiKeyRequest;
import com.binance.account.vo.apimanage.response.ApiModelResponse;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.request.CreateFutureAccountRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.subuser.BrokerSubbindingInfoVo;
import com.binance.account.vo.subuser.request.*;
import com.binance.account.vo.subuser.response.*;
import com.binance.account.vo.subuser.request.BrokerCommissionFuturesRequest;
import com.binance.account.vo.subuser.request.BrokerQueryCommissionFuturesRequest;
import com.binance.account.vo.subuser.request.BrokerSubAccountTransHistoryReq;
import com.binance.account.vo.subuser.request.BrokerSubAccountTransferRequest;
import com.binance.account.vo.subuser.request.BrokerUserCommissionReq;
import com.binance.account.vo.subuser.request.ChangeBrokerSubUserCommissionReq;
import com.binance.account.vo.subuser.request.CreateBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.CreateBrokerSubUserReq;
import com.binance.account.vo.subuser.request.CreateFuturesForBrokerSubUserReq;
import com.binance.account.vo.subuser.request.CreateMarginForBrokerSubUserReq;
import com.binance.account.vo.subuser.request.DeleteBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.QueryBrokerSubAccountReq;
import com.binance.account.vo.subuser.request.QueryBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.UpdateBrokerSubUserApiReq;
import com.binance.account.vo.subuser.request.UpdateBrokerTransferSwitchRequest;
import com.binance.account.vo.subuser.response.BrokerCommissionFuturesResponse;
import com.binance.account.vo.subuser.response.BrokerQueryCommissionFuturesResponse;
import com.binance.account.vo.subuser.response.BrokerSubAccountTranHisRes;
import com.binance.account.vo.subuser.response.BrokerSubAccountTransferResponse;
import com.binance.account.vo.subuser.response.BrokerUserCommissionRes;
import com.binance.account.vo.subuser.response.ChangeBrokerSubUserCommissionRes;
import com.binance.account.vo.subuser.response.CreateBrokerSubUserApiRes;
import com.binance.account.vo.subuser.response.CreateBrokerSubUserResp;
import com.binance.account.vo.subuser.response.CreateFuturesForBrokerSubUserResp;
import com.binance.account.vo.subuser.response.CreateMarginForBrokerSubUserResp;
import com.binance.account.vo.subuser.response.QueryBrokerSubAccountRes;
import com.binance.account.vo.subuser.response.QueryBrokerSubUserApiRes;
import com.binance.account.vo.subuser.response.UpdateBrokerTransferSwitchResponse;
import com.binance.account.vo.user.CreateFutureUserResponse;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.assetservice.enums.SubAccountTranHistoryEnum;
import com.binance.assetservice.vo.request.GetSubAccountTransferHistoryRequest;
import com.binance.assetservice.vo.response.AssetSubAccountTrasnferVo;
import com.binance.assetservice.vo.response.GetSubAccountTransferHistoryResponse;
import com.binance.assetservice.vo.response.UserAssetTransferBtcResponse;
import com.binance.future.api.request.AssetTransferRequest;
import com.binance.future.api.vo.AccountRiskVO;
import com.binance.margin.api.bookkeeper.response.AccountDetailResponse;
import com.binance.margin.api.bookkeeper.response.AccountSummaryResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.commons.SearchResult;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;
import com.binance.memgmt.api.client.domain.general.CommissionResponse;
import com.binance.memgmt.api.client.domain.general.FeeAdjustResponse;
import com.binance.memgmt.api.client.domain.general.NewSymbolResponse;
import com.binance.report.ReportErrorCode;
import com.binance.report.vo.asset.UserCommissionDetailResponse;
import com.binance.streamer.api.request.trade.CommissionType;
import com.binance.streamer.api.request.trade.GetAgentAndUserTradesRequest;
import com.binance.streamer.api.response.vo.GetAgentAndUserTradesVo;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by yangyang on 2019/8/19.
 */
@Log4j2
@Service
public class BrokerSubUserService extends CheckSubUserBusiness implements IBrokerSubUserService {
    private static final String REDIS_BROKER_SUB_USER_CREATE_KEY = "account:broker:subuser:create:";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    private static final Integer MAX_EMAIL_LENGTH = 200;
    protected static final String DEFAULT_RESULT = "lctwmv9fdld6yfdk06g";
    private static final String BORKE_SUB_API_NAME = "_BROKESUBAPI_";
    private static final String BROKER_FUTURE_TRANSFER_LIMIT="broker_future_transfer_limit";

    @Value("${sub.account.history.switch:false}")
    private Boolean subHistorySwitch;
    @Value("${broker.furure.transfer.limit:5000}")
    private Integer brokerFutureTransferLimit;
    @Resource
    protected UserIndexMapper userIndexMapper;
    @Resource
    protected UserMapper userMapper;
    @Resource
    protected UserInfoMapper userInfoMapper;
    @Resource
    protected ISysConfig iSysConfig;
    @Autowired
    private SubUserBindingMapper subUserBindingMapper;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private CheckSubUserBusiness checkSubUserBusiness;
    @Autowired
    private IApiManageService apiManageService;
    @Autowired
    private MatchboxApiClient matchboxApi;
    @Autowired
    private UserSecurityBusiness userSecurityBusiness;
    @Autowired
    private ApiModelMapper apiModelMapper;
    @Autowired
    private UserInfoBusiness userInfoBusiness;
    @Autowired
    private TranApiClient tranApiClient;
    @Autowired
    private BrokerUserCommisssionMapper brokerUserCommisssionMapper;
    @Autowired
    private UserBusiness userBusiness;
    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private UserTradingAccountMapper userTradingAccountMapper;
    @Autowired
    private IUserFuture userFuture;
    @Autowired
    private FutureAccountApiClient futureAccountApiClient;
    @Autowired
    private FutureDeliveryAccountApiClient futureDeliveryAccountApiClient;
    @Autowired
    private ApiAgentUserAliasMapper apiAgentUserAliasMapper;
    @Autowired
    private ReportApiClient reportApiClient;
    @Autowired
    private StreamerOrderApiClient streamerOrderApiClient;
    @Autowired
    private ApiAgentRewardConfigMapper apiAgentRewardConfigMapper;
    @Autowired
    private MarginAccountApiClient marginAccountApiClient;
    @Autowired
    private RiskApiClient riskApiClient;
    @Autowired
    private UserAssetApiClient userAssetApiClient;
    @Autowired
    private BrokerCommissionWhiteMapper brokerCommissionWhiteMapper;
    @Autowired
    private AccountApiClient accountApiClient;
    @Autowired
    private FutureTransferApiClient futureTransferApiClient;
    @Autowired
    private DeliveryFutureTransferApiClient deliveryFutureAssetTransfer;


    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
//    @UserPermissionValidate(userId = "#request.body.parentUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_BROKER_SUB_ACCOUNT)
    public APIResponse<CreateBrokerSubUserResp> createBrokerSubUser(APIRequest<CreateBrokerSubUserReq> request) throws Exception {

        final CreateBrokerSubUserReq requestBody = request.getBody();

        final Long parentUserId = requestBody.getParentUserId();
        final User parentUser = checkAndGetUserById(parentUserId);
        log.info("createBrokerSubUser start parentUserId:{}", parentUserId);
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        // 24小时最多可以创建50次子账户(不论成功失败)
        long tryToCreateCount = RedisCacheUtils.get(parentUserId.toString(), Long.class, REDIS_BROKER_SUB_USER_CREATE_KEY, 0L);
        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(parentUserId);
        if (brokerUserCommisssion == null){
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (tryToCreateCount <= brokerUserCommisssion.getDayMaxSubAccount()) {
            tryToCreateCount = RedisCacheUtils.increment(parentUserId.toString(), REDIS_BROKER_SUB_USER_CREATE_KEY, 1L, 24L, TimeUnit.HOURS);
        }
        if (tryToCreateCount > brokerUserCommisssion.getDayMaxSubAccount()) {
            //TODO 返回吗有问题
            throw new BusinessException(GeneralCode.BROKER_SUB_USER_MAX_TOTAL, new Object[] {brokerUserCommisssion.getMaxSubAccount()});
        }
        long subUserCount = subUserBindingMapper.countSubUsersByParentUserId(parentUserId);
        if (subUserCount > brokerUserCommisssion.getMaxSubAccount()) {
            throw new BusinessException(GeneralCode.BROKER_SUB_USER_MAX_TOTAL, new Object[] {brokerUserCommisssion.getMaxSubAccount()});
        }

        final Long agentId;
        final UserInfo parentUserInfo = this.userInfoMapper.selectByPrimaryKey(parentUserId);
        if (null == parentUserInfo || null == parentUserInfo.getAgentId()) {
            log.warn("parentUserInfo is null, parentUserId:{}", parentUserId);
            agentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
        } else {
            agentId = parentUserInfo.getAgentId();
        }

        // 创建子账号
        User brokerSubUser = ((BrokerSubUserService) AopContext.currentProxy()).createBrokeSubUser(parentUserId, parentUser.getEmail(), true);
        User user = new User();
        user.setEmail(brokerSubUser.getEmail());

        // 绑定主账号、子账号关系
        SubUserBinding subUserBinding= ((BrokerSubUserService) AopContext.currentProxy()).createParentSubUserBindingForBroker(parentUserId, brokerSubUser.getUserId(), requestBody.getRemark());

        // 创建子账号Security信息
        ((BrokerSubUserService) AopContext.currentProxy()).createUserSecurity(brokerSubUser.getUserId(), brokerSubUser.getEmail());

        // 创建broker子账号用户信息
        ((BrokerSubUserService) AopContext.currentProxy()).createBrokerSubUserInfo(parentUserId, brokerSubUser.getUserId(), agentId, null);//todo 注册渠道

        // 添加设备信息、IP信息、日志信息
        ((BrokerSubUserService) AopContext.currentProxy()).addDeviceInfoAndLogs(brokerSubUser.getUserId(), brokerSubUser.getEmail(), request.getTerminal(), null);

        // 默认禁用app交易
        user.setStatus(brokerSubUser.getStatus() | Constant.USER_ACTIVE| Constant.USER_FEE);
        this.userMapper.updateByEmail(user);
        UserInfo brokerSubUserInfo = this.userInfoMapper.selectByPrimaryKey(brokerSubUser.getUserId());
        // 创建margin交易账户
        //这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long brokerTradingAccount = matchboxApiClient.postAccount(brokerSubUserInfo, MatchBoxAccountTypeEnum.SPOT);
        brokerSubUserInfo.setTradingAccount(brokerTradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(brokerSubUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(brokerTradingAccount);
        userTradingAccount.setUserId(brokerSubUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        log.info("createBrokerSubUser.postAccount insert:{}", JSON.toJSONString(userTradingAccount));
        // 发送用户注册MQ消息至PNK同步数据
        userBusiness.sendRegisterMqMsgForMargin(brokerSubUser, brokerSubUserInfo);
        // 构建ResponseBody
        CreateBrokerSubUserResp response = new CreateBrokerSubUserResp();
        response.setSubaccountId(subUserBinding.getBrokerSubAccountId().toString());
        log.info("createBrokerSubUser done parentUserId:{}, brokerSubAccountId:{}", parentUserId, subUserBinding.getBrokerSubAccountId());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<CreateMarginForBrokerSubUserResp> createMarginForBrokerSubUser(APIRequest<CreateMarginForBrokerSubUserReq> request) throws Exception {
        CreateMarginForBrokerSubUserReq req=request.getBody();
        if(!req.getEnableMargin().booleanValue()){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }

        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        //资管子账户不可
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //组装请求参数
        APIRequest<CreateMarginAccountRequest> originRequest = new APIRequest<CreateMarginAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        CreateMarginAccountRequest createMarginAccountRequest=new CreateMarginAccountRequest();
        createMarginAccountRequest.setUserId(subUserBinding.getSubUserId());
        createMarginAccountRequest.setParentUserId(subUserBinding.getParentUserId());
        APIResponse<CreateMarginUserResponse> apiResponse=userBusiness.createMarginAccount(APIRequest.instance(originRequest, createMarginAccountRequest));
        CreateMarginForBrokerSubUserResp resp=new CreateMarginForBrokerSubUserResp();
        resp.setSubaccountId(req.getSubAccountId().toString());
        resp.setEnableMargin(apiResponse.getStatus() == APIResponse.Status.OK);
        resp.setUpdateTime(new Date().getTime());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<CreateFuturesForBrokerSubUserResp> createFuturesForBrokerSubUser(APIRequest<CreateFuturesForBrokerSubUserReq> request) throws Exception {
        CreateFuturesForBrokerSubUserReq req=request.getBody();
        if(!req.getEnableFutures().booleanValue()){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }

        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        //资管子账户不可
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //组装请求参数
        APIRequest<CreateFutureAccountRequest> originRequest = new APIRequest<CreateFutureAccountRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        CreateFutureAccountRequest createFutureAccountRequest=new CreateFutureAccountRequest();
        createFutureAccountRequest.setUserId(subUserBinding.getSubUserId());
        createFutureAccountRequest.setParentUserId(subUserBinding.getParentUserId());
        APIResponse<CreateFutureUserResponse> apiResponse=userFuture.createFutureAccount(APIRequest.instance(originRequest, createFutureAccountRequest));
        CreateFuturesForBrokerSubUserResp resp=new CreateFuturesForBrokerSubUserResp();
        resp.setSubaccountId(req.getSubAccountId().toString());
        resp.setEnableFutures(apiResponse.getStatus() == APIResponse.Status.OK);
        resp.setUpdateTime(new Date().getTime());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<CreateBrokerSubUserApiRes> createBrokerSubUserApi(APIRequest<CreateBrokerSubUserApiReq> request) throws Exception {
        CreateBrokerSubUserApiReq req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        User subUser=checkAndGetUserById(subUserBinding.getSubUserId());
        //资管子账户
        if (checkAssetSubUser(subUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 2.创建api-key-有赋权操作-可修改
        Long ruleId=1L;//默认值，默认就是开启现货
        if(null != req.getMarginTrade() && req.getMarginTrade().booleanValue()){
            if (!BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_EXIST_MARGIN_ACCOUNT)) {
                throw new BusinessException(AccountErrorCode.PLEASE_OPEN_MARGIN_ACCOUNT_FIRST);
            }
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.MARGIN);
        }
        if(null != req.getFuturesTrade() && req.getFuturesTrade().booleanValue()){
            if (!BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_EXIST_FUTURE_ACCOUNT)) {
                throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
            }
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.FUTURE_TRADE);
        }
        SaveApiKeyRequest saveApiKeyRequest = new SaveApiKeyRequest();
        saveApiKeyRequest.setApiName(getApiName(req));
        saveApiKeyRequest.setBackend(true);
        saveApiKeyRequest.setRuleId(ruleId.toString());
        saveApiKeyRequest.setLoginUserId(String.valueOf(req.getParentUserId()));
        saveApiKeyRequest.setTargetUserId(String.valueOf(subUserBinding.getSubUserId()));
        ApiModelResponse apiModelResponse = apiManageService.saveApiKey(saveApiKeyRequest);
        if (apiModelResponse == null) {
            throw new BusinessException(AccountErrorCode.CREATE_BROKER_SUB_ACCOUNT_API_ERROR);
        }
        // 3.enable-apikey,适配canTrade重新调用一次
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 更新现货权限
        if (req.getCanTrade()) {
            this.matchboxApi.putApiKeyPermissions(String.valueOf(userInfo.getTradingAccount()), "true", "true", "true", "true",
                    "true", String.valueOf(apiModelResponse.getKeyId()), "false");
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.TRADE);
        } else {
            this.matchboxApi.putApiKeyPermissions(String.valueOf(userInfo.getTradingAccount()), "true", "true", "false", "true",
                    "true", String.valueOf(apiModelResponse.getKeyId()), "false");
            ruleId=BitUtils.disable(ruleId, ApiManageServiceImpl.ApiManagerUtils.TRADE);
        }

        ApiModelResponse queryApiByUserAndApiKey =
                apiManageService.queryApiByUserAndApiKey(subUserBinding.getSubUserId().toString(), apiModelResponse.getApiKey());
        if (queryApiByUserAndApiKey == null) {
            throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
        }
        ApiModel param = new ApiModel();
        param.setId(Long.parseLong(queryApiByUserAndApiKey.getId()));
        param.setRuleId(ruleId.toString());
        apiModelMapper.updateRuleIdByPrimaryKey(param);
        //更新期货是否可以交易的权限
        try{
            if(null != req.getFuturesTrade() && req.getFuturesTrade().booleanValue() && null!=userInfo.getFutureUserId() ){
                Long futureUserId=userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if(!createTimeAfterCreateFuture(apiModelResponse,futureUserInfo)){
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }
                if (ApiManageServiceImpl.ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                    log.info("broker sync permissions userid={},futureuserid={},trade=true",userInfo.getUserId(),userInfo.getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()),false, true, true, true,
                            true, true);
                } else {
                    log.info("broker sync permissions userid={},futureuserid={},trade=false",userInfo.getUserId(),userInfo.getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()),false, false, true, true,
                            true, true);
                }
            }
        }catch (Exception e){
            log.warn("createBrokerSubUserApi:", e);
        }

        //更新期货交割合约是否可以交易的权限
        try{
            if(null != req.getFuturesTrade() && req.getFuturesTrade().booleanValue() && null!=userInfo.getFutureUserId() ){
                Long futureUserId=userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("broker sync delivery permissions fail, futureUserInfo or deliveryTradingAccount is null");
                } else {
                    if (!createTimeAfterCreateFuture(apiModelResponse, futureUserInfo)) {
                        throw new BusinessException(GeneralCode.SYS_VALID);
                    }
                    if (ApiManageServiceImpl.ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                        log.info("broker sync delivery permissions userid={},futureuserid={},trade=true", userInfo.getUserId(), userInfo.getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()), false, true, true, true,
                                true, true);
                    } else {
                        log.info("broker sync delivery permissions userid={},futureuserid={},trade=false", userInfo.getUserId(), userInfo.getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()), false, false, true, true,
                                true, true);
                    }
                }
            }
        }catch (Exception e){
            log.warn("createBrokerSubUserApi: broker sync delivery permissions error", e);
        }
        CreateBrokerSubUserApiRes res = new CreateBrokerSubUserApiRes();
        res.setSubaccountId(req.getSubAccountId().toString());
        res.setCanTrade(isTradeEnabled(ruleId));
        res.setMarginTrade(isMarginEnabled(ruleId));
        res.setFuturesTrade(isFuturesEnabled(ruleId));
        res.setApiKey(apiModelResponse.getApiKey());
        res.setSecretKey(apiModelResponse.getSecretKey());
        return APIResponse.getOKJsonResult(res);
    }

    public boolean createTimeAfterCreateFuture(ApiModelResponse apiManageModel,UserInfo futureUserInfo){
        if(null==apiManageModel.getCreateTime()){
            return false;
        }
        Long apiCreateTime=apiManageModel.getCreateTime().getTime();
        Long futureUserCreateTime=futureUserInfo.getInsertTime().getTime();
        if(apiCreateTime.longValue()>futureUserCreateTime.longValue()){
            return true;
        }
        return false;
    }

    @Override
    public void deleteBrokerSubApiKey(APIRequest<DeleteBrokerSubUserApiReq> request) throws Exception {
        DeleteBrokerSubUserApiReq req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());

        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        //资管子账户不可
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        ApiModelResponse apiModelResponse=  apiManageService.queryApiByUserAndApiKey(String.valueOf(subUserBinding.getSubUserId()),req.getSubAccountApiKey());
        if(null==apiModelResponse){
            throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
        }
        // 2.调用apiManage删除
        DeleteApiKeyRequest deleteApiKeyRequest = new DeleteApiKeyRequest();
        deleteApiKeyRequest.setId(Long.valueOf(apiModelResponse.getId()));
        deleteApiKeyRequest.setApiKey(apiModelResponse.getApiKey());
        deleteApiKeyRequest.setLoginUid(apiModelResponse.getUserId());
        apiManageService.deleteApiKey(deleteApiKeyRequest);

    }

    @Override
    public APIResponse<List<QueryBrokerSubUserApiRes>> queryBrokerSubApiKey(APIRequest<QueryBrokerSubUserApiReq> request) throws Exception {
        QueryBrokerSubUserApiReq req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(org.apache.commons.lang3.StringUtils.isBlank(req.getSubAccountApiKey())){
            // 2.调用apiManage
            GetApiListRequest getApiListRequest=new GetApiListRequest();
            getApiListRequest.setUserId(subUserBinding.getSubUserId().toString());
            List<ApiModelResponse> response = this.apiManageService.getApiList(getApiListRequest);
            List<QueryBrokerSubUserApiRes> queryBrokerSubUserApiResList=Lists.newArrayList();
            for(ApiModelResponse apiModelResponse:response){
                QueryBrokerSubUserApiRes queryBrokerSubUserApiRes=new QueryBrokerSubUserApiRes();
                queryBrokerSubUserApiRes.setApiKey(apiModelResponse.getApiKey());
                queryBrokerSubUserApiRes.setSubaccountId(req.getSubAccountId().toString());
                queryBrokerSubUserApiRes.setCanTrade(isTradeEnabled(Long.parseLong(apiModelResponse.getRuleId())));
                queryBrokerSubUserApiRes.setMarginTrade(isMarginEnabled(Long.parseLong(apiModelResponse.getRuleId())));
                queryBrokerSubUserApiRes.setFuturesTrade(isFuturesEnabled(Long.parseLong(apiModelResponse.getRuleId())));
                queryBrokerSubUserApiResList.add(queryBrokerSubUserApiRes);
            }
            return APIResponse.getOKJsonResult(queryBrokerSubUserApiResList);
        }else{

            // 2.调用apiManage
            ApiModelResponse apiModelResponse =
                    apiManageService.queryApiByUserAndApiKey(subUserBinding.getSubUserId().toString(), req.getSubAccountApiKey());
            if (apiModelResponse == null) {
                throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
            }
            QueryBrokerSubUserApiRes res = new QueryBrokerSubUserApiRes();
            res.setApiKey(req.getSubAccountApiKey());
            res.setSubaccountId(req.getSubAccountId().toString());
            res.setCanTrade(isTradeEnabled(Long.parseLong(apiModelResponse.getRuleId())));
            res.setMarginTrade(isMarginEnabled(Long.parseLong(apiModelResponse.getRuleId())));
            res.setFuturesTrade(isFuturesEnabled(Long.parseLong(apiModelResponse.getRuleId())));
            return APIResponse.getOKJsonResult(Lists.newArrayList(res));
        }

    }

    @Override
    public APIResponse<CreateBrokerSubUserApiRes> updateBrokerSubApiPermission(APIRequest<UpdateBrokerSubUserApiReq> request) throws Exception {
        UpdateBrokerSubUserApiReq req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        // 2.调用mbx更新权限
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User subUser=checkAndGetUserById(userInfo.getUserId());
        //资管子账户不可
        if (com.binance.account.util.BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_ASSET_SUBUSER)){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 更新权限
        ApiModelResponse apiModelResponse =
                apiManageService.queryApiByUserAndApiKey(String.valueOf(subUserBinding.getSubUserId()), req.getSubAccountApiKey());
        if (apiModelResponse == null) {
            throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
        }
        // 是否可以交易权限
        ApiModel param = new ApiModel();
        param.setId(Long.parseLong(apiModelResponse.getId()));
        Long ruleId = Long.parseLong(apiModelResponse.getRuleId());

        if (req.getCanTrade()) {
            this.matchboxApi.putApiKeyPermissions(String.valueOf(userInfo.getTradingAccount()), "true", "true", "true", "true",
                    "true", String.valueOf(apiModelResponse.getKeyId()), "false");
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.TRADE);
        } else {
            this.matchboxApi.putApiKeyPermissions(String.valueOf(userInfo.getTradingAccount()), "true", "true", "false", "true",
                    "true", String.valueOf(apiModelResponse.getKeyId()), "false");
            ruleId=BitUtils.disable(ruleId, ApiManageServiceImpl.ApiManagerUtils.TRADE);
        }

        if (req.getMarginTrade()) {
            if (!BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_EXIST_MARGIN_ACCOUNT)) {
                throw new BusinessException(AccountErrorCode.PLEASE_OPEN_MARGIN_ACCOUNT_FIRST);
            }
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.MARGIN);
        } else {
            ruleId=BitUtils.disable(ruleId, ApiManageServiceImpl.ApiManagerUtils.MARGIN);
        }

        if (req.getFuturesTrade()) {
            if (!BitUtils.isEnable(subUser.getStatus(), Constant.USER_IS_EXIST_FUTURE_ACCOUNT)) {
                throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
            }
            ruleId=BitUtils.enable(ruleId, ApiManageServiceImpl.ApiManagerUtils.FUTURE_TRADE);
        } else {
            ruleId=BitUtils.disable(ruleId, ApiManageServiceImpl.ApiManagerUtils.FUTURE_TRADE);
        }
        //更新期货是否可以交易的权限
        try{
            if(null!=userInfo.getFutureUserId() ){
                Long futureUserId=userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if (ApiManageServiceImpl.ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                    log.info("broker sync permissions userid={},futureuserid={},trade=true",userInfo.getUserId(),userInfo.getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()),false, true, true, true,
                            true, true);
                } else {
                    log.info("broker sync permissions userid={},futureuserid={},trade=false",userInfo.getUserId(),userInfo.getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()),false, false, true, true,
                            true, true);
                }
            }
        }catch (Exception e){
            log.warn("broker futureAccountApiClient.updateApiKeyPermissions:", e);
        }

        //更新期货交割合约是否可以交易的权限
        try{
            if(null!=userInfo.getFutureUserId() ){
                Long futureUserId=userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("broker futureDeliveryAccountApiClient.updateApiKeyPermissions fail, futureUserInfo or deliveryTradingAccount is null");
                } else {
                    if (ApiManageServiceImpl.ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                        log.info("broker sync delivery permissions userid={},futureuserid={},trade=true", userInfo.getUserId(), userInfo.getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()), false, true, true, true,
                                true, true);
                    } else {
                        log.info("broker sync delivery permissions userid={},futureuserid={},trade=false", userInfo.getUserId(), userInfo.getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiModelResponse.getKeyId()), false, false, true, true,
                                true, true);
                    }
                }
            }
        }catch (Exception e){
            log.warn("broker futureDeliveryAccountApiClient.updateApiKeyPermissions:", e);
        }
        param.setRuleId(ruleId.toString());
        apiModelMapper.updateRuleIdByPrimaryKey(param);

        CreateBrokerSubUserApiRes res = new CreateBrokerSubUserApiRes();
        res.setApiKey(req.getSubAccountApiKey());
        res.setSubaccountId(req.getSubAccountId().toString());
        res.setCanTrade(isTradeEnabled(ruleId));
        res.setMarginTrade(isMarginEnabled(ruleId));
        res.setFuturesTrade(isFuturesEnabled(ruleId));
        return APIResponse.getOKJsonResult(res);
    }

    @Override
    public APIResponse<List<QueryBrokerSubAccountRes>> queryBrokerSubAccount(APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        QueryBrokerSubAccountReq req = request.getBody();
        User parentUser = checkAndGetUserById(req.getParentUserId());
        // 1.验证broke父子账号关系
        if(Objects.nonNull(req.getSubAccountId())){
            assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        }else{
            assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        }
        Integer page = req.getPage();
        Integer size = req.getSize();
        if (page == null || page <= 0){
            page = 1;
        }
        if (size == null || size <= 0 || size > 500){
            size = 500;
        }
        // 2.查询母账号下的子账号
        List<Long> subUserIds = Lists.newArrayList();
        List<SubUserBinding> subUserBindingList = Lists.newArrayList();
        if (req.getSubAccountId() == null) {
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByPage(req.getParentUserId(),(page-1)*size,size);
            if (CollectionUtils.isEmpty(subUserBindings)) {
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            subUserBindingList.addAll(subUserBindings);
            subUserIds.addAll(subUserBindings.stream().map(SubUserBinding::getSubUserId).collect(Collectors.toList()));
        } else {
            SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
            subUserBindingList.add(subUserBinding);
            subUserIds.add(subUserBinding.getSubUserId());
        }
        List<UserInfo> userInfoList = userInfoMapper.selectUserInfoList(subUserIds);
        if (CollectionUtils.isEmpty(userInfoList)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<Long> marginUserIdList=Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(userInfoList)) {
           for(UserInfo userInfo:userInfoList){
               if(null!=userInfo.getMarginUserId()){
                   marginUserIdList.add(userInfo.getMarginUserId());
               }
           }
        }
        List<UserInfo> marginUserInfoList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(marginUserIdList)) {
            marginUserInfoList= userInfoMapper.selectUserInfoList(marginUserIdList);
        }
        Map<Long, UserInfo> marginUserIdMap=Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(marginUserInfoList)) {
            marginUserIdMap= Maps.uniqueIndex(marginUserInfoList, new Function<UserInfo, Long>() {
                @Override
                public Long apply(@Nullable UserInfo userInfo) {
                    return userInfo.getUserId();
                }
            });
        }
        
        // 查询user_index,获取email
        List<UserIndex> userIndexList = userIndexMapper.selectByUserIds(subUserIds);
        Map<Long, UserIndex> userIndexMap = userIndexList.stream().collect(Collectors.toMap(UserIndex::getUserId, a -> a, (k1, k2) -> k1));

        Map<Long, UserInfo> userIdMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, a -> a, (k1, k2) -> k1));
        List<QueryBrokerSubAccountRes> result = new ArrayList<>(userInfoList.size());
        for (SubUserBinding subUserBinding : subUserBindingList) {
            if (userIdMap != null && userIdMap.containsKey(subUserBinding.getSubUserId())) {
                QueryBrokerSubAccountRes res = new QueryBrokerSubAccountRes();
                UserInfo userInfo = userIdMap.get(subUserBinding.getSubUserId());
                res.setCreateTime(userInfo.getInsertTime().getTime());
                res.setMakerCommission(userInfo.getMakerCommission() == null ? "0" : userInfo.getMakerCommission().toPlainString());
                res.setSubaccountId(subUserBinding.getBrokerSubAccountId().toString());
                res.setTakerCommission(userInfo.getTakerCommission() == null ? "0" : userInfo.getTakerCommission().toPlainString());
                if (null != userInfo.getMarginUserId() && null != marginUserIdMap.get(userInfo.getMarginUserId())) {
                    UserInfo marginUserInfo = marginUserIdMap.get(userInfo.getMarginUserId());
                    res.setMarginMakerCommission(marginUserInfo.getMakerCommission() == null ? "0" : marginUserInfo.getMakerCommission().toPlainString());
                    res.setMarginTakerCommission(marginUserInfo.getTakerCommission() == null ? "0" : marginUserInfo.getTakerCommission().toPlainString());
                } else {
                    res.setMarginMakerCommission("-1");
                    res.setMarginTakerCommission("-1");
                }
                
                UserIndex userIndex = userIndexMap.get(subUserBinding.getSubUserId());
                if (userIndex != null) {
                    res.setEmail(userIndex.getEmail());    
                }
                result.add(res);
            }
        }
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<ChangeBrokerSubUserCommissionRes> changeBrokerSubuserCommission(APIRequest<ChangeBrokerSubUserCommissionReq> request)
            throws Exception {
        ChangeBrokerSubUserCommissionReq req = request.getBody();
        validateBrokerSpotAndMarginUserCommisssionRequest(req);
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());

        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(req.getParentUserId());
        if (brokerUserCommisssion == null){
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }

        if (req.getTakerCommission().doubleValue()>brokerUserCommisssion.getMaxTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE,new Object[] { "takerCommission",brokerUserCommisssion.getMinTakerCommiss().toPlainString(), brokerUserCommisssion.getMaxTakerCommiss().toPlainString()});
        }
        if (req.getMakerCommission().doubleValue()>brokerUserCommisssion.getMaxMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE,new Object[] { "makerCommission",brokerUserCommisssion.getMinMakerCommiss().toPlainString(), brokerUserCommisssion.getMaxMakerCommiss().toPlainString()});
        }
        if (req.getTakerCommission().doubleValue()<brokerUserCommisssion.getMinTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE,new Object[] { "takerCommission",brokerUserCommisssion.getMinTakerCommiss().toPlainString(), brokerUserCommisssion.getMaxTakerCommiss().toPlainString()});
        }
        if (req.getMakerCommission().doubleValue()<brokerUserCommisssion.getMinTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE,new Object[] { "makerCommission",brokerUserCommisssion.getMinMakerCommiss().toPlainString(), brokerUserCommisssion.getMaxMakerCommiss().toPlainString()});
        }
        //检查margin的费率设置
        if (null != req.getMarginTakerCommission() && req.getMarginTakerCommission().doubleValue() > brokerUserCommisssion.getMaxTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (null != req.getMarginMakerCommission() && req.getMarginMakerCommission().doubleValue() > brokerUserCommisssion.getMaxMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (null != req.getMarginTakerCommission() && req.getMarginTakerCommission().doubleValue() < brokerUserCommisssion.getMinTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (null != req.getMarginMakerCommission() && req.getMarginMakerCommission().doubleValue() < brokerUserCommisssion.getMinTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }



        SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        //资管子账户
        if (checkAssetSubUser(checkAndGetUserById(subUserBinding.getSubUserId()).getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 2.调用mbx更新权限
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        int result = userInfoBusiness.setCommission(userInfo.getUserId(), userInfo.getBuyerCommission(), userInfo.getSellerCommission(),
                req.getTakerCommission(), req.getMakerCommission(), null, null);
        if (result <= 0) {
            log.warn("setCommission done, userId:{}, result:{}", req.getSubAccountId(), result);
        }

        // 记录broker日志
        addBrokerCommissionChangeLog(userInfo.getUserId(), request);
        ChangeBrokerSubUserCommissionRes res = new ChangeBrokerSubUserCommissionRes();
        //更新margin
        User subUser=checkAndGetUserById(userInfo.getUserId());
        UserStatusEx userStatusEx=new UserStatusEx(subUser.getStatus());
        UserInfo marginUserInfo = null;
        if (userStatusEx.getIsExistMarginAccount()){
            marginUserInfo = userInfoMapper.selectByPrimaryKey(userInfo.getMarginUserId());
        }
        if (null != req.getMarginMakerCommission() && null != req.getMarginTakerCommission()) {
            //如果没有开通那么报错
            if (!userStatusEx.getIsExistMarginAccount().booleanValue()) {
                throw new BusinessException(AccountErrorCode.PLEASE_OPEN_MARGIN_ACCOUNT_FIRST);
            }else{
                if (marginUserInfo == null || marginUserInfo.getTradingAccount() == null) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                int marginResult = userInfoBusiness.setCommission(marginUserInfo.getUserId(), marginUserInfo.getBuyerCommission(), marginUserInfo.getSellerCommission(),
                        req.getMarginTakerCommission(), req.getMarginMakerCommission(), null, null);
                if (marginResult <= 0) {
                    log.warn("broker margin setCommission done, userId:{}, result:{}", req.getSubAccountId(), result);
                }
                // 记录margin日志
                addBrokerCommissionChangeLog(marginUserInfo.getUserId(), request);
            }
        }


        res.setSubAccountId(req.getSubAccountId().toString());
        res.setMakerCommission(req.getMakerCommission());
        res.setTakerCommission(req.getTakerCommission());
        if (null != req.getMarginMakerCommission() && null != req.getMarginTakerCommission()){
            res.setMarginMakerCommission(req.getMarginMakerCommission());
            res.setMarginTakerCommission(req.getMarginTakerCommission());
        }else if (marginUserInfo != null){
            res.setMarginMakerCommission(marginUserInfo.getMakerCommission());
            res.setMarginTakerCommission(marginUserInfo.getTakerCommission());
        }
        return APIResponse.getOKJsonResult(res);
    }

    private void validateBrokerSpotAndMarginUserCommisssionRequest(ChangeBrokerSubUserCommissionReq request) {
        //白名单不检测
        BrokerCommissionWhite brokerCommissionWhite = brokerCommissionWhiteMapper.selectByUserId(request.getParentUserId());
        if(brokerCommissionWhite != null){
            return;
        }
        //spot taker
        if (request.getMakerCommission() != null && (request.getMakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0 || request.getMakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MAKER_COMMISSION_ERROR);
        }

        if (request.getTakerCommission() != null && (request.getTakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0 || request.getTakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MAKER_COMMISSION_ERROR);
        }
        //spot maker
        if (request.getMarginMakerCommission() != null && (request.getMarginMakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0 || request.getMarginMakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MARGIN_MAKER_COMMISSION_ERROR);
        }
        if (request.getMarginTakerCommission() != null && (request.getMarginTakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MIN) < 0 || request.getMarginTakerCommission().compareTo(BrokerSubUserAdminService.BROKER_COMMISSION_MAX) > 0)){
            throw new BusinessException(AccountErrorCode.BROKER_MARGIN_TAKER_COMMISSION_ERROR);
        }
    }

    @Override
    public APIResponse<BrokerUserCommissionRes> queryBrokerUserCommission(APIRequest<BrokerUserCommissionReq> request) throws Exception {
        BrokerUserCommissionReq req = request.getBody();
        // 1.验证broke父子账号关系
        User user = userCommonBusiness.checkAndGetUserById(req.getParentUserId());
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(req.getParentUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        BrokerUserCommissionRes res = new BrokerUserCommissionRes();
        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(req.getParentUserId());
        if (brokerUserCommisssion == null){
            return APIResponse.getOKJsonResult(res);
        }
        res.setMaxMakerCommission(brokerUserCommisssion.getMaxMakerCommiss());
        res.setMinMakerCommission(brokerUserCommisssion.getMinMakerCommiss());
        res.setMaxTakerCommission(brokerUserCommisssion.getMaxTakerCommiss());
        res.setMinTakerCommission(brokerUserCommisssion.getMinTakerCommiss());
        res.setMaxSubAccountQty(brokerUserCommisssion.getMaxSubAccount());
        res.setSubAccountQty(subUserBindingMapper.countSubUsersByParentUserId(req.getParentUserId()));
        return APIResponse.getOKJsonResult(res);
    }

    @Override
    public APIResponse<BrokerSubAccountTransferResponse> subAccountTransfer(APIRequest<BrokerSubAccountTransferRequest> request) throws Exception {
        BrokerSubAccountTransferRequest requestBody = request.getBody();
        requestBody.setAmount(requestBody.getAmount().setScale(8,BigDecimal.ROUND_DOWN));
        log.info("brokerSubAccountTransfer.formatRequest={}",JsonUtils.toJsonNotNullKey(requestBody));
        Long parentUserId = requestBody.getParentUserId();// 母账号userId
        Long senderUserId = null;// 转出方的邮箱
        if(null==requestBody.getFromId()){
            senderUserId= requestBody.getParentUserId();
        }else{
            SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,requestBody.getFromId());
            if(null==subUserBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            senderUserId=subUserBinding.getSubUserId();
        }
        Long recipientUserId = null;// 转入方的邮箱
        if(null==requestBody.getToId()){
            recipientUserId= requestBody.getParentUserId();
        }else{
            SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,requestBody.getToId());
            if(null==subUserBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            recipientUserId=subUserBinding.getSubUserId();
        }
        User senderUser = checkAndGetUserById(senderUserId);
        User recipientUser = checkAndGetUserById(recipientUserId);
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(senderUser.getStatus()) || checkAssetSubUser(recipientUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //判断是否禁止划转
        UserStatusEx senderUserStatus=new UserStatusEx(senderUser.getStatus());
        UserStatusEx recipientUserStatus=new UserStatusEx(recipientUser.getStatus());
        if(senderUserStatus.getIsForbiddenBrokerTrasnfer()|| recipientUserStatus.getIsForbiddenBrokerTrasnfer()){
            throw new BusinessException(AccountErrorCode.FORBIDDEN_BROKER_TRANSFER);
        }
        String asset = requestBody.getAsset();// 资产名字
        BigDecimal amount = requestBody.getAmount();// 划转数量
        // 1 判断是否是母账户
        checkSubUserBusiness.assertIsParentUser(parentUserId);
        // 2 转出方和转入方的userid
        checkSubUserBusiness.validateSenderAndRecipientUserList(senderUser.getEmail(), recipientUser.getEmail());
        // 3 验证转账参数之间关系的合法性
        checkSubUserBusiness.validateSubAccountTransfer(parentUserId, senderUserId, recipientUserId, asset, amount,true);
        // 4 开始正式进行转账操作
        Long transactionId =
                checkSubUserBusiness.internalTransfer(parentUserId,senderUserId, senderUser.getEmail(), recipientUserId, recipientUser.getEmail(), asset, amount,requestBody.getThirdTranId());
        BrokerSubAccountTransferResponse resp = new BrokerSubAccountTransferResponse();
        resp.setTxnId(transactionId);
        resp.setClientTranId(requestBody.getThirdTranId());
        return APIResponse.getOKJsonResult(resp);

    }

    @Override
    public APIResponse<List<BrokerSubAccountTranHisRes>> brokerSubAccountTransferHistory(APIRequest<BrokerSubAccountTransHistoryReq> request)
            throws Exception {
        BrokerSubAccountTransHistoryReq req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        GetSubAccountTransferHistoryRequest getSubAccountTransferHistoryRequest = new GetSubAccountTransferHistoryRequest();
        getSubAccountTransferHistoryRequest.setUserIds(Lists.newArrayList(subUserBinding.getSubUserId()));
        if(null!=req.getEndTime()){
            getSubAccountTransferHistoryRequest.setEndCreateTime(new Date(req.getEndTime()));
        }
        if(null!=req.getStartTime()){
            getSubAccountTransferHistoryRequest.setStartCreateTime(new Date(req.getStartTime()));
        }
        if(null!=req.getPage() && null!=req.getLimit()){
            getSubAccountTransferHistoryRequest.setOffset((req.getPage() - 1) * req.getLimit());// start
            getSubAccountTransferHistoryRequest.setLimit(req.getLimit());
        }
        getSubAccountTransferHistoryRequest.setThirdTranId(req.getThirdTranId());
        getSubAccountTransferHistoryRequest.setTranStaus(SubAccountTranHistoryEnum.DONE);
            //填入，则默认查询parentUserId
        getSubAccountTransferHistoryRequest.setParentUserId(req.getParentUserId());
        getSubAccountTransferHistoryRequest.setSubUserId(subUserBinding.getSubUserId());
        GetSubAccountTransferHistoryResponse transferHistoryResponse = tranApiClient.getAccountTransferHistory(getSubAccountTransferHistoryRequest);
        List<AssetSubAccountTrasnferVo> accountTransferHistoryList = transferHistoryResponse.getAssetSubAccountTrasnferVoList();
        if (CollectionUtils.isEmpty(accountTransferHistoryList)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<BrokerSubAccountTranHisRes> result = new ArrayList<>(accountTransferHistoryList.size());
        List<SubUserBinding> subUserBindingList= subUserBindingMapper.getSubUserBindingsByParentUserId(req.getParentUserId());
        Map<Long,SubUserBinding> subUserBindingMap=Maps.uniqueIndex(subUserBindingList, new Function<SubUserBinding, Long>() {
            @Override
            public Long apply(@Nullable SubUserBinding subUserBinding) {
                return subUserBinding.getSubUserId();
            }
        });
        for (AssetSubAccountTrasnferVo vo : accountTransferHistoryList) {
            BrokerSubAccountTranHisRes res = new BrokerSubAccountTranHisRes();
            res.setAsset(vo.getAsset());
            Long senderUserId=Long.valueOf(vo.getSenderUserId());
            Long recipientUserId=Long.valueOf(vo.getRecipientUserId());
            if(req.getParentUserId().longValue()!=senderUserId.longValue()){
                if(!subUserBindingMap.containsKey(senderUserId)){
                    throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
                }
                res.setFromId(subUserBindingMap.get(senderUserId).getBrokerSubAccountId().toString());
            }
            if(req.getParentUserId().longValue()!=recipientUserId.longValue()){
                if(!subUserBindingMap.containsKey(recipientUserId)){
                    throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
                }
                res.setToId(subUserBindingMap.get(recipientUserId).getBrokerSubAccountId().toString());
            }
            res.setQty(vo.getAmount().toPlainString());
            res.setTime(vo.getCreateTime().getTime());
            res.setTxnId(vo.getTransactionId());
            res.setClientTranId(vo.getThirdTranId());
            result.add(res);
        }
        return APIResponse.getOKJsonResult(result);

    }

    @Override
    public APIResponse<BrokerCommissionFuturesResponse> commissionFutures(APIRequest<BrokerCommissionFuturesRequest> request) throws Exception {
        BrokerCommissionFuturesRequest req = request.getBody();
        validateBrokerFutureUserCommisssionRequest(req);
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());


        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(req.getParentUserId());
        if (brokerUserCommisssion == null){
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (null!=brokerUserCommisssion.getMaxFuturesTakerCommiss()&& req.getTakerAdjustment().doubleValue()>brokerUserCommisssion.getMaxFuturesTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_GREATER_THAN,new Object[] { "futuresTakerCommission", brokerUserCommisssion.getMaxFuturesTakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMaxFuturesMakerCommiss()&& req.getMakerAdjustment().doubleValue()>brokerUserCommisssion.getMaxFuturesMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_GREATER_THAN,new Object[] { "futuresMakerCommission",brokerUserCommisssion.getMaxFuturesMakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMinFuturesTakerCommiss()&& req.getTakerAdjustment().doubleValue()<brokerUserCommisssion.getMinFuturesTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "futuresTakerCommission",brokerUserCommisssion.getMinFuturesTakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMinFuturesMakerCommiss()&& req.getMakerAdjustment().doubleValue()<brokerUserCommisssion.getMinFuturesMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "futuresMakerCommission",brokerUserCommisssion.getMinFuturesMakerCommiss().toPlainString()});
        }

        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(checkAndGetUserById(subUserBinding.getSubUserId()).getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        if(req.getMakerAdjustment().intValue()<-60 && null==brokerUserCommisssion.getMinFuturesMakerCommiss()){
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "futuresMakerCommission",-60});
        }
        if(req.getTakerAdjustment().intValue()<-50 && null==brokerUserCommisssion.getMinFuturesTakerCommiss()){
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "futuresTakerCommission",-50});
        }
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if(null==userInfo.getFutureUserId() ){
            throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
        }
        Long futureUserId=userInfo.getFutureUserId();
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long futureTradingAccount=futureUserInfo.getMeTradingAccount();
        FeeAdjustResponse feeAdjustResponse= futureAccountApiClient.feeAdjust(futureTradingAccount,req.getSymbol(),req.getMakerAdjustment(),req.getTakerAdjustment());
        if(null==feeAdjustResponse){
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        BrokerCommissionFuturesResponse brokerCommissionFuturesResponse=new BrokerCommissionFuturesResponse();
        brokerCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
        brokerCommissionFuturesResponse.setSymbol(req.getSymbol());
        brokerCommissionFuturesResponse.setMakerAdjustment(feeAdjustResponse.getMakerCommissionAdjust());
        brokerCommissionFuturesResponse.setTakerAdjustment(feeAdjustResponse.getTakerCommissionAdjust());
        brokerCommissionFuturesResponse.setMakerCommission(feeAdjustResponse.getMakerCommission());
        brokerCommissionFuturesResponse.setTakerCommission(feeAdjustResponse.getTakerCommission());
        return APIResponse.getOKJsonResult(brokerCommissionFuturesResponse);

    }


    @Override
    public APIResponse<UpdateBrokerCommissionDeliveryResponse> commissionDeliveryFutures(APIRequest<UpdateBrokerCommissionDeliveryRequest> request)throws Exception {
        UpdateBrokerCommissionDeliveryRequest req = request.getBody();
        validateBrokerDeliveryFutureUserCommisssionRequest(req);
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());


        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(req.getParentUserId());
        if (brokerUserCommisssion == null){
            throw new BusinessException(AccountErrorCode.QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR);
        }
        if (null!=brokerUserCommisssion.getMaxDeliveryTakerCommiss()&& req.getTakerAdjustment().doubleValue()>brokerUserCommisssion.getMaxDeliveryTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_GREATER_THAN,new Object[] { "deliveryfuturesTakerCommission", brokerUserCommisssion.getMaxDeliveryTakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMaxDeliveryMakerCommiss()&& req.getMakerAdjustment().doubleValue()>brokerUserCommisssion.getMaxDeliveryMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_GREATER_THAN,new Object[] { "deliveryfuturesMakerCommission",brokerUserCommisssion.getMaxDeliveryMakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMinDeliveryTakerCommiss()&& req.getTakerAdjustment().doubleValue()<brokerUserCommisssion.getMinDeliveryTakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "deliveryfuturesTakerCommission",brokerUserCommisssion.getMinDeliveryTakerCommiss().toPlainString()});
        }
        if (null!=brokerUserCommisssion.getMinDeliveryMakerCommiss()&& req.getMakerAdjustment().doubleValue()<brokerUserCommisssion.getMinDeliveryMakerCommiss().doubleValue()) {
            throw new BusinessException(AccountErrorCode.BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN,new Object[] { "deliveryfuturesMakerCommission",brokerUserCommisssion.getMinDeliveryMakerCommiss().toPlainString()});
        }

        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(checkAndGetUserById(subUserBinding.getSubUserId()).getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if(null==userInfo.getFutureUserId() ){
            throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
        }
        Long futureUserId=userInfo.getFutureUserId();
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long deliveryTradingAccount=futureUserInfo.getDeliveryTradingAccount();
        com.binance.delivery.memgmt.api.client.domain.general.FeeAdjustResponse feeAdjustResponse= futureDeliveryAccountApiClient.feeAdjust(deliveryTradingAccount,req.getPair(),req.getMakerAdjustment(),req.getTakerAdjustment());
        if(null==feeAdjustResponse){
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        UpdateBrokerCommissionDeliveryResponse brokerCommissionFuturesResponse=new UpdateBrokerCommissionDeliveryResponse();
        brokerCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
        brokerCommissionFuturesResponse.setPair(req.getPair());
        brokerCommissionFuturesResponse.setMakerAdjustment(feeAdjustResponse.getMakerCommissionAdjust());
        brokerCommissionFuturesResponse.setTakerAdjustment(feeAdjustResponse.getTakerCommissionAdjust());
        brokerCommissionFuturesResponse.setMakerCommission(feeAdjustResponse.getMakerCommission());
        brokerCommissionFuturesResponse.setTakerCommission(feeAdjustResponse.getTakerCommission());
        return APIResponse.getOKJsonResult(brokerCommissionFuturesResponse);

    }

    private void validateBrokerFutureUserCommisssionRequest(BrokerCommissionFuturesRequest request) {
        //白名单不检测
        BrokerCommissionWhite brokerCommissionWhite = brokerCommissionWhiteMapper.selectByUserId(request.getParentUserId());
        if(brokerCommissionWhite != null){
            return;
        }
        //future taker
        if (request.getTakerAdjustment() != null && (request.getTakerAdjustment() < BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN.intValue() || request.getTakerAdjustment() > BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_TAKER.intValue())){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_TAKER_COMMISSION_ERROR);
        }
        //future maker
        if (request.getMakerAdjustment() != null && (request.getMakerAdjustment() < BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MIN.intValue() || request.getMakerAdjustment() > BrokerSubUserAdminService.BROKER_FUTURE_COMMISSION_MAX_MAKER.intValue())){
            throw new BusinessException(AccountErrorCode.BROKER_FUTURES_MAKER_COMMISSION_ERROR);
        }
    }

    private void validateBrokerDeliveryFutureUserCommisssionRequest(UpdateBrokerCommissionDeliveryRequest request) {
        //白名单不检测
        BrokerCommissionWhite brokerCommissionWhite = brokerCommissionWhiteMapper.selectByUserId(request.getParentUserId());
        if(brokerCommissionWhite != null){
            return;
        }
        //delivery future taker
        if (request.getTakerAdjustment() != null && (request.getTakerAdjustment() < BrokerSubUserAdminService.BROKER_DELIVERY_COMMISSION_MIN.intValue() || request.getTakerAdjustment() > BrokerSubUserAdminService.BROKER_DELIVERY_COMMISSION_MAX_TAKER.intValue())){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_TAKER_COMMISSION_ERROR);
        }
        //delivery future maker
        if (request.getMakerAdjustment() != null && (request.getMakerAdjustment() < BrokerSubUserAdminService.BROKER_DELIVERY_COMMISSION_MIN.intValue() || request.getMakerAdjustment() > BrokerSubUserAdminService.BROKER_DELIVERY_COMMISSION_MAX_MAKER.intValue())){
            throw new BusinessException(AccountErrorCode.BROKER_DELIVERY_MAKER_COMMISSION_ERROR);
        }
    }

    @Override
    public APIResponse<List<BrokerQueryCommissionFuturesResponse>> queryCommissionFutures(APIRequest<BrokerQueryCommissionFuturesRequest> request) throws Exception {
        BrokerQueryCommissionFuturesRequest req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if(null==userInfo.getFutureUserId() ){
            throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
        }
        Long futureUserId=userInfo.getFutureUserId();
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long futureTradingAccount=futureUserInfo.getMeTradingAccount();
        List<BrokerQueryCommissionFuturesResponse> brokerQueryCommissionFuturesResponseList=Lists.newArrayList();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(req.getSymbol())){
            CommissionResponse commissionResponse= futureAccountApiClient.getCommission(req.getSymbol(),futureTradingAccount);
            if(null!=commissionResponse){
                BrokerQueryCommissionFuturesResponse brokerQueryCommissionFuturesResponse=new BrokerQueryCommissionFuturesResponse();
                brokerQueryCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
                brokerQueryCommissionFuturesResponse.setSymbol(req.getSymbol());
                brokerQueryCommissionFuturesResponse.setMakerAdjustment(commissionResponse.getMakerCommissionAdjust());
                brokerQueryCommissionFuturesResponse.setTakerAdjustment(commissionResponse.getTakerCommissionAdjust());
                brokerQueryCommissionFuturesResponse.setMakerCommission(commissionResponse.getMakerCommission());
                brokerQueryCommissionFuturesResponse.setTakerCommission(commissionResponse.getTakerCommission());
                brokerQueryCommissionFuturesResponseList.add(brokerQueryCommissionFuturesResponse);
            }
            return APIResponse.getOKJsonResult(brokerQueryCommissionFuturesResponseList);
        }
        List<CommissionResponse> commissionResponseList= futureAccountApiClient.getCommissionByFutureAccountId(futureTradingAccount,true);
        Map<Integer,NewSymbolResponse> newSymbolResponseMap= futureAccountApiClient.getAllSymbolMap();
        for(CommissionResponse commissionResponse:commissionResponseList){
            BrokerQueryCommissionFuturesResponse brokerQueryCommissionFuturesResponse=new BrokerQueryCommissionFuturesResponse();
            brokerQueryCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
            NewSymbolResponse newSymbolResponse=newSymbolResponseMap.get(commissionResponse.getSymbolId());
            brokerQueryCommissionFuturesResponse.setSymbol(newSymbolResponse.getSymbol());
            brokerQueryCommissionFuturesResponse.setMakerAdjustment(commissionResponse.getMakerCommissionAdjust());
            brokerQueryCommissionFuturesResponse.setTakerAdjustment(commissionResponse.getTakerCommissionAdjust());
            brokerQueryCommissionFuturesResponse.setMakerCommission(commissionResponse.getMakerCommission());
            brokerQueryCommissionFuturesResponse.setTakerCommission(commissionResponse.getTakerCommission());
            brokerQueryCommissionFuturesResponseList.add(brokerQueryCommissionFuturesResponse);
        }
        return APIResponse.getOKJsonResult(brokerQueryCommissionFuturesResponseList);
    }

    @Override
    public APIResponse<List<BrokerQueryCommissionDeliveryFuturesResponse>> queryCommissionDeliveryFutures(APIRequest<BrokerQueryCommissionDeliveryFuturesRequest> request) throws Exception {
        BrokerQueryCommissionDeliveryFuturesRequest req = request.getBody();
        // 1.验证broke父子账号关系
        assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(subUserBinding.getSubUserId());
        if (userInfo == null || userInfo.getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if(null==userInfo.getFutureUserId() ){
            throw new BusinessException(AccountErrorCode.PLEASE_OPEN_FUTURES_ACCOUNT_FIRST);
        }
        Long futureUserId=userInfo.getFutureUserId();
        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
        if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long deliveryTradingAccount=futureUserInfo.getDeliveryTradingAccount();
        List<BrokerQueryCommissionDeliveryFuturesResponse> brokerQueryCommissionFuturesResponseList=Lists.newArrayList();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(req.getPair())){
            com.binance.delivery.memgmt.api.client.domain.general.CommissionResponse commissionResponse= futureDeliveryAccountApiClient.getCommission(req.getPair(),deliveryTradingAccount);
            if(null!=commissionResponse){
                BrokerQueryCommissionDeliveryFuturesResponse brokerQueryCommissionFuturesResponse=new BrokerQueryCommissionDeliveryFuturesResponse();
                brokerQueryCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
                brokerQueryCommissionFuturesResponse.setSymbol(req.getPair());
                brokerQueryCommissionFuturesResponse.setMakerAdjustment(commissionResponse.getMakerCommissionAdjust());
                brokerQueryCommissionFuturesResponse.setTakerAdjustment(commissionResponse.getTakerCommissionAdjust());
                brokerQueryCommissionFuturesResponse.setMakerCommission(commissionResponse.getMakerCommission());
                brokerQueryCommissionFuturesResponse.setTakerCommission(commissionResponse.getTakerCommission());
                brokerQueryCommissionFuturesResponseList.add(brokerQueryCommissionFuturesResponse);
            }
            return APIResponse.getOKJsonResult(brokerQueryCommissionFuturesResponseList);
        }
        List<com.binance.delivery.memgmt.api.client.domain.general.CommissionResponse> commissionResponseList= futureDeliveryAccountApiClient.getCommissionByFutureAccountId(deliveryTradingAccount,false);
        Map<Integer, com.binance.delivery.memgmt.api.client.domain.general.NewSymbolResponse> newSymbolResponseMap= futureDeliveryAccountApiClient.getAllSymbolMap();
        for(com.binance.delivery.memgmt.api.client.domain.general.CommissionResponse commissionResponse:commissionResponseList){
            BrokerQueryCommissionDeliveryFuturesResponse brokerQueryCommissionFuturesResponse=new BrokerQueryCommissionDeliveryFuturesResponse();
            brokerQueryCommissionFuturesResponse.setSubAccountId(req.getSubAccountId());
            com.binance.delivery.memgmt.api.client.domain.general.NewSymbolResponse newSymbolResponse=newSymbolResponseMap.get(commissionResponse.getSymbolId());
            brokerQueryCommissionFuturesResponse.setSymbol(newSymbolResponse.getBaseSymbol());
            brokerQueryCommissionFuturesResponse.setMakerAdjustment(commissionResponse.getMakerCommissionAdjust());
            brokerQueryCommissionFuturesResponse.setTakerAdjustment(commissionResponse.getTakerCommissionAdjust());
            brokerQueryCommissionFuturesResponse.setMakerCommission(commissionResponse.getMakerCommission());
            brokerQueryCommissionFuturesResponse.setTakerCommission(commissionResponse.getTakerCommission());
            brokerQueryCommissionFuturesResponseList.add(brokerQueryCommissionFuturesResponse);
        }
        if(CollectionUtils.isEmpty(brokerQueryCommissionFuturesResponseList)){
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        Map<String, BrokerQueryCommissionDeliveryFuturesResponse> symbolMap = brokerQueryCommissionFuturesResponseList.stream().collect(Collectors.toMap(BrokerQueryCommissionDeliveryFuturesResponse::getSymbol, item -> item, (k1, k2) -> k2));
        return APIResponse.getOKJsonResult(Lists.newArrayList(symbolMap.values()));
    }


    @Override
    public APIResponse<UpdateBrokerTransferSwitchResponse> updateBrokerTransferSwitch(APIRequest<UpdateBrokerTransferSwitchRequest> request) throws Exception {
        final UpdateBrokerTransferSwitchRequest requestBody = request.getBody();
        log.info("updateBrokerTransferSwitch:request={}",JsonUtils.toJsonHasNullKey(requestBody));
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User record = new User();
        record.setEmail(user.getEmail());
        if(requestBody.getNeedForbiddenBrokerTransfer()){
            record.setStatus(BitUtils.enable(user.getStatus(),AccountCommonConstant.FORBIDDEN_BROKER_TRANSFER));
        }else{
            record.setStatus(BitUtils.disable(user.getStatus(),AccountCommonConstant.FORBIDDEN_BROKER_TRANSFER));
        }
        int result=this.userMapper.updateByEmail(record);
        log.info("updateBrokerTransferSwitch:result={}",result);
        return APIResponse.getOKJsonResult(new UpdateBrokerTransferSwitchResponse());
    }




    @SecurityLog(name = "开启BNB燃烧", operateType = Constant.SECURITY_OPERATE_TYPE_OPEN_BNB_FEE,
            userId = "#request.body.subAccountId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<BrokerBnbBurnSwitchResp> updateBrokerSubBNBBurnSwitch(APIRequest<BrokerBnbBurnSwitchRequest> request)throws Exception{
        log.info("BrokerSubUserService.updateBrokerSubBNBBurnSwitch.param:{}",JsonUtils.toJsonHasNullKey(request));
        BrokerBnbBurnSwitchRequest body = request.getBody();
        // 1.验证broke父子账号关系
        User parentUser = checkAndGetUserById(body.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(body.getParentUserId(),body.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        //当前false，需要修改为true--可以合并但是业务代码不好看
        if (BitUtils.isFalse(subUser.getStatus(), Constant.USER_FEE) && body.getSpotBNBBurn()) {
            accountApiClient.setGas(subUser.getUserId().toString(),true);
            final User user = new User();
            user.setEmail(subUser.getEmail());
            user.setStatus(BitUtils.enable(subUser.getStatus(), Constant.USER_FEE));
            this.userMapper.updateByEmailSelective(user);
            if (userCommonBusiness.isExistMarginAccount(subUserBinding.getSubUserId())) {
                userSecurityBusiness.setMarginBnbFee(subUserBinding.getSubUserId(),true);
            }
        }else if (BitUtils.isTrue(subUser.getStatus(), Constant.USER_FEE) && !body.getSpotBNBBurn()){
            //当前true，需要修改为false
            accountApiClient.setGas(subUser.getUserId().toString(),false);
            final User user = new User();
            user.setEmail(subUser.getEmail());
            user.setStatus(BitUtils.disable(subUser.getStatus(), Constant.USER_FEE));
            this.userMapper.updateByEmailSelective(user);
            if (userCommonBusiness.isExistMarginAccount(subUserBinding.getSubUserId())) {
                userSecurityBusiness.setMarginBnbFee(subUserBinding.getSubUserId(),false);
            }
        }
        BrokerBnbBurnSwitchResp brokerBnbBurnSwitchResp = new BrokerBnbBurnSwitchResp();
        brokerBnbBurnSwitchResp.setSpotBNBBurn(body.getSpotBNBBurn());
        brokerBnbBurnSwitchResp.setSubAccountId(body.getSubAccountId());
        return APIResponse.getOKJsonResult(brokerBnbBurnSwitchResp);

    }

    @SecurityLog(name = "开启broker-sub手续费BNB抵扣", operateType = AccountCommonConstant.SECURITY_OPERATE_TYPE_UPDATE_MARGIN_INTEREST_BNB,
            userId = "#request.body.subAccountId")
    @Override
    public APIResponse<BrokerSubMarginInterestBnbBurnSwitchResp> updateBrokerSubMarginInterestBNBBurnSwitch(APIRequest<BrokerSubMarginInterestBnbBurnSwitchRequest> request)throws Exception{
        log.info("BrokerSubUserService.updateBrokerSubMarginInterestBNBBurnSwitch.param:{}",JsonUtils.toJsonHasNullKey(request));
        BrokerSubMarginInterestBnbBurnSwitchRequest body = request.getBody();
        // 1.验证broke父子账号关系
        User parentUser = checkAndGetUserById(body.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(body.getParentUserId(),body.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        UserInfo subUserInfo = this.userInfoMapper.selectByPrimaryKey(subUser.getUserId());
        if (subUserInfo == null || subUserInfo.getMarginUserId() == null){
            throw new BusinessException(AccountErrorCode.MARGIN_ACCOUNT_IS_NOT_EXIST);
        }
        marginAccountApiClient.enableOrdisableMarignInterest(subUserInfo.getUserId(),body.getInterestBNBBurn());
        BrokerSubMarginInterestBnbBurnSwitchResp resp = new BrokerSubMarginInterestBnbBurnSwitchResp();
        resp.setInterestBNBBurn(body.getInterestBNBBurn());
        resp.setSubAccountId(body.getSubAccountId());
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<SelectBrokerSubBnbBurnStatusResp> selectBrokerSubBnbBurnStatus(APIRequest<SelectBrokerSubBnbBurnStatusRequest> request)throws Exception{
        log.info("BrokerSubUserService.selectBrokerSubBnbBurnStatus.param:{}",JsonUtils.toJsonHasNullKey(request));
        SelectBrokerSubBnbBurnStatusRequest body = request.getBody();
        // 1.验证broke父子账号关系
        User parentUser = checkAndGetUserById(body.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(body.getParentUserId(),body.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        UserInfo subUserInfo = this.userInfoMapper.selectByPrimaryKey(subUser.getUserId());
        SelectBrokerSubBnbBurnStatusResp resp = new SelectBrokerSubBnbBurnStatusResp();
        resp.setSubAccountId(body.getSubAccountId());
        resp.setSpotBNBBurn(BitUtils.isTrue(subUser.getStatus(), Constant.USER_FEE));
        if (subUserInfo != null && subUserInfo.getMarginUserId() != null) {
            AccountDetailResponse accountDetailResponse = marginAccountApiClient.selecteMarignAccountDetail(subUserInfo.getUserId());
            resp.setInterestBNBBurn(accountDetailResponse.getIsBnbDiscount());
        }
        return APIResponse.getOKJsonResult(resp);

    }

    private static String getApiName(CreateBrokerSubUserApiReq req) {
        return req.getParentUserId() + BORKE_SUB_API_NAME + req.getSubAccountId();
    }

    public static boolean isTradeEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, ApiManageServiceImpl.ApiManagerUtils.TRADE);
    }

    public static boolean isMarginEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, ApiManageServiceImpl.ApiManagerUtils.MARGIN);
    }

    public static boolean isFuturesEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, ApiManageServiceImpl.ApiManagerUtils.FUTURE_TRADE);
    }

    /**
     * 创建用户
     *
     * @param parentUserId
     * @param parentEmail
     * @param isBrokerSubUser
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createBrokeSubUser(final Long parentUserId, final String parentEmail, final boolean isBrokerSubUser) throws NoSuchAlgorithmException {
        UserIndex userIndex = userCommonBusiness.getBrokerUserIndexForRegister(parentUserId,parentEmail);
        User user = userCommonBusiness.warpBrokerSubUser(userIndex);
        // 若为子账号，则关闭子账号功能、标记成子账号
        if (isBrokerSubUser) {
            Long status = user.getStatus();
            status = BitUtils.disable(status,Constant.USER_LOGIN);
            status = BitUtils.disable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
            status = BitUtils.enable(BitUtils.enable(status, Constant.USER_IS_SUBUSER),
                    Constant.USER_IS_SUB_USER_ENABLED);
            status = BitUtils.enable(status, Constant.USER_GOOGLE);
            status = BitUtils.disable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED);
            status = BitUtils.enable(BitUtils.enable(status, Constant.USER_IS_BROKER_SUBUSER), Constant.USER_IS_BROKER_SUB_USER_ENABLED);
            user.setStatus(status);
        }
        userMapper.insert(user);
        return user;
    }




    protected void checkUserStatus(Long status, Long constants) {// todo
        if (!BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {

            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED);
        }
    }

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

    /**
     * 主账号绑定子账号
     *
     * @param parentUserId
     * @param subUserId
     * @param remark
     */
    protected int createParentSubUserBinding(final Long parentUserId, final Long subUserId, final String remark) {
        SubUserBinding subUserBinding = new SubUserBinding();
        subUserBinding.setParentUserId(parentUserId);
        subUserBinding.setSubUserId(subUserId);
        subUserBinding.setRemark(StringUtils.defaultString(remark));
        return subUserBindingMapper.insert(subUserBinding);
    }


    /**
     * 主账号绑定子账号(经销商专用)
     *
     * @param parentUserId
     * @param subUserId
     * @param remark
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected SubUserBinding createParentSubUserBindingForBroker(final Long parentUserId, final Long subUserId, final String remark) {
        SubUserBinding subUserBinding = new SubUserBinding();
        subUserBinding.setParentUserId(parentUserId);
        subUserBinding.setSubUserId(subUserId);
        subUserBinding.setRemark(StringUtils.defaultString(remark));
        subUserBinding.setBrokerSubAccountId(keyGenerator.generateKey().longValue());
        int result= subUserBindingMapper.insert(subUserBinding);
        return subUserBinding;
    }

    /**
     * 记录broker子账账户api修改费率
     */
    private void addBrokerCommissionChangeLog(Long userId, APIRequest<ChangeBrokerSubUserCommissionReq> request) {
        // 添加操作日志
        try {
            final String ip = WebUtils.getRequestIp();
            String locationCity = IP2LocationUtils.getCountryCity(ip);
            String clientType = "";
            if (request.getTerminal() != null) {
                clientType = request.getTerminal().getCode();
            }
            ChangeBrokerSubUserCommissionReq req = request.getBody();
            String desc = "Broker修改费率";
            Map<String, BigDecimal> commissionMap = new HashMap<>();
            if (req.getMakerCommission() != null) {
                commissionMap.put("make",req.getMakerCommission());
            }
            if (req.getTakerCommission() != null) {
                commissionMap.put("taker",req.getTakerCommission());
            }
            if (req.getMarginMakerCommission() != null) {
                commissionMap.put("marginMake",req.getMarginMakerCommission());
            }
            if (req.getMarginTakerCommission() != null) {
                commissionMap.put("marginTaker",req.getMarginTakerCommission());
            }
            if (!commissionMap.isEmpty()) {
                desc += ":";
                desc += JSON.toJSONString(commissionMap);
            }
            final UserSecurityLog securityLog = new UserSecurityLog(userId, ip, locationCity, clientType,
                    AccountConstants.BROKER_API_CHANGE_COMMISSION, desc);
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add commission change log failed, userId:%s, exception:", userId), e);
        }
    }


    @Override
    public APIResponse<CreateApiAgentAliasRes> createApiAgentAlias(APIRequest<CreateApiAgentAliasReq> request) throws Exception {
        log.info("createBrokerAlias start request:{}", JsonUtils.toJsonHasNullKey(request));
        final CreateApiAgentAliasReq requestBody = request.getBody();
        User agentUser = checkAndGetUserById(requestBody.getAgentId());
        List<ApiAgentRewardConfig> apiAgentRewardConfigs = apiAgentRewardConfigMapper.selectByAgentId(requestBody.getAgentId());
        if (CollectionUtils.isEmpty(apiAgentRewardConfigs) || apiAgentRewardConfigs.size() == 0){
            throw new BusinessException(AccountErrorCode.API_AGENT_RAWARD_IS_NOT_EXIST);
        }
        User subUser = checkAndGetUserByEmail(requestBody.getEmail());

        ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
        searchParam.setAgentId(requestBody.getAgentId());
        searchParam.setCustomerId(requestBody.getCustomerId());
        ApiAgentUserAlias exist = apiAgentUserAliasMapper.selectByAgentIdCustomerId(searchParam);
        if (exist != null){
            throw new BusinessException(AccountErrorCode.APIAGENT_CUSTOMERID_IS_EXIST);
        }
        searchParam.setRefereeId(subUser.getUserId());
        ApiAgentUserAlias refereeExist = apiAgentUserAliasMapper.selectByAgentIdRefereeId(searchParam);
        if (refereeExist == null){
            ApiAgentUserAlias apiAgentUserAlias = new ApiAgentUserAlias();
            apiAgentUserAlias.setAgentId(requestBody.getAgentId());
            apiAgentUserAlias.setCustomerId(requestBody.getCustomerId());
            apiAgentUserAlias.setEmail(subUser.getEmail());
            apiAgentUserAlias.setRefereeId(subUser.getUserId());
            apiAgentUserAliasMapper.insertSelective(apiAgentUserAlias);
        }else{
            refereeExist.setCustomerId(requestBody.getCustomerId());
            apiAgentUserAliasMapper.updateByPrimaryKey(refereeExist);
        }
        CreateApiAgentAliasRes res = new CreateApiAgentAliasRes();
        res.setCustomerId(requestBody.getCustomerId());
        res.setEmail(requestBody.getEmail());
        return APIResponse.getOKJsonResult(res);
    }

    @Override
    public APIResponse<CreateApiAgentAliasByAgentCodeRes> createApiAgentAliasByAgentCode(APIRequest<CreateApiAgentAliasByAgentCodeReq> request)throws Exception {
        log.info("createApiAgentAliasByAgentCode start request:{}", JsonUtils.toJsonHasNullKey(request));
        final CreateApiAgentAliasByAgentCodeReq requestBody = request.getBody();
        User reffeeUser = checkAndGetUserById(requestBody.getReffeeId());
        ApiAgentRewardConfig apiAgentRewardConfig = apiAgentRewardConfigMapper.selectByAgentCode(requestBody.getApiAgentCode());
        if (apiAgentRewardConfig == null){
            throw new BusinessException(AccountErrorCode.APIAGENT_CODE_IS_NOT_EXIST);
        }
        ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
        searchParam.setAgentId(apiAgentRewardConfig.getAgentId());
        searchParam.setCustomerId(requestBody.getCustomerId());
        ApiAgentUserAlias exist = apiAgentUserAliasMapper.selectByAgentIdCustomerId(searchParam);
        if (exist != null){
            throw new BusinessException(AccountErrorCode.APIAGENT_CUSTOMERID_IS_EXIST);
        }
        searchParam.setRefereeId(reffeeUser.getUserId());
        ApiAgentUserAlias refereeExist = apiAgentUserAliasMapper.selectByAgentIdRefereeId(searchParam);
        if (refereeExist == null){
            ApiAgentUserAlias apiAgentUserAlias = new ApiAgentUserAlias();
            apiAgentUserAlias.setAgentId(apiAgentRewardConfig.getAgentId());
            apiAgentUserAlias.setCustomerId(requestBody.getCustomerId());
            apiAgentUserAlias.setEmail(reffeeUser.getEmail());
            apiAgentUserAlias.setRefereeId(reffeeUser.getUserId());
            apiAgentUserAliasMapper.insertSelective(apiAgentUserAlias);
        }else{
            refereeExist.setCustomerId(requestBody.getCustomerId());
            apiAgentUserAliasMapper.updateByPrimaryKey(refereeExist);
        }

        CreateApiAgentAliasByAgentCodeRes res = new CreateApiAgentAliasByAgentCodeRes();
        res.setApiAgentCode(requestBody.getApiAgentCode());
        res.setCustomerId(requestBody.getCustomerId());
        return APIResponse.getOKJsonResult(res);
    }



    @Override
    public APIResponse<List<SelectApiAgentCodeAliasRes>> selectApiAgentAlias(APIRequest<SelectApiAgentAliasReq> request) throws Exception {
        log.info("selectApiAgentAlias start request:{}", JsonUtils.toJsonHasNullKey(request));
        final SelectApiAgentAliasReq requestBody = request.getBody();
        User parentUser = checkAndGetUserById(requestBody.getAgentId());
        ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
        searchParam.setAgentId(requestBody.getAgentId());
        searchParam.setCustomerId(requestBody.getCustomerId());
        searchParam.setEmail(requestBody.getEmail());
        if (requestBody.getPage() != null && requestBody.getSize() != null){
            searchParam.setStart((requestBody.getPage()-1)*requestBody.getSize());
            searchParam.setOffset(requestBody.getSize());
        }
        List<ApiAgentUserAlias> apiAgentUserAliases = apiAgentUserAliasMapper.selectByAgentIdCustomerIdEmail(searchParam);
        log.info("selectApiAgentAlias res:{}",JsonUtils.toJsonHasNullKey(apiAgentUserAliases));
        List<SelectApiAgentCodeAliasRes> result = new ArrayList<>(apiAgentUserAliases.size());
        for (ApiAgentUserAlias apiAgentUserAlias:apiAgentUserAliases){
            SelectApiAgentCodeAliasRes res = new SelectApiAgentCodeAliasRes();
            res.setCustomerId(apiAgentUserAlias.getCustomerId());
            res.setEmail(apiAgentUserAlias.getEmail());
            result.add(res);
        }
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAgentAliasByAgentCode(APIRequest<SelectApiAgentAliasByAgentCodeReq> request)throws Exception{
        log.info("selectApiAgentAliasByAgentCode start request:{}", JsonUtils.toJsonHasNullKey(request));
        final SelectApiAgentAliasByAgentCodeReq requestBody = request.getBody();
        User refereeUser = checkAndGetUserById(requestBody.getRefereeId());
        ApiAgentRewardConfig apiAgentRewardConfig = apiAgentRewardConfigMapper.selectByAgentCode(requestBody.getApiAgentCode());
        if (apiAgentRewardConfig == null){
            throw new BusinessException(AccountErrorCode.APIAGENT_CODE_IS_NOT_EXIST);
        }
        ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
        searchParam.setAgentId(apiAgentRewardConfig.getAgentId());
        searchParam.setEmail(refereeUser.getEmail());
        List<ApiAgentUserAlias> apiAgentUserAliases = apiAgentUserAliasMapper.selectByAgentIdCustomerIdEmail(searchParam);
        log.info("selectApiAgentAliasByAgentCode res:{}",JsonUtils.toJsonHasNullKey(apiAgentUserAliases));
        if (CollectionUtils.isEmpty(apiAgentUserAliases) || apiAgentUserAliases.size() != 1){
            throw new BusinessException(AccountErrorCode.APIAGENT_CUSTOMERID_IS_NOT_EXIST);
        }
        SelectApiAgentCodeAliasRes res = new SelectApiAgentCodeAliasRes();
        res.setCustomerId(apiAgentUserAliases.get(0).getCustomerId());
        return APIResponse.getOKJsonResult(res);
    }

    @Override
    public APIResponse<SelectApiAgentCodeAliasRes> selectApiAliasByAgentAndCustomer(APIRequest<SelectApiAgentAliasReq> request) throws Exception {
        log.info("selectApiAliasByAgentAndCustomer start request:{}", JsonUtils.toJsonHasNullKey(request));
        final SelectApiAgentAliasReq requestBody = request.getBody();
        ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
        searchParam.setAgentId(requestBody.getAgentId());
        searchParam.setCustomerId(requestBody.getCustomerId());
        List<ApiAgentUserAlias> apiAgentUserAliasList = apiAgentUserAliasMapper.selectByAgentIdCustomerIdEmail(searchParam);
        log.info("selectApiAliasByAgentAndCustomer res:{}",JsonUtils.toJsonHasNullKey(apiAgentUserAliasList));
        if (CollectionUtils.isEmpty(apiAgentUserAliasList) || apiAgentUserAliasList.size() == 0){
            throw new BusinessException(AccountErrorCode.APIAGENT_CUSTOMERID_IS_NOT_EXIST);
        }
        SelectApiAgentCodeAliasRes res = new SelectApiAgentCodeAliasRes();
        res.setCustomerId(requestBody.getCustomerId());
        res.setEmail(requestBody.getEmail());
        res.setRefereeId(apiAgentUserAliasList.get(0).getRefereeId());
        return APIResponse.getOKJsonResult(res);
    }
//
    @Override
    public APIResponse<List<SelectApiAgentCommissionDetailRes>> selectApiAgentCommissionDetail(APIRequest<SelectApiAgentCommissionDetailReq> request)throws Exception {
        log.info("selectBrokerCommissionDetail start request:{}", JsonUtils.toJsonHasNullKey(request));
        SelectApiAgentCommissionDetailReq requestBody = request.getBody();
        //1000*3600*24*7 7天
        if (requestBody.getEndTime().getTime() - requestBody.getStartTime().getTime() > 604800000) {
            throw new BusinessException(ReportErrorCode.QUERY_TIME_MORE_THAN_7DAY);
        }
        List<ApiAgentRewardConfig> apiAgentRewardConfigs = apiAgentRewardConfigMapper.selectByAgentId(requestBody.getAgentId());
        if (CollectionUtils.isEmpty(apiAgentRewardConfigs) || apiAgentRewardConfigs.size() == 0){
            throw new BusinessException(AccountErrorCode.API_AGENT_RAWARD_IS_NOT_EXIST);
        }
        User agentUser = checkAndGetUserById(requestBody.getAgentId());
        if (StringUtils.isNotBlank(requestBody.getCustomerId())){
            ApiAgentUserAlias searchParam = new ApiAgentUserAlias();
            searchParam.setAgentId(requestBody.getAgentId());
            searchParam.setCustomerId(requestBody.getCustomerId());
            ApiAgentUserAlias apiAgentUserAlias = apiAgentUserAliasMapper.selectByAgentIdCustomerId(searchParam);
            if (apiAgentUserAlias == null){
                throw new BusinessException(AccountErrorCode.APIAGENT_CUSTOMERID_IS_NOT_EXIST);
            }
            GetAgentAndUserTradesRequest getAgentAndUserTradesRequest = new GetAgentAndUserTradesRequest();
            getAgentAndUserTradesRequest.setUserId(apiAgentUserAlias.getRefereeId());
            getAgentAndUserTradesRequest.setAgentId(apiAgentUserAlias.getAgentId());
            getAgentAndUserTradesRequest.setStartTime(requestBody.getStartTime().getTime());
            getAgentAndUserTradesRequest.setEndTime(requestBody.getEndTime().getTime());
            getAgentAndUserTradesRequest.setPage(1);
            getAgentAndUserTradesRequest.setRows(requestBody.getLimit());
            getAgentAndUserTradesRequest.setType(CommissionType.MIDDLEMAN_COMMISSION);
            List<GetAgentAndUserTradesVo> userByAgentIdAndTradeIds = streamerOrderApiClient.getUserByAgentIdAndTradeIds(getAgentAndUserTradesRequest);
            if (CollectionUtils.isEmpty(userByAgentIdAndTradeIds) || userByAgentIdAndTradeIds.size() == 0){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            List<SelectApiAgentCommissionDetailRes> resList = new ArrayList<>(userByAgentIdAndTradeIds.size());
            for (GetAgentAndUserTradesVo vo:userByAgentIdAndTradeIds){
                SelectApiAgentCommissionDetailRes res = new SelectApiAgentCommissionDetailRes();
                res.setEmail(MaskUtils.maskEmail(apiAgentUserAlias.getEmail()));
                res.setAsset(vo.getAsset());
                res.setCustomerId(requestBody.getCustomerId());
                res.setIncome(vo.getCommission()==null?"0":vo.getCommission().toPlainString());
                res.setSymbol(vo.getSymbol());
                res.setTime(vo.getInsertTime() == null?null:vo.getInsertTime().getTime());
                resList.add(res);
            }
            return APIResponse.getOKJsonResult(resList);
        }else{
            SearchResult<UserCommissionDetailResponse> userCommissionDetailResponseSearchResult = reportApiClient.selectBrokerCommissionDetail(requestBody.getAgentId(), requestBody.getStartTime(), requestBody.getEndTime(), 1,requestBody.getLimit(),3);
            if (userCommissionDetailResponseSearchResult == null || CollectionUtils.isEmpty(userCommissionDetailResponseSearchResult.getRows()) || userCommissionDetailResponseSearchResult.getRows().size() == 0){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            List<String> emails = userCommissionDetailResponseSearchResult.getRows().stream().map(UserCommissionDetailResponse::getEmail).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(emails)){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            Map<String,Object> searchMap = new HashMap<>();
            searchMap.put("emails",emails);
            searchMap.put("agentId",requestBody.getAgentId());
            List<ApiAgentUserAlias> brokerUserAliasList = apiAgentUserAliasMapper.selectByEmails(searchMap);
            Map<String, String> emailCustomerIdMap = null;
            if (CollectionUtils.isNotEmpty(brokerUserAliasList) && brokerUserAliasList.size() > 0){
                emailCustomerIdMap = brokerUserAliasList.stream().collect(Collectors.toMap(ApiAgentUserAlias::getEmail, ApiAgentUserAlias::getCustomerId, (k1, k2) -> k2));
            }
            List<SelectApiAgentCommissionDetailRes> resList = new ArrayList<>(userCommissionDetailResponseSearchResult.getRows().size());
            for (UserCommissionDetailResponse detail: userCommissionDetailResponseSearchResult.getRows()){
                SelectApiAgentCommissionDetailRes res = new SelectApiAgentCommissionDetailRes();
                res.setSymbol(detail.getSymbol());
                res.setIncome(detail.getCommission());
                if (emailCustomerIdMap != null && emailCustomerIdMap.containsKey(detail.getEmail())){
                    res.setCustomerId(emailCustomerIdMap.get(detail.getEmail()));
                }
                res.setAsset(detail.getAsset());
                res.setEmail(MaskUtils.maskEmail(detail.getEmail()));
                res.setTime(detail.getTime() == null?null:detail.getTime().getTime());
                resList.add(res);
            }
            return APIResponse.getOKJsonResult(resList);
        }
    }


    @Override
    public APIResponse<List<SelectBrokerCommissionDetailRes>> selectBrokerCommissionDetail(APIRequest<SelectBrokerCommissionDetailReq> request)throws Exception {
        log.info("selectBrokerCommissionDetail start request:{}", JsonUtils.toJsonHasNullKey(request));
        SelectBrokerCommissionDetailReq requestBody = request.getBody();
        //1000*3600*24*7 7天
        if (requestBody.getEndTime().getTime() - requestBody.getStartTime().getTime() > 604800000) {
            throw new BusinessException(ReportErrorCode.QUERY_TIME_MORE_THAN_7DAY);
        }
        User parentUser = checkAndGetUserById(requestBody.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(requestBody.getParentUserId());
        if (parentUserInfo == null || parentUserInfo.getAgentId() == null){
            throw new BusinessException(AccountErrorCode.BROKER_SUBACCOUNTID_IS_NOT_EXIST);
        }
        if (requestBody.getSubaccountId() != null){
            SubUserBinding subUserBinding = subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(requestBody.getParentUserId(), requestBody.getSubaccountId());
            log.info("selectByParentUserIdAndBrokerSubAccountId res:{}",JsonUtils.toJsonHasNullKey(subUserBinding));
            if (subUserBinding == null){
                throw new BusinessException(AccountErrorCode.BROKER_SUBACCOUNTID_IS_NOT_EXIST);
            }
            GetAgentAndUserTradesRequest getAgentAndUserTradesRequest = new GetAgentAndUserTradesRequest();
            getAgentAndUserTradesRequest.setUserId(subUserBinding.getSubUserId());
            getAgentAndUserTradesRequest.setAgentId(parentUserInfo.getAgentId());
            getAgentAndUserTradesRequest.setStartTime(requestBody.getStartTime().getTime());
            getAgentAndUserTradesRequest.setEndTime(requestBody.getEndTime().getTime());
            getAgentAndUserTradesRequest.setPage(requestBody.getPage());
            getAgentAndUserTradesRequest.setRows(requestBody.getLimit());
            getAgentAndUserTradesRequest.setType(CommissionType.REFERENCE_COMMISSION);
            List<GetAgentAndUserTradesVo> userByAgentIdAndTradeIds = streamerOrderApiClient.getUserByAgentIdAndTradeIds(getAgentAndUserTradesRequest);
            if (CollectionUtils.isEmpty(userByAgentIdAndTradeIds) || userByAgentIdAndTradeIds.size() == 0){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            List<SelectBrokerCommissionDetailRes> resList = new ArrayList<>(userByAgentIdAndTradeIds.size());
            for (GetAgentAndUserTradesVo vo:userByAgentIdAndTradeIds){
                SelectBrokerCommissionDetailRes res = new SelectBrokerCommissionDetailRes();
                res.setSubaccountId(String.valueOf(requestBody.getSubaccountId()));
                res.setIncome(vo.getCommission()==null?"0":vo.getCommission().toPlainString());
                res.setAsset(vo.getAsset());
                res.setSymbol(vo.getSymbol());
                res.setTradeId(vo.getTradeId());
                res.setTime(vo.getInsertTime() == null?null:vo.getInsertTime().getTime());
                resList.add(res);
            }
            return APIResponse.getOKJsonResult(resList);
        }else{
            SearchResult<UserCommissionDetailResponse> userCommissionDetailResponseSearchResult = reportApiClient.selectBrokerCommissionDetail(requestBody.getParentUserId(), requestBody.getStartTime(), requestBody.getEndTime(), requestBody.getPage(),requestBody.getLimit(),1);
            if (userCommissionDetailResponseSearchResult == null || CollectionUtils.isEmpty(userCommissionDetailResponseSearchResult.getRows()) || userCommissionDetailResponseSearchResult.getRows().size() == 0){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            List<String> emails = userCommissionDetailResponseSearchResult.getRows().stream().map(UserCommissionDetailResponse::getEmail).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(emails)){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            List<User> users = userMapper.selectByEmails(emails);
            if (CollectionUtils.isEmpty(users)){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }

            List<Long> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
            Map<Long, String> userIdEmailMap = users.stream().collect(Collectors.toMap(User::getUserId, User::getEmail, (k1, k2) -> k2));
            if (CollectionUtils.isEmpty(userIds) || userIdEmailMap == null || userIdEmailMap.size() == 0){
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            Map<String,Long> subUserEmailAndSubAccountIdMap = toSubUserEmailAndSubAccountIdMap(requestBody.getParentUserId(),userIds,userIdEmailMap);
            List<SelectBrokerCommissionDetailRes> resList = new ArrayList<>(userCommissionDetailResponseSearchResult.getRows().size());
            for (UserCommissionDetailResponse detail: userCommissionDetailResponseSearchResult.getRows()){
                SelectBrokerCommissionDetailRes res = new SelectBrokerCommissionDetailRes();
                if (subUserEmailAndSubAccountIdMap.containsKey(detail.getEmail())){
                    res.setSubaccountId(String.valueOf(subUserEmailAndSubAccountIdMap.get(detail.getEmail())));
                }
                res.setIncome(detail.getCommission());
                res.setAsset(detail.getAsset());
                res.setSymbol(detail.getSymbol());
                res.setTradeId(detail.getTradeId());
                res.setTime(detail.getTime() == null?null:detail.getTime().getTime());
                resList.add(res);
            }
            return APIResponse.getOKJsonResult(resList);
        }
    }

    @Override
    public APIResponse<QueryBrokerSubUserBySubAccountRes> queryBrokerSubUserIdBySubAccount(APIRequest<QueryBrokerSubAccountReq> request)throws Exception{
        QueryBrokerSubAccountReq requestBody = request.getBody();
        User parentUser = checkAndGetUserById(requestBody.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        QueryBrokerSubUserBySubAccountRes res = new QueryBrokerSubUserBySubAccountRes();
        UserInfo parentUserInfo = userInfoMapper.selectByPrimaryKey(requestBody.getParentUserId());
        res.setParentUserId(requestBody.getParentUserId());
        res.setSubaccountId(requestBody.getSubAccountId());
        res.setBrokerAgentId(parentUserInfo.getAgentId());
        if (requestBody.getSubAccountId() != null){
            SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(requestBody.getParentUserId(),requestBody.getSubAccountId());
            if(null==subUserBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            res.setSubUserId(subUserBinding.getSubUserId());
        }
        BrokerUserCommisssion brokerUserCommisssion = brokerUserCommisssionMapper.selectByUserId(requestBody.getParentUserId());
        if (brokerUserCommisssion != null){
            res.setSource(brokerUserCommisssion.getSource());
        }
        return APIResponse.getOKJsonResult(res);
    }

    @Override
    public APIResponse<Boolean> updateBrokerSource(APIRequest<UpdateBrokerUserCommissionSourceReq> request)throws Exception{
        UpdateBrokerUserCommissionSourceReq requestBody = request.getBody();
        User parentUser = checkAndGetUserById(requestBody.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        BrokerUserCommisssion brokerUserCommisssion = new BrokerUserCommisssion();
        brokerUserCommisssion.setUserId(requestBody.getParentUserId());
        brokerUserCommisssion.setSource(requestBody.getSource());
        brokerUserCommisssionMapper.updateByUserIdSelective(brokerUserCommisssion);
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<CheckRelationShipAndReturnSubUserResp> checkRelationShipAndReturnSubUser(APIRequest<CheckRelationShipAndReturnSubUserReq> request) throws Exception {
        CheckRelationShipAndReturnSubUserReq body=request.getBody();
        // 1.验证broke父子账号关系
        User parentUser = checkAndGetUserById(body.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(body.getParentUserId(),body.getSubAccountId());
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        User subUser = checkAndGetUserById(subUserBinding.getSubUserId());
        UserInfo subUserInfo = this.userInfoMapper.selectByPrimaryKey(subUser.getUserId());
        if (subUserInfo == null|| subUser == null ){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        UserVo subUserVo = null;
        if (subUser != null) {
            subUserVo = new UserVo();
            BeanUtils.copyProperties(subUser, subUserVo);
            //密码不给别人看
            subUserVo.setSalt(null);
            subUserVo.setPassword(null);
        }
        UserInfoVo subUserInfoVo = null;
        if (subUserInfo != null) {
            subUserInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(subUserInfo, subUserInfoVo);
        }

        log.info("checkRelationShipAndReturnSubUser done, userId:{}", subUser==null ? "null":subUser.getUserId());
        CheckRelationShipAndReturnSubUserResp resp=new CheckRelationShipAndReturnSubUserResp();
        resp.setUserVo(subUserVo);
        resp.setUserInfoVo(subUserInfoVo);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<List<BrokerSubUserBindingsResp>> checkAndGetBrokerSubUserBindings(APIRequest<QueryBrokerSubAccountReq> request) {
        QueryBrokerSubAccountReq req = request.getBody();
        User parentUser = checkAndGetUserById(req.getParentUserId());
        // 1.验证broke父子账号关系
        if(Objects.nonNull(req.getSubAccountId())){
            assertBrokerParentSubUserBound(req.getParentUserId(), req.getSubAccountId());
        }else{
            assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        }
        Integer page = req.getPage();
        Integer size = req.getSize();
        if (page == null || page <= 0){
            page = 1;
        }
        if (size == null || size <= 0 || size > 500){
            size = 500;
        }
        // 2.查询母账号下的子账号
        List<Long> subUserIds = Lists.newArrayList();
        List<SubUserBinding> subUserBindingList = Lists.newArrayList();
        if (req.getSubAccountId() == null) {
            List<SubUserBinding> subUserBindings = subUserBindingMapper.getSubUserBindingsByPage(req.getParentUserId(),(page-1)*size,size);
            if (CollectionUtils.isEmpty(subUserBindings)) {
                return APIResponse.getOKJsonResult(Lists.newArrayList());
            }
            subUserBindingList.addAll(subUserBindings);
            subUserIds.addAll(subUserBindings.stream().map(SubUserBinding::getSubUserId).collect(Collectors.toList()));
        } else {
            SubUserBinding subUserBinding=subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(), req.getSubAccountId());
            subUserBindingList.add(subUserBinding);
            subUserIds.add(subUserBinding.getSubUserId());
        }

        List<BrokerSubUserBindingsResp> result = subUserBindingList.stream().map(x -> {
            BrokerSubUserBindingsResp subUserBindingsResp = new BrokerSubUserBindingsResp();
            BeanUtils.copyProperties(x, subUserBindingsResp);
            return subUserBindingsResp;
        }).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<GetrSubUserBindingsResp> getSubBindingInfo(APIRequest<GetSubbindingInfoReq> request) {
        GetSubbindingInfoReq req=request.getBody();
        if(null==req.getSubAccountId() && null==req.getSubUserId()){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 1.验证broke父子账号关系
        User parentUser = checkAndGetUserById(req.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        SubUserBinding subUserBinding=  null;
        if(null!=req.getSubAccountId()){
            subUserBinding= subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(req.getParentUserId(),req.getSubAccountId());
        }
        if(null!=req.getSubUserId()){
            subUserBinding= subUserBindingMapper.selectByParentUserIdAndSubUserId(req.getParentUserId(),req.getSubUserId());
        }
        if(null==subUserBinding){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }
        GetrSubUserBindingsResp getrSubUserBindingsResp=new GetrSubUserBindingsResp();
        getrSubUserBindingsResp.setParentUserId(subUserBinding.getParentUserId());
        getrSubUserBindingsResp.setSubUserId(subUserBinding.getSubUserId());
        getrSubUserBindingsResp.setBrokerSubAccountId(subUserBinding.getBrokerSubAccountId().toString());
        return APIResponse.getOKJsonResult(getrSubUserBindingsResp);
    }

    @Override
    public APIResponse<GetSubBindingInfoByPageResp> getSubBindingInfoByPage(APIRequest<GetSubBindingInfoByPageReq> request) {
        GetSubBindingInfoByPageReq req = request.getBody();
        User parentUser = checkAndGetUserById(req.getParentUserId());
        // 1.验证broke母账号权限
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        Integer page = req.getPage();
        Integer size = req.getSize();
        if (page == null || page <= 0){
            page = 1;
        }
        if (size == null || size <= 0 || size > 500){
            size = 500;
        }
        Map<String, Object> param=Maps.newHashMap();
        param.put("parentUserId",req.getParentUserId());
        param.put("subUserId",req.getSubUserId());
        param.put("brokerSubAccountId",req.getSubAccountId());
        param.put("offset",(page-1)*size);
        param.put("limit",size);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(req.getSubUserEmail())){
            User subUser = checkAndGetUserByEmail(req.getSubUserEmail());
            param.put("subUserId",subUser.getUserId());
        }
        GetSubBindingInfoByPageResp resp=new GetSubBindingInfoByPageResp();
        try{
            long totalCount= subUserBindingMapper.countSubUsersBySelective(param);
            if(totalCount<=0){
                log.info("count less than zero");
                return APIResponse.getOKJsonResult(resp);
            }
            List<SubUserBinding> resultList=subUserBindingMapper.getBrokerSubbindingInfoByPage(param);
            if(CollectionUtils.isEmpty(resultList)){
                log.info("resultList is empty");
                return APIResponse.getOKJsonResult(resp);
            }
            List<BrokerSubbindingInfoVo> resultListVo=Lists.newArrayList();
            for(SubUserBinding subUserBinding:resultList){
                BrokerSubbindingInfoVo vo=new BrokerSubbindingInfoVo();
                BeanUtils.copyProperties(subUserBinding,vo);
                resultListVo.add(vo);
            }
            resp.setTotal(totalCount);
            resp.setResult(resultListVo);
            return APIResponse.getOKJsonResult(resp);
        }catch (Exception e){
            log.error("BrokerSubUserService.getSubBindingInfoByPage error:{}", e);
            throw new RuntimeException("BrokerSubUserService.getSubBindingInfoByPage error",e);
        }
    }

    private Map<String, Long> toSubUserEmailAndSubAccountIdMap(Long parentUserId, List<Long> userIds, Map<Long, String> userIdEmailMap) {
        Map<String,Long> subUserAndSubAccountMap = new HashMap<>(userIdEmailMap.size());
        Map<String,Object> param = new HashMap<>();
        param.put("parentUserId", parentUserId);
        param.put("userIds", userIds);
        List<SubUserBinding> subUserBindings = subUserBindingMapper.selectByParentUserIdAndSubUserIds(param);
        if (CollectionUtils.isEmpty(subUserBindings)){
            return subUserAndSubAccountMap;
        }
        for (SubUserBinding sub:subUserBindings){
            if (userIdEmailMap.containsKey(sub.getSubUserId())){
                subUserAndSubAccountMap.put(userIdEmailMap.get(sub.getSubUserId()),sub.getBrokerSubAccountId());
            }
        }
        return subUserAndSubAccountMap;
    }

    @Override
    public APIResponse<List<SubAccountSpotAssetResp>> queryBrokerSubAccountSpotAsset(APIRequest<QueryBrokerSubAccountReq> request) throws Exception {
        log.info("queryBrokerSubAccountSpotAsset start request:{}", JsonUtils.toJsonHasNullKey(request));
        QueryBrokerSubAccountReq req = request.getBody();
        // 验证参数、确保请求的母账户是broker母账户
        checkSubAccountQueryPaginationParams(req, 10, 20);
        assertBrokerSubUserFunctionEnabled(req);
        // 查询子账户（分页或者按subAccountId查询）
        List<SubUserBinding> subUserBindings = checkAndGetBrokerSubUserBindings(req);
        if (CollectionUtils.isEmpty(subUserBindings)) {
            return APIResponse.getOKJsonResult(Collections.emptyList());
        }
        List<SubAccountSpotAssetResp> spotAssetResps = subUserBindings.stream().map(subUserBinding -> {
            SubAccountSpotAssetResp resp = new SubAccountSpotAssetResp();
            UserAssetTransferBtcResponse assetResponse;
            try {
                assetResponse = userAssetApiClient.getUserAssetTransferBtc(subUserBinding.getSubUserId());
            } catch (final Exception e) {
                log.error("getUserAssetTransferBtc error, subUserId={} error={}", subUserBinding.getSubUserId(), e.getMessage());
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            resp.setSubAccountId(String.valueOf(subUserBinding.getBrokerSubAccountId()));
            resp.setTotalBalanceOfBtc(
                    assetResponse != null ? assetResponse.getTotalTransferBtc().toPlainString() : new BigDecimal(0).toPlainString());
            return resp;
        }).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(spotAssetResps);
    }

    @Override
    public APIResponse<List<SubAccountMarginAssetResp>> queryBrokerSubAccountMarginAsset(APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception {
        log.info("queryBrokerSubAccountMarginAsset start request:{}", JsonUtils.toJsonHasNullKey(request));
        QueryBrokerSubAccountReq req = request.getBody();
        // 验证参数、确保请求的母账户是broker母账户
        checkSubAccountQueryPaginationParams(req, 10, 20);
        assertBrokerSubUserFunctionEnabled(req);
        // 查询子账户（分页或者按subAccountId查询）
        List<SubUserBinding> subUserBindings = checkAndGetBrokerSubUserBindings(req);
        if (CollectionUtils.isEmpty(subUserBindings)) {
            return APIResponse.getOKJsonResult(Collections.emptyList());
        }
        // 查询marginUserId -> subAccountId 的映射
        Map<Long, Long> marginUserIdToSubAccountId = getSubAccountIdMapping(subUserBindings, UserInfo::getMarginUserId);
        // 查询杠杆账户资产信息
        Map<Long, AccountSummaryResponse.AccountSummary> marginAsset = batchQuerySubAccountMarginAsset(marginUserIdToSubAccountId);
        // 组装查询结果
        List<SubAccountMarginAssetResp> marginAssetResps = subUserBindings.stream().map(subUserBinding -> {
            Long subAccountId = subUserBinding.getBrokerSubAccountId();
            SubAccountMarginAssetResp resp = new SubAccountMarginAssetResp();
            resp.setSubAccountId(String.valueOf(subAccountId));

            AccountSummaryResponse.AccountSummary accountSummary = marginAsset.get(subAccountId);
            if (accountSummary != null) {
                resp.setMarginLevel(accountSummary.getMarginLevel().toPlainString());
                resp.setTotalAssetOfBtc(accountSummary.getTotalAssetOfBtc().toPlainString());
                resp.setTotalLiabilityOfBtc(accountSummary.getTotalLiabilityOfBtc().toPlainString());
                resp.setTotalNetAssetOfBtc(accountSummary.getTotalNetAssetOfBtc().toPlainString());
                resp.setMarginEnable(true);
            }
            return resp;
        }).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(marginAssetResps);
    }

    @Override
    public APIResponse<List<SubAccountFuturesAssetResp>> queryBrokerSubAccountFuturesAsset(APIRequest<QueryBrokerSubAccountReq> request)
            throws Exception {
        log.info("queryBrokerSubAccountFuturesAsset start request:{}", JsonUtils.toJsonHasNullKey(request));
        QueryBrokerSubAccountReq req = request.getBody();
        // 验证参数、确保请求的母账户是broker母账户
        checkSubAccountQueryPaginationParams(req, 10, 100);
        assertBrokerSubUserFunctionEnabled(req);
        // 查询子账户（分页或者按subAccountId查询）
        List<SubUserBinding> subUserBindings = checkAndGetBrokerSubUserBindings(req);
        if (CollectionUtils.isEmpty(subUserBindings)) {
            return APIResponse.getOKJsonResult(Collections.emptyList());
        }
        // 查询futuresUserId -> subAccountId 的映射
        Map<Long, Long> futuresUserIdToSubAccountId = getSubAccountIdMapping(subUserBindings, UserInfo::getFutureUserId);
        // 查询期货资产
        Map<Long, AccountRiskVO> futuresAsset = batchQuerySubAccountFuturesAsset(futuresUserIdToSubAccountId);
        // 组装查询结果
        List<SubAccountFuturesAssetResp> futuresAssetResps = subUserBindings.stream().map(subUserBinding -> {
            Long subAccountId = subUserBinding.getBrokerSubAccountId();
            SubAccountFuturesAssetResp resp = new SubAccountFuturesAssetResp();
            resp.setSubAccountId(String.valueOf(subAccountId));

            AccountRiskVO riskVO = futuresAsset.get(subAccountId);
            if (riskVO != null) {
                resp.setTotalInitialMarginOfUsdt(riskVO.getTotalInitialMargin());
                resp.setTotalMaintenanceMarginOfUsdt(riskVO.getTotalMaintenanceMargin());
                resp.setTotalWalletBalanceOfUsdt(riskVO.getTotalWalletBalance());
                resp.setTotalUnrealizedProfitOfUsdt(riskVO.getTotalUnrealizedProfit());
                resp.setTotalMarginBalanceOfUsdt(riskVO.getTotalMarginBalance());
                resp.setTotalPositionInitialMarginOfUsdt(riskVO.getTotalPositionInitialMargin());
                resp.setTotalOpenOrderInitialMarginOfUsdt(riskVO.getTotalOpenOrderInitialMargin());
                resp.setFuturesEnable(true);
            }
            return resp;
        }).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(futuresAssetResps);
    }

    @Override
    public APIResponse<Boolean> brokerFutureAssetTransfer(APIRequest<BrokerFutureTransferReq> request)throws Exception{
        log.info("BrokerSubUserService.brokerFutureAssetTransfer.request:{}",JsonUtils.toJsonHasNullKey(request));
        BrokerFutureTransferReq body = request.getBody();
        Long parentUserId = body.getParentUserId();
        Long fromId = body.getFromId();
        Long toId = body.getToId();
        User parentUser = checkAndGetUserById(parentUserId);
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        if (fromId == null){
            SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,toId);
            if(null==subUserBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            fromId = parentUserId;
        }else if (toId == null){
            SubUserBinding subUserBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,fromId);
            if(null==subUserBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            toId = parentUserId;
        }else if (fromId.equals(toId)){
            throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
        }else {
            SubUserBinding fromBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,fromId);
            SubUserBinding toBinding=  subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(parentUserId,toId);
            if(null==fromBinding || null==toBinding){
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
        }
        User fromUser = checkAndGetUserById(fromId);
        User toUser = checkAndGetUserById(toId);
        //发送方、接收方有一方为资管子账户，则不可划转
        if (checkAssetSubUser(fromUser.getStatus()) || checkAssetSubUser(toUser.getStatus())){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //判断是否禁止划转
        UserStatusEx senderUserStatus=new UserStatusEx(fromUser.getStatus());
        UserStatusEx recipientUserStatus=new UserStatusEx(toUser.getStatus());
        if(senderUserStatus.getIsForbiddenBrokerTrasnfer()|| recipientUserStatus.getIsForbiddenBrokerTrasnfer()){
            throw new BusinessException(AccountErrorCode.FORBIDDEN_BROKER_FUTURE_TRANSFER);
        }
        // 确保转出转入方账号已激活
        if (!BitUtils.isEnable(fromUser.getStatus(), Constant.USER_ACTIVE)||!BitUtils.isEnable(toUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED);
        }
        if(!checkSubUserBusiness.transferExceededFrequencyFutureLimits(parentUserId,BROKER_FUTURE_TRANSFER_LIMIT,brokerFutureTransferLimit)){
            log.info("BrokerSubUserBusiness.exceeded frequency limits, parentUserId:{}", parentUserId);
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        UserInfo fromUserInfo = userInfoMapper.selectByPrimaryKey(fromId);
        UserInfo toUserInfo = userInfoMapper.selectByPrimaryKey(toId);
        if (fromUserInfo == null || fromUserInfo.getFutureUserId() == null || toUserInfo == null || toUserInfo.getFutureUserId() == null){
            log.info("broker sub user has not future:{}", parentUserId);
            throw new BusinessException(AccountErrorCode.BROKER_FUTURE_ACCOUNT_NOT_EXIST);
        }
        //检查用户关系
        if (body.getFuturesType() == 1){
            Long transId = futureTransferApiClient.getTransIdForFutureTransfer(String.valueOf(body.getToId()), AccountConstants.FUTURE_INNER_TRANSFER);
            log.info("brokerFutureAssetTransfer.getTransIdForFutureTransfer.transId:{}",transId);
            AssetTransferRequest assetTransferRequest = new AssetTransferRequest();
            assetTransferRequest.setAmount(body.getAmount());
            assetTransferRequest.setAsset(body.getAsset());
            assetTransferRequest.setFromUserId(fromUserInfo.getFutureUserId());
            assetTransferRequest.setToUserId(toUserInfo.getFutureUserId());
            assetTransferRequest.setTranId(transId);
            futureTransferApiClient.futureAssetTransfer(assetTransferRequest);
        }else if (body.getFuturesType() == 2){
            Long transId = deliveryFutureAssetTransfer.getDeliveryTransIdForFutureTransfer(String.valueOf(body.getToId()), AccountConstants.DELIVERY_INNER_TRANSFER);
            log.info("brokerFutureAssetTransfer.getTransIdForFutureTransfer.transId:{}",transId);
            com.binance.delivery.periphery.api.request.core.AssetTransferRequest assetTransferRequest = new com.binance.delivery.periphery.api.request.core.AssetTransferRequest();
            assetTransferRequest.setAmount(body.getAmount());
            assetTransferRequest.setAsset(body.getAsset());
            assetTransferRequest.setFromUserId(fromUserInfo.getFutureUserId());
            assetTransferRequest.setToUserId(toUserInfo.getFutureUserId());
            assetTransferRequest.setTranId(transId);
            deliveryFutureAssetTransfer.deliveryFutureAssetTransfer(assetTransferRequest);
        }
        log.info("BrokerSubUserService.brokerFutureAssetTransfer.end");
        return APIResponse.getOKJsonResult(true);
    }


    @Override
    public APIResponse<List<QueryBrokerSubAccountIdResponse>> queryBrokerSubAccountId(APIRequest<QueryBrokerSubAccountIdRequest> request) throws Exception {
        final QueryBrokerSubAccountIdRequest requestBody = request.getBody();
        List<Long> subUserIds = requestBody.getSubUserIds();
        if (CollectionUtils.isEmpty(subUserIds)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());            
        }
        // 如果userId列表超过500个，截取保留500个
        if (subUserIds.size() > 501) {
            subUserIds = subUserIds.subList(0, 501);
        }
        User parentUser = checkAndGetUserById(requestBody.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
        
        Map<String, Object> param = new HashMap<>();
        param.put("parentUserId", requestBody.getParentUserId());
        param.put("userIds", subUserIds);
        List<SubUserBinding> subUserBindingList = subUserBindingMapper.selectByParentUserIdAndSubUserIds(param);

        List<QueryBrokerSubAccountIdResponse> result = subUserBindingList.stream().map(x -> {
            QueryBrokerSubAccountIdResponse oneResponse = new QueryBrokerSubAccountIdResponse();
            BeanUtils.copyProperties(x, oneResponse);
            oneResponse.setSubAccountId(x.getBrokerSubAccountId());
            return oneResponse;
        }).collect(Collectors.toList());
        
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<QueryByBrokerSubAccountIdResponse> queryByBrokerSubAccountId(APIRequest<QueryByBrokerSubAccountIdRequest> request)throws Exception{
        SubUserBinding  subUserBinding = subUserBindingMapper.selectByBrokerSubAccountId(request.getBody().getBrokerSubAccountId());
        if(subUserBinding == null || subUserBinding.getBrokerSubAccountId() == null){
            return APIResponse.getOKJsonResult(null);
        }
        QueryByBrokerSubAccountIdResponse response = new QueryByBrokerSubAccountIdResponse();
        response.setSubUserId(subUserBinding.getSubUserId());
        response.setBrokerSubAccountId(subUserBinding.getBrokerSubAccountId());
        response.setParentUserId(subUserBinding.getParentUserId());
        return APIResponse.getOKJsonResult(response);
    }

    private void checkSubAccountQueryPaginationParams(QueryBrokerSubAccountReq req, Integer defaultSize, Integer maxSize) {
        if (req.getSubAccountId() != null) {
            return;
        }
        if (req.getPage() == null) {
            req.setPage(1);
        }
        if (req.getPage() < 1) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (req.getSize() == null) {
            req.setSize(defaultSize);
        }
        if (req.getSize() < 1 || req.getSize() > maxSize) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
    }

    private void assertBrokerSubUserFunctionEnabled(QueryBrokerSubAccountReq req) {
        User parentUser = checkAndGetUserById(req.getParentUserId());
        assertBrokerSubUserFunctionEnabled(parentUser.getStatus());
    }

    private List<SubUserBinding> checkAndGetBrokerSubUserBindings(QueryBrokerSubAccountReq request) {
        if (request.getSubAccountId() != null) {
            SubUserBinding subUserBinding =
                    subUserBindingMapper.selectByParentUserIdAndBrokerSubAccountId(request.getParentUserId(), request.getSubAccountId());
            if (null == subUserBinding) {
                throw new BusinessException(GeneralCode.TWO_USER_ID_NOT_BOUND);
            }
            return Lists.newArrayList(subUserBinding);
        }
        return subUserBindingMapper.getSubUserBindingsByPage(request.getParentUserId(), (request.getPage() - 1) * request.getSize(),
                request.getSize());
    }

    /**
     * 查询 marginUserId -> subAccountId 或者 futuresUserId -> subAccountId 的映射
     */
    private Map<Long, Long> getSubAccountIdMapping(Collection<SubUserBinding> subUserBindings, java.util.function.Function<UserInfo, Long> mapper) {
        Map<Long, Long> subUserIdToSubAccountId =
                subUserBindings.stream().collect(Collectors.toMap(SubUserBinding::getSubUserId, SubUserBinding::getBrokerSubAccountId));
        List<UserInfo> subUserInfoList = userInfoMapper.selectUserInfoList(Lists.newArrayList(subUserIdToSubAccountId.keySet()));
        return subUserInfoList.stream().filter(item -> Objects.nonNull(mapper.apply(item)))
                .collect(Collectors.toMap(mapper, item -> subUserIdToSubAccountId.get(item.getUserId())));
    }

    /**
     * 批量查询杠杆资产
     *
     * @return subAccountId -> AccountSummaryResponse.AccountSummary
     */
    private Map<Long, AccountSummaryResponse.AccountSummary> batchQuerySubAccountMarginAsset(Map<Long, Long> marginUserIdToSubAccountId) {
        if (MapUtils.isEmpty(marginUserIdToSubAccountId)) {
            return Collections.emptyMap();
        }
        AccountSummaryResponse response =
                marginAccountApiClient.subAccountSummary(null, Lists.newArrayList(marginUserIdToSubAccountId.keySet()), null);
        if (response == null || CollectionUtils.isEmpty(response.getSubAccounts())) {
            return Collections.emptyMap();
        }
        return response.getSubAccounts().stream()
                .collect(Collectors.toMap(item -> marginUserIdToSubAccountId.get(item.getUid()), java.util.function.Function.identity()));
    }

    /**
     * 批量查询期货资产
     *
     * @return subAccountId -> AccountRiskVO
     */
    private Map<Long, AccountRiskVO> batchQuerySubAccountFuturesAsset(Map<Long, Long> futuresUserIdToSubAccountId) throws Exception {
        if (MapUtils.isEmpty(futuresUserIdToSubAccountId)) {
            return Collections.emptyMap();
        }
        Map<Long, AccountRiskVO> riskVOMap = riskApiClient.batchGetBalanceRisks(futuresUserIdToSubAccountId.keySet());
        if (MapUtils.isEmpty(riskVOMap)) {
            return Collections.emptyMap();
        }
        return riskVOMap.entrySet().stream().collect(Collectors.toMap(entry -> futuresUserIdToSubAccountId.get(entry.getKey()), Map.Entry::getValue));
    }

    private String formatAsset(BigDecimal amount) {
        return amount == null ? new BigDecimal(0).setScale(8, BigDecimal.ROUND_DOWN).toPlainString()
                : amount.setScale(8, BigDecimal.ROUND_DOWN).toPlainString();
    }
}
