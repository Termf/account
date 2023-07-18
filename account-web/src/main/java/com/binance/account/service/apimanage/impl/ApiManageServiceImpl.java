package com.binance.account.service.apimanage.impl;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.api.UserSecurityLogApi;
import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.apimanage.ApiDeletedModel;
import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.account.data.entity.apimanage.OperateLogModel;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.apimanage.ApiDeletedModelMapper;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.data.mapper.apimanage.OperateLogModelMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.integration.futureengine.FutureDeliveryAccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.integration.mbxgateway.MbxgatewayIOrderApiClient;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.service.apimanage.IOperateLogService;
import com.binance.account.service.country.impl.CountryBlacklistBusiness;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.subuser.ISubUser;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.RegexUtils;
import com.binance.account.vo.apimanage.ApiKeyVo;
import com.binance.account.vo.apimanage.enums.ApiKeyStatus;
import com.binance.account.vo.apimanage.request.DeleteAllApiKeyRequest;
import com.binance.account.vo.apimanage.request.DeleteApiKeyRequest;
import com.binance.account.vo.apimanage.request.EnableApiCreateRequest;
import com.binance.account.vo.apimanage.request.EnableApiWithdrawRequest;
import com.binance.account.vo.apimanage.request.EnableUpdateApiKeyRequest;
import com.binance.account.vo.apimanage.request.GetApiListRequest;
import com.binance.account.vo.apimanage.request.GetApisRequest;
import com.binance.account.vo.apimanage.request.SaveApiKeyRequest;
import com.binance.account.vo.apimanage.request.SaveApiKeyV2Request;
import com.binance.account.vo.apimanage.request.SearchApiRequest;
import com.binance.account.vo.apimanage.request.SearchDeletedApiRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyRestrictIpRequest;
import com.binance.account.vo.apimanage.request.UpdateApiKeyV3Request;
import com.binance.account.vo.apimanage.response.ApiModelResponse;
import com.binance.account.vo.apimanage.response.EnableApiWithdrawResponse;
import com.binance.account.vo.apimanage.response.EnableUpdateApiKeyResponse;
import com.binance.account.vo.apimanage.response.PagingResult;
import com.binance.account.vo.apimanage.response.SaveApiKeyV2Response;
import com.binance.account.vo.apimanage.response.UpdateApiKeyResponse;
import com.binance.account.vo.apimanage.response.UpdateApiKeyV3Response;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.GetUserRequest;
import com.binance.account.vo.user.response.GetUserEmailResponse;
import com.binance.account.vo.user.response.GetUserResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.CopyBeanUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.HeaderUtils;
import com.binance.master.utils.LanguageUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.master.utils.security.EncryptionUtils;
import com.binance.master.web.handlers.MessageHelper;
import com.binance.messaging.api.msg.MsgApi;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiManageServiceImpl extends BaseServiceImpl implements IApiManageService {

    private static final String IP = "0.0.0.0";
    private static final String IP_LIST_SEPARATOR = ",";
    private static final String ILLEGAL_CHAR = "[`~!#$^&*()=|{}':;',\\[\\].<>/?~！#￥……&*（）——|{}【】‘；：”“'。，、？\\\\]";
    private static final Pattern ILLEGAL_CHAR_PATTERN = Pattern.compile(ILLEGAL_CHAR);
    @Value("${api.key.cipher.code}")
    private String aesPass;

    @Value("${api.key.cipher.oldcode}")
    private String oldAesPass;

    @Value("${api.key.restrict.ip.size:30}")
    private int apiKeyRestrictIpSize;

    @Autowired
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Resource
    private IUserSecurity userSecurityApi;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private CountryBlacklistBusiness countryBlacklistBusiness;
    @Resource
    private UserSecurityLogApi userSecurityLogApi;
    @Resource
    private MsgApi msgApi;
    @Resource
    private IUser userService;
    @Resource
    private ISubUser subUser;
    @Autowired
    private MatchboxApiClient matchboxApi;
    @Resource
    private ApiModelMapper apiModelMapper;

    @Resource
    private ApiDeletedModelMapper apiDeletedModelMapper;
    @Resource
    private OperateLogModelMapper operateLogModelMapper;
    @Resource
    private IOperateLogService operateLogService;
    @Resource
    private MessageHelper messageHelper;
    @Autowired
    private FutureAccountApiClient futureAccountApiClient;
    @Autowired
    private FutureDeliveryAccountApiClient futureDeliveryAccountApiClient;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private MbxgatewayIOrderApiClient mbxgatewayOrderApiClient;

    @Value("${beta.domain:beta.binance.com}")
    private String betaDomain;
    private static final String TEMP_CACHE_API_SECRET_KEY = "TEMP_CACHE_API_SECRET_KEY_%s";
    private static final String TEMP_CACHE_API_KEY_UPDATE = "TEMP_CACHE_API_KEY_UPDATE_%s";

    @SuppressWarnings("unchecked")
    @Override
    public PagingResult<ApiModelResponse> loadApiList(SearchApiRequest body) throws Exception {
        log.info("ApiServiceImpl.loadApiList,the request param is {}", body);
        if (StringUtils.isNotBlank(body.getApiKey()) && body.isFrontEnd()) {
            body.setApiKey(CryptoUtils.encryptAESToString(body.getApiKey(), this.aesPass));
        }
        // 原逻辑没传时间的话，默认取当天时间作为时间区间
        if (body.isDefaultTimeLimit()) {
            if (body.getStartTime() == null) {
                body.setStartTime(DateUtils.getTodayBegin());
            }
            if (body.getEndTime() == null) {
                body.setEndTime(DateUtils.getTodayEnd());
            }
        }
        if (body.getStart() == null || body.getStart() < 0) {
            body.setStart(0);
        }
        if (body.getOffset() == null || body.getOffset() < 0) {
            body.setOffset(0);
        }
        String userId = body.getUserId();
        String email = body.getEmail();
        if (StringUtils.isNotBlank(email)) {
            GetUserRequest getUserRequest = new GetUserRequest();
            getUserRequest.setEmail(body.getEmail());
            String theUserId = String.valueOf(
                    this.getAPIRequestResponse(this.userService.getUserIdByEmail(this.newAPIRequest(getUserRequest))));
            if (StringUtils.isNotBlank(theUserId) && !StringUtils.equals(theUserId, "null")) {
                if (StringUtils.isNotBlank(userId)) {
                    if (!StringUtils.equals(userId, theUserId)) {
                        return new PagingResult<>(Lists.newArrayList(), 0);
                    }
                } else {
                    userId = theUserId;
                }
            } else {
                return new PagingResult<>(Lists.newArrayList(), 0);
            }
        }
        body.setUserId(userId);

        Map<String, Object> param = CopyBeanUtils.copy(body, Map.class);
        param.put("userIds", body.getUserIds());
        param.put("start", body.getStart());
        param.put("offset", body.getOffset());

        log.info("ApiServiceImpl.loadApiList,the param is {}", body);
        List<ApiModel> mapList = this.apiModelMapper.selectApiList(param);
        List<ApiModelResponse> list = Lists.newArrayList();
        for (ApiModel model : mapList) {
            String apiKey = model.getApiKey();
            if (StringUtils.isNotBlank(apiKey)) {
                model.setApiKey(CryptoUtils.decryptAESToString(apiKey, this.aesPass));
            }
            if (StringUtils.isBlank(model.getEmail())) {
                if (StringUtils.isBlank(email)) {
                    UserIdRequest userIdRequest = new UserIdRequest();
                    userIdRequest.setUserId(Long.parseLong(model.getUserId()));
                    GetUserEmailResponse getUserEmailResponse = this.getAPIRequestResponse(
                            this.userService.getUserEmailByUserId(this.newAPIRequest(userIdRequest)));
                    if (getUserEmailResponse != null) {
                        model.setEmail(getUserEmailResponse.getEmail());
                    }
                } else {
                    model.setEmail(email);
                }
            }
            list.add(CopyBeanUtils.copy(model, ApiModelResponse.class));
        }
        return new PagingResult<ApiModelResponse>(list, this.apiModelMapper.selectApiListCount(param));
    }

    @Override
    public PagingResult<ApiModelResponse> loadDeletedApiList(SearchDeletedApiRequest body) throws Exception {
        if (body.getStartTime() == null) {
            body.setStartTime(DateUtils.getTodayBegin());
        }
        if (body.getEndTime() == null) {
            body.setEndTime(DateUtils.getTodayEnd());
        }
        if (body.getStart() == null || body.getStart() < 0) {
            body.setStart(0);
        }
        if (body.getOffset() == null || body.getOffset() < 0) {
            body.setOffset(0);
        }
        Map<String, Object> param = CopyBeanUtils.copy(body, Map.class);
        param.put("start", body.getStart());
        param.put("offset", body.getOffset());

        List<ApiDeletedModel> mapList = this.apiDeletedModelMapper.loadByParams(param);
        List<ApiModelResponse> list = Lists.newArrayList();
        for (ApiDeletedModel model : mapList) {
            String apiKey = model.getApiKey();
            if (StringUtils.isNotBlank(apiKey)) {
                model.setApiKey(CryptoUtils.decryptAESToString(apiKey, this.aesPass));
            }
            ApiModelResponse response = CopyBeanUtils.copy(model, ApiModelResponse.class);
            response.setId(model.getOriginalId().toString());
            list.add(response);
        }
        return new PagingResult<ApiModelResponse>(list, this.apiDeletedModelMapper.countByParams(param));
    }

    @Override
    public ApiModelResponse queryApiByUserAndApiKey(String userId, String apiKey) throws Exception {
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "userId不能为空");
        }
        if (StringUtils.isBlank(apiKey)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "apiKey不能为空");
        }
        Map<String, Object> param = Maps.newHashMap();
        param.put("userId", userId);
        param.put("apiKey", CryptoUtils.encryptAESToString(apiKey, this.aesPass));
        List<ApiModel> mapList = this.apiModelMapper.selectApiList(param);
        if (mapList != null && mapList.size() > 0) {
            ApiModel model = mapList.get(0);
            if (StringUtils.isNotBlank(model.getApiKey())) {
                model.setApiKey(CryptoUtils.decryptAESToString(model.getApiKey(), this.aesPass));
            }
            if (StringUtils.isBlank(model.getEmail())) {
                UserIdRequest userIdRequest = new UserIdRequest();
                userIdRequest.setUserId(Long.parseLong(model.getUserId()));
                GetUserEmailResponse getUserEmailResponse = this
                        .getAPIRequestResponse(this.userService.getUserEmailByUserId(this.newAPIRequest(userIdRequest)));
                if (getUserEmailResponse != null) {
                    model.setEmail(getUserEmailResponse.getEmail());
                }
            }
            return CopyBeanUtils.copy(model, ApiModelResponse.class);
        }
        return null;
    }

    private boolean isContainSpecialChar(String content) {
        return ILLEGAL_CHAR_PATTERN.matcher(content).find();
    }


    @Override
    @UserPermissionValidate(userId = "#body.loginUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_APIKEY)
    public ApiModelResponse saveApiKey(SaveApiKeyRequest body) throws Exception {
        String loginUserId = body.getLoginUserId();
        Pair<Boolean, Boolean> rt = countryBlacklistBusiness.isBlack(Long.valueOf(loginUserId));
        if (rt.getLeft()) {
            throw new BusinessException(GeneralCode.BLACKLISTUSER_NOT_ENABLED, rt.getRight() ?
                    messageHelper.getMessage(AccountErrorCode.COUNTRY_KYC_NOT_SPPORT) : messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT));
        }
        if (isContainSpecialChar(body.getApiName())) {
            throw new BusinessException(GeneralCode.SYS_VALID, "apiName包含非法字符");
        }

        if (body.getBackend().booleanValue()) {
            String userId = body.getTargetUserId();
            if (StringUtils.isBlank(userId)) {
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
            String ruleId = body.getRuleId();
            if (StringUtils.isBlank(ruleId)) {
                ruleId = String.valueOf(ApiManagerUtils.TRADE);
            }
            String apiName = body.getApiName();
            String ip = body.getIp();
            AuthTypeEnum operationType = body.getOperationType();
            String info = body.getInfo();
            String publicKey = body.getPublicKey();
            Boolean enableWithdrawStatus=body.getEnableWithdrawStatus();
            ApiModel model = this.backendGetApiKey(ruleId, apiName, ip, operationType, info, userId, loginUserId, publicKey,enableWithdrawStatus);
            return CopyBeanUtils.copy(model, ApiModelResponse.class);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("system_maintenance"))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
        if (body.getOperationType() == null) {
            throw new BusinessException(GeneralCode.SYS_VALID, "operationType必须是GOOGLE、SMS之一");
        }
        // 验证登录用户
        GetUserResponse userResponse = this.getCheckedUser(loginUserId);
        userSecurityApi.verificationsTwoV2(Long.parseLong(loginUserId),
                ObjectUtils.defaultIfNull(body.getOperationType(), AuthTypeEnum.GOOGLE),
                body.getVerifyCode(), SecurityKeyApplicationScenario.withdrawAndApi, true);

        ApiKeyVo apiKeyVo = this.matchboxApi.postApiKey(userResponse.getUserInfo().getTradingAccount().toString(),
                "api" + String.valueOf(System.currentTimeMillis()), "true", "true", "true", "true", "true", "false", body.getPublicKey());

        String uuid = UUID.randomUUID().toString().replace("-", "");
        ApiModel apiModel = new ApiModel();
        apiModel.setUuid(uuid);
        apiModel.setUserId(loginUserId);
        apiModel.setApiKey(CryptoUtils.encryptAESToString(apiKeyVo.getApiKey(), this.aesPass));
        apiModel.setKeyId(apiKeyVo.getKeyId().intValue());
        apiModel.setApiName(body.getApiName());
       // apiModel.setSecretKey(CryptoUtils.encryptAESToString(apiKeyVo.getSecretKey(), this.aesPass));
        apiModel.setSecretKey("xxxxxxxxxx");
        apiModel.setTradeIp(IP);
        apiModel.setWithdrawIp(IP);
        apiModel.setStatus(1);
        apiModel.setEnableWithdrawStatus(false);
        apiModel.setRuleId(String.valueOf(ApiManagerUtils.TRADE));
        apiModel.setInfo(StringUtils.defaultString(body.getInfo(), "account-service创建"));
        apiModel.setApiEmailVerify(false);
        apiModel.setCreateEmailSendTime(DateUtils.getNewUTCDate());
        apiModel.setCreateTime(DateUtils.getNewUTCDate());
        apiModel.setType(apiKeyVo.getType());

        this.apiModelMapper.insertWithId(apiModel);

        Map<String, Object> emailParams = Maps.newHashMap();
        emailParams.put("apiName", apiModel.getApiName());
        String link = this.getHttpBasePath() + "userCenter/createApi.html?id=" + uuid;
        if(StringUtils.isNotBlank(body.getEmailLink())){
            link = body.getEmailLink()+uuid;
        }
        emailParams.put("link", link);
        try {
            this.sendEmail(AccountConstants.NODE_TYPE_API_CREATE_ENABLE, emailParams, userResponse.getUser().getEmail(), loginUserId);
        } catch (Exception e) {
            log.error("API创建邮件发送失败:", e);
        }
        try {
            if(StringUtils.isNotBlank(apiKeyVo.getSecretKey())) {
                RedisCacheUtils.set(String.format(TEMP_CACHE_API_SECRET_KEY, uuid),CryptoUtils.encryptAESToString(apiKeyVo.getSecretKey(), this.aesPass), Constant.HOUR_HALF);
            }
        } catch (Exception e) {
            log.error("缓存secrectkey失败:", e);
        }

        //同步期货的apikey
        try {
            if(null!=userResponse.getUserInfo().getFutureUserId()){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                log.info("sync future apikey userid={},futureuserid={}",loginUserId,futureUserId);
                futureAccountApiClient.createFutureApiKey(futureUserInfo.getMeTradingAccount(),apiKeyVo.getApiKey(),
                        apiKeyVo.getKeyId(),apiKeyVo.getSecretKey(),"api" + String.valueOf(System.currentTimeMillis()));
                log.info("sync future apikey userid={},futureuserid={} end",loginUserId,futureUserId);
            }
        } catch (Exception e) {
            log.error("sync future apikey error", e);
        }

        // 同步期货交割合约的apikey
        try {
            if(null!=userResponse.getUserInfo().getFutureUserId()){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("sync future delivery apikey fail: futureUserInfo or deliveryTradingAccount is null, userid={},futureuserid={}", loginUserId, futureUserId);
                } else {
                    log.info("sync future delivery apikey userid={},futureuserid={}", loginUserId, futureUserId);
                    futureDeliveryAccountApiClient.createFutureApiKey(futureUserInfo.getDeliveryTradingAccount(), apiKeyVo.getApiKey(),
                            apiKeyVo.getKeyId(), apiKeyVo.getSecretKey(), "api" + String.valueOf(System.currentTimeMillis()));
                    log.info("sync future delivery apikey userid={},futureuserid={} end", loginUserId, futureUserId);
                }
            }
        } catch (Exception e) {
            log.error("sync future delivery apikey error", e);
        }
        //apiModel.setApiKey(apiKeyVo.getApiKey());
        //apiModel.setSecretKey(apiKeyVo.getSecretKey());
        return CopyBeanUtils.copy(apiModel, ApiModelResponse.class);
    }

    @Override
    @UserPermissionValidate(userId = "#request.loginUserId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_APIKEY)
    public SaveApiKeyV2Response saveApiKeyV2(SaveApiKeyV2Request request) throws Exception {
        String loginUserId = request.getLoginUserId();
        Pair<Boolean, Boolean> rt = countryBlacklistBusiness.isBlack(Long.valueOf(loginUserId));
        if (rt.getLeft()) {
            throw new BusinessException(GeneralCode.BLACKLISTUSER_NOT_ENABLED, rt.getRight() ?
                    messageHelper.getMessage(AccountErrorCode.COUNTRY_KYC_NOT_SPPORT) : messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT));
        }
        if (isContainSpecialChar(request.getApiName())) {
            throw new BusinessException(GeneralCode.SYS_VALID, "apiName包含非法字符");
        }

        validateAPiKeySysFunction();

        // 验证登录用户
        GetUserResponse userResponse = this.getCheckedUser(loginUserId);
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(Long.parseLong(loginUserId))
                .bizScene(BizSceneEnum.API_KEY_MANAGE)
                .emailVerifyCode(request.getEmailVerifyCode())
                .googleVerifyCode(request.getGoogleVerifyCode())
                .mobileVerifyCode(request.getMobileVerifyCode())
                .yubikeyVerifyCode(request.getYubikeyVerifyCode())
                .build();
        userSecurityApi.verifyMultiFactors(verify);

        ApiKeyVo apiKeyVo = this.matchboxApi.postApiKey(userResponse.getUserInfo().getTradingAccount().toString(),
                "api" + String.valueOf(System.currentTimeMillis()), "true", "true", "true",
                "true", "true", "false", request.getPublicKey());

        String uuid = UUID.randomUUID().toString().replace("-", "");
        ApiModel apiModel = new ApiModel();
        apiModel.setUuid(uuid);
        apiModel.setUserId(loginUserId);
        apiModel.setApiKey(CryptoUtils.encryptAESToString(apiKeyVo.getApiKey(), this.aesPass));
        apiModel.setKeyId(apiKeyVo.getKeyId().intValue());
        apiModel.setApiName(request.getApiName());
        apiModel.setSecretKey("xxxxxxxxxx");
        apiModel.setTradeIp(IP);
        apiModel.setWithdrawIp(IP);
        apiModel.setStatus(1);
        apiModel.setEnableWithdrawStatus(false);
        apiModel.setRuleId(String.valueOf(ApiManagerUtils.TRADE));
        apiModel.setInfo(StringUtils.defaultString(request.getInfo(), "account-service创建"));
        apiModel.setApiEmailVerify(true);
        apiModel.setCreateEmailSendTime(DateUtils.getNewUTCDate());
        apiModel.setCreateTime(DateUtils.getNewUTCDate());
        apiModel.setType(apiKeyVo.getType());

        this.apiModelMapper.insertWithId(apiModel);

        try {
            if (StringUtils.isNotBlank(apiKeyVo.getSecretKey())) {
                RedisCacheUtils.set(String.format(TEMP_CACHE_API_SECRET_KEY, uuid), CryptoUtils.encryptAESToString(apiKeyVo.getSecretKey(), this.aesPass), Constant.HOUR_HALF);
                apiModel.setSecretKey(apiKeyVo.getSecretKey());
            }
        } catch (Exception e) {
            log.error("缓存secrectkey失败:", e);
        }

        //同步期货的apikey
        try {
            if (null != userResponse.getUserInfo().getFutureUserId()) {
                Long futureUserId = userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                log.info("sync future apikey userid={},futureuserid={}", loginUserId, futureUserId);
                futureAccountApiClient.createFutureApiKey(futureUserInfo.getMeTradingAccount(), apiKeyVo.getApiKey(),
                        apiKeyVo.getKeyId(), apiKeyVo.getSecretKey(), "api" + String.valueOf(System.currentTimeMillis()));
                log.info("sync future apikey userid={},futureuserid={} end", loginUserId, futureUserId);
            }
        } catch (Exception e) {
            log.error("sync future apikey error", e);
        }

        // 同步期货交割合约的apikey
        try {
            if(null!=userResponse.getUserInfo().getFutureUserId()){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("sync future delivery apikey fail: futureUserInfo or deliveryTradingAccount is null, userid={},futureuserid={}", loginUserId, futureUserId);
                } else {
                    log.info("sync future delivery apikey userid={},futureuserid={}", loginUserId, futureUserId);
                    futureDeliveryAccountApiClient.createFutureApiKey(futureUserInfo.getDeliveryTradingAccount(), apiKeyVo.getApiKey(),
                            apiKeyVo.getKeyId(), apiKeyVo.getSecretKey(), "api" + String.valueOf(System.currentTimeMillis()));
                    log.info("sync future delivery apikey userid={},futureuserid={} end", loginUserId, futureUserId);
                }
            }
        } catch (Exception e) {
            log.error("sync future delivery apikey error", e);
        }

        apiModel.setApiKey(apiKeyVo.getApiKey());
        return CopyBeanUtils.copy(apiModel, SaveApiKeyV2Response.class);
    }

    private ApiModel backendGetApiKey(String ruleId, String apiName, String ip, AuthTypeEnum operationType, String info,
            String userId, String loginUserId, String publicKey,Boolean enableWithdrawStatus) {
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException(GeneralCode.SYS_VALID);
        }
        if ("3".equals(ruleId) && StringUtils.isBlank(ip)) {
            throw new BusinessException(GeneralCode.SYS_VALID, "如果可提现，则必须提供IP地址");
        }
        GetUserResponse userResponse = this.getCheckedUser(userId);
        boolean ipStatus = true;
        List<String> ipList = new ArrayList<String>();
        if (StringUtils.isNotBlank(ip)) {
            String ips[] = ip.split(",");
            for (int i = 0; i < ips.length; i++) {
                String ipStr = ips[i];
                Pattern pattern = Pattern.compile(RegexUtils.IP_REGEX);
                Matcher matcher = pattern.matcher(ipStr);
                // 字符串是否与正则表达式相匹配
                if (matcher.matches()) {
                    ipList.add(ipStr);
                } else {
                    ipStatus = false;
                }
            }
        }
        if (!ipStatus) {
            throw new BusinessException(GeneralCode.SYS_VALID, "ip地址错误");
        }
        if (IP.equals(ip)) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "IP地址不能为" + IP);
        }
        ApiKeyVo apiKeyVo = this.matchboxApi.postApiKey(userResponse.getUserInfo().getTradingAccount().toString(),
                "api" + String.valueOf(System.currentTimeMillis()), "true", "true", "true", "true", "true", "false", publicKey);
        ApiModel apiModel = new ApiModel();
        apiModel.setUserId(userId);
        apiModel.setApiKey(CryptoUtils.encryptAESToString(apiKeyVo.getApiKey(), this.aesPass));
        apiModel.setKeyId(apiKeyVo.getKeyId().intValue());
        apiModel.setApiName(apiName);
        apiModel.setSecretKey("xxxxxxxxxx");

        apiModel.setTradeIp("3".equals(ruleId) ? ip : IP);
        apiModel.setWithdrawIp("3".equals(ruleId) ? ip : IP);
        apiModel.setStatus("3".equals(ruleId) ? 2 : 1);
        apiModel.setRuleId(ruleId);
        apiModel.setInfo(StringUtils.defaultString(info, "后台account-service创建"));
        apiModel.setCreateTime(DateUtils.getNewUTCDate());
        apiModel.setType(apiKeyVo.getType());
        apiModel.setApiEmailVerify(true);
        if(null!=enableWithdrawStatus && enableWithdrawStatus.booleanValue()){
            apiModel.setEnableWithdrawStatus(enableWithdrawStatus.booleanValue());
        }
        this.apiModelMapper.insertWithId(apiModel);

        apiModel.setApiKey(apiKeyVo.getApiKey());
        apiModel.setSecretKey(apiKeyVo.getSecretKey());

        //同步期货的apikey
        try {
            if(null!=userResponse.getUserInfo().getFutureUserId()){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                log.info("sync future apikey userid={},futureuserid={}",userId,futureUserId);
                futureAccountApiClient.createFutureApiKey(futureUserInfo.getMeTradingAccount(),apiKeyVo.getApiKey(),
                        apiKeyVo.getKeyId(),apiKeyVo.getSecretKey(), "api" + String.valueOf(System.currentTimeMillis()));
                log.info("sync future apikey userid={},futureuserid={} end",userId,futureUserId);
            }
        } catch (Exception e) {
            log.error("sync future apikey error", e);
        }

        //同步期货交割合约的的apikey
        try {
            if(null!=userResponse.getUserInfo().getFutureUserId()){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("sync future delivery apikey fail: futureUserInfo or deliveryTradingAccount is null, userid={},futureuserid={}", userId, futureUserId);
                } else {
                    log.info("sync future delivery apikey userid={},futureuserid={}", userId, futureUserId);
                    futureDeliveryAccountApiClient.createFutureApiKey(futureUserInfo.getDeliveryTradingAccount(), apiKeyVo.getApiKey(),
                            apiKeyVo.getKeyId(), apiKeyVo.getSecretKey(), "api" + String.valueOf(System.currentTimeMillis()));
                    log.info("sync future delivery apikey userid={},futureuserid={} end", userId, futureUserId);
                }
            }
        } catch (Exception e) {
            log.error("sync future delivery apikey error", e);
        }


        //更新期货是否可以交易的权限
        try{
            if(null!=userResponse.getUserInfo().getFutureUserId() ){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                    log.info("sync permissions userid={},futureuserid={},trade=true",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), apiKeyVo.getKeyId(),false, true, true, true,
                            true, true);
                } else {
                    log.info("sync permissions userid={},futureuserid={},trade=false",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                    this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), apiKeyVo.getKeyId(),false, false, true, true,
                            true, true);
                }
            }
        }catch (Exception e){
            log.warn("futureAccountApiClient.updateApiKeyPermissions:", e);
        }

        //更新期货交割合约是否可以交易的权限
        try{
            if(null!=userResponse.getUserInfo().getFutureUserId() ){
                Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("futureDeliveryAccountApiClient.updateApiKeyPermissions fail, futureUserInfo or deliveryTradingAccount is null");
                } else {
                    if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                        log.info("sync delivery permissions userid={},futureuserid={},trade=true", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), apiKeyVo.getKeyId(), false, true, true, true,
                                true, true);
                    } else {
                        log.info("sync delivery permissions userid={},futureuserid={},trade=false", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                        this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), apiKeyVo.getKeyId(), false, false, true, true,
                                true, true);
                    }
                }
            }
        }catch (Exception e){
            log.warn("futureDeliveryAccountApiClient.updateApiKeyPermissions:", e);
        }


        // 记录操作日志
        try {
            OperateLogModel model = new OperateLogModel();
            model.setId(UUID.randomUUID().toString().replace("-", ""));
            model.setUserId(loginUserId);
            model.setOperateTime(new Date());
            model.setIpAddress(WebUtils.getRequestIp());
            model.setOperateType(AccountConstants.TYPE_ADD);
            model.setOperateModel(AccountConstants.MODEL_TRADE);
            model.setOperateResult(AccountConstants.RESULT_SUCCESS);
            model.setResInfo(
                    String.format("创建API成功：userId:%s,ruleId:%s,apiKey:%s,", userId, ruleId, apiKeyVo.getApiKey()));
            this.operateLogModelMapper.insert(model);
        } catch (Exception e) {
            log.error("创建API后记录操作日志出现异常", e);
        }
        return apiModel;
    }

    private void sendEmail(String tplCode, Map<String, Object> params, String email, String userId) throws Exception {
        String language = LanguageUtils.getLanguage(WebUtils.getHttpServletRequest());
        UserIdRequest body = new UserIdRequest();
        body.setUserId(Long.parseLong(userId));
//        if (StringUtils.isBlank(userId)) {
//            try {
//                UserSecurityLogVo userSecurityLogVo =
//                        this.getAPIRequestResponse(this.userSecurityLogApi.getLastLoginLog(this.newAPIRequest(body)));
//                String ipLocation = userSecurityLogVo.getIpLocation();
//                if (ipLocation != null) {
//                    if (ipLocation.endsWith(" China")) {
//                        language = "cn";
//                    } else {
//                        language = "en";
//                    }
//                }
//            } catch (Exception e) {
//                log.warn("获取用户最后一次登录信息出错", e);
//            }
//        }
        if (!("en").equals(language) && !("cn").equals(language)) {
            language = "en";
        }
        String ip = WebUtils.getRequestIp();
        params.put("ip", ip);
        params.put("time", DateUtils.getNewDateUTC() + " (UTC)");

        String emailVerifyCode = this.userCommonBusiness.generateAndSetDisableCode(body.getUserId());
//        String forbiddenLink = this.getHttpBasePath() + "forbiddenAccount.html?userId=" + userId + "&emailVerifyCode="
//                + emailVerifyCode;
        String forbiddenLink = String.format("%s%s/usercenter/security/disable-account",WebUtils.getHeader(Constant.BASE_URL),
                    StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en"));
        params.put("userId", userId);
        params.put("email", email);
        params.put("emailVerifyCode", emailVerifyCode);
        params.put("forbiddenLink", forbiddenLink);

        String antiCode =
                this.getAPIRequestResponse(this.userSecurityApi.selectAntiPhishingCode(this.newAPIRequest(body)));
        if (StringUtils.isNotBlank(antiCode)) {
            params.put("antiCode", antiCode);
        }
        // UserForbiddenCodeModel userForbiddenCode = new UserForbiddenCodeModel();
        // userForbiddenCode.setUserId(userId);
        // userForbiddenCode.setEmailVerifyCode(emailVerifyCode);
        // this.userForbiddenCodeModelMapper.insert(userForbiddenCode);
        SendMsgRequest sendMsgBody = new SendMsgRequest();
        sendMsgBody.setTplCode(tplCode);
        sendMsgBody.setAntiPhishingCode((String) params.get("antiCode"));
        sendMsgBody.setRecipient(email);
        sendMsgBody.setUserId(userId);
        sendMsgBody.setData(params);

        HeaderUtils.set("ip-address", WebUtils.getRequestIp());
        HeaderUtils.set("base-url", this.getHttpBasePath());
        HeaderUtils.set("base-brower", WebUtils.getOsAndBrowserInfo());
        APIRequest<SendMsgRequest> apiRequest = this.newAPIRequest(sendMsgBody, language);
        TerminalEnum terminal = WebUtils.getTerminal();
        if (terminal == null) {
            terminal = TerminalEnum.WEB;
        }
        apiRequest.setTerminal(terminal);
        String version = WebUtils.getHeader("versionCode");
        if (StringUtils.isBlank(version)) {
            version = WebUtils.getParameter("versionCode");
        }
        if (StringUtils.isNotBlank(version)) {
            apiRequest.setVersion(version);
        }
        this.msgApi.sendMsg(apiRequest);
    }

    @Override
    public String fetchAntiCode(Long userId) throws Exception {
        UserIdRequest body = new UserIdRequest();
        body.setUserId(userId);
        return getAPIRequestResponse(this.userSecurityApi.selectAntiPhishingCode(this.newAPIRequest(body)));
    }

    @Override
    public ApiModelResponse modifyApiKeyIpRestrictSwitch(UpdateApiKeyRestrictIpRequest request) throws Exception {
        log.info("modifyApiKeyIpRestrictSwitch:{}", request.toString());
        // 1. system check
        checkSystem();

        GetUserResponse userResponse = getCheckedUser(request.getUserId());
        ApiModel apiModel = apiModelMapper.selectByApiKey(CryptoUtils.encryptAESToString(request.getApiKey(), this.aesPass)); // api 查询加密
        if (!apiModel.getUserId().equals(request.getUserId())) {
            // margin userId
            // 检查用户防止安全漏洞
            if (!apiModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userResponse = getCheckedUser(apiModel.getUserId());
        }
        Long tradingAccount = userResponse.getUserInfo().getTradingAccount();

        // 2. 同步Ip
        // 限制Ip: 新增 + DB ，若空则默认填充 0.0.0.0
        // 不限制 0.0.0.0
        Set<String> ipSet = Sets.newHashSet();
        int status = request.getStatus();
        String ip = null;
        if (status == ApiKeyStatus.LIMITED.getCode()) {
            if (StringUtils.isNotBlank(request.getIp())) {
                if (request.getIp().contains(IP)) {
                    throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
                }
                ipSet.addAll(checkAndExtractIpList(request.getIp()));
            }
            if (StringUtils.isNotBlank(apiModel.getTradeIp())) {
                ipSet.addAll(extractIpList(apiModel.getTradeIp()));
            }
            if (ipSet.isEmpty()) {
                ipSet.addAll(extractIpList(IP));
            }
            if (ipSet.size() > apiKeyRestrictIpSize) {
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            ip = StringUtils.join(ipSet, IP_LIST_SEPARATOR);
        } else if (status == ApiKeyStatus.ALL.getCode()) {
            ipSet.addAll(extractIpList(IP));
            ip = IP;
        }
        checkWithDrawIpIfNecessary(apiModel.getRuleId(), ipSet);
        // 同步ip给撮合
        syncRestrictIp2MatchBox(ipSet, tradingAccount.toString(), apiModel.getKeyId().toString());
        // 同步ip给期货
        syncRestrictIp2Future(userResponse.getUserInfo(), apiModel, ip);

        // 3. 更新 DB
        apiModel.setStatus(status);
        if (status == ApiKeyStatus.LIMITED.getCode()) {
            apiModel.setTradeIp(ip);
            apiModel.setWithdrawIp(ip);
        }
        apiModel.setUpdateTime(new Date());
        apiModelMapper.updateByPrimaryKey(apiModel);

        return ApiModelResponse.builder()
                .apiKey(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass)) //返回结果解密
                .userId(apiModel.getUserId())
                .status(apiModel.getStatus())
                .withdrawIp(apiModel.getWithdrawIp())
                .tradeIp(apiModel.getTradeIp())
                .updateTime(apiModel.getUpdateTime()).build();
    }

    @Override
    public ApiModelResponse addApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception {
        log.info("addApiKeyRestrictIp:{}", request.toString());
        // 添加限制ip必须为有效ip
        if (StringUtils.isBlank(request.getIp()) || request.getIp().contains(IP)) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        checkSystem();

        ApiModel apiModel = apiModelMapper.selectByApiKey(CryptoUtils.encryptAESToString(request.getApiKey(), this.aesPass));
        if (apiModel.getStatus() == ApiKeyStatus.ALL.getCode()) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        GetUserResponse userResponse = getCheckedUser(request.getUserId());
        if (!apiModel.getUserId().equals(request.getUserId())) {
            // margin userId
            // 检查用户防止安全漏洞
            if (!apiModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userResponse = getCheckedUser(apiModel.getUserId());
        }
        Long tradingAccount = userResponse.getUserInfo().getTradingAccount();

        Set<String> ipSet = Sets.newHashSet();
        // 新增 + DB
        ipSet.addAll(checkAndExtractIpList(request.getIp()));
        List<String> oldIps = extractIpList(apiModel.getTradeIp());
        // 保留有效Ip
        ipSet.addAll(oldIps.stream().filter(s -> !s.equals(IP) && StringUtils.isNotBlank(s)).collect(Collectors.toList()));

        if (ipSet.size() > apiKeyRestrictIpSize) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        String ip = StringUtils.join(ipSet, IP_LIST_SEPARATOR);

        checkWithDrawIpIfNecessary(apiModel.getRuleId(), ipSet);
        syncRestrictIp2MatchBox(ipSet, tradingAccount.toString(), apiModel.getKeyId().toString());
        syncRestrictIp2Future(userResponse.getUserInfo(), apiModel, ip);

        apiModel.setTradeIp(ip);
        apiModel.setWithdrawIp(ip);
        apiModel.setUpdateTime(new Date());
        apiModelMapper.updateByPrimaryKey(apiModel);

        return ApiModelResponse.builder()
                .apiKey(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass))
                .userId(apiModel.getUserId())
                .status(apiModel.getStatus())
                .withdrawIp(apiModel.getWithdrawIp())
                .tradeIp(apiModel.getTradeIp())
                .updateTime(apiModel.getUpdateTime()).build();
    }

    @Override
    public ApiModelResponse deleteApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception {
        log.info("deleteApiKeyRestrictIp:{}", request.toString());
        checkSystem();

        ApiModel apiModel = apiModelMapper.selectByApiKey(CryptoUtils.encryptAESToString(request.getApiKey(), this.aesPass));
        if (apiModel.getStatus() == ApiKeyStatus.ALL.getCode()) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        GetUserResponse userResponse = getCheckedUser(request.getUserId());
        if (!apiModel.getUserId().equals(request.getUserId())) {
            // margin userId
            // 检查用户防止安全漏洞
            if (!apiModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userResponse = getCheckedUser(apiModel.getUserId());
        }
        Long tradingAccount = userResponse.getUserInfo().getTradingAccount();

        Set<String> deletedIps = Sets.newHashSet(checkAndExtractIpList(request.getIp()));
        Set<String> remainIps = Sets.newHashSet(extractIpList(apiModel.getTradeIp()));
        // 取差集
        remainIps.removeAll(deletedIps);
        if (remainIps.isEmpty()) {
            remainIps.add(IP);
        }
        String ip = StringUtils.join(remainIps, IP_LIST_SEPARATOR);

        checkWithDrawIpIfNecessary(apiModel.getRuleId(), remainIps);
        syncRestrictIp2MatchBox(remainIps, tradingAccount.toString(), apiModel.getKeyId().toString());
        syncRestrictIp2Future(userResponse.getUserInfo(), apiModel, ip);

        apiModel.setTradeIp(ip);
        apiModel.setWithdrawIp(ip);
        apiModel.setUpdateTime(new Date());
        apiModelMapper.updateByPrimaryKey(apiModel);

        return ApiModelResponse.builder()
                .apiKey(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass))
                .userId(apiModel.getUserId())
                .status(apiModel.getStatus())
                .withdrawIp(apiModel.getWithdrawIp())
                .tradeIp(apiModel.getTradeIp())
                .updateTime(apiModel.getUpdateTime()).build();
    }

    @Override
    public ApiModelResponse queryApiKeyRestrictIp(UpdateApiKeyRestrictIpRequest request) throws Exception {
        log.info("queryApiKeyRestrictIp:{}", request.toString());
        ApiModel apiModel = apiModelMapper.selectByApiKey(CryptoUtils.encryptAESToString(request.getApiKey(), this.aesPass));
        return ApiModelResponse.builder()
                .apiKey(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass))
                .userId(apiModel.getUserId())
                .status(apiModel.getStatus())
                .withdrawIp(apiModel.getWithdrawIp())
                .tradeIp(apiModel.getTradeIp())
                .updateTime(apiModel.getUpdateTime()).build();
    }

    @Override
    public void deleteApiKey(DeleteApiKeyRequest deleteApiKeyRequest) throws Exception {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue(AccountConstants.SYSTEM_MAINTENANCE))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        String loginUserId = deleteApiKeyRequest.getLoginUid();
        GetUserResponse user = this.getCheckedUser(loginUserId);
        UserInfoVo userInfo = user.getUserInfo();
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", deleteApiKeyRequest.getId());
        if (StringUtils.isNotBlank(deleteApiKeyRequest.getApiKey())) {
            param.put("apiKey", CryptoUtils.encryptAESToString(deleteApiKeyRequest.getApiKey(), this.aesPass));
        }
        List<ApiModel> apiModels = this.apiModelMapper.getApiByMap(param);
        if (apiModels == null || apiModels.isEmpty()) {
            throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
        }
        ApiModel apiModel = apiModels.get(0);
        if(!apiModel.getUserId().equals(loginUserId)) {
            //margin userId
            user = getCheckedUser(apiModel.getUserId());
            userInfo = user.getUserInfo();
        }
        this.deleteApiKey(loginUserId, userInfo, apiModel);
    }

    private void deleteApiKey(String loginUserId, UserInfoVo userInfo, ApiModel apiModel)
            {
        if (getApiExsit(userInfo.getTradingAccount(), apiModel.getKeyId())) {
            this.matchboxApi.deleteApiKey(userInfo.getTradingAccount().toString(), apiModel.getKeyId().toString());
            this.apiModelMapper.deleteById(apiModel.getId());
            // 记录操作日志
            operateLogService.insert(loginUserId, "api", AccountConstants.RESULT_SUCCESS, AccountConstants.TYPE_DELETE,
                    String.format(("用户:%s,删除apiKey:%s成功"), loginUserId,
                            CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass)));
        } else {
            this.apiModelMapper.deleteById(apiModel.getId());
            // 记录操作日志
            operateLogService.insert(loginUserId, "api", AccountConstants.RESULT_SUCCESS, AccountConstants.TYPE_DELETE,
                    String.format(("用户:%s,删除apiKey:%s成功"), loginUserId,
                            CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass)));
        }

        //同步删除期货的apikey
        try {
            if (null != userInfo.getFutureUserId()) {
                Long futureUserId = userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if(!createTimeAfterCreateFuture(apiModel,futureUserInfo)){
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }
                log.info("sync future deleteApiKey userid={},futureuserid={}", loginUserId, futureUserId);
                futureAccountApiClient.deleteApiKey(futureUserInfo.getMeTradingAccount(),apiModel.getKeyId().longValue());
                log.info("sync future deleteApiKey userid={},futureuserid={} end", loginUserId, futureUserId);
            }
        } catch (Exception e) {
            log.warn("sync future deleteApiKey error", e);
        }

        //同步删除期货交割合约的apikey
        try {
            if (null != userInfo.getFutureUserId()) {
                Long futureUserId = userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                    log.info("sync future delivery deleteApiKey fail: futureUserInfo or deliveryTradingAccount is null, userid={},futureuserid={}", loginUserId, futureUserId);
                } else {
                    if (!createTimeAfterCreateFuture(apiModel, futureUserInfo)) {
                        throw new BusinessException(GeneralCode.SYS_VALID);
                    }
                    log.info("sync future delivery deleteApiKey userid={},futureuserid={}", loginUserId, futureUserId);
                    futureDeliveryAccountApiClient.deleteApiKey(futureUserInfo.getDeliveryTradingAccount(), apiModel.getKeyId().longValue());
                    log.info("sync future delivery deleteApiKey userid={},futureuserid={} end", loginUserId, futureUserId);
                }
            }
        } catch (Exception e) {
            log.warn("sync future delivery deleteApiKey error", e);
        }
        this.afterDeteApiKey(apiModel, loginUserId);
    }

    private void afterDeteApiKey(ApiModel apiModel, String loginUserId) {
        try {
            ApiDeletedModel deletedModel = CopyBeanUtils.copy(apiModel, ApiDeletedModel.class);
            deletedModel.setOriginalId(apiModel.getId());
            deletedModel.setId(null);
            try {
                deletedModel.setEmail(this.getUser(loginUserId).getUser().getEmail());
            } catch (Exception e) {
                log.warn("afterDeteApiKey occurs error:", e);
            }
            this.apiDeletedModelMapper.insert(deletedModel);
        } catch (Exception e) {
            log.error("保存删除的api出错：{},{}", apiModel == null ? null : JSON.toJSONString(apiModel), e);
        }
    }

    @Override
    public void deleteAllApiKey(DeleteAllApiKeyRequest body) throws Exception {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue(AccountConstants.SYSTEM_MAINTENANCE))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        String loginUserId = body.getLoginUid();
        GetUserResponse user = this.getCheckedUser(loginUserId);
        UserInfoVo userInfo = user.getUserInfo();
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", loginUserId);
        List<ApiModel> apiModels = this.apiModelMapper.getApiByMap(param);

        UserInfoVo marginUserInfo = null;
        if(userInfo.getMarginUserId() != null) {
            marginUserInfo = this.getCheckedUser(String.valueOf(userInfo.getMarginUserId())).getUserInfo();
            param.put("userId", userInfo.getMarginUserId().toString());
            apiModels.addAll(this.apiModelMapper.getApiByMap(param));
        }

        if (apiModels == null || apiModels.isEmpty()) {
            throw new BusinessException(GeneralCode.API_KEY_NOT_FOUND);
        }
        for (ApiModel apiModel : apiModels) {
            if(!apiModel.getUserId().equals(loginUserId)) {
                //margin userId
                this.deleteApiKey(loginUserId, marginUserInfo, apiModel);
            } else {
                this.deleteApiKey(loginUserId, userInfo, apiModel);
            }
        }
    }

    private Boolean getApiExsit(Long accountId, Integer keyId) {
        try {
            Boolean status = false;
            String json = this.matchboxApi.getApiKeys(accountId.toString());
            if (json != null) {
                JSONArray jsonArray = JSON.parseArray(json);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getIntValue("keyId") == keyId) {
                        return true;
                    }
                }
            }
            return status;
        } catch (Exception e) {
            return true;
        }
    }

    private void checkSubUserWithdraw(String userId, Integer ruleId) throws Exception {
        // 勾选了"开放提现"或"内部划转"
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))
                || ApiManagerUtils.isInternalTransferEnabled(Long.valueOf(ruleId))) {
            // 子账户禁止使用该功能
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(Long.parseLong(userId));
            Boolean checkResp = this.getAPIRequestResponse(
                    this.subUser.notSubUserOrIsEnabledSubUser(this.newAPIRequest(userIdReq)));
            if (!BooleanUtils.isTrue(checkResp)) {
                throw new BusinessException("子账户禁止使用该功能");
            }
        }
    }

    private void checkSystem() {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("system_maintenance"))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
    }

    private ApiModel getApiModelById(Long id) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", id);
        List<ApiModel> apiManageModels = this.apiModelMapper.getApiByMap(param);
        if (apiManageModels == null || apiManageModels.isEmpty()) {
            throw new BusinessException(GeneralCode.KEY_API_KEY_NOT_EXIST);
        }
        ApiModel apiManageModel = apiManageModels.get(0);
        if (!apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_NOT_BEEN_VERIFY);
        }

        return apiManageModel;
    }

    private List<String> apiKeyIpCheck(String ip, Integer status, Integer ruleId) {
        if (status.equals(1)) {
            ip = IP;
        } else {
            if (StringUtils.isBlank(ip)) {
                throw new BusinessException(GeneralCode.SYS_VALID);
            }
        }
        boolean ipStatus = true;
        List<String> ipList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(ip)) {
            String ips[] = ip.split(",");
            for (int i = 0; i < ips.length; i++) {
                String ipStr = ips[i];
                Pattern pattern = Pattern.compile(RegexUtils.IP_REGEX);
                Matcher matcher = pattern.matcher(ipStr);
                // 字符串是否与正则表达式相匹配
                if (matcher.matches()) {
                    ipList.add(ipStr);
                } else {
                    ipStatus = false;
                }
            }
        }
        if (!ipStatus) {
            throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_ILLEGEL);
        }
        // 提现时ip必须除了0.0.0.0还有其他的地址
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
            if (ipList.size() == 1 && ip.contains(IP)) {
                throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_APPOINT);
            }
        }

        return ipList;
    }

    @Override
    public UpdateApiKeyResponse updateApiKey(UpdateApiKeyRequest request) throws Exception {
        log.info("updateApiRequest:{}", request.toString());
        UpdateApiKeyResponse response = new UpdateApiKeyResponse();

        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("system_maintenance"))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
        Long id = request.getId();
        String userId = request.getUserId();
        String ip = request.getIp();
        Integer ruleId = request.getRuleId();
        GetUserResponse userResponse = this.getCheckedUser(userId);
        UserInfoVo userInfo = userResponse.getUserInfo();

        // 勾选了"开放提现"或"内部划转"
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))
                || ApiManagerUtils.isInternalTransferEnabled(Long.valueOf(ruleId))) {
            // 子账户禁止使用该功能
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(Long.parseLong(userId));
            Boolean checkResp = this.getAPIRequestResponse(
                    this.subUser.notSubUserOrIsEnabledSubUser(this.newAPIRequest(userIdReq)));
            if (!BooleanUtils.isTrue(checkResp)) {
                throw new BusinessException("子账户禁止使用该功能");
            }
        }
        //2fa用当前登录人的2fa来check，这么做的目的是有些请求是母账号修改子账号的apikey，那么肯定得用母账号的2fa来check
        if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getLoginUid())){
            userSecurityApi.verificationsTwoV2(Long.parseLong(request.getLoginUid()), ObjectUtils.defaultIfNull(request.getOperationType(), AuthTypeEnum.GOOGLE),
                    request.getVerifyCode(), SecurityKeyApplicationScenario.withdrawAndApi, true);
        }else{
            userSecurityApi.verificationsTwoV2(Long.parseLong(userId), ObjectUtils.defaultIfNull(request.getOperationType(), AuthTypeEnum.GOOGLE),
                    request.getVerifyCode(), SecurityKeyApplicationScenario.withdrawAndApi, true);
        }

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", id);
        List<ApiModel> apiManageModels = this.apiModelMapper.getApiByMap(param);
        if (apiManageModels == null || apiManageModels.isEmpty()) {
            throw new BusinessException(GeneralCode.KEY_API_KEY_NOT_EXIST);
        }
        ApiModel apiManageModel = apiManageModels.get(0);
        if (!apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_NOT_BEEN_VERIFY);
        }

        Long tradingAccount = userInfo.getTradingAccount();
        String email = userResponse.getUser().getEmail();
        if(!apiManageModel.getUserId().equals(userId)) {
            //margin userId
            //检查用户防止安全漏洞
            if(!apiManageModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userResponse = getCheckedUser(apiManageModel.getUserId());
            tradingAccount = userResponse.getUserInfo().getTradingAccount();
        }
        Map<String, Object> ruleMap = getRuleMap(tradingAccount, apiManageModel.getKeyId());
        Map<String, Object> emailParams = Maps.newHashMap();
        if (request.getStatus() == 1) {
            ip = IP;
        } else {
            if (StringUtils.isBlank(ip)) {
                throw new BusinessException(AccountErrorCode.PLEASE_INPUT_TRUST_IP);
            }
        }
        boolean ipStatus = true;
        List<String> ipList = new ArrayList<String>();
        if (StringUtils.isNotEmpty(ip)) {
            String ips[] = ip.split(",");
            for (int i = 0; i < ips.length; i++) {
                String ipStr = ips[i];
                Pattern pattern = Pattern.compile(RegexUtils.IP_REGEX);
                Matcher matcher = pattern.matcher(ipStr);
                // 字符串是否与正则表达式相匹配
                if (matcher.matches()) {
                    ipList.add(ipStr);
                } else {
                    ipStatus = false;
                }
            }
        }
        if (!ipStatus) {
            throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_ILLEGEL);
        }
        // 提现时ip必须除了0.0.0.0还有其他的地址
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
            if (ipList.size() == 1 && ip.contains(IP)) {
                throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_APPOINT);
            }
        }

        for (int i = 0; i < ipList.size(); i++) {
            String ipItem = ipList.get(i);
            if (ruleMap.get(ipItem) == null) {
                this.matchboxApi.postApiKeyRule(tradingAccount.toString(), ipItem,
                        apiManageModel.getKeyId().toString());
                log.info("updateApi  --saveApiRule:{}", ipItem);
            } else {
                ruleMap.remove(ipItem);
            }
        }

        log.info("updateApi...ip end");

        for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
            this.matchboxApi.deleteApiKeyRule(tradingAccount.toString(), apiManageModel.getKeyId().toString(),
                    entry.getValue().toString());
        }

        log.info("updateApi...deleteRuleByRuleId end");

        if (!apiManageModel.getRuleId().equals(String.valueOf(ruleId))) {
            // 是否可以交易权限
            if (ApiManagerUtils.isTradeEnabled(Long.valueOf(ruleId))) {
                this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "true", "true",
                        "true", apiManageModel.getKeyId().toString(), "false");
            } else {
                this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "false", "true",
                        "true", apiManageModel.getKeyId().toString(), "false");
            }
            //更新期货是否可以交易的权限
            try{
                if(null!=userResponse.getUserInfo().getFutureUserId() ){
                    Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                    UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                    if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    if(!createTimeAfterCreateFuture(apiManageModel,futureUserInfo)){
                        throw new BusinessException(GeneralCode.SYS_VALID);
                    }
                    if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                        log.info("sync permissions userid={},futureuserid={},trade=true",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                        this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiManageModel.getKeyId()),false, true, true, true,
                                true, true);
                    } else {
                        log.info("sync permissions userid={},futureuserid={},trade=false",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                        this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiManageModel.getKeyId()),false, false, true, true,
                                true, true);
                    }
                }
            }catch (Exception e){
                log.warn("futureAccountApiClient.updateApiKeyPermissions:", e);
            }

            //更新期货交割合约是否可以交易的权限
            try{
                if(null!=userResponse.getUserInfo().getFutureUserId() ){
                    Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                    UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                    if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                        log.info("futureDeliveryAccountApiClient.updateApiKeyPermissions fail, futureUserInfo or deliveryTradingAccount is null");
                    } else {
                        if (!createTimeAfterCreateFuture(apiManageModel, futureUserInfo)) {
                            throw new BusinessException(GeneralCode.SYS_VALID);
                        }
                        if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                            log.info("sync delivery permissions userid={},futureuserid={},trade=true", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                            this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiManageModel.getKeyId()), false, true, true, true,
                                    true, true);
                        } else {
                            log.info("sync delivery permissions userid={},futureuserid={},trade=false", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                            this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiManageModel.getKeyId()), false, false, true, true,
                                    true, true);
                        }
                    }
                }
            }catch (Exception e){
                log.warn("futureDeliveryAccountApiClient.updateApiKeyPermissions:", e);
            }

        }

        // 同步ip限制给期货
        try {
            if (null != userResponse.getUserInfo().getFutureUserId()) {
                Long futureUserId = userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo)) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if (!createTimeAfterCreateFuture(apiManageModel, futureUserInfo)) {
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }

                // 同步ip限制给永续
                if (futureUserInfo.getMeTradingAccount() != null) {
                    try {
                        this.futureAccountApiClient.updateApiKeyRules(Long.valueOf(apiManageModel.getKeyId()), futureUserInfo.getMeTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureAccountApiClient.updateApiKeyRules:", e);
                    }
                }

                // 同步ip限制给交割
                if (futureUserInfo.getDeliveryTradingAccount() != null) {
                    try {
                        this.futureDeliveryAccountApiClient.updateApiKeyRules(Long.valueOf(apiManageModel.getKeyId()), futureUserInfo.getDeliveryTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureDeliveryAccountApiClient.updateApiKeyRules:", e);
                    }
                }
                log.info("updateApi...syncIpToFutures end");
            }
        }catch (Exception e){
            log.warn("updateApi syncIpToFutures error:", e);
        }

        String preRuleId = apiManageModel.getRuleId();
        apiManageModel.setApiName(request.getApiName());
        apiManageModel.setInfo(request.getInfo());
        apiManageModel.setTradeIp(ip);
        apiManageModel.setStatus(request.getStatus());
        apiManageModel.setRuleId(String.valueOf(ruleId));
        apiManageModel.setWithdrawIp(ip);
        apiManageModel.setUpdateTime(new Date());

        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
            if (!apiManageModel.isEnableWithdrawStatus()) {
                String vCode = UUID.randomUUID().toString().replace("-", "");
                apiManageModel.setEnableWithdrawStatus(false);
                apiManageModel.setWithdrawVerifycode(vCode);
                apiManageModel.setWithdrawVerifycodeTime(new Date());
                emailParams.put("apiName", request.getApiName());
                String link = this.getHttpBasePath()
                        + "gateway-api/v1/public/api-mgmt/api/enable-api-withdraw?verifyCode=" + vCode;
                emailParams.put("link", link);
                response.setFrontendTip(this.messageHelper.getMessage(GeneralCode.API_KEY_WITHDRAW_EAMIL_ENABEL));
            }
        } else {
            apiManageModel.setEnableWithdrawStatus(false);
            apiManageModel.setWithdrawVerifycode(null);
            apiManageModel.setWithdrawVerifycodeTime(null);
        }
        this.apiModelMapper.updateByPrimaryKey(apiManageModel);
        log.info("updateApi...preRuleId:{}", preRuleId);
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId)) && !apiManageModel.isEnableWithdrawStatus()) {
            try {
                this.sendEmail(AccountConstants.NODE_TYPE_API_WITHDRAW_ENABLE, emailParams,
                        email, userId);
            } catch (Exception e) {
                log.error("API提现确认邮件发送失败:", e);
            }
        }
        response.setUserId(request.getUserId());
        return response;
    }

    @Override
    public UpdateApiKeyResponse updateApiKeyV2(UpdateApiKeyRequest request) throws Exception {

        checkSystem();

        String userId = request.getUserId();
        Integer ruleId = request.getRuleId();
        checkSubUserWithdraw(userId, ruleId);

        userSecurityApi.verificationsTwoV2(Long.parseLong(userId), ObjectUtils.defaultIfNull(request.getOperationType(), AuthTypeEnum.GOOGLE),
                request.getVerifyCode(), SecurityKeyApplicationScenario.withdrawAndApi, true);

        String ip = request.getIp();
        apiKeyIpCheck(ip, request.getStatus(), ruleId);

        String vCode = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> emailParams = Maps.newHashMap();
        emailParams.put("apiName", request.getApiName());
        String link = this.getHttpBasePath()
                + "gateway-api/v1/public/api-mgmt/api/enable-api-update?verifyCode=" + vCode;
        emailParams.put("link", link);
        UpdateApiKeyResponse response = new UpdateApiKeyResponse();
        response.setFrontendTip(this.messageHelper.getMessage(GeneralCode.API_KEY_UPDATE_EAMIL_ENABLE));

        Long id = request.getId();
        ApiModel apiManageModel = getApiModelById(id);
        if (apiManageModel == null) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_NOT_EXIST);
        }

        //检查用户防止安全漏洞
        if(!userId.equals(apiManageModel.getUserId())) {
            GetUserResponse userResponse = getCheckedUser(userId);
            if(!apiManageModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
        }

        this.apiModelMapper.updateApikeyVerifyCode(vCode, id);

        GetUserResponse userResponse = this.getCheckedUser(userId);
        try {
            this.sendEmail(AccountConstants.NODE_TYPE_API_WITHDRAW_ENABLE, emailParams,
                    userResponse.getUser().getEmail(), userId);
        } catch (Exception e) {
            log.error("APIKEY更新确认邮件发送失败:", e);
        }

        //设置redis
        RedisCacheUtils.set(String.format(TEMP_CACHE_API_KEY_UPDATE, vCode), id + "#" + request.getStatus() + "#" + ruleId + "#" + ip, Constant.HOUR_HALF);

        response.setUserId(request.getUserId());
        return response;
    }

    @Override
    public UpdateApiKeyV3Response updateApiKeyV3(UpdateApiKeyV3Request request) throws Exception {
        log.info("UpdateApiKeyV3Request:{}", request.toString());
        validateAPiKeySysFunction();

        String userId = request.getUserId();
        GetUserResponse userResponse = this.getCheckedUser(userId);

        // 勾选了"开放提现"或"内部划转"
        Integer ruleId = request.getRuleId();
        boolean isEnableWithdraw = ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId));
        if (isEnableWithdraw || ApiManagerUtils.isInternalTransferEnabled(Long.valueOf(ruleId))) {
            // 子账户禁止使用该功能
            UserIdReq userIdReq = new UserIdReq();
            userIdReq.setUserId(Long.parseLong(userId));
            Boolean checkResp = this.getAPIRequestResponse(this.subUser.notSubUserOrIsEnabledSubUser(this.newAPIRequest(userIdReq)));
            if (!BooleanUtils.isTrue(checkResp)) {
                throw new BusinessException("子账户禁止使用该功能");
            }
        }

        // 根据ruleId来区分api修改场景
        BizSceneEnum bizScene = BizSceneEnum.API_EDIT_SWITCH;

        List<ApiModel> apiManageModels = this.apiModelMapper.getApiByMap(new HashMap<String, Object>(){{put("id", request.getId());}});
        if (CollectionUtils.isEmpty(apiManageModels)) {
            throw new BusinessException(GeneralCode.KEY_API_KEY_NOT_EXIST);
        }
        ApiModel apiManageModel = apiManageModels.get(0);
        if (!apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_NOT_BEEN_VERIFY);
        }
        if (isEnableWithdraw) {
           if (!apiManageModel.isDisableStatus()) {
                throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_DISABLE);
            }
        }
        boolean isEnableWithdrawOld = ApiManagerUtils.isWithdrawEnabled(Long.valueOf(apiManageModel.getRuleId()));
        boolean isChangeEnableWithdraw=!isEnableWithdrawOld && isEnableWithdraw;
        log.info("oldRuleId={},newRuleId={}",apiManageModel.getRuleId(),ruleId);
        if(isChangeEnableWithdraw){
            bizScene = BizSceneEnum.API_WITHDRAW_SWITCH ;
        }

        //2fa用当前登录人的2fa来check，这么做的目的是有些请求是母账号修改子账号的apikey，那么肯定得用母账号的2fa来check
        String twofaUserId = request.getUserId();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getLoginUid())) {
            twofaUserId = request.getLoginUid();
        }
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(Long.parseLong(twofaUserId))
                .bizScene(bizScene)
                .emailVerifyCode(request.getEmailVerifyCode())
                .googleVerifyCode(request.getGoogleVerifyCode())
                .mobileVerifyCode(request.getMobileVerifyCode())
                .yubikeyVerifyCode(request.getYubikeyVerifyCode())
                .build();
        userSecurityApi.verifyMultiFactors(verify);

        Long tradingAccount = userResponse.getUserInfo().getTradingAccount();
        if(!apiManageModel.getUserId().equals(userId)) {
            //margin userId
            //检查用户防止安全漏洞
            if(!apiManageModel.getUserId().equals(String.valueOf(userResponse.getUserInfo().getMarginUserId()))) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userResponse = getCheckedUser(apiManageModel.getUserId());
            tradingAccount = userResponse.getUserInfo().getTradingAccount();
        }

        String ip = request.getIp();
        if (request.getStatus() == 1) {
            ip = IP;
        }
        if (StringUtils.isBlank(ip)) {
            throw new BusinessException(AccountErrorCode.PLEASE_INPUT_TRUST_IP);
        }
        List<String> ipList = new ArrayList<String>();
        String[] ips = ip.split(",");
        for (String ipStr : ips) {
            Pattern pattern = Pattern.compile(RegexUtils.IP_REGEX);
            Matcher matcher = pattern.matcher(ipStr);
            // 字符串是否与正则表达式相匹配
            if (matcher.matches()) {
                ipList.add(ipStr);
            } else {
                throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_ILLEGEL);
            }
        }

        // 提现时ip必须除了0.0.0.0还有其他的地址
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
            if (ipList.size() == 1 && ip.contains(IP)) {
                throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_APPOINT);
            }
        }

        Map<String, Object> ruleMap = getRuleMap(tradingAccount, apiManageModel.getKeyId());
        for (String ipItem : ipList) {
            if (ruleMap.get(ipItem) == null) {
                this.matchboxApi.postApiKeyRule(tradingAccount.toString(), ipItem, apiManageModel.getKeyId().toString());
                log.info("updateApiKeyV3  --saveApiRule:{}", ipItem);
            } else {
                ruleMap.remove(ipItem);
            }
        }

        log.info("updateApiKeyV3...ip end");

        for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
            this.matchboxApi.deleteApiKeyRule(tradingAccount.toString(), apiManageModel.getKeyId().toString(),
                    entry.getValue().toString());
        }

        log.info("updateApiKeyV3...deleteRuleByRuleId end");

        if (!apiManageModel.getRuleId().equals(String.valueOf(ruleId))) {
            // 是否可以交易权限
            if (ApiManagerUtils.isTradeEnabled(Long.valueOf(ruleId))) {
                this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "true", "true",
                        "true", apiManageModel.getKeyId().toString(), "false");
            } else {
                this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "false", "true",
                        "true", apiManageModel.getKeyId().toString(), "false");
            }
            //更新期货是否可以交易的权限
            try{
                if(null!=userResponse.getUserInfo().getFutureUserId() ){
                    Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                    UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                    if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                        throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                    }
                    if(!createTimeAfterCreateFuture(apiManageModel,futureUserInfo)){
                        throw new BusinessException(GeneralCode.SYS_VALID);
                    }
                    if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                        log.info("sync permissions userid={},futureuserid={},trade=true",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                        this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiManageModel.getKeyId()),false, true, true, true,
                                true, true);
                    } else {
                        log.info("sync permissions userid={},futureuserid={},trade=false",userResponse.getUserInfo().getUserId(),userResponse.getUserInfo().getFutureUserId());
                        this.futureAccountApiClient.updateApiKeyPermissions(futureUserInfo.getMeTradingAccount(), Long.valueOf(apiManageModel.getKeyId()),false, false, true, true,
                                true, true);
                    }
                }
            }catch (Exception e){
                log.warn("futureAccountApiClient.updateApiKeyPermissions:", e);
            }

            //更新期货交割合约是否可以交易的权限
            try{
                if(null!=userResponse.getUserInfo().getFutureUserId() ){
                    Long futureUserId=userResponse.getUserInfo().getFutureUserId();
                    UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                    if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getDeliveryTradingAccount())) {
                        log.info("futureDeliveryAccountApiClient.updateApiKeyPermissions fail, futureUserInfo or deliveryTradingAccount is null");
                    } else {
                        if (!createTimeAfterCreateFuture(apiManageModel, futureUserInfo)) {
                            throw new BusinessException(GeneralCode.SYS_VALID);
                        }
                        if (ApiManagerUtils.isFutureTradeEnabled(Long.valueOf(ruleId))) {
                            log.info("sync delivery permissions userid={},futureuserid={},trade=true", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                            this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiManageModel.getKeyId()), false, true, true, true,
                                    true, true);
                        } else {
                            log.info("sync delivery permissions userid={},futureuserid={},trade=false", userResponse.getUserInfo().getUserId(), userResponse.getUserInfo().getFutureUserId());
                            this.futureDeliveryAccountApiClient.updateApiKeyPermissions(futureUserInfo.getDeliveryTradingAccount(), Long.valueOf(apiManageModel.getKeyId()), false, false, true, true,
                                    true, true);
                        }
                    }
                }
            }catch (Exception e){
                log.warn("futureDeliveryAccountApiClient.updateApiKeyPermissions:", e);
            }
        }

        // 同步ip限制给期货
        try {
            if (null != userResponse.getUserInfo().getFutureUserId()) {
                Long futureUserId = userResponse.getUserInfo().getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo)) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if (!createTimeAfterCreateFuture(apiManageModel, futureUserInfo)) {
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }

                // 同步ip限制给永续
                if (futureUserInfo.getMeTradingAccount() != null) {
                    try {
                        this.futureAccountApiClient.updateApiKeyRules(Long.valueOf(apiManageModel.getKeyId()), futureUserInfo.getMeTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureAccountApiClient.updateApiKeyRules:", e);
                    }
                }

                // 同步ip限制给交割
                if (futureUserInfo.getDeliveryTradingAccount() != null) {
                    try {
                        this.futureDeliveryAccountApiClient.updateApiKeyRules(Long.valueOf(apiManageModel.getKeyId()), futureUserInfo.getDeliveryTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureDeliveryAccountApiClient.updateApiKeyRules:", e);
                    }
                }
                log.info("updateApiKeyV3...syncIpToFutures end");
            }
        }catch (Exception e){
            log.warn("updateApiKeyV3 syncIpToFutures error:", e);
        }

        apiManageModel.setApiName(request.getApiName());
        apiManageModel.setTradeIp(ip);
        apiManageModel.setStatus(request.getStatus());
        apiManageModel.setRuleId(String.valueOf(ruleId));
        apiManageModel.setWithdrawIp(ip);
        apiManageModel.setUpdateTime(new Date());
        apiManageModel.setWithdrawVerifycodeTime(null);
        apiManageModel.setWithdrawVerifycode(null);
        apiManageModel.setEnableWithdrawStatus(isEnableWithdraw);
        this.apiModelMapper.updateByPrimaryKey(apiManageModel);
        log.info("updateApiKeyV3...preRuleId:{}", apiManageModel.getRuleId());

        return new UpdateApiKeyV3Response();
    }

    @Override
    public EnableUpdateApiKeyResponse enableUpdateApiKey(EnableUpdateApiKeyRequest request) throws Exception {

        checkSystem();
        String vCode = request.getVerifyCode();
        String redisValue = RedisCacheUtils.get(String.format(TEMP_CACHE_API_KEY_UPDATE, vCode));
        if(org.apache.commons.lang3.StringUtils.isBlank(redisValue)) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_TIME_FAIL);
        }

        String redisValues[] = redisValue.split("#");

        Long id = Long.valueOf(redisValues[0]);
        Integer status  = Integer.valueOf(redisValues[1]);
        Integer ruleId = Integer.valueOf(redisValues[2]);
        String ip = redisValues[3];

        String userId = request.getUserId();
        GetUserResponse userResponse = this.getCheckedUser(userId);
        UserInfoVo userInfo = userResponse.getUserInfo();

        checkSubUserWithdraw(userId, ruleId);

        ApiModel apiManageModel = getApiModelById(id);
        if (apiManageModel == null) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_NOT_EXIST);
        } else if (!apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_NOT_BEEN_VERIFY);
        } else if (!apiManageModel.isDisableStatus()) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_DISABLE);
        } else {
            Long tradingAccount = userInfo.getTradingAccount();
            if(!apiManageModel.getUserId().equals(userId)) {
                //margin userId
                userResponse = getCheckedUser(apiManageModel.getUserId());
                tradingAccount = userResponse.getUserInfo().getTradingAccount();
            }

            Map<String, Object> ruleMap = getRuleMap(tradingAccount, apiManageModel.getKeyId());

            List<String> ipList = apiKeyIpCheck(ip, status, ruleId);
            for (int i = 0; i < ipList.size(); i++) {
                String ipItem = ipList.get(i);
                if (ruleMap.get(ipItem) == null) {
                    this.matchboxApi.postApiKeyRule(tradingAccount.toString(), ipItem,
                            apiManageModel.getKeyId().toString());
                    log.info("updateApi  --saveApiRule:{}", ipItem);
                } else {
                    ruleMap.remove(ipItem);
                }
            }

            log.info("updateApi...ip end");

            for (Map.Entry<String, Object> entry : ruleMap.entrySet()) {
                this.matchboxApi.deleteApiKeyRule(tradingAccount.toString(), apiManageModel.getKeyId().toString(),
                        entry.getValue().toString());
            }

            log.info("updateApi...deleteRuleByRuleId end");

            if (!apiManageModel.getRuleId().equals(String.valueOf(ruleId))) {
                // 是否可以交易权限
                if (ApiManagerUtils.isTradeEnabled(Long.valueOf(ruleId))) {
                    this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "true", "true",
                            "true", apiManageModel.getKeyId().toString(), "false");
                } else {
                    this.matchboxApi.putApiKeyPermissions(tradingAccount.toString(), "true", "true", "false", "true",
                            "true", apiManageModel.getKeyId().toString(), "false");
                }
            }

            apiManageModel.setTradeIp(ip);
            apiManageModel.setStatus(status);
            apiManageModel.setRuleId(String.valueOf(ruleId));
            apiManageModel.setWithdrawIp(ip);
            apiManageModel.setUpdateTime(new Date());
            apiManageModel.setWithdrawVerifycode("");

            if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
                apiManageModel.setEnableWithdrawStatus(true);
            } else {
                apiManageModel.setEnableWithdrawStatus(false);
            }
            this.apiModelMapper.updateByPrimaryKey(apiManageModel);
        }

        return new EnableUpdateApiKeyResponse();
    }

    private Map<String, Object> getRuleMap(long accountId, Integer keyId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String resultStr = this.matchboxApi.getApiKeys(String.valueOf(accountId));
        if (StringUtils.isNotEmpty(resultStr)) {
            JSONArray jsonArray = JSONArray.parseArray(resultStr);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (Objects.equals(jsonObject.getInteger("keyId"), keyId)) {
                    JSONArray ruleArray = null;
                    try {
                        ruleArray = jsonObject.getJSONArray("rules");
                    } catch (Exception e) {
                        ruleArray = jsonObject.getJSONArray("rules:");
                    }
                    for (int j = 0; j < ruleArray.size(); j++) {
                        JSONObject ruleObject = ruleArray.getJSONObject(j);
                        resultMap.put(ruleObject.getString("ip"), ruleObject.getInteger("ruleId"));
                    }
                }
            }
        }
        return resultMap;
    }

    @Override
    public ApiModelResponse enableApiCreate(EnableApiCreateRequest body) throws Exception {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue(AccountConstants.SYSTEM_MAINTENANCE))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
        ApiModel apiManageModel = this.apiModelMapper.selectModelByUuid(body.getUuid(), body.getUserId());
        if (apiManageModel == null) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_NOT_EXIST);
        } else if (apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_HAVE_BEEN_VERIFY);
        } else if (!apiManageModel.isDisableStatus()) {
            throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_DISABLE);
        } else {
            Long tempTime = DateUtils.getNewUTCTimeMillis() - apiManageModel.getCreateEmailSendTime().getTime();
            if (tempTime > (30 * 60 * 1000L)) {
                throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_TIME_FAIL);
            }
            apiManageModel.setApiEmailVerify(true);
            this.apiModelMapper.updateEmailVerifyStatus(true, apiManageModel.getId(), apiManageModel.getUserId());
            String cacheSecretKey="";
            try {
                cacheSecretKey= RedisCacheUtils.get(String.format(TEMP_CACHE_API_SECRET_KEY, body.getUuid()));
            } catch (Exception e) {
                log.error("缓存secrectkey失败:", e);
            }
            if(org.apache.commons.lang3.StringUtils.isNotBlank(cacheSecretKey)){
                apiManageModel.setSecretKey(CryptoUtils.decryptAESToString(cacheSecretKey, this.aesPass));
            }
            apiManageModel.setApiKey(CryptoUtils.decryptAESToString(apiManageModel.getApiKey(), this.aesPass));
            return CopyBeanUtils.copy(apiManageModel, ApiModelResponse.class);
        }
    }

    @Override
    public EnableApiWithdrawResponse enableApiWithdraw(EnableApiWithdrawRequest body) throws Exception {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue(AccountConstants.SYSTEM_MAINTENANCE))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String type = "3";
        ApiModel apiManageModel = this.apiModelMapper.selectByWithdrawVerifycode(body.getVerifyCode(), body.getUserId());
        if (apiManageModel == null) {
            //margin账户
            Long marginUserId = getCheckedUser(body.getUserId()).getUserInfo().getMarginUserId();
            if (marginUserId == null) {
                throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_NOT_EXIST);
            }
            apiManageModel = this.apiModelMapper.selectByWithdrawVerifycode(body.getVerifyCode(), marginUserId.toString());
        }

        if (apiManageModel == null) {
            resultMap.put("success", false);
            resultMap.put("msg", GeneralCode.API_ENABLEAPIWITHDRAW_NOT_EXIST);
        } else if (!apiManageModel.isApiEmailVerify()) {
            throw new BusinessException(GeneralCode.API_NOT_BEEN_VERIFY);
        } else if (apiManageModel.isEnableWithdrawStatus()) {
            resultMap.put("success", false);
            resultMap.put("msg", GeneralCode.API_ENABLEAPIWITHDRAW_ENABLED);
            type = "4";
        } else if (!apiManageModel.isDisableStatus()) {
            resultMap.put("success", false);
            resultMap.put("msg", GeneralCode.API_ENABLEAPIWITHDRAW_DISABLE);
        } else {
            Long tempTime = System.currentTimeMillis() - apiManageModel.getWithdrawVerifycodeTime().getTime();
            if (tempTime > (30 * 60 * 1000L)) {
                throw new BusinessException(GeneralCode.API_ENABLEAPIWITHDRAW_TIME_FAIL);
            }
            Map<String, Object> param = Maps.newHashMap();
            param.put("id", apiManageModel.getId());
            param.put("enableWithdrawStatus", true);
            param.put("preEnableWithdrawStatus", false);
            param.put("disableStatus", 1);
            param.put("updateTime", DateUtils.getNewDateUTC());
            param.put("withdrawVerifycode", " "); // 验证完成之后，清空verifyCode
            this.apiModelMapper.updateForApiWithdraw(param);
            resultMap.put("success", true);
            resultMap.put("msg", GeneralCode.API_ENABLEAPIWITHDRAW_SUCCESS);
        }
        // String redirect = "/withdrawVerify.html?type=" + type;
        // try {
        // WebUtils.getHttpServletResponse().sendRedirect(redirect);
        // } catch (IOException e) {
        // log.error("sendRedirectError", e);
        // }
        EnableApiWithdrawResponse response = new EnableApiWithdrawResponse();
        response.setType(type);
        if (resultMap.get("success") != null && !((Boolean) resultMap.get("success"))) {
            throw new BusinessException((GeneralCode) resultMap.get("msg"));
        }
        return response;
    }

    @Override
    public List<ApiModelResponse> getApis(GetApisRequest body) throws Exception {
        GetApiListRequest getApiListRequest = new GetApiListRequest();
        getApiListRequest.setUserId(body.getUserId());
        return this.getApiList(getApiListRequest);
    }

    @Override
    public List<ApiModelResponse> getApiList(GetApiListRequest body) throws Exception {
        List<ApiModelResponse> list = Lists.newArrayList();
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("id", body.getId());
        param.put("userId", body.getUserId());
        if(StringUtils.isNotBlank(body.getApiKey())){
            param.put("apiKey", CryptoUtils.encryptAESToString(body.getApiKey(), this.aesPass));
        }
        param.put("apiName", body.getApiName());
        List<ApiModel> apiManageModels = this.apiModelMapper.getApiByMap(param);
        if(StringUtils.isNotBlank(body.getUserId())) {
            GetUserResponse userResponse = this.getCheckedUser(body.getUserId());
            Long userId = userResponse.getUserInfo().getMarginUserId();
            if(userId != null) {
                param.put("userId", userId.toString());
                apiManageModels.addAll(this.apiModelMapper.getApiByMap(param));
            }
        }

        APIRequest<UserIdRequest> request = new APIRequest<>();
        UserIdRequest userIdRequest = new UserIdRequest();
        request.setBody(userIdRequest);

        for (ApiModel apiModel : apiManageModels) {
            apiModel.setApiKey(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.aesPass));
            apiModel.setSecretKey("--");
            apiModel.setWithdrawVerifycode(null);
            apiModel.setWithdraw((ApiManagerUtils.isWithdrawEnabled(Long.valueOf(apiModel.getRuleId())))
                    && (apiModel.isEnableWithdrawStatus()) ? true : false);

            userIdRequest.setUserId(Long.valueOf(apiModel.getUserId()));
            APIResponse<UserStatusEx> response = userService.getUserStatusByUserId(request);
            apiModel.setAccountType("SPOT");
            if(response.getData().getIsMarginUser()) {
                apiModel.setAccountType("MARGIN");
            }
            list.add(CopyBeanUtils.copy(apiModel, ApiModelResponse.class));
        }

        return list;

    }
    //具体看注释之前的逻辑好乱，有一部分枚举维护到了 sapi去了，这里没同步，我copy过来了
    public static class ApiManagerUtils {

        // 开放交易 1
        public static final long TRADE = 1L;//1L <<0
        // 开放提现 2
        public static final long WITHDRAW = 2L << 0;//1L <<1
        // 内部划转 4
        public static final long INTERNAL_TRANSFER = 2L << 1; //1L <<2
        // 内部划转 8
        public static final long MARGIN = 1L << 3; //1L <<3
        // 期货交易权限 16
        public static final long FUTURE_TRADE = 2L << 3; //1L <<4 == 2L << 3

        public static boolean isTradeEnabled(Long ruleId) {
            return BitUtils.isEnable(ruleId, ApiManagerUtils.TRADE);
        }

        public static boolean isWithdrawEnabled(Long ruleId) {
            return BitUtils.isEnable(ruleId, ApiManagerUtils.WITHDRAW);
        }

        public static boolean isInternalTransferEnabled(Long ruleId) {
            return BitUtils.isEnable(ruleId, ApiManagerUtils.INTERNAL_TRANSFER);
        }

        public static boolean isMarginEnable(Long ruleId) {
            return BitUtils.isEnable(ruleId, ApiManagerUtils.MARGIN);
        }

        public static boolean isFutureTradeEnabled(Long ruleId) {
            return BitUtils.isEnable(ruleId, ApiManagerUtils.FUTURE_TRADE);
        }

    }
    private static class CryptoUtils {
        /**
         * 将二进制转换成16进制
         *
         * @param buf
         * @return
         */
        public static String parseByte2HexStr(byte buf[]) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < buf.length; i++) {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        }

        /**
         * 将16进制转换为二进制
         *
         * @param hexStr
         * @return
         */
        public static byte[] parseHexStr2Byte(String hexStr) {
            if (hexStr.length() < 1) {
                return null;
            }
            byte[] result = new byte[hexStr.length() / 2];
            for (int i = 0; i < hexStr.length() / 2; i++) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }
            return result;
        }

        public static String encryptAESToString(String conent, String pass) {
            byte[] buf = EncryptionUtils.encryptAES(conent, pass);
            return parseByte2HexStr(buf);
        }

        public static String decryptAESToString(String conent, String pass) {
            byte[] buf = EncryptionUtils.decryptAES(parseHexStr2Byte(conent), pass);
            return new String(buf, Charset.forName("UTF-8"));
        }
    }

    private GetUserResponse getCheckedUser(String userId){
        GetUserResponse response = this.getUser(userId);
        if (null == response || response.getUserInfo().getTradingAccount() == null) {
            throw new BusinessException(GeneralCode.USER_ACCOUNT_NOT_EXIST);
        }
        return response;
    }

    private GetUserResponse getUser(String userId){
        try {
            UserIdRequest body = new UserIdRequest();
            body.setUserId(Long.parseLong(userId));
            APIRequest<UserIdRequest> request = this.newAPIRequest(body);
            return this.getAPIRequestResponse(userService.getUserById(request));
        } catch (Exception e) {
            log.error("查询getUserById失败:{}", e);
            throw new BusinessException("query getUserById fail");
        }
    }

    @Override
    public void updateDBApikey() throws Exception {
        log.info("start updateApikey");
        List<ApiDeletedModel> result = apiDeletedModelMapper.loadAllApikey();
        log.info("select apikey size:{}", result.size());
        int count = 0;
        for(ApiDeletedModel apiModel : result) {
            try {
                try {
                    apiModel.setApiKey(CryptoUtils.encryptAESToString(CryptoUtils.decryptAESToString(apiModel.getApiKey(), this.oldAesPass), this.aesPass));
                } catch (Exception e) {
                    log.info("已经用new ciphercode 加密");
                    continue;
                }
                apiDeletedModelMapper.updateApikey(apiModel);
                count++;
            } catch (Exception e) {
                log.error("updateApikey error:{}", e);
            }
        }
        log.info("update apikey size:{}", count);
        log.info("updateApikey end");
    }

    @Override
    @Async
    public void deleteApiAndOrders(String userId) {
        try {
            // 删除api
            DeleteAllApiKeyRequest deleteAllApiKeyRequest = new DeleteAllApiKeyRequest();
            deleteAllApiKeyRequest.setLoginUid(userId);
            deleteAllApiKey(deleteAllApiKeyRequest);
        } catch (Exception e) {
            log.error("afterDisableSubUser deleteAllApiKey failed, subUserId:{} error:{}", userId, e.getMessage());
        }

        try {
            // mbx 删除orders
            mbxgatewayOrderApiClient.deleteAllOrders(userId);
        } catch (Exception e) {
            log.error("afterDisableSubUser deleteAllOrders failed, subUserId:{} error:{}", userId, e.getMessage());
        }
    }

    public boolean createTimeAfterCreateFuture(ApiModel apiManageModel, UserInfo futureUserInfo) {
        if (null == apiManageModel.getCreateTime()) {
            return false;
        }
        long apiCreateTime = apiManageModel.getCreateTime().getTime();
        long futureUserCreateTime = futureUserInfo.getInsertTime().getTime();
        return apiCreateTime > futureUserCreateTime;
    }

    private void validateAPiKeySysFunction() {
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("system_maintenance"))) {
            throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
        }
        if (StringUtils.equals("1", this.sysConfigVarCacheService.getValue("user_api_status"))) {
            throw new BusinessException(GeneralCode.SYS_ERROR, "api_msg");
        }
    }

    private List<String> extractIpList(String ips) {
        return Arrays.asList(ips.split(IP_LIST_SEPARATOR));
    }

    private List<String> checkAndExtractIpList(String ips) {
        List<String> ipList = extractIpList(ips);
        Pattern pattern = Pattern.compile(RegexUtils.IP_REGEX);
        if (ipList.stream().allMatch(ip -> pattern.matcher(ip).matches())) {
            return ipList;
        } else {
            throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_ILLEGEL);
        }
    }

    private void syncRestrictIp2MatchBox(Collection<String> ips, String tradingAccount, String keyId) {
        Map<String, Object> ipRuleIdMap = getRuleMap(Long.parseLong(tradingAccount), Integer.valueOf(keyId));
        for (String ipItem : ips) {
            // 新ip同步给matchbox
            if (ipRuleIdMap.get(ipItem) == null) {
                this.matchboxApi.postApiKeyRule(tradingAccount, ipItem, keyId);
                log.info("syncRestrictIp2MatchBox --> postApiKeyRule Ip={}", ipItem);
            } else {
                ipRuleIdMap.remove(ipItem);
            }
        }

        // 删除的ip同步matchbox
        for (Object ruleId : ipRuleIdMap.values()) {
            matchboxApi.deleteApiKeyRule(tradingAccount, keyId, ruleId.toString());
            log.info("syncRestrictIp2MatchBox --> deleteApiKeyRule ruleId={}", ruleId);
        }
    }

    private void syncRestrictIp2Future(UserInfoVo userInfo, ApiModel apiModel, String ip) {
        try {
            if (userInfo.getFutureUserId() != null) {
                Long futureUserId = userInfo.getFutureUserId();
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                // 检查（短路优先）
                if (Objects.isNull(futureUserInfo)) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                if (!createTimeAfterCreateFuture(apiModel, futureUserInfo)) {
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }

                log.info("syncRestrictIp2Future --> updateApiKeyRules ip={}", ip);
                // 同步ip给永续
                if (futureUserInfo.getMeTradingAccount() != null) {
                    try {
                        futureAccountApiClient.updateApiKeyRules(Long.valueOf(apiModel.getKeyId()), futureUserInfo.getMeTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureAccountApiClient.updateApiKeyRules:", e);
                    }
                }

                // 同步ip给交割
                if (futureUserInfo.getDeliveryTradingAccount() != null) {
                    try {
                        futureDeliveryAccountApiClient.updateApiKeyRules(Long.valueOf(apiModel.getKeyId()),
                                futureUserInfo.getDeliveryTradingAccount().intValue(), ip);
                    } catch (Exception e) {
                        log.warn("futureDeliveryAccountApiClient.updateApiKeyRules:", e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("updateAndGetApiIpStatus syncIpToFutures error:", e);
        }
    }

    private void checkWithDrawIpIfNecessary(String ruleId, Collection<String> ips) {
        // broker子账户不允许提现，broker子账户正常走不到这里
        if (ApiManagerUtils.isWithdrawEnabled(Long.valueOf(ruleId))) {
            if (ips.size() == 1 && ips.contains(IP)) {
                throw new BusinessException(GeneralCode.API_KEY_UPDATE_IP_APPOINT);
            }
        }
    }
}
