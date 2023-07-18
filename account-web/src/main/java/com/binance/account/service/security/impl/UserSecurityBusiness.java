package com.binance.account.service.security.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.aop.FrontTask;
import com.binance.account.aop.RiskTask;
import com.binance.account.aop.RiskTaskAspect;
import com.binance.account.aop.SecurityLog;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.constants.enums.AccountTypeEnum;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityCache;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.security.VerificationsTwo;
import com.binance.account.data.entity.user.MarketMakerUser;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.MarketMakerUserMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.data.utils.CryptoAlgoUtils;
import com.binance.account.domain.bo.CapitalWithdrawRedisVerify;
import com.binance.account.domain.bo.FrontPushEventType;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.integration.mbxgateway.MbxgatewayIOrderApiClient;
import com.binance.account.integration.risk.RuleDecisionApiClient;
import com.binance.account.integration.streamer.StreamerOrderApiClient;
import com.binance.account.integration.tg.TelegramClient;
import com.binance.account.service.async.MsgAsyncTask;
import com.binance.account.service.async.UserAsyncTask;
import com.binance.account.service.async.UserInfoAsyncTask;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.datamigration.impl.MsgNotificationToC2CHelper;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.model.MultiFactorSceneCheckQuery;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.security.model.UserBindSendInfo;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.service.security.service.MultiFactorVerifyService;
import com.binance.account.service.user.IUserEmailChange;
import com.binance.account.service.user.UserConfigCommonBusiness;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.service.user.impl.UserSimpleBusiness;
import com.binance.account.util.UserEmailUtils;
import com.binance.account.utils.EncryptUtil;
import com.binance.account.utils.MaskUtils;
import com.binance.account.vo.apimanage.request.DeleteAllApiKeyRequest;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.CapitalWithdrawVerifyParam;
import com.binance.account.vo.security.UserMobileVo;
import com.binance.account.vo.security.UserSecurityLevelVo;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.request.AccountResetPasswordRequestV2;
import com.binance.account.vo.security.request.ActiveLoginRequest;
import com.binance.account.vo.security.request.BatchUpdateSecurityLevelRequest;
import com.binance.account.vo.security.request.BindEmailRequest;
import com.binance.account.vo.security.request.BindGoogleVerifyRequest;
import com.binance.account.vo.security.request.BindGoogleVerifyV2Request;
import com.binance.account.vo.security.request.BindMobileRequest;
import com.binance.account.vo.security.request.BindMobileV2Request;
import com.binance.account.vo.security.request.BindPhishingCodeRequest;
import com.binance.account.vo.security.request.BindPhishingCodeV2Request;
import com.binance.account.vo.security.request.ChangeEmailRequest;
import com.binance.account.vo.security.request.ChangeMobileRequest;
import com.binance.account.vo.security.request.CloseWithdrawWhiteStatusV2Request;
import com.binance.account.vo.security.request.ConfirmCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.DisableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.DisableFundPasswordRequest;
import com.binance.account.vo.security.request.DisableLoginRequest;
import com.binance.account.vo.security.request.EnableFastWithdrawSwitchRequest;
import com.binance.account.vo.security.request.EnableFundPasswordRequest;
import com.binance.account.vo.security.request.GetCapitalWithdrawVerifyParamRequest;
import com.binance.account.vo.security.request.GetUserEmailAndMobileByUserIdRequest;
import com.binance.account.vo.security.request.GetUserIdByEmailOrMobileRequest;
import com.binance.account.vo.security.request.MobileRateLimitRequest;
import com.binance.account.vo.security.request.MobileRequest;
import com.binance.account.vo.security.request.OneButtonActivationRequest;
import com.binance.account.vo.security.request.OneButtonDisableRequest;
import com.binance.account.vo.security.request.OpenOrCloseBNBFeeRequest;
import com.binance.account.vo.security.request.OpenOrCloseMobileVerifyRequest;
import com.binance.account.vo.security.request.OpenOrCloseWithdrawWhiteStatusRequest;
import com.binance.account.vo.security.request.OpenWithdrawWhiteStatusV2Request;
import com.binance.account.vo.security.request.ResetActivationUserRequest;
import com.binance.account.vo.security.request.ResetFundPasswordRequest;
import com.binance.account.vo.security.request.ResetGoogleRequest;
import com.binance.account.vo.security.request.ResetSecurityRequest;
import com.binance.account.vo.security.request.SecurityLevelSettingRequest;
import com.binance.account.vo.security.request.SecurityStatusRequest;
import com.binance.account.vo.security.request.SendBindEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.SendBindMobileVerifyCodeRequest;
import com.binance.account.vo.security.request.SendEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.UnbindGoogleRequest;
import com.binance.account.vo.security.request.UnbindGoogleV2Request;
import com.binance.account.vo.security.request.UnbindMobileRequest;
import com.binance.account.vo.security.request.UnbindMobileV2Request;
import com.binance.account.vo.security.request.UpdateUserSecurityByUserIdRequest;
import com.binance.account.vo.security.request.UpdateWithdrawStatusRequest;
import com.binance.account.vo.security.request.UserEmailRequest;
import com.binance.account.vo.security.request.UserForbidRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.request.UserLockRequest;
import com.binance.account.vo.security.request.VerificationsDemandRequest;
import com.binance.account.vo.security.request.VerifyFundPasswordRequest;
import com.binance.account.vo.security.request.WithdrawFaceStatusChangeRequest;
import com.binance.account.vo.security.response.AccountResetPasswordResponseV2;
import com.binance.account.vo.security.response.AccountUpdateTimeForTrade;
import com.binance.account.vo.security.response.BindEmailResponse;
import com.binance.account.vo.security.response.BindGoogleVerifyResponse;
import com.binance.account.vo.security.response.BindGoogleVerifyV2Response;
import com.binance.account.vo.security.response.ChangeEmailResponse;
import com.binance.account.vo.security.response.ChangeMobileResponse;
import com.binance.account.vo.security.response.CheckForbidCodeResponse;
import com.binance.account.vo.security.response.CloseWithdrawWhiteStatusResponse;
import com.binance.account.vo.security.response.CloseWithdrawWhiteStatusV2Response;
import com.binance.account.vo.security.response.ConfirmCloseWithdrawWhiteStatusResponse;
import com.binance.account.vo.security.response.DisableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.DisableFundPasswordResponse;
import com.binance.account.vo.security.response.EnableFastWithdrawSwitchResponse;
import com.binance.account.vo.security.response.EnableFundPasswordResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetUser2faResponse;
import com.binance.account.vo.security.response.GetCapitalWithdrawVerifyParamResponse;
import com.binance.account.vo.security.response.GetUserEmailAndMobileByUserIdResponse;
import com.binance.account.vo.security.response.GetUserIdByEmailOrMobileResponse;
import com.binance.account.vo.security.response.GoogleAuthKeyResp;
import com.binance.account.vo.security.response.OpenWithdrawWhiteStatusV2Response;
import com.binance.account.vo.security.response.ResetFundPasswordResponse;
import com.binance.account.vo.security.response.SendBindEmailVerifyCodeResponse;
import com.binance.account.vo.security.response.SendBindMobileVerifyCodeResponse;
import com.binance.account.vo.security.response.SendEmailVerifyCodeResponse;
import com.binance.account.vo.security.response.UnbindGoogleVerifyResponse;
import com.binance.account.vo.security.response.UnbindGoogleVerifyV2Response;
import com.binance.account.vo.security.response.UnbindMobileResponse;
import com.binance.account.vo.security.response.UnbindMobileV2Response;
import com.binance.account.vo.security.response.UserRiskInfoResponse;
import com.binance.account.vo.security.response.UserSecurityListResponse;
import com.binance.account.vo.security.response.VerificationsDemandResponse;
import com.binance.account.vo.security.response.VerifyFundPasswordResponse;
import com.binance.account.vo.security.response.WithdrawTimeForTradeResponse;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.AccountForgotPasswordPreCheckRequest;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.response.AccountForgotPasswordPreCheckResponse;
import com.binance.account.yubikey.WebAuthnFrontHandler;
import com.binance.authcenter.api.AuthApi;
import com.binance.authcenter.vo.LogoutResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.enums.SysType;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.RedisVerify;
import com.binance.master.old.data.withdraw.OldWithdrawDailyLimitModifyMapper;
import com.binance.master.old.models.withdraw.OldWithdrawDailyLimitModify;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.PasswordUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.master.utils.security.EncryptionUtils;
import com.binance.master.utils.security.TokenUtils;
import com.binance.master.validator.regexp.Regexp;
import com.binance.matchbox.api.AccountApi;
import com.binance.matchbox.vo.TradingAccountDetails;
import com.binance.messaging.api.msg.request.MsgType;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.messaging.api.twilio.request.TwilioFeedBackRequest;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.binance.risk.api.RiskSecurityApi;
import com.binance.risk.vo.UserIdRequestVo;
import com.binance.streamer.api.response.vo.OpenOrderVo;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.binance.account.constants.AccountConstants.USER_CONFIG_LAST_2FA_TYPE;
import static com.binance.account.service.user.impl.UserBusiness.MAX_EMAIL_LENGTH;
import static com.binance.account.service.user.impl.UserBusiness.REGEX_EMAIL;
import static com.binance.account.vo.security.enums.AccountVerificationTwoEnum.EMAIL;
import static com.binance.account.vo.security.enums.AccountVerificationTwoEnum.GOOGLE;
import static com.binance.account.vo.security.enums.AccountVerificationTwoEnum.SMS;
import static com.binance.account.vo.security.enums.BizSceneEnum.BIND_EMAIL;
import static com.binance.account.vo.security.enums.BizSceneEnum.BIND_MOBILE;
import static com.binance.account.vo.security.enums.BizSceneEnum.NEW_EMAIL_VERIFY;

@Log4j2
@Service
public class UserSecurityBusiness implements IUserSecurity {

    @Resource
    private IMsgNotification iMsgNotification;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserMobileIndexMapper userMobileIndexMapper;
    @Resource
    private ICountry iCountry;
    @Resource
    private CountryMapper countryMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private AccountApi accountApi;
    @Autowired
    private MsgAsyncTask msgAsyncTask;
    @Autowired
    private ApolloCommonConfig commonConfig;
    @Autowired
    private AccountApiClient accountApiClient;
    @Autowired
    private MbxgatewayIOrderApiClient mbxGatewayOrderApiCLient;
    @Autowired
    private StreamerOrderApiClient streamerOrderApiClient;
    @Autowired
    private WebAuthnFrontHandler webAuthnFrontHandler;
    @Autowired
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Resource
    private OldWithdrawDailyLimitModifyMapper oldWithdrawDailyLimitModifyMapper;
    @Autowired
    private RuleDecisionApiClient ruleDecisionApiClient;
    @Resource
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private MarketMakerUserMapper marketMakerUserMapper;
    @Autowired
    private TelegramClient telegramClient;
    @Autowired
    private UserIpMapper userIpMapper;
    @Autowired
    private UserSimpleBusiness userSimpleBusiness;
    @Autowired
    private IUserEmailChange iUserEmailChange;
    @Autowired
    protected IUserDevice userDeviceBusiness;
    @Autowired
    private RiskSecurityApi riskSecurityApi;
    @Autowired
    private UserConfigCommonBusiness iUser;
    @Autowired
    private MultiFactorVerifyService multiFactorVerifyService;
    @Autowired
    private AuthApi authApi;
    @Autowired
    private UserAsyncTask userAsyncTask;
    @Autowired
    private MsgNotificationToC2CHelper notificationToC2CHelper;
    @Autowired
    private UserInfoAsyncTask userInfoAsyncTask;


    public static final String UPDATE_PSW_TIME_PREFIX = "account:password:update:time:%s";
    public static final String FORGET_PSW_TIME_PREFIX = "account:password:forget:time:%s";
    private static final long ONE_DAY = 60 * 60 * 24;

    private static final String DEFAULT_CIPHER_CODE = "lctwmv9fdld6yfdk06g";

    private static final String SEND_BIND_MOBILE_VERIFY_CODE_KEY = "account:bind:mobile";

    private static final String QR_CODE = "otpauth://totp/%s:%s?secret=%s&issuer=%s";
    private static final String GAUTH_KEY_GENERATOR_KEY = "account:gauth:key:generator:%s";

    private static final String BIND_MOBILE_SOURCE_FUNCTION = "account-sendBindMobileVerifyCode";

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${twilio.feedback.switch:true}")
    private Boolean isOpenTwilioFeedBack;

    @Value("${account.gauth.issuer:Binance.com}")
    private String accountGauthIssuer;

    @Value("${account.forget.password.switch:true}")
    private Boolean forgetPasswordSwitch;

    @Value("${withdraw.status.update.tgchat:-482990602}")
    private String updateWithdrawStatusChatId;

    @Value("${withdraw.status.update.webex:Y2lzY29zcGFyazovL3VzL1JPT00vNDlkMDMyMjAtYmFhNi0xMWVhLTkwMmMtYjkwZGE0YmJkMmU0}")
    private String updateWithdrawStatusRoomId;

    //dev环境无服务，只能线上推送
    @Value("${withdraw.status.send.tgchat:false}")
    private boolean isSendupdateWithdrawStatusChatId;

    //1主战 2美国站
    @Value("${withdraw.status.update.site:1}")
    private Integer withdrawStatusUpdteSite;

    private static final int ERROR_COUNT = 3;

    private static final String CACHE_COUNTRY_MAP = "user_country_map1";

    private static final String ENV_LOCAL = "-local";
    private static final String ENV_DEV = "-dev";
    private static final String ENV_QA = "-qa";
    private static final String NON_PROD_2FA = "111111";

    @Value("${bind.google.verify.days:30}")
    private Integer bindGoogleVerifyDays;

    @Value("${account.fastwithdraw.need2fa.switch:false}")
    private Boolean fastwithdrawNeed2FaSwitch;

    @Value("${account.fastwithdraw.needkyc.switch:false}")
    private Boolean fastwithdrawNeedKycSwitch;

    @Value("#{'${account.fundpassword.whitelist:}'.split(',')}")
    private List<Long> fundPasswordWhitelist;

    private static final String IS_MOBILE_EXIST_TOTALRATELIMIT = "account:mobileexisttotallimit:user";
    private static final String UPDATE_WITHDRAW_STATUS_SEND_TG_TEMPLATE = "【%s】- 用户【%d】，VIP等级【%d】，于【%s】因【%s】被【%s】";
    private static final String UPDATE_PARENT_TRADE_STATUS_SEND_TG_TEMPLATE = "【主站】- 【母账户】【%d】，VIP等级【%d】，于【%s】被禁止【现货交易】";
    private static final String UPDATE_TRADE_STATUS_SEND_TG_TEMPLATE = "【主站】- 【普通账户】【%d】，VIP等级【%d】，于【%s】被禁止【现货交易】";

    private boolean nonProdEnv2Fa(String code) {
        return (active.endsWith(ENV_LOCAL) || active.endsWith(ENV_DEV) || active.endsWith(ENV_QA))
                && StringUtils.equalsAnyIgnoreCase(NON_PROD_2FA, code);
    }


    // 用户锁定时间
    private static final int USER_LOCK_TIME = 2;


    @Value("${fund.password.max.retry.tinmes:5}")
    private Integer fundPasswordMaxRetryTimes;


    @Autowired
    private SecurityNotificationService securityNotificationService;


    @SecurityLog(name = "解绑2fa", operateType = Constant.SECURITY_OPERATE_TYPE_UNBIND_GOOGLE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<UnbindGoogleVerifyResponse> unbindGoogleVerify(APIRequest<UnbindGoogleRequest> request)
            throws Exception {
        final UnbindGoogleRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 检查密码
        //final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        final User dbUser = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(dbUser == null) {
        	throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_CIPHER_CODE, true);
        final String password = PasswordUtils.encode(requestBody.getPassword(), dbUser.getSalt(), cipherCode);
        if (!StringUtils.equals(password, dbUser.getPassword())) {
            throw new BusinessException(GeneralCode.USER_PWD_ERROR_NOT_COUNT);
        }
        checkGoogleAuthenticator(userId, requestBody.getGoogleCode());
        //风控
        boolean riskEngineRes = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, userId ,requestBody.getDeviceInfo());
        this.unbindGoogleVerify(userId,riskEngineRes);
        // 发送解绑2fa邮件
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineRes?AccountConstants.NODE_TYPE_GOOGLE_VERIFY_UNBIND_USABLE:Constant.NODE_TYPE_GOOGLE_VERIFY_UNBIND, dbUser,
                null, "发送解绑2fa邮件", requestBody.getCustomForbiddenLink());
        // 解绑2fa消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userId);
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_GOOGLE_VERIFY, dataMsg);
        log.info("iMsgNotification unbindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 解绑2fa消息通知 end
        return APIResponse.getOKJsonResult(new UnbindGoogleVerifyResponse(userId, disableToken));
    }

    @SecurityLog(name = "解绑2fa", operateType = Constant.SECURITY_OPERATE_TYPE_UNBIND_GOOGLE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<UnbindGoogleVerifyV2Response> unbindGoogleVerifyV2(APIRequest<UnbindGoogleV2Request> request) throws Exception {
        final UnbindGoogleV2Request requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final User dbUser = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(dbUser == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.UNBIND_GOOGLE)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        //风控
        boolean riskEngineRes = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, userId ,requestBody.getDeviceInfo());
        this.unbindGoogleVerify(userId,riskEngineRes);
        // 发送解绑2fa邮件
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineRes?AccountConstants.NODE_TYPE_GOOGLE_VERIFY_UNBIND_USABLE:Constant.NODE_TYPE_GOOGLE_VERIFY_UNBIND, dbUser,
                null, "发送解绑2fa邮件", requestBody.getCustomForbiddenLink());
        // 解绑2fa消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userId);
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_GOOGLE_VERIFY, dataMsg);
        log.info("iMsgNotification unbindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 解绑2fa消息通知 end
        return APIResponse.getOKJsonResult(new UnbindGoogleVerifyV2Response(userId, disableToken));
    }

    private void unbindGoogleVerify(Long userId, boolean riskEngineRes) {
        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());

        // 更新user状态
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.disable(status, Constant.USER_GOOGLE));
        this.userMapper.updateUserStatusByEmail(user);
        final UserSecurity security = new UserSecurity();
        security.setUserId(userId);
        //风控
        if (!riskEngineRes){
            // 解绑谷歌验证(更新unbind_time)
            security.setAuthKey(null);
            this.userSecurityMapper.updateAuthKeyByEmail(security);
        }else{
            this.userSecurityMapper.updateSecurityByUserId(security);
        }
    }

    @Override
    @SecurityLog(name = "重置2fa", operateType = Constant.SECURITY_OPERATE_TYPE_UNBIND_GOOGLE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<UnbindGoogleVerifyResponse> resetGoogleVerify(APIRequest<ResetGoogleRequest> request) {
        final ResetGoogleRequest requestBody = request.getBody();
        this.unbindGoogleVerify(requestBody.getUserId(),requestBody.getRiskEngineRes() == null ? false : requestBody.getRiskEngineRes());
        return APIResponse.getOKJsonResult(new UnbindGoogleVerifyResponse(requestBody.getUserId(), null));
    }

    @Override
    @SecurityLog(name = "绑定2fa", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_GOOGLE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.TWOFA_COMPLETE_ROUTING,userId = "#request.body.userId" ,eventType = FrontPushEventType.TWOFA_COMPLETE,tfaType = FrontPushEventType.TWOFA_GOOGLE_AUTH)
    public APIResponse<BindGoogleVerifyResponse> bindGoogleVerify(APIRequest<BindGoogleVerifyRequest> request)
            throws Exception {
        final BindGoogleVerifyRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        final String secretKey = requestBody.getSecretKey();
        final Integer googleCode = requestBody.getGoogleCode();

        if(org.apache.commons.lang3.StringUtils.isBlank(secretKey) || googleCode == null || org.apache.commons.lang3.StringUtils.isBlank(requestBody.getPassword())) {
        	log.warn("bindGoogleVerify非空参数为null，userId:{},secretKey:{},googleCode:{},password:{}", userId,secretKey,googleCode,requestBody.getPassword());
        	throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        VerificationsTwo verificationsTwo = null;
        final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());

        // 用户已经绑定不可继续绑定
        if (BitUtils.isTrue(dbUser.getStatus(), Constant.USER_GOOGLE)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        boolean is2faVerify = true;
        if (nonProdEnv2Fa(
                null != googleCode ? googleCode.toString() : StringUtils.EMPTY)) {
            is2faVerify = false;
        }
        if (is2faVerify) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean isVerifyCode = gAuth.authorize(secretKey, googleCode);
            if (!isVerifyCode) {
                throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR);
            }
            if (BitUtils.isEnable(dbUser.getStatus(), Constant.USER_MOBILE)) {// 开启手机验证的情况需要输入手机验证码
                if (StringUtils.isBlank(requestBody.getSmsCode())) {
                    throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_FILL);
                }
                verificationsTwo =
                        this.isSmsAuthenticator(userIndex.getUserId(), requestBody.getSmsCode(), false, null, null);
            }
        }

        // 检查密码
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_CIPHER_CODE, true);
        final String password = PasswordUtils.encode(requestBody.getPassword(), dbUser.getSalt(), cipherCode);
        if (!StringUtils.equals(password, dbUser.getPassword())) {
            throw new BusinessException(GeneralCode.USER_PWD_ERROR_NOT_COUNT);
        }

        // 更新user状态
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.enable(dbUser.getStatus(), Constant.USER_GOOGLE));
        this.userMapper.updateUserStatusByEmail(user);

        // 确保绑定时使用的key，是当前用户自己生成的key
        String cachedAuthKey =
                RedisCacheUtils.get(String.format(GAUTH_KEY_GENERATOR_KEY, requestBody.getUserId()), String.class);
        if (StringUtils.isBlank(cachedAuthKey)
                || !StringUtils.equalsIgnoreCase(cachedAuthKey.trim(), requestBody.getSecretKey().trim())) {
            log.warn("illegal authKey, userId:{}, userIp:{}", requestBody.getUserId(), WebUtils.getRequestIp());
            throw new BusinessException(GeneralCode.AC_VALIDATE_FAILED_REFRESH_AND_RETRY);
        }

        // 绑定谷歌验证
        final UserSecurity security = new UserSecurity();
        security.setUserId(requestBody.getUserId());
        final String securityCipher =
                RedisCacheUtils.get(CacheKeys.SECURITY_CIPHER, "Q8oYo6tna4LWlIhQjPX6XNMVtwqZOXJY", true);
        final String authKey = EncryptionUtils.encryptAESToString(requestBody.getSecretKey(), securityCipher);// 谷歌2次验证

        security.setAuthKey(authKey);
        this.userSecurityMapper.updateSecurityByUserId(security);
        if (verificationsTwo != null) {
            verificationsTwo.delMobileCode();
        }
        // 绑定手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put("secretKey", requestBody.getSecretKey());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_GOOGLE, dataMsg);
        log.info("iMsgNotification bindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 绑定手机消息通知 end
        return APIResponse
                .getOKJsonResult(new BindGoogleVerifyResponse(userIndex.getUserId(), requestBody.getSecretKey()));
    }

    @SecurityLog(name = "绑定2fa", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_GOOGLE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.TWOFA_COMPLETE_ROUTING,userId = "#request.body.userId" ,eventType = FrontPushEventType.TWOFA_COMPLETE,tfaType = FrontPushEventType.TWOFA_GOOGLE_AUTH)
    @Override
    public APIResponse<BindGoogleVerifyV2Response> bindGoogleVerifyV2(APIRequest<BindGoogleVerifyV2Request> request) throws Exception {
        final BindGoogleVerifyV2Request requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        final String secretKey = requestBody.getSecretKey();
        final String googleCode = requestBody.getGoogleVerifyCode();

        if(StringUtils.isBlank(secretKey) || StringUtils.isBlank(googleCode) || !StringUtils.isNumeric(googleCode)) {
            log.warn("bindGoogleVerify非空参数为null，userId:{},secretKey:{},googleCode:{}", userId,secretKey,googleCode);
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        VerificationsTwo verificationsTwo = null;
        final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());

        // 用户已经绑定不可继续绑定
        if (BitUtils.isTrue(dbUser.getStatus(), Constant.USER_GOOGLE)) {
            throw new BusinessException(AccountErrorCode.USER_GOOGLE_ALREADY_BIND);
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isVerifyCode = gAuth.authorize(secretKey, Integer.parseInt(googleCode));
        if (!isVerifyCode) {
            throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR);
        }

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.BIND_GOOGLE)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        // 更新user状态
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.enable(dbUser.getStatus(), Constant.USER_GOOGLE));
        this.userMapper.updateUserStatusByEmail(user);

        // 确保绑定时使用的key，是当前用户自己生成的key
        String cachedAuthKey =
                RedisCacheUtils.get(String.format(GAUTH_KEY_GENERATOR_KEY, requestBody.getUserId()), String.class);
        if (StringUtils.isBlank(cachedAuthKey)
                || !StringUtils.equalsIgnoreCase(cachedAuthKey.trim(), requestBody.getSecretKey().trim())) {
            log.warn("illegal authKey, userId:{}, userIp:{}", requestBody.getUserId(), WebUtils.getRequestIp());
            throw new BusinessException(GeneralCode.AC_VALIDATE_FAILED_REFRESH_AND_RETRY);
        }

        // 绑定谷歌验证
        final UserSecurity security = new UserSecurity();
        security.setUserId(requestBody.getUserId());
        final String securityCipher =
                RedisCacheUtils.get(CacheKeys.SECURITY_CIPHER, "Q8oYo6tna4LWlIhQjPX6XNMVtwqZOXJY", true);
        final String authKey = EncryptionUtils.encryptAESToString(requestBody.getSecretKey(), securityCipher);// 谷歌2次验证

        security.setAuthKey(authKey);
        this.userSecurityMapper.updateSecurityByUserId(security);
        if (verificationsTwo != null) {
            verificationsTwo.delMobileCode();
        }
        // 绑定手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put("secretKey", requestBody.getSecretKey());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_GOOGLE, dataMsg);
        log.info("iMsgNotification bindGoogleVerify:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 绑定手机消息通知 end
        return APIResponse
                .getOKJsonResult(new BindGoogleVerifyV2Response(userIndex.getUserId(), requestBody.getSecretKey()));
    }

    @Override
    @SecurityLog(name = "绑定手机", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_MOBILE,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.TWOFA_COMPLETE_ROUTING,userId = "#request.body.userId" ,eventType = FrontPushEventType.TWOFA_COMPLETE,tfaType = FrontPushEventType.TWOFA_SMS)
    public APIResponse<Integer> bindMobile(APIRequest<BindMobileRequest> request) {
        final BindMobileRequest requestBody = request.getBody();
        String mobileCode = requestBody.getMobileCode();
        if(StringUtils.isBlank(mobileCode)) {
        	log.warn("mobileCode不合法，值为:{}", mobileCode);
        	throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 手机码校验
        requestBody.setMobileCode(requestBody.getMobileCode().toUpperCase());
        Country country = this.iCountry.getCountryByCode(requestBody.getMobileCode().toUpperCase());
        if (null == country) {
            log.warn("bindMobile,mobileCode:{} invalid", requestBody.getMobileCode());
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 判断手机是否已经被使用
        final UserMobileIndex userMobileIndex =
                this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());
        if (userMobileIndex != null) {
            throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
        }
        final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        if (BitUtils.isTrue(dbUser.getStatus(), Constant.USER_MOBILE)) {
            throw new BusinessException(GeneralCode.USER_MOBILE_BIND);// 用户已经绑定无需继续绑定
        }

        VerificationsTwo verificationsTwo = this.isSmsAuthenticator(dbUser.getUserId(), requestBody.getSmsCode(), false,
                requestBody.getMobile(), requestBody.getMobileCode());

        UserSecurity userSecurity = null;
        if (BitUtils.isEnable(dbUser.getStatus(), Constant.USER_GOOGLE)) {// 开启谷歌2次验证
            if (requestBody.getGoogleCode() == null) {
                throw new BusinessException(GeneralCode.USER_GOOGLE_AUTH_CODE_FILL);
            }
            userSecurity = this.userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
            final String securityCipher =
                    RedisCacheUtils.get(CacheKeys.SECURITY_CIPHER, "Q8oYo6tna4LWlIhQjPX6XNMVtwqZOXJY", true);
            final String authKey = EncryptionUtils.decryptAESToString(userSecurity.getAuthKey(), securityCipher);// 谷歌2次验证
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            boolean isVerifyCode = gAuth.authorize(authKey, requestBody.getGoogleCode());
            if (!isVerifyCode) {
                throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR);
            }
        }

        // 更新user状态
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.enable(dbUser.getStatus(), Constant.USER_MOBILE));
        this.userMapper.updateUserStatusByEmail(user);

        // 记录手机索引
        final UserMobileIndex mobileIndex = new UserMobileIndex();
        mobileIndex.setMobile(requestBody.getMobile());
        mobileIndex.setCountry(requestBody.getMobileCode());
        mobileIndex.setUserId(userIndex.getUserId());
        this.userMobileIndexMapper.insert(mobileIndex);

        // 更新手机号
        final UserSecurity newSecurity = new UserSecurity();
        newSecurity.setUserId(requestBody.getUserId());
        newSecurity.setMobile(requestBody.getMobile());
        newSecurity.setMobileCode(requestBody.getMobileCode());
        if (verificationsTwo != null) {
            verificationsTwo.delMobileCode();
        }
        // 绑定手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put("mobile", requestBody.getMobile());
        dataMsg.put("mobileCode", requestBody.getMobileCode());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_MOBILE, dataMsg);
        log.info("iMsgNotification bindMobile:{}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        // 绑定手机消息通知 end

        notificationToC2CHelper.sendMobileBindingChangeMsgAsync(newSecurity);
        return APIResponse.getOKJsonResult(this.userSecurityMapper.updateBindInfoByUserId(newSecurity));
    }

    @SecurityLog(name = "绑定手机", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_MOBILE,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.TWOFA_COMPLETE_ROUTING,userId = "#request.body.userId" ,eventType = FrontPushEventType.TWOFA_COMPLETE,tfaType = FrontPushEventType.TWOFA_SMS)
    @Override
    public APIResponse<Integer> bindMobileV2(APIRequest<BindMobileV2Request> request) throws Exception {
        final BindMobileV2Request requestBody = request.getBody();
        String mobileCode = requestBody.getMobileCode();
        if(StringUtils.isBlank(mobileCode)) {
            log.warn("mobileCode不合法，值为:{}", mobileCode);
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 手机码校验
        requestBody.setMobileCode(requestBody.getMobileCode().toUpperCase());
        Country country = this.iCountry.getCountryByCode(requestBody.getMobileCode().toUpperCase());
        if (null == country) {
            log.warn("bindMobile,mobileCode:{} invalid", requestBody.getMobileCode());
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 判断手机是否已经被使用
        final UserMobileIndex userMobileIndex =
                this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());
        if (userMobileIndex != null) {
            throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
        }
        final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        if (BitUtils.isTrue(dbUser.getStatus(), Constant.USER_MOBILE)) {
            throw new BusinessException(GeneralCode.USER_MOBILE_BIND);// 用户已经绑定无需继续绑定
        }

        // 绑定手机验证码校验
        VerificationsTwo smsVerificationsTwo = this.isSmsAuthenticator(requestBody.getUserId(), requestBody.getMobileVerifyCode(),
                false, requestBody.getMobile(), requestBody.getMobileCode());

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BIND_MOBILE)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);


        // 更新user状态
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.enable(dbUser.getStatus(), Constant.USER_MOBILE));
        this.userMapper.updateUserStatusByEmail(user);

        // 记录手机索引
        final UserMobileIndex mobileIndex = new UserMobileIndex();
        mobileIndex.setMobile(requestBody.getMobile());
        mobileIndex.setCountry(requestBody.getMobileCode());
        mobileIndex.setUserId(userIndex.getUserId());
        this.userMobileIndexMapper.insert(mobileIndex);

        // 更新手机号
        final UserSecurity newSecurity = new UserSecurity();
        newSecurity.setUserId(requestBody.getUserId());
        newSecurity.setMobile(requestBody.getMobile());
        newSecurity.setMobileCode(requestBody.getMobileCode());

        // 绑定手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<String, Object>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put("mobile", requestBody.getMobile());
        dataMsg.put("mobileCode", requestBody.getMobileCode());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_MOBILE, dataMsg);
        log.info("iMsgNotification bindMobile:{}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        // 绑定手机消息通知 end
        int result = this.userSecurityMapper.updateBindInfoByUserId(newSecurity);

        // 删除验证码
        if (smsVerificationsTwo != null) {
            smsVerificationsTwo.delMobileCode();
        }

        notificationToC2CHelper.sendMobileBindingChangeMsgAsync(newSecurity);
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    @SecurityLog(name = "解绑手机", operateType = Constant.SECURITY_OPERATE_TYPE_UNBIND_MOBILE,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<UnbindMobileResponse> unbindMobile(APIRequest<UnbindMobileRequest> request) throws Exception {

        final UnbindMobileRequest requestBody = request.getBody();

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        //final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        final User dbUser = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(dbUser == null) {
        	throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 未绑定过手机无法解绑
        if (BitUtils.isFalse(dbUser.getStatus(), Constant.USER_MOBILE)) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }

        // 未绑定邮箱不允许解绑手机
        if (BitUtils.isTrue(dbUser.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL)) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_NOT_BIND);
        }

        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_CIPHER_CODE, true);
        final String password = PasswordUtils.encode(requestBody.getPassword(), dbUser.getSalt(), cipherCode);
        if (!StringUtils.equals(password, dbUser.getPassword())) {
            throw new BusinessException(GeneralCode.USER_PWD_ERROR_NOT_COUNT);
        }

        VerificationsTwo verificationsTwo =
                this.isSmsAuthenticator(userIndex.getUserId(), requestBody.getSmsCode(), false, null, null);
        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, userIndex.getUserId(), requestBody.getDeviceInfo());


        unbindMobile(dbUser,riskEngineResult);

        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_MOBILE_UNBIND_USABLE:Constant.NODE_TYPE_MOBILE_UNBIND, dbUser, null,
                "解绑手机发送邮件", requestBody.getCustomForbiddenLink());
        if (verificationsTwo != null) {
            verificationsTwo.delMobileCode();
        }
        // 解绑手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, requestBody.getUserId());
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_MOBILE, dataMsg);
        log.info("iMsgNotification unbindMobile:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 解绑手机消息通知 end

        return APIResponse
                .getOKJsonResult(new UnbindMobileResponse(userIndex.getUserId(), userIndex.getEmail(), disableToken));
    }

    @SecurityLog(name = "解绑手机", operateType = Constant.SECURITY_OPERATE_TYPE_UNBIND_MOBILE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @RiskTask(userId = "#request.body.userId")
    @Override
    public APIResponse<UnbindMobileV2Response> unbindMobileV2(APIRequest<UnbindMobileV2Request> request) throws Exception {
        final UnbindMobileV2Request requestBody = request.getBody();

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        //final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        final User dbUser = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(dbUser == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 未绑定过手机无法解绑
        if (BitUtils.isFalse(dbUser.getStatus(), Constant.USER_MOBILE)) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }

        // 未绑定邮箱不允许解绑手机
        if (BitUtils.isTrue(dbUser.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL)) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_NOT_BIND);
        }

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.UNBIND_MOBILE)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, userIndex.getUserId(), requestBody.getDeviceInfo());

        // 解绑手机
        unbindMobile(dbUser,riskEngineResult);

        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_MOBILE_UNBIND_USABLE:Constant.NODE_TYPE_MOBILE_UNBIND, dbUser, null,
                "解绑手机发送邮件", requestBody.getCustomForbiddenLink());

        // 解绑手机消息通知 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, requestBody.getUserId());
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UNBIND_MOBILE, dataMsg);
        log.info("iMsgNotification unbindMobile:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 解绑手机消息通知 end
        return APIResponse
                .getOKJsonResult(new UnbindMobileV2Response(userIndex.getUserId(), userIndex.getEmail(), disableToken));
    }

    private void unbindMobile(User dbUser, boolean riskEngineResult) {
        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(dbUser.getUserId());
        if (StringUtils.isBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }

        // 删除索引
        this.userMobileIndexMapper.deleteByPrimaryKey(userSecurity.getMobile(), userSecurity.getMobileCode());

        // 更新user状态
        final User user = new User();
        user.setEmail(dbUser.getEmail());
        user.setStatus(BitUtils.disable(dbUser.getStatus(), Constant.USER_MOBILE));
        this.userMapper.updateUserStatusByEmail(user);
        //风控
//        Map<String,Object> parameters = Maps.newHashMap();
//        parameters.put("uid",userSecurity.getUserId());
//        parameters.put("ipaddr",WebUtils.getRequestIp());
        final UserSecurity newSecurity = new UserSecurity();
        newSecurity.setUserId(dbUser.getUserId());
        newSecurity.setMobile(null);
        newSecurity.setMobileCode(null);
        if (!riskEngineResult) {
            // 更新手机号(更新unbind_time)
//        RuleRequest ruleRequest = new RuleRequest();
//        ruleRequest.setParameters();
//        ruleRequest.setRuleName();
//        ruleDecisionApi.doRule()
            this.userSecurityMapper.updateMobileByUserId(newSecurity);
        }else{
            this.userSecurityMapper.updateBindInfoByUserId(newSecurity);
        }

        notificationToC2CHelper.sendMobileBindingChangeMsgAsync(newSecurity);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> resetLoginFailedNum(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        final int rows = this.userSecurityMapper.resetLoginFailedNum(requestBody.getUserId());
        if (rows == 0) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return APIResponse.getOKJsonResult(rows);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateUserSecurityByUserId(APIRequest<UpdateUserSecurityByUserIdRequest> request) {
        final UpdateUserSecurityByUserIdRequest requestBody = request.getBody();

        UserSecurity userSecurity = new UserSecurity();
        BeanUtils.copyProperties(requestBody.getUserSecurity(), userSecurity);

        final int rows = this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
        if (rows == 0) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return APIResponse.getOKJsonResult(rows);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<UserSecurityVo> getUserSecurityByUserId(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserSecurityVo userSecurityVo = new UserSecurityVo();
        BeanUtils.copyProperties(userSecurity, userSecurityVo);
        if (!commonConfig.isWithdrawFaceSwitchOn()) {
            // 如果提币人脸识别开关关闭的，全部返回的是0
            userSecurityVo.setWithdrawSecurityFaceStatus(UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO);
        }
        return APIResponse.getOKJsonResult(userSecurityVo);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<UserSecurityVo> getUserSecurityByEmail(APIRequest<UserEmailRequest> request) {
        final UserEmailRequest requestBody = request.getBody();
        final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        UserSecurityVo userSecurityVo = null;
        if (userSecurity != null) {
            userSecurityVo = new UserSecurityVo();
            BeanUtils.copyProperties(userSecurity, userSecurityVo);
        }
        if (!commonConfig.isWithdrawFaceSwitchOn()) {
            // 如果提币人脸识别开关关闭的，全部返回的是0
            userSecurityVo.setWithdrawSecurityFaceStatus(UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO);
        }
        return APIResponse.getOKJsonResult(userSecurityVo);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<Boolean> isMobileExist(APIRequest<MobileRequest> request) {
        final MobileRequest requestBody = request.getBody();
        final UserMobileIndex userMobileIndex =
                this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());
        if (userMobileIndex != null) {
            return APIResponse.getOKJsonResult(true);
        }
        return APIResponse.getOKJsonResult(false);
    }


    @Override
    public APIResponse<Boolean> isMobileExistRateLimit(APIRequest<MobileRateLimitRequest> request) {
        final MobileRateLimitRequest requestBody = request.getBody();
        log.info("isMobileExistRateLimit start userId={}", requestBody.getUserId());


        Long totalrequest = RedisCacheUtils.get(String.valueOf(requestBody.getUserId()), Long.class,
                IS_MOBILE_EXIST_TOTALRATELIMIT,0L);
        log.info("isMobileExistRateLimit start userId={},totalrequest={}", requestBody.getUserId(),totalrequest);
        if (null!=totalrequest && totalrequest.longValue() >= 5000L) {
            log.warn("overlimit isMobileExistRateLimit,totalrequest={}",totalrequest);
            throw new BusinessException(GeneralCode.TOO_MANY_REQUESTS);
        }
        totalrequest=totalrequest+1;
        RedisCacheUtils.set(String.valueOf(requestBody.getUserId()), totalrequest, 7*24*60*60L, IS_MOBILE_EXIST_TOTALRATELIMIT);


        Boolean isExist=false;
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null!=userSecurity) {
            String mobile=userSecurity.getMobile();
            if(org.apache.commons.lang3.StringUtils.isNotBlank(mobile)){
                isExist= mobile.endsWith(requestBody.getMobile());
            }
        }
        if (isExist) {
            UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(requestBody.getUserId());
            if (null != userInfo && null != userInfo.getTradeLevel()) {
                if (userInfo.getTradeLevel().intValue() >= 4) {
                    isExist = false;
                    log.info("hint currentUserId level");
                }
            }
        }
        return APIResponse.getOKJsonResult(isExist);
    }

    @Override
    public APIResponse<SendBindMobileVerifyCodeResponse> sendBindMobileVerifyCode(
            APIRequest<SendBindMobileVerifyCodeRequest> request) {

        final SendBindMobileVerifyCodeRequest requestBody = request.getBody();

        final Long userId = requestBody.getUserId();
        final String mobile = requestBody.getMobile();
        final String mobileCode = requestBody.getMobileCode();

        if(org.apache.commons.lang3.StringUtils.isBlank(mobile) || org.apache.commons.lang3.StringUtils.isBlank(mobileCode)) {
        	log.warn("参数为null,userId:{},mobile:{},mobileCode:{}", userId, mobile, mobileCode);
        	throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 一分钟频率控制
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(userId), String.class, SEND_BIND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, SEND_BIND_MOBILE_VERIFY_CODE_KEY);

        this.iCountry.getCountryByCode(requestBody.getMobileCode());

        UserMobileIndex userMobileIndex =
                this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());
        if (userMobileIndex != null) {
            throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
        }

        // 手机号以0开头的过滤
        if (requestBody.getMobile().startsWith("0")) {
            if (requestBody.getMobile().length() > 1) {
                userMobileIndex = this.userMobileIndexMapper.selectByPrimaryKey(
                        requestBody.getMobile().substring(1, requestBody.getMobile().length()),
                        requestBody.getMobileCode());
                if (userMobileIndex != null) {
                    throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
                }
            } else {
                throw new BusinessException(GeneralCode.SYS_VALID);
            }

        } else {
            userMobileIndex = this.userMobileIndexMapper.selectByPrimaryKey("0" + requestBody.getMobile(),
                    requestBody.getMobileCode());
            if (userMobileIndex != null) {
                throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
            }
        }

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 用户已经绑定过手机号, 如果是重置手机的，不需要校验这步
        if (!requestBody.getResetMobile() && StringUtils.isNotBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_MOBILE_BIND);
        }

        String code = StringUtils.getNumberRandomString(6);

        SendMsgRequest requestSms = new SendMsgRequest();
        requestSms.setIp(WebUtils.getRequestIp());
        Country country = this.iCountry.getCountryByCode(requestBody.getMobileCode());
        if (country == null){
            throw new BusinessException(AccountErrorCode.USER_MOBILE_CODE_NOT_EXIST);
        }
        requestSms.setMobileCode(country.getMobileCode());
        requestSms.setRecipient(requestBody.getMobile());
        requestSms.setUserId(requestBody.getUserId().toString());
        requestSms.setTplCode(Constant.NODE_TYPE_MOBILE_VERIFY);
        requestSms.setSourceFunction(BIND_MOBILE_SOURCE_FUNCTION);

        // 绑定手机加上 verifyCodeId
        requestSms.setVerifyCodeId(requestBody.getVerifyCodeId());
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, code);
        requestSms.setData(params);

        //增加是否是语音类型模板和是否重新发送的类型
        requestSms.setMsgType(MsgType.valueOf(requestBody.getMsgType().name()));
        requestSms.setResend(requestBody.getResend());


        // 一分钟频率控制
        String globalmobilekey=requestSms.getMobileCode()+requestSms.getRecipient();
        String frequencymobileLimits = RedisCacheUtils.get(globalmobilekey, String.class, SEND_BIND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencymobileLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        RedisCacheUtils.set(globalmobilekey, globalmobilekey, 60L, SEND_BIND_MOBILE_VERIFY_CODE_KEY);
        log.info("frequencymobileLimits userId={}",requestBody.getUserId());


        // 发送短信
        userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());

        // 有效期30分钟
        Long expireTime = 30L;
        String mobileKey = StringUtils.getMobileKey(requestBody.getMobile(), requestBody.getMobileCode()).toUpperCase();

        RedisVerify redisVerify = new RedisVerify();
        redisVerify.setTime(DateUtils.getNewUTCDate());
        redisVerify.setCode(commonConfig.convertSecretCode(requestBody.getMobile(),code));
        redisVerify.setErrorCount(0);

        // 验证码
        RedisCacheUtils.set(mobileKey, redisVerify, expireTime * 60L, CacheKeys.MOBILE_AUTH_TIME);

        return APIResponse.getOKJsonResult(new SendBindMobileVerifyCodeResponse(null));
    }

    @Override
    public SendBindEmailVerifyCodeResponse sendBindEmailVerifyCode(SendBindEmailVerifyCodeRequest request) throws Exception {

        final Long userId = request.getUserId();
        // email强制转换成小写
        final String email = request.getEmail().trim().toLowerCase();
        if (!Pattern.matches(REGEX_EMAIL, email) || email.length() > MAX_EMAIL_LENGTH) {
            log.warn("参数为null,userId:{},email:{}", userId, email);
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        // 一分钟频率控制
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(userId), String.class, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);

        User existUser = this.userMapper.queryByEmail(email);
        if (existUser != null) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }
        User user = userCommonBusiness.checkAndGetUserById(userId);

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        // 用户已经绑定过邮箱了
        if (!request.getResetEmail() && !userStatusEx.getIsUserNotBindEmail()) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_ALREADY_BIND);
        }
        // 一分钟频率控制
        String frequencymobileLimits = RedisCacheUtils.get(email, String.class, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);
        if (StringUtils.isNotBlank(frequencymobileLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }

        String[] sendParams = new String[2];
        try {
            log.info("sendBindEmailVerifyCode 发送邮件验证码");
            user.setEmail(email);
            sendParams = userCommonBusiness.sendEmailVerifyCode(user, BIND_EMAIL, null);
        } catch (Exception e) {
            log.error(String.format("send sendBindEmailVerifyCode failed, userId:%s, exception:", user.getUserId()), e);
        }
        RedisCacheUtils.set(email, email, 60L, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);
        log.info("frequencyEmaileLimits userId={}",userId);
        UserBindSendInfo userBindSendInfo =new UserBindSendInfo();
        userBindSendInfo.setUserId(userId);
        userBindSendInfo.setEmail(email);
        RedisCacheUtils.set(userId.toString(), JsonUtils.toJsonNotNullKey(userBindSendInfo), 60 * 60L, AccountConstants.SEND_BIND_INFO_KEY);
        log.info("SEND_BIND_INFO_KEY userId={},bindInfo={}",userId,JsonUtils.toJsonNotNullKey(userBindSendInfo));


        return new SendBindEmailVerifyCodeResponse();
    }

    @Override
    @SecurityLog(name = "绑定邮箱", operateType = AccountConstants.SECURITY_OPERATE_TYPE_BIND_EMAIL,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId",type = RiskTaskAspect.UPDATE_EMAIL,email = "#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public BindEmailResponse bindEmail(APIRequest<BindEmailRequest> request) throws Exception {
        BindEmailRequest requestBody = request.getBody();
        String email = requestBody.getEmail().trim().toLowerCase();
        Long userId = requestBody.getUserId();
        User originUser=userCommonBusiness.checkAndGetUserById(userId);
        UserStatusEx userStatusEx=new UserStatusEx(originUser.getStatus());
        if (!userStatusEx.getIsUserNotBindEmail()) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_ALREADY_BIND);// 用户已经绑定无需继续绑定
        }
        //验证email合法性
        if (!Pattern.matches(REGEX_EMAIL, email)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        long userIdBindCount = Long.valueOf(String.valueOf(RedisCacheUtils.get(userId.toString(), Long.class, AccountConstants.BIND_EMAIL_COUNT_USERID, 0L)));
        log.info("userIdBindCount userId={},userIdBindCount={}",userId,userIdBindCount);
        if(userIdBindCount>2){
            try {
                RedisCacheUtils.increment(userId.toString(), AccountConstants.BIND_EMAIL_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
            } catch (Exception e) {
                log.error("bindEmail userid限制", e);
            }
            throw new BusinessException(GeneralCode.SYS_ZUUL_ERROR);
        }
        //不能用别人用过的邮箱
        User existedUserByEmail = userMapper.queryByEmail(email);
        if (null != existedUserByEmail) {
            try {
                RedisCacheUtils.increment(userId.toString(), AccountConstants.BIND_EMAIL_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
            } catch (Exception e) {
                log.error("bindEmail userid限制", e);
            }
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);

        }

        String userBindInfoStr = RedisCacheUtils.get(userId.toString(), String.class, AccountConstants.SEND_BIND_INFO_KEY);
        UserBindSendInfo userBindSendInfo =JSON.parseObject(userBindInfoStr, UserBindSendInfo.class);
        if(null==userBindSendInfo|| !email.equalsIgnoreCase(userBindSendInfo.getEmail())){
            log.warn("checkBindInfoError userId={},requestEmail={},bindInfo={}",userId,email,JsonUtils.toJsonNotNullKey(userBindSendInfo));
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        email=userBindSendInfo.getEmail().trim().toLowerCase();


        // 邮箱验证码验证
        VerificationsTwo emailVerificationsTwo = this.isEmailAuthenticator(requestBody.getUserId(), requestBody.getEmailVerifyCode(), false);

        //新2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.BIND_EMAIL)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        //验证逻辑完毕，开始修改邮箱
        int num = userMapper.deleteByEmail(originUser.getEmail());
        if (num > 0) {
            //user表使用email做分表条件的所以需要删除
            User newSubUser = new User();
            BeanUtils.copyProperties(originUser, newSubUser);
            newSubUser.setStatus(BitUtils.disable(newSubUser.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL));
            newSubUser.setEmail(email);
            userMapper.insert(newSubUser);
            //userindex和usersecurity是根据userid做分表条件的所以只要update
            UserIndex updateUserIndex = new UserIndex();
            updateUserIndex.setUserId(userId);
            updateUserIndex.setEmail(email);
            userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);
            UserSecurity newSecurity = new UserSecurity();
            newSecurity.setUserId(userId);
            newSecurity.setEmail(email);
            userSecurityMapper.updateByPrimaryKeySelective(newSecurity);
            log.info("UserSecurityBusiness.bindEmail: success email:{},userId={} ", email,userId);

            notificationToC2CHelper.sendEmailBindingChangeMsgAsync(newSecurity);
        } else {
            log.error("UserSecurityBusiness.bindEmail:Delete  failed, email:{},userId={}", email,userId);
        }
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = Maps.newHashMap();
        dataMsg.put(UserConst.USER_ID, userId);
        dataMsg.put(UserConst.EMAIL, email);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.MODIFY_SUBACCOUNT_EMAIL, dataMsg);
        log.info("iMsgNotification bindEmail:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);

        // 绑定成功删除验证码
        if (emailVerificationsTwo != null) {
            RedisCacheUtils.del(requestBody.getUserId().toString(), AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
        }
        return new BindEmailResponse();
    }


    @Override
    public SendEmailVerifyCodeResponse sendEmailVerifyCode(SendEmailVerifyCodeRequest request) throws Exception {
        Long userId=request.getUserId();
        log.info("sendEmailVerifyCode userId={}",userId);
        // 一分钟频率控制 userid纬度
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(userId), String.class, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        User user= userCommonBusiness.checkAndGetUserById(userId);
        if (BitUtils.isTrue(user.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        String[] sendParams = new String[2];
        try {
            log.info("sendEmailVerifyCode 发送邮件验证码");
            if (request.getBizScene() == NEW_EMAIL_VERIFY) {
                if (StringUtils.isBlank(request.getFlowId())) {
                    log.error("sendEmailVerifyCode,new_email_verify,flowid blank");
                    throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
                }
                APIResponse<UserEmailChange> emailChange = iUserEmailChange.findByFlowIdAndUid(request.getFlowId(), userId);
                if (null == emailChange.getData()) {
                    throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
                }
                user.setEmail(emailChange.getData().getNewEmail());
            }
            sendParams = userCommonBusiness.sendEmailVerifyCode(user, request.getBizScene(), request.getParams());
        } catch (Exception e) {
            log.error(String.format("send sendEmailVerifyCode failed, userId:%s, exception:", user.getUserId()), e);
        }
        RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, AccountConstants.SEND_EMAIL_VERIFY_CODE_LIMIT_KEY);
        return new SendEmailVerifyCodeResponse();
    }

    @SecurityLog(name = "打开手机验证", operateType = Constant.SECURITY_OPERATE_TYPE_OPEN_MOBILE_VERIFY,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> openMobileVerify(APIRequest<OpenOrCloseMobileVerifyRequest> request) {
        final OpenOrCloseMobileVerifyRequest requestBody = request.getBody();
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        int flag = 0;
        if (BitUtils.isFalse(status, Constant.USER_MOBILE)) {
            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            if (StringUtils.isBlank(userSecurity.getMobile())) {// 用户没有绑定手机
                throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
            }
            User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.enable(status, Constant.USER_MOBILE));
            flag = this.userMapper.updateByEmailSelective(user);
        }
        return APIResponse.getOKJsonResult(flag);
    }

    @SecurityLog(name = "关闭手机验证", operateType = Constant.SECURITY_OPERATE_TYPE_CLOSE_MOBILE_VERIFY,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> closeMobileVerify(APIRequest<OpenOrCloseMobileVerifyRequest> request) {
        final OpenOrCloseMobileVerifyRequest requestBody = request.getBody();
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        int flag = 0;
        if (BitUtils.isTrue(status, Constant.USER_MOBILE)) {
            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            if (StringUtils.isBlank(userSecurity.getMobile())) {// 用户没有绑定手机
                throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
            }
            User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.disable(status, Constant.USER_MOBILE));
            flag = this.userMapper.updateByEmailSelective(user);
        }
        return APIResponse.getOKJsonResult(flag);
    }

    @SecurityLog(name = "开启BNB燃烧", operateType = Constant.SECURITY_OPERATE_TYPE_OPEN_BNB_FEE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> openBNBFee(APIRequest<OpenOrCloseBNBFeeRequest> request)throws Exception {
        final OpenOrCloseBNBFeeRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        UserStatusEx userStatusEx=new UserStatusEx(status);
        int flag = 0;
        if (BitUtils.isFalse(status, Constant.USER_FEE)) {
            final User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.enable(status, Constant.USER_FEE));
            flag = this.userMapper.updateByEmailSelective(user);
            if (userStatusEx.getIsExistMarginAccount()) {
                ((UserSecurityBusiness) AopContext.currentProxy()).setMarginBnbFee(requestBody.getUserId(),true);
            }
            if (userStatusEx.getIsExistIsolatedMarginAccount()) {
                userInfoAsyncTask.setIsolatedMarginUserBnbBurn(requestBody.getUserId(),true);
            }
        }
        return APIResponse.getOKJsonResult(flag);
    }
    /**
     * 设置margin账号是否使用燃烧bnb的操作
     * */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Integer setMarginBnbFee(Long rootUserId,Boolean enableBnbFlag)throws Exception{
        log.info("setMarginBnbFee: rootUserId={}, enableBnbFlag={}",rootUserId,enableBnbFlag);
        try{
            final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(rootUserId);
            if(null==userInfo||null==userInfo.getMarginUserId()){
                log.info("setMarginBnbFee: userInfo is null or marginUserId is null  rootUserId={} ",rootUserId);
                return 0;
            }
            UserIndex marginUserIndex = userIndexMapper.selectByPrimaryKey(userInfo.getMarginUserId());
            User marginUser = userMapper.queryByEmail(marginUserIndex.getEmail());
            if(!userCommonBusiness.isMarginUser(marginUser.getStatus())){
                throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);//不支持的操作
            }
            int updateResult = 0;
            if(enableBnbFlag){
                marginUser.setStatus(BitUtils.enable(marginUser.getStatus(), Constant.USER_FEE));
                updateResult = this.userMapper.updateByEmailSelective(marginUser);
            }else{
                marginUser.setStatus(BitUtils.disable(marginUser.getStatus(), Constant.USER_FEE));
                updateResult = this.userMapper.updateByEmailSelective(marginUser);
            }
            //同步像撮合修改燃烧bnb的状态
            accountApiClient.setGas(marginUser.getUserId().toString(),enableBnbFlag);
            log.info("setMarginBnbFee finish: rootUserId={}, enableBnbFlag={}，updateResult={}",rootUserId,enableBnbFlag,updateResult);
            return updateResult;
        }catch (Exception e){
            log.error("setMarginBnbFee error",e);
            return 0;
        }

    }



    @SecurityLog(name = "关闭BNB燃烧", operateType = Constant.SECURITY_OPERATE_TYPE_CLOSE_BNB_FEE,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> closeBNBFee(APIRequest<OpenOrCloseBNBFeeRequest> request) throws Exception{
        final OpenOrCloseBNBFeeRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        UserStatusEx userStatusEx=new UserStatusEx(status);
        int flag = 0;
        if (BitUtils.isTrue(status, Constant.USER_FEE)) {
            final User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.disable(status, Constant.USER_FEE));
            flag = this.userMapper.updateByEmailSelective(user);
            if (userStatusEx.getIsExistMarginAccount()) {
                ((UserSecurityBusiness) AopContext.currentProxy()).setMarginBnbFee(requestBody.getUserId(),false);
            }
            if (userStatusEx.getIsExistIsolatedMarginAccount()) {
                userInfoAsyncTask.setIsolatedMarginUserBnbBurn(requestBody.getUserId(),false);
            }
        }

        return APIResponse.getOKJsonResult(flag);
    }

    public void checkGoogleAuthenticator(Long userId, String code) {
        code = code.replaceAll(StringUtils.SPACE, StringUtils.EMPTY);
        if (!StringUtils.isNumeric(code) || code.length() != 6) {
            log.warn("2FA无效输入, code:{}, userIp:{}", code, WebUtils.getRequestIp());
            throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR, userId, null);
        }
        checkGoogleAuthenticator(userId, Integer.valueOf(code));
    }

    @Override
    public void checkGoogleAuthenticator(Long userId, Integer code) {// 内部调用
        if (nonProdEnv2Fa(null != code ? code.toString() : StringUtils.EMPTY)) {
            return;
        }
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (StringUtils.isBlank(userSecurity.getAuthKey())) {
            throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
        }
        UserSecurityCache userSecurityCache =
                RedisCacheUtils.get(userId.toString(), UserSecurityCache.class, CacheKeys.USER_SECURITY_INFO);
        if (userSecurityCache != null) {
            if (userSecurityCache.getGoogleVerifyCode() != null
                    && userSecurityCache.getGoogleVerifyCode().intValue() == code.intValue()) {
                throw new BusinessException(GeneralCode.USER_GOOGLE_CODE_NOT_REUSE);
            }
            if (userSecurityCache.getGoogleErrorTime() != null
                    && (userSecurityCache.getGoogleErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils
                            .getNewUTCTimeMillis()) {
                throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
            }
        }
        final String securityCipher =
                RedisCacheUtils.get(CacheKeys.SECURITY_CIPHER, "Q8oYo6tna4LWlIhQjPX6XNMVtwqZOXJY", true);
        // 谷歌2次验证
        final String authKey = EncryptionUtils.decryptAESToString(userSecurity.getAuthKey(), securityCipher);

        log.info("checkGoogleAuthenticator");
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean isVerifyCode = gAuth.authorize(authKey, code);
        // 有效期30分钟
        Long expireTime = 30L;
        if (userSecurityCache == null) {
            userSecurityCache = new UserSecurityCache();
        }
        // 验证错误
        if (!isVerifyCode) {
            userSecurityCache.setGoogleErrorTime(DateUtils.getNewUTCTimeMillis());
            RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60, CacheKeys.USER_SECURITY_INFO);
            throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR, userId, null);
        }
        userSecurityCache.setGoogleVerifyCode(code);
        RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60, CacheKeys.USER_SECURITY_INFO);
    }

    @Override
    public VerificationsTwo isSmsAuthenticator(Long userId, String code, boolean autoDel, String mobile,
            String mobileCode) throws BusinessException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_FILL);
        }

        if (nonProdEnv2Fa(code)) {
            return null;
        }
        if (StringUtils.isBlank(mobile)) {
            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            mobile = userSecurity.getMobile();
            mobileCode = userSecurity.getMobileCode();
        }
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }
        UserSecurityCache userSecurityCache =
                RedisCacheUtils.get(userId.toString(), UserSecurityCache.class, CacheKeys.USER_SECURITY_INFO);
        if (userSecurityCache != null) {
            if (userSecurityCache.getSmsErrorTime() != null
                    && (userSecurityCache.getSmsErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils
                            .getNewUTCTimeMillis()) {
                throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
            }
        }
        String mobileKey = StringUtils.getMobileKey(mobile, mobileCode).toUpperCase();
        RedisVerify redisVerify = RedisCacheUtils.get(mobileKey, RedisVerify.class, CacheKeys.MOBILE_AUTH_TIME);

        if (null == redisVerify || StringUtils.isBlank(redisVerify.getCode())) {
            throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_EXPIRED);
        }

        if (null != redisVerify.getErrorCount() && redisVerify.getErrorCount() >= ERROR_COUNT) {
            throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_TIME_LIMITE);
        }

        Long expireTime = 30L;// 有效期30分钟
        if (userSecurityCache == null) {
            userSecurityCache = new UserSecurityCache();
        }
        if (!StringUtils.equals(redisVerify.getCode(), commonConfig.convertSecretCode(mobile,code))) {

            redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);

            if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount() >= ERROR_COUNT) {
                // redis 删除短信验证码
                // RedisCacheUtils.del(mobileKey, CacheKeys.MOBILE_AUTH_TIME);
                RedisCacheUtils.set(mobileKey, redisVerify, -1, CacheKeys.MOBILE_AUTH_TIME);

                userSecurityCache.setSmsErrorTime(DateUtils.getNewUTCTimeMillis());
                RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60,
                        CacheKeys.USER_SECURITY_INFO);

                throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_TIME_LIMITE);
            } else {
                // count +1 更新
                RedisCacheUtils.set(mobileKey, redisVerify, -1, CacheKeys.MOBILE_AUTH_TIME);
            }

            userSecurityCache.setSmsErrorTime(DateUtils.getNewUTCTimeMillis());
            RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60, CacheKeys.USER_SECURITY_INFO);
            throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_ERROR);
        }

        if (isOpenTwilioFeedBack) {
            try {
                if (!"CN".equals(mobileCode) && !"cn".equals(mobileCode)) {
                    // code userId 异步去调用一个message 的twilioAPI接口。
                    final TwilioFeedBackRequest twilioFeedBackRequest = new TwilioFeedBackRequest();
                    twilioFeedBackRequest.setUserId(userId.toString());
                    twilioFeedBackRequest.setVerifyCode(code);
                    msgAsyncTask.sendTwilioFeedBack(twilioFeedBackRequest, WebUtils.getAPIRequestHeader().getLanguage(),
                            WebUtils.getAPIRequestHeader().getTerminal());
                }
            } catch (Exception e) {
                log.warn("调用twilioFeedbackAPI失败");
                log.warn(e);
            }
        }

        VerificationsTwo verificationsTwo = new VerificationsTwo(mobileKey);

        if (autoDel) {
            verificationsTwo.delMobileCode();
        }
        return verificationsTwo;
    }


    public VerificationsTwo isEmailAuthenticator(Long userId, String code, boolean autoDel) throws BusinessException {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_CODE_LOSS);
        }

        if (nonProdEnv2Fa(code)) {
            return null;
        }
        UserSecurityCache userSecurityCache =
                RedisCacheUtils.get(userId.toString(), UserSecurityCache.class, CacheKeys.USER_SECURITY_INFO);
        if (userSecurityCache != null) {
            if (userSecurityCache.getEmailErrorTime() != null
                    && (userSecurityCache.getEmailErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils
                    .getNewUTCTimeMillis()) {
                throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
            }
        }

        String emailVerifyKeyStr = RedisCacheUtils.get(userId.toString(), String.class, AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
        if (StringUtils.isBlank(emailVerifyKeyStr)) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_CODE_EXPIRED);
        }

        RedisVerify redisVerify = JSON.parseObject(emailVerifyKeyStr, RedisVerify.class);
        if (null != redisVerify.getErrorCount() && redisVerify.getErrorCount() >= ERROR_COUNT) {
            throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_TIME_LIMIT);
        }

        Long expireTime = 30L;// 有效期30分钟
        if (userSecurityCache == null) {
            userSecurityCache = new UserSecurityCache();
        }
        if (!StringUtils.equals(redisVerify.getCode(), code)) {

            redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);

            if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount() >= ERROR_COUNT) {
                // redis 删除短信验证码
                // RedisCacheUtils.del(mobileKey, CacheKeys.MOBILE_AUTH_TIME);
                RedisCacheUtils.set(userId.toString(), redisVerify, -1, AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);

                userSecurityCache.setEmailErrorTime(DateUtils.getNewUTCTimeMillis());
                RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60,
                        CacheKeys.USER_SECURITY_INFO);

                throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_TIME_LIMIT);
            } else {
                // count +1 更新
                RedisCacheUtils.set(userId.toString(), redisVerify, -1, AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
            }

            userSecurityCache.setEmailErrorTime(DateUtils.getNewUTCTimeMillis());
            RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60, CacheKeys.USER_SECURITY_INFO);
            throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_CODE_ERROR);
        }


        VerificationsTwo verificationsTwo = new VerificationsTwo();

        if (autoDel) {
            RedisCacheUtils.del(userId.toString(), AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
        }
        return verificationsTwo;
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public VerificationsTwo verificationsTwoV2(Long userId, AuthTypeEnum authType, String code, SecurityKeyApplicationScenario scenario, boolean autoDel)
            throws Exception {
        if (scenario == null) {
            //兼容pnk
            scenario = SecurityKeyApplicationScenario.withdrawAndApi;
        }
        if (isYubikeyEnabledInSpecifiedScenario(userId, scenario)) {
            if (authType != AuthTypeEnum.FIDO2) {
                log.warn("enabled yubikey but use other auth type:{}, user:{}, scenario: {}", authType, userId, scenario);
                throw new BusinessException(AccountErrorCode.MUST_USE_YUBIKEY_TO_AUTHENTICATE);
            }
            log.info("verificationsTwoV2 fido2, user:{}", userId);
            verifySecurityKey(userId, code);
            return null;
        } else {
            if (authType == AuthTypeEnum.FIDO2) {
                log.warn("user intend to verify with yubikey but not registered or not enabled in this scenario. userId: {}, scenario: {}", userId, scenario);
            }
            return verificationsTwo(userId, authType, code, autoDel);
        }
    }

    public void verifySecurityKey(Long userId, String code) {
        try {
            code = new String(BaseEncoding.base64().decode(code));
        } catch (Exception e) {
            log.info("verificationsTwoV2 base64 decode failed.", e);
        }
        boolean pass = false;
        try {
            pass = webAuthnFrontHandler.finishAuthenticate(userId, code, false);
        } catch (Exception e) {
            log.info("verificationsTwoV2 fido2 failed.", e);
            pass = false;
        }
        if (!pass) {
            throw new BusinessException(GeneralCode.USER_2FA_CODE_ERROR,userId,null);
        }
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public VerificationsTwo verificationsTwo(Long userId, AuthTypeEnum authType, String code, boolean autoDel)
            throws Exception {
        VerificationsTwo verificationsTwo = null;
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        UserIndex userIndex=this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (BitUtils.isFalse(user.getStatus(), Constant.USER_MOBILE)
                && BitUtils.isFalse(user.getStatus(), Constant.USER_GOOGLE)) {// 谷歌和手机都未开启直接过
            return null;
        }
        if (BitUtils.isFalse(user.getStatus(), Constant.USER_MOBILE)) {
            if (authType == AuthTypeEnum.SMS) {// 未开启手机使用手机登录
                throw new BusinessException(GeneralCode.USER_MOBILE_VERIFY_NO);
            }
        }
        if (BitUtils.isFalse(user.getStatus(), Constant.USER_GOOGLE)) {
            if (authType == AuthTypeEnum.GOOGLE) {// 未开启google使用google
                throw new BusinessException(GeneralCode.USER_GOOGLE_VERIFY_NO);
            }
        }
        switch (authType) {
            // 使用谷歌
            case GOOGLE:
                checkGoogleAuthenticator(userId, code);
                break;
            // 使用短信
            case SMS:
                verificationsTwo = this.isSmsAuthenticator(userId, code, autoDel, null, null);
                break;
            default:
                throw new BusinessException(GeneralCode.SYS_NOT_VALID_TYPE);
        }
        return verificationsTwo;
    }

    @Override
    public void verifyMultiFactors(MultiFactorSceneVerify verify) throws Exception {
        log.info("verifyMultiFactors verify={}", verify.toString());
        MultiFactorSceneCheckResult checkResult = getVerificationTwoCheckFactors(verify.getUserId(),verify.getBizScene(), null, null);
        log.info("verifyMultiFactors verify={},factors={}", verify.toString(), JSON.toJSONString(checkResult));
        //如果校验出现绑定提示，说明验证前跳过了获取验证列表或获取的验证列表已过期
        if(!CollectionUtils.isEmpty(checkResult.getNeedBindVerifyList())){
            checkResult.getNeedBindVerifyList().forEach(x -> {
                switch (x.getVerifyType()) {
                    case SMS:
                        throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_BIND);
                    case EMAIL:
                        throw new BusinessException(AccountErrorCode.USER_EMAIL_NOT_BIND);
                    case GOOGLE:
                        throw new BusinessException(AccountErrorCode.USER_GOOGLE_NOT_BIND);
                    case YUBIKEY:
                        throw new BusinessException(AccountErrorCode.YUBIKEY_NOT_REGISTER);
                    default:
                        throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
                }
            });
        }

        // 校验项为空就不需要校验
        if (CollectionUtils.isEmpty(checkResult.getNeedCheckVerifyList())) {
            return;
        }

        boolean emailPassFlag=false;
        boolean mobilePassFlag=false;
        VerificationsTwo smsVerificationsTwo=null;
        VerificationsTwo emailVerificationsTwo=null;

        //2 遍历需要check的list，如果都check通过那么清空缓存，有一个不过就异常扔出去
        boolean isChoose2FaBizScene = false;
        boolean verifyExistSuccess = false;
        AccountVerificationTwoEnum userChoose2Fa = null;

        // 针对必验+选验场景进行排序，先验证必验，再验证选验
        List<AccountVerificationTwoCheck> checkList = new ArrayList<>(checkResult.getNeedCheckVerifyList());
        checkList.sort((m,n) -> n.getOption().compareTo(m.getOption()));

        Iterator<AccountVerificationTwoCheck> iterator = checkList.iterator();
        while (iterator.hasNext()) {
            if (isChoose2FaBizScene && verifyExistSuccess) {
                break;
            }
            AccountVerificationTwoCheck check = iterator.next();
            switch (check.getVerifyType()) {
                case YUBIKEY:
                    SecurityKeyApplicationScenario scenario = fetchScenarioByScene(verify.getBizScene());
                    if (scenario != null && isYubikeyEnabledInSpecifiedScenario(verify.getUserId(), scenario)) {
                        log.info("verificationsTwoV2 fido2, user:{}", verify.getUserId());
                        if (StringUtils.isBlank(verify.getYubikeyVerifyCode())) {
                            throw new BusinessException(AccountErrorCode.USER_YUBIKEY_VERIFY_CODE_LOSS);
                        }
                        verifySecurityKey(verify.getUserId(), verify.getYubikeyVerifyCode());
                    }
                    break;
                case SMS:
                    // 选验场景校验
                    if (check.getOption() != null && check.getOption().equals(0)) {
                        isChoose2FaBizScene = true;
                        if (StringUtils.isBlank(verify.getMobileVerifyCode())) {
                            if (iterator.hasNext()) {
                                break;
                            }
                            throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_FILL);
                        }
                        userChoose2Fa = SMS;
                    }

                    smsVerificationsTwo = this.isSmsAuthenticator(verify.getUserId(), verify.getMobileVerifyCode(),
                            false, null, null);

                    mobilePassFlag = true;
                    verifyExistSuccess = true;
                    break;
                case EMAIL:
                    // 选验场景校验
                    if (check.getOption() != null && check.getOption().equals(0)) {
                        isChoose2FaBizScene = true;
                        if (StringUtils.isBlank(verify.getEmailVerifyCode())) {
                            if (iterator.hasNext()) {
                                break;
                            }
                            throw new BusinessException(AccountErrorCode.USER_EMAIL_VERIFY_CODE_LOSS);
                        }
                        userChoose2Fa = EMAIL;
                    }

                    emailVerificationsTwo = this.isEmailAuthenticator(verify.getUserId(), verify.getEmailVerifyCode(), false);
                    emailPassFlag = true;
                    verifyExistSuccess = true;
                    break;
                case GOOGLE:
                    // 选验场景校验
                    if (check.getOption() != null && check.getOption().equals(0)) {
                        isChoose2FaBizScene = true;
                        if (StringUtils.isBlank(verify.getGoogleVerifyCode())) {
                            if (iterator.hasNext()) {
                                break;
                            }
                            throw new BusinessException(AccountErrorCode.USER_GOOGLE_VERIFY_CODE_LOSS);
                        }
                        userChoose2Fa = GOOGLE;
                    }

                    checkGoogleAuthenticator(verify.getUserId(), verify.getGoogleVerifyCode());
                    verifyExistSuccess = true;
                    break;
                default:
                    throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
            }
        }

        // 选验场景下记录用户上次选择2fa类型，这将在获取2fa列表且存在当前2fa项可选时优先透出这次该2fa项
        if (userChoose2Fa != null && verifyExistSuccess) {
            iUser.addOrUpdateUserConfig(verify.getUserId(), USER_CONFIG_LAST_2FA_TYPE, userChoose2Fa.getCode());
        }

        if (emailPassFlag && null != emailVerificationsTwo) {
            RedisCacheUtils.del(verify.getUserId().toString(), AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
            log.info("del emailCode userId={}",verify.getUserId());
        }
        if (mobilePassFlag && null != smsVerificationsTwo) {
            smsVerificationsTwo.delMobileCode();
            log.info("del mobileCode userId={}",verify.getUserId());

        }
    }

    @Override
    public MultiFactorSceneCheckResult getVerificationTwoCheckList(MultiFactorSceneCheckQuery query) throws Exception {
        MultiFactorSceneCheckResult result = getVerificationTwoCheckFactors(query.getUserId(), query.getBizScene(), query.getClientType(), query.getDeviceInfo());
        if (!CollectionUtils.isEmpty(result.getNeedBindVerifyList())) {
            // Yubikey获取2fa列表不带出，前端无法识别，只在验证时验证
            result.getNeedBindVerifyList().removeIf(x -> x.getVerifyType() == AccountVerificationTwoEnum.YUBIKEY);
        }

        if (!CollectionUtils.isEmpty(result.getNeedBindVerifyList())) {
            result.setNeedCheckVerifyList(null);
            return result;
        }

        if (CollectionUtils.isEmpty(result.getNeedCheckVerifyList())) {
            return result;
        }

        // Yubikey验证项不带出(只在校验时会校验)
        result.getNeedCheckVerifyList().removeIf(x -> x.getVerifyType() == AccountVerificationTwoEnum.YUBIKEY);

        if (CollectionUtils.isEmpty(result.getNeedCheckVerifyList())) {
            return result;
        }

        // 选验场景下优先透出用户上次选择的2fa类型并置1代表优先只展示这一个，其他继续0透出，代表可切换验证
        List<AccountVerificationTwoCheck> optionCheckList = result.getNeedCheckVerifyList().stream().filter(x ->
                x.getOption() != null && x.getOption().equals(0)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(optionCheckList)) {
            String last2Fa = iUser.getConfigByConfigType(query.getUserId(), USER_CONFIG_LAST_2FA_TYPE);
            if (StringUtils.isNotBlank(last2Fa)) {
                // 必须先确认用户上次选择的2fa在此次可选2fa中，再设置该2fa为必选，代表优先只展示这一项
                result.getNeedCheckVerifyList().forEach(x -> {
                    if (StringUtils.equals(last2Fa, x.getVerifyType().getCode())) {
                        x.setOption(1);
                    }
                });
                // 重排序，1的提前（前端优先展示1）
                List<AccountVerificationTwoCheck> list = Lists.newArrayList(result.getNeedCheckVerifyList());
                list.sort((o1, o2) -> o2.getOption().compareTo(o1.getOption()));
                result.setNeedCheckVerifyList(Sets.newHashSet(list));
            }else if(BizSceneEnum.LOGIN == query.getBizScene()){
                // 如果是登录场景，并且上次没有选择验证哪儿个2fa，那么默认手机必选
                result.getNeedCheckVerifyList().forEach(x -> {
                    if (StringUtils.equals("sms", x.getVerifyType().getCode())) {
                        x.setOption(1);
                    }
                });
                // 重排序，1的提前（前端优先展示1）
                List<AccountVerificationTwoCheck> list = Lists.newArrayList(result.getNeedCheckVerifyList());
                list.sort((o1, o2) -> o2.getOption().compareTo(o1.getOption()));
                result.setNeedCheckVerifyList(Sets.newHashSet(list));
            }
        }

        Optional<AccountVerificationTwoCheck> needMask2Fa = result.getNeedCheckVerifyList().stream().filter(x ->
                x.getVerifyType() == SMS || x.getVerifyType() == EMAIL).findFirst();
        if (!needMask2Fa.isPresent()) {
            return result;
        }

        // 掩码相关
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(query.getUserId());
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        result.getNeedCheckVerifyList().forEach(x -> {
            if (x.getVerifyType() == SMS) {
                x.setVerifyTargetMask(MaskUtils.maskMobileNo(userSecurity.getMobile()));
                return;
            }
            if (x.getVerifyType() == EMAIL) {
                if (query.getBizScene() != NEW_EMAIL_VERIFY) {
                    x.setVerifyTargetMask(MaskUtils.maskHalfEmail(userSecurity.getEmail()));
                    return;
                }

                // 更换邮箱场景，需要透出新邮箱掩码
                if (StringUtils.isBlank(query.getFlowId())) {
                    throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
                }
                APIResponse<UserEmailChange> emailChange = iUserEmailChange.findByFlowIdAndUid(query.getFlowId(), query.getUserId());
                if (null == emailChange.getData()) {
                    throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
                }
                x.setVerifyTargetMask(MaskUtils.maskHalfEmail(emailChange.getData().getNewEmail()));
            }
        });
        log.info("获取2fa列表,verifyQuery={},checkList={}", JSON.toJSONString(query), JSON.toJSONString(result));
        return result;
    }

    private MultiFactorSceneCheckResult getVerificationTwoCheckFactors(Long userId, BizSceneEnum bizScene, String clientType, Map<String, String> deviceInfo) {
        User user = userCommonBusiness.checkAndGetUserById(userId);
        UserStatusEx status = new UserStatusEx(user.getStatus());

        SecurityKeyApplicationScenario scenario = fetchScenarioByScene(bizScene);
        //存在需要拦截的场景，并且该场景下开启了yubikey才会为true
        boolean isEnableYubikey = scenario != null && isYubikeyEnabledInSpecifiedScenario(user.getUserId(), scenario);

        UserTwoVerifyInfo verifyInfo = new UserTwoVerifyInfo();
        verifyInfo.setUserId(userId);
        verifyInfo.setBizScene(bizScene);
        verifyInfo.setIsBindEmail(!status.getIsUserNotBindEmail());
        verifyInfo.setIsBindMobile(status.getIsUserMobile());
        verifyInfo.setIsBindGoogle(status.getIsUserGoogle());
        verifyInfo.setIsBindYubikey(isEnableYubikey);
        return multiFactorVerifyService.get2FaVerifyList(verifyInfo);
    }

    private SecurityKeyApplicationScenario fetchScenarioByScene(BizSceneEnum bizScene) {
        SecurityKeyApplicationScenario scenario = null;
        switch (bizScene) {
            case LOGIN:
            case AUTHORIZE_NEW_DEVICE:
                scenario = SecurityKeyApplicationScenario.login;
                break;
            case API_KEY_MANAGE:
            case API_EDIT_SWITCH:
            case API_WITHDRAW_SWITCH:
            case CRYPTO_WITHDRAW:
                scenario = SecurityKeyApplicationScenario.withdrawAndApi;
                break;
            case MODIFY_PASSWORD:
                scenario = SecurityKeyApplicationScenario.resetPassword;
                break;
        }
        return scenario;
    }

    @SecurityLog(name = "打开出币白名单", operateType = Constant.SECURITY_OPEN_WITHDRAW_WHITE_STATUS,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> openWithdrawWhiteStatus(APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request)
            throws Exception {
        final OpenOrCloseWithdrawWhiteStatusRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        int flag = 0;
        if (BitUtils.isFalse(status, Constant.USER_WITHDRAW_WHITE)) {
            verificationsTwo(requestBody.getUserId(), requestBody.getAuthType(), requestBody.getCode(), true);
            final User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.enable(status, Constant.USER_WITHDRAW_WHITE));
            flag = this.userMapper.updateByEmailSelective(user);
        }
        return APIResponse.getOKJsonResult(flag);
    }

    @SecurityLog(name = "打开出币白名单V2", operateType = Constant.SECURITY_OPEN_WITHDRAW_WHITE_STATUS,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<OpenWithdrawWhiteStatusV2Response> openWithdrawWhiteStatusV2(APIRequest<OpenWithdrawWhiteStatusV2Request> request)throws Exception {
        final OpenWithdrawWhiteStatusV2Request requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        if (BitUtils.isFalse(status, Constant.USER_WITHDRAW_WHITE)) {
            MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                    .userId(requestBody.getUserId())
                    .bizScene(BizSceneEnum.WITHDRAW_WHITE_ENABLE)
                    .emailVerifyCode(requestBody.getEmailVerifyCode())
                    .googleVerifyCode(requestBody.getGoogleVerifyCode())
                    .mobileVerifyCode(requestBody.getMobileVerifyCode())
                    .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                    .build();
            verifyMultiFactors(verify);
            final User user = new User();
            user.setEmail(userIndex.getEmail());
            user.setStatus(BitUtils.enable(status, Constant.USER_WITHDRAW_WHITE));
            this.userMapper.updateByEmailSelective(user);
        }
        return APIResponse.getOKJsonResult();
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<CloseWithdrawWhiteStatusResponse> closeWithdrawWhiteStatus(
            APIRequest<OpenOrCloseWithdrawWhiteStatusRequest> request) throws Exception {
        final OpenOrCloseWithdrawWhiteStatusRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        this.verificationsTwo(requestBody.getUserId(), requestBody.getAuthType(), requestBody.getCode(), true);
        //final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        final User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(user == null) {
        	throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 做市商账号，不允许用户在前台取消白名单状态
        MarketMakerUser marketMakerUser = marketMakerUserMapper.selectByPrimaryKey(user.getUserId());
        if (marketMakerUser != null) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        if (BitUtils.isFalse(user.getStatus(), Constant.USER_WITHDRAW_WHITE)) {
            throw new BusinessException(GeneralCode.USER_WITHDRAW_WHITE_CLOSE);
        }
        // 发送关闭邮件
        String otherCipher = RedisCacheUtils.get(CacheKeys.OTHER_CIPHER, DEFAULT_CIPHER_CODE, true);
        int time = 45;// 有效期45分钟
        String token =
                TokenUtils.createJWT(user.getUserId().toString(), DateUtils.getNewUTCDateAddMinute(time), otherCipher);
        String link = String.format("%suser/enableCloseWhiteStatus.html?id=%s&code=%s",
                WebUtils.getHeader(Constant.BASE_URL), token, StringUtils.getRandomString(4));
        if (StringUtils.isNotBlank(requestBody.getCustomForbiddenLink())) {
            link = UserCommonBusiness.emailLinkGenerator(requestBody.getCustomForbiddenLink(),
                    null,
                    ImmutableMap.of("id", token, "code", StringUtils.getRandomString(4)));
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("link", link);
        data.put("uuid", token);
        String disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_WITHDRAW_WHITE_LIST_CLOSE,
                user, data, "关闭提币白名单：", requestBody.getCustomForbiddenLink());
        return APIResponse.getOKJsonResult(new CloseWithdrawWhiteStatusResponse(token, disableToken));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<CloseWithdrawWhiteStatusV2Response> closeWithdrawWhiteStatusV2(APIRequest<CloseWithdrawWhiteStatusV2Request> request) throws Exception {
        final CloseWithdrawWhiteStatusV2Request requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }

        // 多因子校验
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.WITHDRAW_WHITE_SWITCH)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        final User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if(user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 做市商账号，不允许用户在前台取消白名单状态
        MarketMakerUser marketMakerUser = marketMakerUserMapper.selectByPrimaryKey(user.getUserId());
        if (marketMakerUser != null) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        if (BitUtils.isFalse(user.getStatus(), Constant.USER_WITHDRAW_WHITE)) {
            throw new BusinessException(GeneralCode.USER_WITHDRAW_WHITE_CLOSE);
        }

        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_WITHDRAW_WHITE));
        this.userMapper.updateByEmailSelective(user);
        final UserSecurityLog securityLog = new UserSecurityLog();
        securityLog.setIp(WebUtils.getRequestIp());
        securityLog.setUserId(userIndex.getUserId());
        securityLog.setIpLocation(IP2LocationUtils.getCountryCity(securityLog.getIp()));
        securityLog.setClientType(request.getTerminal().getCode());
        securityLog.setOperateType(Constant.SECURITY_CLOSE_WITHDRAW_WHITE_STATUS);
        securityLog.setOperateTime(DateUtils.getNewUTCDate());
        securityLog.setDescription("关闭出币白名单");
        this.userSecurityLogMapper.insertSelective(securityLog);

        return APIResponse.getOKJsonResult();
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<ConfirmCloseWithdrawWhiteStatusResponse> confirmCloseWithdrawWhiteStatus(
            APIRequest<ConfirmCloseWithdrawWhiteStatusRequest> request) {
        final ConfirmCloseWithdrawWhiteStatusRequest requestBody = request.getBody();
        final String token = requestBody.getToken();
        final Long currentUserId = requestBody.getUserId();
        String otherCipher = RedisCacheUtils.get(CacheKeys.OTHER_CIPHER, DEFAULT_CIPHER_CODE, true);
        Long userId = null;
        try {
            String userIdStr = TokenUtils.parseJWTSubject(token, otherCipher);
            userId = Long.valueOf(userIdStr);
            Boolean userIdvalidateFlag=currentUserId.equals(userId);
            if(!userIdvalidateFlag){
                log.info("confirmCloseWithdrawWhiteStatus error: currentUserId={},userId={},userIdvalidateFlag={}",
                        currentUserId,userId, userIdvalidateFlag );
                throw new BusinessException(GeneralCode.TOKEN_NOT_EXIST);
            }
        } catch (ExpiredJwtException e) {// 过期的验证码
            throw new BusinessException(GeneralCode.TOKEN_EXPIRE);
        } catch (UnsupportedJwtException e) {// 不是一个有效的token
            throw new BusinessException(GeneralCode.TOKEN_NOT_EXIST);
        }
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        if (BitUtils.isFalse(status, Constant.USER_WITHDRAW_WHITE)) {
            throw new BusinessException(GeneralCode.USER_WITHDRAW_WHITE_CLOSE);
        }
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.disable(status, Constant.USER_WITHDRAW_WHITE));
        this.userMapper.updateByEmailSelective(user);
        final UserSecurityLog securityLog = new UserSecurityLog();
        securityLog.setIp(WebUtils.getRequestIp());
        securityLog.setUserId(userIndex.getUserId());
        securityLog.setIpLocation(IP2LocationUtils.getCountryCity(securityLog.getIp()));
        securityLog.setClientType(request.getTerminal().getCode());
        securityLog.setOperateType(Constant.SECURITY_CLOSE_WITHDRAW_WHITE_STATUS);
        securityLog.setOperateTime(DateUtils.getNewUTCDate());
        securityLog.setDescription("关闭出币白名单");
        this.userSecurityLogMapper.insertSelective(securityLog);
        return APIResponse.getOKJsonResult(new ConfirmCloseWithdrawWhiteStatusResponse(userId));
    }

    @Override
    @SecurityLog(name = "设置防钓鱼码", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_PHISHING_CODE,
            userId = "#request.body.userId")
    public APIResponse<Integer> aouAntiPhishingCode(APIRequest<BindPhishingCodeRequest> request) throws Exception {
        final BindPhishingCodeRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        String antiPhishingCode = requestBody.getAntiPhishingCode();// 防钓鱼码
        String code = requestBody.getCode();// 2fa验证码

        if(org.apache.commons.lang3.StringUtils.isBlank(antiPhishingCode) || antiPhishingCode.length() < 4 || antiPhishingCode.length() > 20) {
        	log.warn("antiPhishingCode不合法，值为:{}", antiPhishingCode);
        	throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        if(!Pattern.compile(Regexp.PHISHING_CODE_IGNORE).matcher(antiPhishingCode).find()) {//正则匹配有特殊字符
        	log.warn("antiPhishingCode含特殊字符，值为:{}", antiPhishingCode);
        	throw new BusinessException(GeneralCode.USER_SPECIAL_CHARACTERS_ERROR);
        }


        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        verificationsTwo(userId, requestBody.getAuthType(), code, true);

        // 临时的代码 完全迁移后移除 start
        try {
            Map<String, Object> dataMsg = new HashMap<String, Object>();
            dataMsg.put(UserConst.USER_ID, userId);
            dataMsg.put("antiPhishingCode", antiPhishingCode);
            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ANTI_PHISHING_CODE, dataMsg);
            log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
            this.iMsgNotification.send(msg);
        } catch (Exception e) {
            log.error("add anti_phishing_code to MQ error-->", e);
        }
        // 临时的代码 完全迁移后移除 end

        // 验证通过,修改用户的防钓鱼码
        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setAntiPhishingCode(antiPhishingCode);
        return APIResponse.getOKJsonResult(this.userSecurityMapper.updateByPrimaryKeySelective(user));
    }

    @Override
    @SecurityLog(name = "设置防钓鱼码", operateType = Constant.SECURITY_OPERATE_TYPE_BIND_PHISHING_CODE,
            userId = "#request.body.userId")
    public APIResponse<Integer> aouAntiPhishingCodeV2(APIRequest<BindPhishingCodeV2Request> request) throws Exception {
        final BindPhishingCodeV2Request requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        String antiPhishingCode = requestBody.getAntiPhishingCode();// 防钓鱼码

        if(StringUtils.isBlank(antiPhishingCode) || antiPhishingCode.length() < 4 || antiPhishingCode.length() > 20) {
            log.warn("antiPhishingCode不合法，值为:{}", antiPhishingCode);
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        if(!Pattern.compile(Regexp.PHISHING_CODE_IGNORE).matcher(antiPhishingCode).find()) {//正则匹配有特殊字符
            log.warn("antiPhishingCode含特殊字符，值为:{}", antiPhishingCode);
            throw new BusinessException(GeneralCode.USER_SPECIAL_CHARACTERS_ERROR);
        }

        // 根据userid查询email
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // 验证2fa
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(userId)
                .bizScene(BizSceneEnum.SET_ANTI_PHISHING_CODE)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        // 临时的代码 完全迁移后移除 start
        try {
            Map<String, Object> dataMsg = new HashMap<String, Object>();
            dataMsg.put(UserConst.USER_ID, userId);
            dataMsg.put("antiPhishingCode", antiPhishingCode);
            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ANTI_PHISHING_CODE, dataMsg);
            log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
            this.iMsgNotification.send(msg);
        } catch (Exception e) {
            log.error("add anti_phishing_code to MQ error-->", e);
        }
        // 临时的代码 完全迁移后移除 end

        // 验证通过,修改用户的防钓鱼码
        UserSecurity user = new UserSecurity();
        user.setUserId(userId);
        user.setAntiPhishingCode(antiPhishingCode);
        return APIResponse.getOKJsonResult(this.userSecurityMapper.updateByPrimaryKeySelective(user));
    }

    @Override
    public CheckForbidCodeResponse checkForbidCode(String code) {
        final String userId = RedisCacheUtils.get(code, String.class, CacheKeys.USER_DISABLE_CODE);
        if (StringUtils.isNotBlank(userId)) {
            try {
                final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(Long.parseLong(userId));
                final CheckForbidCodeResponse response = new CheckForbidCodeResponse();
                response.setEmail(userIndex.getEmail());
                response.setUserId(userIndex.getUserId());
                return response;
            } catch (NumberFormatException e) {
                log.error("invalid userId: " + userId, e);
            }
        }
        return null;
    }

    private void cancelOrder(Long userId){
        //查询到这个用户的所有挂单，并且全部撤销
        List<OpenOrderVo> openOrderVoList= streamerOrderApiClient.selectOpenOrderOnlyByUserId(userId);
        if(CollectionUtils.isEmpty(openOrderVoList)){
            return;
        }
        for(OpenOrderVo openOrderVo:openOrderVoList){
            try {
               String symbol= openOrderVo.getSymbol();
               Long orderId=openOrderVo.getOrderId();
                mbxGatewayOrderApiCLient.mDeleteOrder(userId.toString(), Lists.newArrayList(symbol),Lists.newArrayList(orderId.toString()));
            }catch (Exception e){
                String errorMessage=String.format("用户一键禁用 撤销用户订单失败:userId:%s, symbol:%s, orderId:%s",userId,openOrderVo.getSymbol(),openOrderVo.getOrderId());
                log.error(errorMessage, e);
            }
        }
    }

    @Override
    @SecurityLog(name = "锁定用户", operateType = Constant.SECURITY_OPERATE_TYPE_LOCK_USER, userId = "#request.body.userId")
    public APIResponse<?> lockUser(APIRequest<UserLockRequest> request) {
        final UserLockRequest requestBody = request.getBody();
        if (requestBody.getLockEndTime() == null
                || requestBody.getLockEndTime().getTime() < System.currentTimeMillis()) {
            return APIResponse.getErrorJsonResult("锁定结束时间不能小于当前时间");
        }
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        userSecurity.setLockEndTime(requestBody.getLockEndTime());
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);

        User user = userMapper.queryByEmail(userSecurity.getEmail());

        User record = new User();
        record.setEmail(user.getEmail());
        record.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_LOCK));
        this.userMapper.updateByEmail(record);
        addLockUserCache(requestBody.getLockEndTime().getTime(), user.getEmail());
        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public Boolean addLockUserCache(Long lockEndTime, String email) {
        try {
            Boolean addResult = RedisCacheUtils.zadd(CacheKeys.LOCKED_USER_EMAIL_ZSET, email, lockEndTime);
            log.info("cache locked user[{}]", email);
            return addResult;
        } catch (Exception e) {
            log.error("add lock user cache error", e);
        }
        return Boolean.FALSE;
    }

    @Override
    @SecurityLog(name = "设置用户安全等级", operateType = Constant.SECURITY_OPERATE_TYPE_SECURITY_LEVEL_SETTING,
            userId = "#request.body.userId")
    public APIResponse<Integer> setSecurityLevel(APIRequest<SecurityLevelSettingRequest> request) {

        final SecurityLevelSettingRequest requestBody = request.getBody();

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null == userSecurity) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserSecurity newUserSecurity = new UserSecurity();
        newUserSecurity.setUserId(requestBody.getUserId());
        newUserSecurity.setSecurityLevel(requestBody.getLevel());
        return APIResponse.getOKJsonResult(userSecurityMapper.updateByPrimaryKeySelective(newUserSecurity));
    }

    @Override
    public APIResponse<List<String>> batchUpdateSecurityUserLevel(BatchUpdateSecurityLevelRequest request)
            throws Exception {
        List<UserSecurityLevelVo> vos = request.getList();
        List<String> fails = new ArrayList<>(500);
        for (UserSecurityLevelVo vo : vos) {
            try {
                SecurityLevelSettingRequest settingRequest = new SecurityLevelSettingRequest();
                settingRequest.setUserId(Long.parseLong(vo.getUserId()));
                settingRequest.setLevel(Integer.parseInt(vo.getSecurityLevel()));
                APIRequest<SecurityLevelSettingRequest> singleRequest = APIRequest.instance(settingRequest);
                this.setSecurityLevel(singleRequest);
                log.info("用户安全等级[{}->{}]修改成功", vo.getUserId(), vo.getSecurityLevel());
            } catch (BusinessException be) {
                log.info("用户安全等级[{}->{}]修改失败", vo.getUserId(), vo.getSecurityLevel());
                fails.add(vo.getUserId());
            }
        }
        return APIResponse.getOKJsonResult(fails);
    }

    @Override
    @SecurityLog(name = "重置用户二次验证", operateType = Constant.SECURITY_OPERATE_REVIEW_SECURITY_RESET,
            userId = "#request.body.userId")
    public APIResponse<Integer> resetSecurity(APIRequest<ResetSecurityRequest> request) {

        final ResetSecurityRequest requestBody = request.getBody();
        final Long requestUserId = requestBody.getUserId();
        final ResetSecurityRequest.ResetType resetType = requestBody.getResetType();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        switch (resetType) {
            case GOOGLE:
                unbindGoogleVerify(requestUserId,false);
                break;
            case MOBILE:
                unbindMobile(user,false);
                break;
            case ENABLE:
                enableUserAfterReset(user.getUserId(), user);
                break;
            default:
                break;
        }

        return APIResponse.getOKJsonResult(1);
    }

    @Override
    public Integer resetSecurity(User user, ResetSecurityRequest.ResetType resetType, String ip, TerminalEnum terminal) {
        if (user == null || resetType == null || StringUtils.isBlank(ip) || terminal == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = user.getUserId();
        switch (resetType) {
            case GOOGLE:
                log.info("重置GOOGLE安全信息. userId:{}", userId);
                //风控 重置流程不禁止提币
                unbindGoogleVerify(userId,true);
                break;
            case MOBILE:
                log.info("重置MOBILE安全信息. userId:{}", userId);
                //风控
                unbindMobile(user,true);
                break;
            case ENABLE:
                log.info("解禁用户重置安全信息. userId:{}", userId);
                enableUserAfterReset(user.getUserId(), user);
                break;
            default:
                break;
        }
        saveSecurityLog(userId, ip, terminal.getCode(), Constant.SECURITY_OPERATE_REVIEW_SECURITY_RESET, "重置用户二次验证");
        return 1;
    }

    /**
     * 手动登记安全变更日志信息
     * @param userId
     * @param ip
     * @param terminalCode
     * @param operateType
     * @param description
     */
    private void saveSecurityLog(Long userId, String ip, String terminalCode, String operateType, String description) {
        try{
            //登记安全信息变更日志
            log.info("登记安全信息变更日志. userId:{} operateType:{}", userId, operateType);
            final UserSecurityLog securityLog = new UserSecurityLog();
            securityLog.setIp(ip);
            securityLog.setUserId(userId);
            securityLog.setIpLocation(IP2LocationUtils.getCountryCity(ip));
            securityLog.setClientType(terminalCode);
            securityLog.setOperateType(operateType);
            securityLog.setOperateTime(DateUtils.getNewUTCDate());
            securityLog.setDescription(description);
            this.userSecurityLogMapper.insertSelective(securityLog);
        }catch (Exception e) {
            log.error("登记安全变更日志失败: userId:{} operateType:{}", userId, operateType, e);
        }
    }

    public void enableUserAfterReset(Long userId, User user) {
        // 1.启用登录
        Long status = user.getStatus();
        status = BitUtils.disable(status, Constant.USER_DISABLED);
        // 2.启用交易
        status = BitUtils.disable(status, Constant.USER_TRADE);
        User userRecord = new User();
        userRecord.setStatus(status);
        userRecord.setEmail(user.getEmail());
        userMapper.updateByEmail(userRecord);

        // 3.修改用户安全信息
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setLoginFailedNum(0);
//        userSecurityRecord.setWithdrawSecurityStatus(0); // 2020-07-29 重置激活用户不能把提币禁用标志关闭
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);

        // 4.启用交易-调用撮合引擎
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {
            TradingAccountDetails detailsByTradingAccountId = accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
            log.info("oneButtonActivation.getDetailsByTradingAccountId.userId:{},resp:{}",userId,JsonUtils.toJsonHasNullKey(detailsByTradingAccountId));
            if (detailsByTradingAccountId != null && !detailsByTradingAccountId.getCanTrade()){
                accountApi.setTradingAccount(userInfo.getTradingAccount(), true, true, true);
                log.info("enable tradingAccount done, userId:{}, tradingAccountId:{}", userId,
                        userInfo.getTradingAccount());
            }
        }
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "后台一键启用", operateType = Constant.SECURITY_OPERATE_ONE_BUTTON_ACTIVATION,
            userId = "#request.body.userId")
    @Override
    public APIResponse<?> oneButtonActivation(APIRequest<OneButtonActivationRequest> request) {
        final OneButtonActivationRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User record = new User();
        record.setEmail(user.getEmail());// 启用交易启用登录
        record.setStatus(
                BitUtils.disable(BitUtils.disable(user.getStatus(), Constant.USER_TRADE), Constant.USER_DISABLED));
        this.userMapper.updateByEmail(record);
        // 更改错误次数
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setLoginFailedNum(0);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {// 开启用户交易
            TradingAccountDetails detailsByTradingAccountId = accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
            log.info("oneButtonActivation.getDetailsByTradingAccountId.userId:{},resp:{}",requestBody.getUserId(),JsonUtils.toJsonHasNullKey(detailsByTradingAccountId));
            if (detailsByTradingAccountId != null && !detailsByTradingAccountId.getCanTrade()){
                accountApi.setTradingAccount(userInfo.getTradingAccount(), true, true, true);
            }
        } else {
            log.warn("oneButtonActivation TradingAccount = {}",userInfo.getTradingAccount());
        }
        return APIResponse.getOKJsonResult(null);
    }

    /**
     * 服务内一键启用功能
     * @param user
     * @param ip
     * @param terminal
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void oneButtonActivation(User user, String ip, TerminalEnum terminal) {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userId = user.getUserId();
        log.info("启动功能请求: userId:{}", userId);
        final User record = new User();
        // 启用交易启用登录
        log.info("启用交易和启用登录: userId:{}", user.getUserId());
        record.setEmail(user.getEmail());
        record.setStatus(
                BitUtils.disable(BitUtils.disable(user.getStatus(), Constant.USER_TRADE), Constant.USER_DISABLED));
        this.userMapper.updateByEmail(record);
        // 更改错误次数
        log.info("变更错误次数为0，userId:{}", user.getUserId());
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setLoginFailedNum(0);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {
            log.info("开启用户交易, userId:{}", user.getUserId());
            accountApi.setTradingAccount(userInfo.getTradingAccount(), true, true, true);
        } else {
            log.warn("oneButtonActivation TradingAccount is null, userId:{}", user.getUserId());
        }
        saveSecurityLog(userId, ip, terminal.getCode(), Constant.SECURITY_OPERATE_ONE_BUTTON_ACTIVATION, "一键启用");
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "后台一键禁用", operateType = AccountConstants.ONE_BUTTON_DISABLE_BACKEND,
            userId = "#request.body.userId")
    @Override
    public APIResponse<?> oneButtonDisable(APIRequest<OneButtonDisableRequest> request) {
        // 1.更改交易状态为禁用
        // 2.禁用登录
        final OneButtonDisableRequest requestBody = request.getBody();
        //撤单
        cancelOrder(requestBody.getUserId());
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
        record.setStatus(
                BitUtils.enable(BitUtils.enable(user.getStatus(), Constant.USER_TRADE), Constant.USER_DISABLED));
        this.userMapper.updateByEmail(record);
        // 更改错误次数
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setDisableTime(DateUtils.getNewUTCDate());
//        userSecurityRecord.setWithdrawSecurityStatus(1);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {// 禁用交易
            TradingAccountDetails detailsByTradingAccountId = accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
            log.info("getDetailsByTradingAccountId.userId:{},resp:{}",requestBody.getUserId(),JsonUtils.toJsonHasNullKey(detailsByTradingAccountId));
            if (detailsByTradingAccountId != null && detailsByTradingAccountId.getCanTrade()){
                accountApi.setTradingAccount(userInfo.getTradingAccount(), false, true, true);
            }
        } else {
            log.warn("oneButtonDisable TradingAccount is null");
        }
        // remark
        if (null != userInfo && org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getRemark())) {
            userInfo.setRemark(requestBody.getRemark());
            userInfoMapper.updateByPrimaryKeySelective(userInfo);
        }
        return APIResponse.getOKJsonResult(null);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "风控禁用交易及取消订单", operateType = AccountConstants.DISABLE_TRADE_AND_CANCEL_ORDER,
            userId = "#request.body.userId")
    @Override
    public APIResponse<?> disableTradeAndCancleOrder(APIRequest<OneButtonDisableRequest> request) {
        // 1.更改交易状态为禁用
        // 2.禁用登录
        final OneButtonDisableRequest requestBody = request.getBody();
        //撤单
        cancelOrder(requestBody.getUserId());
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
        record.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_TRADE));
        this.userMapper.updateByEmail(record);

        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {// 禁用交易
            TradingAccountDetails detailsByTradingAccountId = accountApi.getDetailsByTradingAccountId(userInfo.getTradingAccount());
            log.info("getDetailsByTradingAccountId.userId:{},resp:{}",requestBody.getUserId(),JsonUtils.toJsonHasNullKey(detailsByTradingAccountId));
            if (detailsByTradingAccountId != null && detailsByTradingAccountId.getCanTrade()){
                accountApi.setTradingAccount(userInfo.getTradingAccount(), false, true, true);
            }
        } else {
            log.warn("oneButtonDisable TradingAccount is null");
        }
        // remark
        if (null != userInfo && org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getRemark())) {
            userInfo.setRemark(requestBody.getRemark());
            userInfoMapper.updateByPrimaryKeySelective(userInfo);
        }
        return APIResponse.getOKJsonResult(null);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void resetActivationUser(ResetActivationUserRequest request) {
        Long userId = request.getUserId();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        enableUserAfterReset(userId, user);
        saveSecurityLog(userId, request.getApplyIp(), request.getTerminalCode(), Constant.SECURITY_OPERATE_REVIEW_SECURITY_RESET, "解禁账户流程激活");
        // 从风控那边解禁用户的API交易
        try {
            UserIdRequestVo requestVo = new UserIdRequestVo();
            requestVo.setUserId(userId);
            APIResponse<Boolean> response = riskSecurityApi.unbanByUserId(APIRequest.instance(requestVo));
            log.info("禁用用户--> 风控解禁用户API信息结果：userId:{} response:{}", userId, JSON.toJSONString(response));
        }catch (Exception e) {
            log.error("解禁用户--> 风控解禁用户API异常失败. userId:{} ", userId, e);
        }
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "禁用登录", operateType = Constant.SECURITY_OPERATE_DISABLE_LOGIN, userId = "#request.body.userId")
    @Override
    public APIResponse<?> disableLogin(APIRequest<DisableLoginRequest> request) {
        // 禁用登录
        final DisableLoginRequest requestBody = request.getBody();
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
        record.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_LOGIN));
        this.userMapper.updateByEmail(record);
        // 更改错误次数
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setDisableTime(DateUtils.getNewUTCDate());
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);

        return APIResponse.getOKJsonResult(null);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "启用登录", operateType = Constant.SECURITY_OPERATE_ACTIVE_LOGIN, userId = "#request.body.userId")
    @Override
    public APIResponse<?> activeLogin(APIRequest<ActiveLoginRequest> request) {
        final ActiveLoginRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User record = new User();
        record.setEmail(user.getEmail());//启用登录
        record.setStatus(BitUtils.disable(user.getStatus(),Constant.USER_LOGIN));
        this.userMapper.updateByEmail(record);
        // 更改错误次数
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setLoginFailedNum(0);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);

        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public APIResponse<String> selectAntiPhishingCode(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        return APIResponse.getOKJsonResult(userSecurityMapper.selectAntiPhishingCode(requestBody.getUserId()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public APIResponse<UserSecurityListResponse> selectUserSecurityList(APIRequest<GetUserListRequest> request) {
        GetUserListRequest requestBody = request.getBody();
        List<UserSecurity> modelList = userSecurityMapper.selectUserSecurityList(requestBody.getUserIds());
        List<UserMobileVo> result = new ArrayList<>();
        if (modelList != null && modelList.size() > 0) {
            // 从缓存中获取countryMap
            Map<String, String> countryMap = RedisCacheUtils.get(CACHE_COUNTRY_MAP, Map.class);
            if (countryMap == null || countryMap.size() <= 0) {
                Map<String, String> map = Maps.newHashMap();

                List<Country> countryList = countryMapper.selectCountryList();// 从数据库查
                // 设置到缓存中并设置三天失效
                countryList.stream().forEach(cl -> {
                    String value = "";
                    if (!StringUtils.isEmpty(cl.getMobileCode()) && !"+".equals(cl.getMobileCode().substring(0, 1))) {
                        value = new StringBuilder().append("+").append(cl.getMobileCode()).toString();
                    } else {
                        value = cl.getMobileCode();
                    }

                    map.put(cl.getCode().toLowerCase(), value);
                });

                RedisCacheUtils.set(CACHE_COUNTRY_MAP, map, Constant.DAY * 3);

                countryMap = map;
            }

            for (int i = 0; i < modelList.size(); i++) {
                UserSecurity model = modelList.get(i);
                UserMobileVo vo = new UserMobileVo();
                vo.setUserId(model.getUserId());
                vo.setEmail(model.getEmail());
                vo.setMobile(model.getMobile());
                String code = model.getMobileCode();
                if (!StringUtils.isEmpty(code)) {
                    vo.setMobileCode(code);
                    // 根据code去缓存中查mobileNum
                    if (countryMap != null) {
                        vo.setMobileNum(countryMap.get(code.toLowerCase()));
                    }
                }
                result.add(vo);
            }
        }

        UserSecurityListResponse response = new UserSecurityListResponse();
        response.setResult(result);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Integer> updateStatusByUserId(APIRequest<SecurityStatusRequest> request) {
        SecurityStatusRequest requestBody = request.getBody();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, requestBody.getUserId());
        paramMap.put("withdrawSecurityStatus", requestBody.getWithdrawSecurityStatus());
        paramMap.put("withdrawSecurityAutoStatus", requestBody.getWithdrawSecurityAutoStatus());
        Integer count = userSecurityMapper.updateStatusByUserId(paramMap);

        String isCancelPwdEmail = sysConfigVarCacheService.getValue("withdraw_manage_pwd_email");
        if(StringUtils.isBlank(isCancelPwdEmail) || StringUtils.equalsIgnoreCase(isCancelPwdEmail, "ON")){
            final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            RedisCacheUtils.del(String.format(UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()));
            log.info("updateStatusByUserId, userId:{} pwd withdraw recover", requestBody.getUserId());
            RedisCacheUtils.del(String.format(UserBusiness.UPDATE_EMAIL_TIME_PREFIX, requestBody.getUserId()));
            log.info("updateStatusByUserId, userId:{} email withdraw recover", requestBody.getUserId());
        }

        stopWatch.stop();
        log.info("updateStatusByUserId end, userId:{}, elapsedTime: {} seconds", requestBody.getUserId(),
                stopWatch.getTotalTimeSeconds());
        return APIResponse.getOKJsonResult(count);
    }


    @Override
    public APIResponse<Integer> updateWithdrawStatusByUserId(APIRequest<UpdateWithdrawStatusRequest> request) {
        log.info("UserSecurityBusiness.updateWithdrawStatusByUserId.request:{}",JsonUtils.toJsonHasNullKey(request));
        UpdateWithdrawStatusRequest requestBody = request.getBody();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(UserConst.USER_ID, requestBody.getUserId());
        paramMap.put("withdrawSecurityStatus", requestBody.getWithdrawSecurityStatus());
        paramMap.put("withdrawSecurityAutoStatus", requestBody.getWithdrawSecurityAutoStatus());
        Integer count = userSecurityMapper.updateStatusByUserId(paramMap);

        String isCancelPwdEmail = sysConfigVarCacheService.getValue("withdraw_manage_pwd_email");
        if(StringUtils.isBlank(isCancelPwdEmail) || StringUtils.equalsIgnoreCase(isCancelPwdEmail, "ON")){
            final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            RedisCacheUtils.del(String.format(UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()));
            log.info("updateStatusByUserId, userId:{} pwd withdraw recover", requestBody.getUserId());
            RedisCacheUtils.del(String.format(UserBusiness.UPDATE_EMAIL_TIME_PREFIX, requestBody.getUserId()));
            log.info("updateStatusByUserId, userId:{} email withdraw recover", requestBody.getUserId());
        }
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userInfo == null){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        stopWatch.stop();

        log.info("updateStatusByUserId end, userId:{}, elapsedTime: {} seconds", requestBody.getUserId(),
                stopWatch.getTotalTimeSeconds());
        try {
            String operate = "";
            if(requestBody.getWithdrawSecurityAutoStatus() != null && requestBody.getWithdrawSecurityAutoStatus() == 1){
                operate = "风控禁用";
            }else if(requestBody.getWithdrawSecurityAutoStatus() != null && requestBody.getWithdrawSecurityAutoStatus() == 0){
                operate = "风控提币状态位启用";
            }
            if (requestBody.getWithdrawSecurityStatus() != null && requestBody.getWithdrawSecurityStatus() == 1){
                operate += "手动禁用";
            }else if (requestBody.getWithdrawSecurityStatus() != null && requestBody.getWithdrawSecurityStatus() == 0){
                operate += "手动提币状态位启动";
            }
            String sendTgMessage = getSendTgMessage(requestBody.getUserId(), userInfo.getTradeLevel(), requestBody.getReason(), operate);
            final UserSecurityLog log = new UserSecurityLog();
            log.setUserId(requestBody.getUserId());
            log.setIp(WebUtils.getRequestIp());
            TerminalEnum terminal = WebUtils.getTerminal();
            log.setClientType(terminal == null?"other":terminal.getCode());
            log.setIpLocation(IP2LocationUtils.getCountryCity(WebUtils.getRequestIp()));
            log.setOperateType(AccountConstants.UPDATE_WITHDRAWSTATUS_NEW);
            log.setDescription(requestBody.getChannel().getDesc()+"_"+requestBody.getReason()+"_"+sendTgMessage);
            log.setOperateTime(DateUtils.getNewUTCDate());
            this.userSecurityLogMapper.insertSelective(log);
            if (isSendupdateWithdrawStatusChatId){
                if (userInfo.getTradeLevel() != null && userInfo.getTradeLevel() >= 4){
                    if ((requestBody.getWithdrawSecurityAutoStatus() != null && requestBody.getWithdrawSecurityAutoStatus() == 1)
                            || requestBody.getWithdrawSecurityStatus() != null && requestBody.getWithdrawSecurityStatus() == 1){
                        telegramClient.sendTg(updateWithdrawStatusChatId, sendTgMessage,updateWithdrawStatusRoomId);
                    }
                }
            }
        }catch (Exception e){
            log.warn("save userSecurityLog error",e);
        }
        return APIResponse.getOKJsonResult(count);
    }

    private String getSendTgMessage(Long userId,Integer tradeLevel,String reason,String operate){
        return String.format(UPDATE_WITHDRAW_STATUS_SEND_TG_TEMPLATE, withdrawStatusUpdteSite==1?"主站":"美国站",
                userId,tradeLevel,
                DateUtils.formatterUTC(new Date(),DateUtils.DETAILED_NUMBER_PATTERN),
                reason,operate);
    }

    private String getSiteName(){
        if ( withdrawStatusUpdteSite==1){
            return "主战";
        }else if(withdrawStatusUpdteSite==2){
            return "美国站";
        }
        return null;
    }

    @Override
    public String generateDisableCode(Long userId) {
        return userCommonBusiness.generateAndSetDisableCode(userId);
    }

    /**
     *
     * @param email
     * @param salt
     * @param encodedPassword
     * @param riskEngineResult 是否可以提币，true可以，false禁止
     * @return
     */
    @Override
    public int updateUserPassword(final String email, final String salt, final String encodedPassword, boolean riskEngineResult, String redisKey) {
        User userRecord = new User();
        userRecord.setSalt(salt);
        userRecord.setPassword(encodedPassword);
        userRecord.setEmail(email);
        int result = userMapper.updateByEmailAndClearSafePassword(userRecord);
        log.info("updateUserPassword result:{}", result);

        if (result == 1 && !riskEngineResult) {
            long updateTime = System.currentTimeMillis();
            RedisCacheUtils.set(String.format(redisKey, email), updateTime, ONE_DAY);
            log.info("updateUserPassword updateTime:{}, will be expired in:{}seconds", updateTime, ONE_DAY);
        }
        return result;
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int updateUserPasswordAndSafePwd(String email, String salt, String encodedPassword, boolean riskEngineResult, String rediskey,String safePassword)throws Exception {
        User userRecord = new User();
        userRecord.setSalt(salt);
        userRecord.setPassword(encodedPassword);
        userRecord.setEmail(email);
        //有传safePassword才能存
        if(org.apache.commons.lang3.StringUtils.isNotBlank(safePassword)){
            userRecord.setSafePassword(CryptoAlgoUtils.validateAndHash512(safePassword,salt));
        }
        int result = userMapper.updateByEmail(userRecord);
        log.info("updateUserPasswordAndSafePwd result:{}", result);

        if (result == 1 && !riskEngineResult) {
            long updateTime = System.currentTimeMillis();
            RedisCacheUtils.set(String.format(rediskey, email), updateTime, ONE_DAY);
            log.info("updateUserPasswordAndSafePwd updateTime:{}, will be expired in:{}seconds", updateTime, ONE_DAY);
        }

        return result;
    }

    @Override
    public WithdrawTimeForTradeResponse getWithdrawTimeForTrade(Long userId) {
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long updatePasswordTime = RedisCacheUtils.get(String.format(UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()), Long.class);
        Long forgetAndUpdatePasswordTime = RedisCacheUtils.get(String.format(forgetPasswordSwitch?FORGET_PSW_TIME_PREFIX:UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()), Long.class);

        Long updateEmailTime = RedisCacheUtils.get(String.format(UserBusiness.UPDATE_EMAIL_TIME_PREFIX, userId), Long.class);

        log.info("getWithdrawTimeForTrade userId:{}", userId);

        WithdrawTimeForTradeResponse withdrawTimeForTradeResponse = new WithdrawTimeForTradeResponse();
        //密码修改提现限制24小时
        if (updatePasswordTime != null){
            withdrawTimeForTradeResponse.setWithdrawRestrictedPassword(updatePasswordTime);
        }
        //忘记密码/未登录
        if (forgetAndUpdatePasswordTime != null){
            withdrawTimeForTradeResponse.setWithdrawForgetPassword(forgetAndUpdatePasswordTime);
        }
        //email修改提现限制5天
        if (updateEmailTime != null){
            withdrawTimeForTradeResponse.setWithdrawRestrictedEmail(updateEmailTime);
        }
        //unbind解绑禁止提现时间
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if(userSecurity != null && userSecurity.getUnbindTime() != null){
            withdrawTimeForTradeResponse.setWithdrawRestrictedUnbind2fa(userSecurity.getUnbindTime().getTime());
        }
        //lastLoginTime
        UserSecurityLog lastLoginLogByUserId = userSecurityLogMapper.getLastLoginLogByUserId(userId);
        if (lastLoginLogByUserId != null && lastLoginLogByUserId.getOperateTime() != null){
            withdrawTimeForTradeResponse.setWithdrawLoginDelay(lastLoginLogByUserId.getOperateTime().getTime());
        }
        //DeregisterYubikeyTime
        if (userSecurity != null && userSecurity.getDeregisterYubikeyTime() != null){
            withdrawTimeForTradeResponse.setWithdrawUnbindYoubikey(userSecurity.getDeregisterYubikeyTime().getTime());
        }
        //getWITHDRAW_UNBIND_YUBIKEY
        OldWithdrawDailyLimitModify withdrawDailyLimitModify = oldWithdrawDailyLimitModifyMapper.selectByPrimaryKey(String.valueOf(userId));
        if (withdrawDailyLimitModify != null && withdrawDailyLimitModify.getForbidRestoreTime() != null) {
            withdrawTimeForTradeResponse.setWithdrawAccountRestrictedReset2fa(DateUtils.addHours(withdrawDailyLimitModify.getForbidRestoreTime(),-48).getTime());
        }
        log.info("getWithdrawTimeForTrade userId:{}, res:{}", userId, JsonUtils.toJsonHasNullKey(withdrawTimeForTradeResponse));
        return withdrawTimeForTradeResponse;
    }

    @Override
    public EnableFastWithdrawSwitchResponse enableFastWithdrawSwitch(EnableFastWithdrawSwitchRequest request) throws Exception {
        User user = userCommonBusiness.checkAndGetUserById(request.getUserId());
        //当前账号没有激活
        if (!BitUtils.isEnable(user.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        EnableFastWithdrawSwitchResponse resp = new EnableFastWithdrawSwitchResponse();
        //1 验证是否过kyc
        if (fastwithdrawNeedKycSwitch.booleanValue()) {
            UserKycApprove userKyc = userKycApproveMapper.selectByPrimaryKey(user.getUserId());
            if (null == userKyc) {
                resp.setNeedKyc(true);
                return resp;
            }
        }
        //2 验证2fa
        if (fastwithdrawNeed2FaSwitch) {
            //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
            Boolean isPass2FA = BitUtils.isEnable(user.getStatus(), Constant.USER_MOBILE) || BitUtils.isEnable(user.getStatus(), Constant.USER_GOOGLE);
            if (!isPass2FA) {
                throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
            }
            if (null == request.getAuthType() || null == request.getCode()) {
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            verificationsTwo(request.getUserId(), request.getAuthType(), request.getCode(), true);
        }
        //3 修改开关
        final User updateUser = new User();
        updateUser.setEmail(user.getEmail());
        updateUser.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_FAST_WITHDRAW_ENABLED));
        this.userMapper.updateUserStatusByEmail(updateUser);
        return resp;
    }

    @Override
    public DisableFastWithdrawSwitchResponse disableFastWithdrawSwitch(DisableFastWithdrawSwitchRequest request) throws Exception {
        User user=userCommonBusiness.checkAndGetUserById(request.getUserId());
        //1 修改开关
        final User updateUser = new User();
        updateUser.setEmail(user.getEmail());
        updateUser.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_FAST_WITHDRAW_ENABLED));
        this.userMapper.updateUserStatusByEmail(updateUser);
        DisableFastWithdrawSwitchResponse resp=new DisableFastWithdrawSwitchResponse();
        return resp;
    }

    @Override
    public GetUserEmailAndMobileByUserIdResponse getUserEmailAndMobileByUserId(GetUserEmailAndMobileByUserIdRequest request) throws Exception {
        User user=userCommonBusiness.checkAndGetUserById(request.getUserId());
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        GetUserEmailAndMobileByUserIdResponse resp=new GetUserEmailAndMobileByUserIdResponse();
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if(!userStatusEx.getIsUserNotBindEmail().booleanValue()){
            resp.setEmail(userSecurity.getEmail());
        }

        if(userStatusEx.getIsUserMobile().booleanValue()){
            resp.setMobileCode(userSecurity.getMobileCode());
            resp.setMobile(userSecurity.getMobile());
            if (StringUtils.isNotBlank(userSecurity.getMobileCode())) {
                Country country = this.iCountry.getCountryByCode(userSecurity.getMobileCode());
                if (country != null) {
                    resp.setCountryCode(country.getMobileCode());
                }
            }
        }
        return resp;
    }

    @Override
    public GetUserIdByEmailOrMobileResponse getUserIdByMobileOrEmail(GetUserIdByEmailOrMobileRequest request) throws Exception {
        String email=request.getEmail();
        String mobile=request.getMobile();
        String mobileCode=request.getMobileCode();

        if(StringUtils.isAnyBlank(mobile,mobileCode) && StringUtils.isBlank(email)){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId=null;
        if(StringUtils.isNotBlank(email)){
            User user = this.userMapper.queryByEmail(email);
            if (user == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userId = user.getUserId();
        } else {
            Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(mobileCode);
            if (null == country) {
                log.warn("register,mobileCode invalid");
                throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
            }
            mobileCode=country.getCode();
            final UserMobileIndex userMobileIndex = this.userMobileIndexMapper.selectByPrimaryKey(mobile, mobileCode);
            if (null == userMobileIndex) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            userId = userMobileIndex.getUserId();
        }

        GetUserIdByEmailOrMobileResponse resp=new GetUserIdByEmailOrMobileResponse();
        resp.setUserId(userId);
        return resp;
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public AccountForgotPasswordPreCheckResponse forgotPasswordPreCheck(APIRequest<AccountForgotPasswordPreCheckRequest> apiRequest) throws Exception {
        AccountForgotPasswordPreCheckRequest request=apiRequest.getBody();

        //参数合法性校验
        if(org.apache.commons.lang3.StringUtils.isBlank(request.getEmail())&& org.apache.commons.lang3.StringUtils.isAnyBlank(request.getMobile(),request.getMobileCode())){
            log.warn("forgotPasswordSendEmail中的email为null");
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        // 因为忘记密码的请求用户没有登录态的，所以要通过账号来反推userid
        GetUserIdByEmailOrMobileRequest getUserIdByEmailOrMobileRequest=new GetUserIdByEmailOrMobileRequest();
        getUserIdByEmailOrMobileRequest.setEmail(request.getEmail());
        getUserIdByEmailOrMobileRequest.setMobile(request.getMobile());
        getUserIdByEmailOrMobileRequest.setMobileCode(request.getMobileCode());
        GetUserIdByEmailOrMobileResponse getUserIdByEmailOrMobileResponse= getUserIdByMobileOrEmail(getUserIdByEmailOrMobileRequest);
        if (getUserIdByEmailOrMobileResponse == null|| null==getUserIdByEmailOrMobileResponse.getUserId()) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }


        Long userId=getUserIdByEmailOrMobileResponse.getUserId();

        // 新版2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(userId)
                .bizScene(BizSceneEnum.FORGET_PASSWORD)
                .emailVerifyCode(request.getEmailVerifyCode())
                .googleVerifyCode(request.getGoogleVerifyCode())
                .mobileVerifyCode(request.getMobileVerifyCode())
                .yubikeyVerifyCode(request.getYubikeyVerifyCode())
                .build();
        verifyMultiFactors(verify);

        // 主业务逻辑，修改密码通知风控
        final User user = userCommonBusiness.checkAndGetUserById(userId);

        String token = RedisCacheUtils.get(userId.toString(), String.class, CacheKeys.RESET_PASSWORD_EMAIL);
        if (StringUtils.isBlank(token)) {
            token = TokenUtils.emailRedisToken();
        } else {
            if (TokenUtils.isEmailVerifyCodeExpire(token, UserCommonBusiness.EMAIL_GAP_TIME)) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND,
                        new Object[] {UserCommonBusiness.EMAIL_GAP_TIME});
            } else if (!TokenUtils.isEmailVerifyCodeExpire(token, UserCommonBusiness.EXPIRED_TIME)) {// 30分钟过去换一个
                token = TokenUtils.emailRedisToken();
            }
        }
        // 通过notification发送通知
        securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.FORGET_PWD, apiRequest.getLanguage());
        RedisCacheUtils.set(userId.toString(), token, UserCommonBusiness.EXPIRED_TIME * 60,
                CacheKeys.RESET_PASSWORD_EMAIL);
        log.info("forgotPasswordSendEmail 处理完成！");
        AccountForgotPasswordPreCheckResponse resp=new AccountForgotPasswordPreCheckResponse();
        resp.setToken(token);
        return resp;
    }

    @Override
    public AccountResetPasswordResponseV2 resetPasswordV2(AccountResetPasswordRequestV2 request) throws Exception {
        //参数合法性校验
        if(org.apache.commons.lang3.StringUtils.isBlank(request.getEmail()) &&  org.apache.commons.lang3.StringUtils.isAnyBlank(request.getMobile(),request.getMobileCode())){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 因为忘记密码的请求用户没有登录态的，所以要通过账号来反推userid
        GetUserIdByEmailOrMobileRequest getUserIdByEmailOrMobileRequest=new GetUserIdByEmailOrMobileRequest();
        getUserIdByEmailOrMobileRequest.setEmail(request.getEmail());
        getUserIdByEmailOrMobileRequest.setMobile(request.getMobile());
        getUserIdByEmailOrMobileRequest.setMobileCode(request.getMobileCode());
        GetUserIdByEmailOrMobileResponse getUserIdByEmailOrMobileResponse= getUserIdByMobileOrEmail(getUserIdByEmailOrMobileRequest);
        if (getUserIdByEmailOrMobileResponse == null|| null==getUserIdByEmailOrMobileResponse.getUserId()) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Long userId=getUserIdByEmailOrMobileResponse.getUserId();

        String token = RedisCacheUtils.get(userId.toString(), String.class, CacheKeys.RESET_PASSWORD_EMAIL);
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(GeneralCode.TOKEN_EXPIRE);
        }
        if (!StringUtils.equals(token, request.getToken())) {
            throw new BusinessException(GeneralCode.TOKEN_NOT_EXIST);
        }


        // 主业务逻辑，修改密码通知风控
        final User user = userCommonBusiness.checkAndGetUserById(userId);
        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.FORGET_PWD, user.getUserId(), request.getDeviceInfo());


        // 重置密码
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_CIPHER_CODE, true);
        user.setSalt(StringUtils.uuid());
        user.setPassword(PasswordUtils.encode(request.getPassword(), user.getSalt(), cipherCode));
        updateUserPasswordAndSafePwd(user.getEmail(), user.getSalt(), user.getPassword(), riskEngineResult,forgetPasswordSwitch?UserSecurityBusiness.FORGET_PSW_TIME_PREFIX:UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX,request.getSafePassword());

        // 重置密码错误次数
        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLockEndTime(DateUtils.getNewUTCDateAddHour(-2));
        this.userSecurityMapper.updateByPrimaryKey(userSecurity);
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_RESET_PASSWORD_USABLE:Constant.NODE_TYPE_RESET_PASSWORD, user, null,
                "重置密码异常：", request.getCustomForbiddenLink());

        // 使EmailVerifyCode失效
        RedisCacheUtils.del(user.getUserId().toString(), CacheKeys.RESET_PASSWORD_EMAIL);
        AccountResetPasswordResponseV2 resp=new AccountResetPasswordResponseV2();
        resp.setUserId(user.getUserId());
        try {
            userAsyncTask.selectSendRiskMessage(RiskTaskAspect.RISK_EXCHANGE, RiskTaskAspect.RISK_ROUNTING_KEY,user.getUserId(),user.getEmail(),JsonUtils.toJsonNotNullKey(request.getDeviceInfo()),true,WebUtils.getRequestIp(),false);
        }catch (Exception e){
            log.warn("send message to risk error",e);
        }
        try{
            unlockUserDirectly(user);
        }catch (Exception e){
            log.warn("unlockUserDirectly error",e);
        }
        return resp;
    }

    /**
     * 强制解锁账号
     *
     * @param user
     * @return
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean unlockUserDirectly(User user) {
        UserSecurity temp = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        // 可以解锁
        if (null!=userStatusEx.getIsUserLock() && userStatusEx.getIsUserLock().booleanValue()) {
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setUserId(user.getUserId());
            userSecurity.setLoginFailedNum(0);
            userSecurity.setFundPasswordFailedNum(0);
            userSecurity.setUpdateTime(DateUtils.getNewDate());
            this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
            User record = new User();
            record.setEmail(user.getEmail());
            record.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_LOCK));
            this.userMapper.updateByEmail(record);
            log.info("unlockUserDirectly success:userId={}",user.getUserId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public EnableFundPasswordResponse enableFundPassword(EnableFundPasswordRequest request) throws Exception {
        // 功能开放校验
        checkFundPasswordFunction(request.getUserId());

        log.info("enableFundPassword :userId={}",request.getUserId());
        User user = userCommonBusiness.checkAndGetUserById(request.getUserId());
        //当前账号没有激活
        if (!BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        // 账户锁定返回异常
        if (BitUtils.isEnable(user.getStatus(), Constant.USER_LOCK)) {
            throw new BusinessException(GeneralCode.USER_LOCK, null, new Object[] {USER_LOCK_TIME});
        }
        //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
        Boolean isPass2FA = BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_MOBILE) || BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_GOOGLE);
        if (!isPass2FA) {
            throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
        }
        if (null == request.getAuthType() || null == request.getCode()) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        verificationsTwo(request.getUserId(), request.getAuthType(), request.getCode(), true);


        //1 修改开关
        User updateUser = new User();
        updateUser.setEmail(user.getEmail());
        updateUser.setStatus(BitUtils.enable(user.getStatus(), AccountCommonConstant.USER_FUND_PASSWORD));
        this.userMapper.updateUserStatusByEmail(updateUser);

        //判断是否需要输入密码
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(request.getUserId());
        if (Objects.isNull(userSecurity)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(userSecurity.getFundPassword())){
            return new EnableFundPasswordResponse();
        }

        if (org.apache.commons.lang3.StringUtils.isAnyBlank(request.getPassword(),request.getConfirmPassword())) {
            // 密码不能为空
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        if (!StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
            // 密码错误
            throw new BusinessException(GeneralCode.USER_PWD_ERROR);
        }
        //密码正则校验
        UserSecurity record = new UserSecurity();
        record.setUserId(request.getUserId());
        record.setFundPassword(EncryptUtil.hashPassword(request.getPassword(),user.getSalt()));
        this.userSecurityMapper.updateByPrimaryKeySelective(record);
        return new EnableFundPasswordResponse();
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public DisableFundPasswordResponse disableFundPassword(DisableFundPasswordRequest request) throws Exception {
        // 功能开放校验
        checkFundPasswordFunction(request.getUserId());

        log.info("disableFundPassword :userId={}",request.getUserId());
        User user = userCommonBusiness.checkAndGetUserById(request.getUserId());
        //当前账号没有激活
        if (!BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        // 账户锁定返回异常
        if (BitUtils.isEnable(user.getStatus(), Constant.USER_LOCK)) {
            throw new BusinessException(GeneralCode.USER_LOCK, null, new Object[] {USER_LOCK_TIME});
        }
        //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
        Boolean isPass2FA = BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_MOBILE) || BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_GOOGLE);
        if (!isPass2FA) {
            throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
        }
        if (null == request.getAuthType() || null == request.getCode()) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        verificationsTwo(request.getUserId(), request.getAuthType(), request.getCode(), true);
        //1 修改开关
        User updateUser = new User();
        updateUser.setEmail(user.getEmail());
        updateUser.setStatus(BitUtils.disable(user.getStatus(), AccountCommonConstant.USER_FUND_PASSWORD));
        this.userMapper.updateUserStatusByEmail(updateUser);
        return new DisableFundPasswordResponse();
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ResetFundPasswordResponse resetFundPassword(ResetFundPasswordRequest request) throws Exception {
        // 功能开放校验
        checkFundPasswordFunction(request.getUserId());

        log.info("resetFundPassword :userId={}",request.getUserId());
        User user = userCommonBusiness.checkAndGetUserById(request.getUserId());
        //当前账号没有激活
        if (!BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        // 账户锁定返回异常
        if (BitUtils.isEnable(user.getStatus(), Constant.USER_LOCK)) {
            throw new BusinessException(GeneralCode.USER_LOCK, null, new Object[] {USER_LOCK_TIME});
        }
        //当前账号是否绑定手机验证或者google验证,只要有一个验证过我们就算通过
        Boolean isPass2FA = BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_MOBILE) || BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_GOOGLE);
        if (!isPass2FA) {
            throw new BusinessException(GeneralCode.USER_GOOGLE_NOT_BIND);
        }
        if (null == request.getAuthType() || null == request.getCode()) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        verificationsTwo(request.getUserId(), request.getAuthType(), request.getCode(), true);

        //判断是否需要输入密码
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(request.getUserId());
        if (Objects.isNull(userSecurity)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (!StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
            // 密码错误
            throw new BusinessException(GeneralCode.USER_PWD_ERROR);
        }
        //密码正则校验
        UserSecurity record = new UserSecurity();
        record.setUserId(request.getUserId());
        record.setFundPassword(EncryptUtil.hashPassword(request.getPassword(),user.getSalt()));
        record.setFundPasswordFailedNum(0);// 密码重置失败次数归零
        this.userSecurityMapper.updateByPrimaryKeySelective(record);
        return new ResetFundPasswordResponse();
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public VerifyFundPasswordResponse verifyFundPassword(VerifyFundPasswordRequest request) throws Exception {
        // 功能开放校验
        checkFundPasswordFunction(request.getUserId());

        log.info("verifyFundPassword :userId={}",request.getUserId());
        User user = userCommonBusiness.checkAndGetUserById(request.getUserId());
        //当前账号没有激活
        if (!BitUtils.isEnable(user.getStatus(), AccountCommonConstant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_NOT_ACTIVE);
        }
        // 账户锁定返回异常
        if (BitUtils.isEnable(user.getStatus(), Constant.USER_LOCK)) {
            throw new BusinessException(GeneralCode.USER_LOCK, null, new Object[] {USER_LOCK_TIME});
        }
        //判断是否需要输入密码
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(request.getUserId());
        if (Objects.isNull(userSecurity)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        VerifyFundPasswordResponse response=new VerifyFundPasswordResponse();
        response.setAvailableRetryTimes(fundPasswordMaxRetryTimes);
        if (!StringUtils.equals(EncryptUtil.hashPassword(request.getPassword(), user.getSalt()), userSecurity.getFundPassword())) {
            try {
                if (userSecurity.getFundPasswordFailedTime() != null
                        && DateUtils.add(userSecurity.getFundPasswordFailedTime(), Calendar.HOUR_OF_DAY, USER_LOCK_TIME)
                        .getTime() < DateUtils.getNewUTCTimeMillis()) {
                    userSecurity.setFundPasswordFailedNum(0);
                }
                UserSecurity updateUserSecurity = new UserSecurity();
                updateUserSecurity.setUserId(user.getUserId());
                updateUserSecurity.setFundPasswordFailedTime(DateUtils.getNewUTCDate());
                updateUserSecurity.setUpdateTime(DateUtils.getNewDate());
                int fundPasswordFailedNum = userSecurity.getFundPasswordFailedNum() == null ? 1 : userSecurity.getFundPasswordFailedNum() + 1;
                updateUserSecurity.setFundPasswordFailedNum(fundPasswordFailedNum);
                if (fundPasswordFailedNum >= fundPasswordMaxRetryTimes) {// 锁定账号
                    Date lockEndTime = DateUtils.getNewDateAddHour(USER_LOCK_TIME);
                    updateUserSecurity.setLockEndTime(lockEndTime);// 锁定2小时
                    // Add to cache, used by unlock user job(com.binance.account.job.UnlockUserJobHandler)
                    addLockUserCache(lockEndTime.getTime(), user.getEmail());
                    User record = new User();
                    record.setEmail(user.getEmail());
                    record.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_LOCK));
                    this.userMapper.updateByEmail(record);
                    // 锁定状态回写到pnk
                    Map<String, Object> dataMsg = new HashMap<>();
                    dataMsg.put(UserConst.USER_ID, user.getUserId());
                    dataMsg.put("status", "lock");
                    MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_LOCK, dataMsg);
                    log.info("iMsgNotification account_lock:{}",
                            LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                    this.iMsgNotification.send(msg);
                }
                this.userSecurityMapper.updateByPrimaryKeySelective(updateUserSecurity);
                Integer availableRetryTimes= fundPasswordMaxRetryTimes - fundPasswordFailedNum;
                response.setAvailableRetryTimes(availableRetryTimes);
                return response;
            } catch (Exception e) {
                log.error(String.format("fundpassWordError failed, userId:%s, exception:", user.getUserId()), e);
            }
        }

        // 验证通过，之前的失败次数清零
        if (userSecurity.getFundPasswordFailedNum() != null && userSecurity.getFundPasswordFailedNum() > 0) {
            UserSecurity modifySecurity = new UserSecurity();
            modifySecurity.setUserId(user.getUserId());
            modifySecurity.setFundPasswordFailedNum(0);
            modifySecurity.setUpdateTime(DateUtils.getNewDate());
            this.userSecurityMapper.updateByPrimaryKeySelective(modifySecurity);
        }

        response.setVerifyResult(true);
        return response;
    }

    @Override
    public Long getPasswordUpdateTime(Long userId) {
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long updateTime = RedisCacheUtils.get(String.format(UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()), Long.class);
        log.info("getPasswordUpdateTime userId:{}, updateTime:{}", userId, updateTime);
        return updateTime;
    }

    @Override
    public AccountUpdateTimeForTrade getAccountUpdateTimeForTrade(Long userId) {
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long updatePasswordTime = RedisCacheUtils.get(String.format(UPDATE_PSW_TIME_PREFIX, userIndex.getEmail()), Long.class);
        Long updateEmailTime = RedisCacheUtils.get(String.format(UserBusiness.UPDATE_EMAIL_TIME_PREFIX, userId), Long.class);
        log.info("getAccountUpdateTime userId:{}, updatePasswordTime:{},updateEmailTime:{}", userId, updatePasswordTime,updateEmailTime);
        AccountUpdateTimeForTrade accountUpdateTimeForTrade = new AccountUpdateTimeForTrade();
        accountUpdateTimeForTrade.setUpdateEmailTime(updateEmailTime);
        accountUpdateTimeForTrade.setUpdatePasswordTime(updatePasswordTime);
        return accountUpdateTimeForTrade;
    }

    @Override
    public GoogleAuthKeyResp generateAuthKeyAndQrCode(UserIdRequest requestBody) {

        final Long userId = requestBody.getUserId();

        User user = userCommonBusiness.checkAndGetUserById(userId);

        // 用户已经绑定不可继续生成
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_GOOGLE)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (null != userSecurity && StringUtils.isBlank(userSecurity.getAuthKey())) {

            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            final GoogleAuthenticatorKey key = gAuth.createCredentials();
            UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
            String qrCode =
                    String.format(QR_CODE, accountGauthIssuer, user.getEmail(), key.getKey(), accountGauthIssuer);
            if(userStatusEx.getIsUserNotBindEmail().booleanValue()){
                qrCode = String.format(QR_CODE, accountGauthIssuer, userSecurity.getMobile(), key.getKey(), accountGauthIssuer);
            }

            // 确保绑定时使用的key，是当前用户自己生成的key
            RedisCacheUtils.set(String.format(GAUTH_KEY_GENERATOR_KEY, userId), key.getKey(), ONE_DAY);

            GoogleAuthKeyResp googleAuthKeyResp = new GoogleAuthKeyResp();
            googleAuthKeyResp.setAuthKey(key.getKey());
            googleAuthKeyResp.setQrCode(qrCode);
            return googleAuthKeyResp;
        } else {
            log.warn("generateAuthKeyAndQrCode userSecurity is null or authKey existed, userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
    }

    @Override
    public Long get2FaUnbindTime(Long userId) {
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        Long unbindTime = null;
        if (null != userSecurity && null != userSecurity.getUnbindTime()) {
            unbindTime = userSecurity.getUnbindTime().getTime();
        }
        log.info("get2FaUnbindTime userId:{}, unbindTime:{}", userId, unbindTime);
        return unbindTime;
    }

    @Override
    public Boolean getReBindGoogleVerifyStatus(Long userId) {
        UserSecurityLog userSecurityLog = this.userSecurityLogMapper.getLastBindGoogleVerify(userId);
        if(userSecurityLog != null && userSecurityLog.getOperateTime() != null) {
            if(org.apache.commons.lang3.time.DateUtils.addDays(new Date(), -bindGoogleVerifyDays).after(userSecurityLog.getOperateTime())) {
                return true;
            }
            return false;
        }
        return false;
    }


    @Override
    public boolean isYubikeyEnabledInSpecifiedScenario(Long userId, SecurityKeyApplicationScenario scenario) {
        boolean bound = webAuthnFrontHandler.hasUserBound(userId);
        if (!bound) {
            log.info("1 isYubikeyEnabledInSpecifiedScenario user: {}, scenario: {}, bound: {}", userId, scenario, false);
            return false;
        }
        Long enableStatus = userSecurityMapper.selectYubikeyEnabledScenarios(userId);
        if (enableStatus == null) {
            log.info("2 isYubikeyEnabledInSpecifiedScenario2 user: {}, scenario: {}, bound: {}", userId, scenario, false);
            return false;
        }

        boolean result = BitUtils.isTrue(enableStatus, scenario.bitVal());
        log.info("3 isYubikeyEnabledInSpecifiedScenario user: {}, scenario: {}, bound: {}", userId, scenario, result);
        return result;
    }

    @Override
    public void updateYubikeyEnableScenarios(Long userId, Map<SecurityKeyApplicationScenario, Boolean> scenarios, String code) {
        if (CollectionUtils.isEmpty(scenarios)) {
            return;
        }

        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        //判断是否存在 开->关。如果有则需要验证Security key.
        if (scenarios.values().stream().anyMatch(v -> BooleanUtils.isFalse(v))) {
            verifySecurityKey(userId, code);
        }

        long enableScenarios = userSecurity.getYubikeyEnabledScenarios();
        for (Map.Entry<SecurityKeyApplicationScenario, Boolean> entry : scenarios.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            if (BooleanUtils.isTrue(entry.getValue())) {
                enableScenarios = BitUtils.enable(enableScenarios, entry.getKey().bitVal());
            } else {
                enableScenarios = BitUtils.disable(enableScenarios, entry.getKey().bitVal());
            }
        }

        if (userSecurity.getYubikeyEnabledScenarios() != enableScenarios) {
            userSecurity = new UserSecurity();
            userSecurity.setUserId(userId);
            userSecurity.setYubikeyEnabledScenarios(enableScenarios);
            userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
        }
    }
    private String getUpdateTradeSendTgText(boolean isParent,Long userId,Integer level,String date){
        return String.format(isParent?UPDATE_PARENT_TRADE_STATUS_SEND_TG_TEMPLATE:UPDATE_TRADE_STATUS_SEND_TG_TEMPLATE,userId,level,date);
    }

    @Override
    public APIResponse<?> disableForFiat(APIRequest<UserIdRequest> request) throws Exception {
        // 1.更改交易状态为禁用
        final UserIdRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (null == userIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (userInfo != null && userInfo.getTradeLevel() != null && userInfo.getTradeLevel() >= 4){
            String text = getUpdateTradeSendTgText(AccountTypeEnum.PARENT.getAccountType().equals(AccountTypeEnum.getAccountType(user.getStatus())),
                    requestBody.getUserId(),userInfo.getTradeLevel(),DateUtils.formatterUTC(new Date(),DateUtils.DETAILED_NUMBER_PATTERN));
            telegramClient.sendTg(updateWithdrawStatusChatId,text,updateWithdrawStatusRoomId);
        }
        final User record = new User();
        record.setEmail(user.getEmail());
        record.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_TRADE));
        this.userMapper.updateByEmail(record);
        // 禁止提币
        UserSecurity userSecurityRecord = new UserSecurity();
        userSecurityRecord.setUserId(user.getUserId());
        userSecurityRecord.setDisableTime(DateUtils.getNewUTCDate());
        userSecurityRecord.setWithdrawSecurityStatus(1);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurityRecord);


        if (null != userInfo && null != userInfo.getTradingAccount() && userInfo.getTradingAccount() > 0) {// 禁用交易
            accountApi.setTradingAccount(userInfo.getTradingAccount(), false, true, true);
        } else {
            log.warn("disableForFiat TradingAccount is null");
        }
        return APIResponse.getOKJsonResult(null);
    }


    /**
     * 设置逐仓margin账号是否使用燃烧bnb的操作
     * */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Integer setIsolatedMarginBnbFee(Long isolatedMarginUserId,Boolean enableBnbFlag)throws Exception{
        log.info("setIsolatedMarginBnbFee: isolatedMarginUserId={}, enableBnbFlag={}",isolatedMarginUserId,enableBnbFlag);
        final UserInfo isolatedMarginUserInfo = this.userInfoMapper.selectByPrimaryKey(isolatedMarginUserId);
        if(null==isolatedMarginUserInfo){
            log.info("setIsolatedMarginBnbFee: userInfo is null or marginUserId is null  isolatedMarginUserId={} ",isolatedMarginUserId);
            return 0;
        }
        UserIndex isolatedMarginUserIndex = userIndexMapper.selectByPrimaryKey(isolatedMarginUserId);
        User isolatedMarginUser = userMapper.queryByEmail(isolatedMarginUserIndex.getEmail());
        UserStatusEx userStatusEx=new UserStatusEx(isolatedMarginUser.getStatus());
        if(!userStatusEx.getIsIsolatedMarginUser()){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);//不支持的操作
        }
        int updateResult = 0;
        if(enableBnbFlag){
            isolatedMarginUser.setStatus(BitUtils.enable(isolatedMarginUser.getStatus(), Constant.USER_FEE));
            updateResult = this.userMapper.updateByEmailSelective(isolatedMarginUser);
        }else{
            isolatedMarginUser.setStatus(BitUtils.disable(isolatedMarginUser.getStatus(), Constant.USER_FEE));
            updateResult = this.userMapper.updateByEmailSelective(isolatedMarginUser);
        }
        //同步像撮合修改燃烧bnb的状态
        accountApiClient.setGas(isolatedMarginUserInfo.getUserId().toString(),enableBnbFlag);
        log.info("setIsolatedMarginBnbFee finish: rootUserId={}, enableBnbFlag={}，updateResult={}",isolatedMarginUserId,enableBnbFlag,updateResult);
        return updateResult;
    }

    private void checkFundPasswordFunction(Long userId) {
        if (CollectionUtils.isEmpty(fundPasswordWhitelist)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        // 开启全开放
        if (fundPasswordWhitelist.size() == 1 && fundPasswordWhitelist.contains(-999L)) {
            return;
        }
        // 灰度名单中
        if (fundPasswordWhitelist.contains(userId)) {
            return;
        }
        throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
    }

    @Override
    public GetUser2faResponse getUser2fa(UserIdRequest body) throws Exception{
        if (body == null || body.getUserId() == null){
            return new GetUser2faResponse();
        }
        User user = userCommonBusiness.checkAndGetUserById(body.getUserId());
        GetUser2faResponse response = new GetUser2faResponse();
        boolean flag = BitUtils.isTrue(user.getStatus(), Constant.USER_MOBILE)
                || BitUtils.isTrue(user.getStatus(), Constant.USER_GOOGLE);
        Long enableStatus = userSecurityMapper.selectYubikeyEnabledScenarios(body.getUserId());
        if (enableStatus != null && enableStatus != 0) {
            log.info("getUser2fa.selectYubikeyEnabledScenarios user: {}", body.getUserId());
            flag = true;
            boolean loginResult = BitUtils.isTrue(enableStatus, SecurityKeyApplicationScenario.login.bitVal());
            boolean resetPasswordResult = BitUtils.isTrue(enableStatus, SecurityKeyApplicationScenario.resetPassword.bitVal());
            boolean withdrawAndApiResult = BitUtils.isTrue(enableStatus, SecurityKeyApplicationScenario.withdrawAndApi.bitVal());
            List<SecurityKeyApplicationScenario> youbikeyEnums = Lists.newArrayList();
            if (loginResult){
                youbikeyEnums.add(SecurityKeyApplicationScenario.login);
            }
            if (resetPasswordResult){
                youbikeyEnums.add(SecurityKeyApplicationScenario.resetPassword);
            }
            if (withdrawAndApiResult){
                youbikeyEnums.add(SecurityKeyApplicationScenario.withdrawAndApi);
            }
            if (!CollectionUtils.isEmpty(youbikeyEnums) && youbikeyEnums.size() > 0){
                response.setYoubikeyEnums(youbikeyEnums);
            }
        }

        if (flag){
            response.setIfDo2fa(true);
            List<AuthTypeEnum> authTypeEnums = Lists.newArrayList();
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_MOBILE)){
                authTypeEnums.add(AuthTypeEnum.SMS);
            }
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_GOOGLE)){
                authTypeEnums.add(AuthTypeEnum.GOOGLE);
            }
            response.setAuthTypeEnums(authTypeEnums);
        }
        return response;

    }

    @Override
    public UserRiskInfoResponse getUserRiskInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        User user = userMapper.queryByEmail(userIndex.getEmail());
        UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        UserRiskInfoResponse response = new UserRiskInfoResponse();
        response.setUserId(userId);
        response.setEmail(user.getEmail());
        response.setRegisterTime(user.getInsertTime());
        response.setGoogleAuth(userStatusEx.getIsUserGoogle());
        response.setMobileAuth(userStatusEx.getIsUserMobile());
        response.setAgentId(userInfo.getAgentId());
        response.setTradeLevel(userInfo.getTradeLevel());
        response.setMobile(userSecurity.getMobile());
        response.setMobileCode(userSecurity.getMobileCode());
        List<UserIp> userIps = userIpMapper.getIpByUser(userId);
        if (userIps != null) {
            Optional<UserIp> userIp = userIps.stream().sorted(Comparator.comparing(UserIp::getInsertTime)).findFirst();
            if (userIp.isPresent()) {
                response.setRegisterIp(userIp.get().getIp());
                response.setRegisterIpCountry(IP2LocationUtils.getCountryCity(response.getRegisterIp()));
            }
        }
        UserConfig queryConfig = new UserConfig();
        queryConfig.setUserId(userId);
        queryConfig.setConfigType("userLastLoginLanguage");
        UserConfig userConfig = userInfoMapper.selectLatestUserConfig(queryConfig);
        if (userConfig != null) {
            response.setLastLoginLang(userConfig.getConfigName());
        }
        return response;
    }

    @SecurityLog(name = "提币人脸识别状态变更", operateType = AccountConstants.USER_WITHDRAW_FACE_CHANGE_LOG,
            userId = "#request.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Integer changeWithdrawFaceCheckStatus(WithdrawFaceStatusChangeRequest request) {
        log.info("changeWithdrawFaceCheckStatus => by {}", request);
        int row = userSecurityMapper.updateWithdrawSecurityFaceStatusByUserId(request.getUserId(), request.getToStatus(), request.getFromStatus());
        return row;
    }


    @SecurityLog(name = "换绑邮箱", operateType = AccountConstants.SECURITY_OPERATE_TYPE_CHANGE_EMAIL,
            userId = "#request.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @RiskTask(userId = "#request.userId",type = RiskTaskAspect.UPDATE_EMAIL,email = "#request.newEmail")
    @Override
    public ChangeEmailResponse changeEmail(ChangeEmailRequest request) {
        final Long requestUserId = request.getUserId();
        String requestEmail = request.getNewEmail();

        UserIndex originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == originalUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User originalUser = userMapper.queryByEmail(originalUserIndex.getEmail());
        if (null == originalUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userStatus = originalUser.getStatus();
        UserStatusEx userStatusEx=new UserStatusEx(userStatus);
        if(userStatusEx.getIsUserNotBindEmail().booleanValue()){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 更新Email
        requestEmail=requestEmail.toLowerCase();
        if (!Pattern.matches(REGEX_EMAIL, requestEmail)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        User existedUserByEmail = userMapper.queryByEmail(requestEmail);
        if (null != existedUserByEmail && existedUserByEmail.getUserId().longValue() != requestUserId.longValue()) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }
        if (null == existedUserByEmail) {
            int num = userMapper.deleteByEmail(originalUserIndex.getEmail());
            if (num > 0) {
                User newUser = new User();
                BeanUtils.copyProperties(originalUser, newUser);
                newUser.setEmail(requestEmail);
                userMapper.insert(newUser);

                UserIndex updateUserIndex = new UserIndex();
                updateUserIndex.setUserId(requestUserId);
                updateUserIndex.setEmail(requestEmail);
                userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);

                UserSecurity newSecurity = new UserSecurity();
                newSecurity.setUserId(requestUserId);
                newSecurity.setEmail(requestEmail);
                userSecurityMapper.updateByPrimaryKeySelective(newSecurity);

                // 临时的代码 完全迁移后移除 start
                Map<String, Object> dataMsg = com.google.common.collect.Maps.newHashMap();
                dataMsg.put(UserConst.USER_ID, requestUserId);
                dataMsg.put(UserConst.EMAIL, requestEmail.toLowerCase());
                MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.MODIFY_SUBACCOUNT_EMAIL, dataMsg);
                log.info("iMsgNotification changeEmail:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                this.iMsgNotification.send(msg);

                // 登出用户的登录态, 这个地方做参数判断问题主要是在reset2fa中需要做延时几秒中踢出登录态，主要是为了能让前端显示reset2fa成功的状态
                if (request.isLogoutUser()) {
                    logoutUserAll(requestUserId);
                }

                notificationToC2CHelper.sendEmailBindingChangeMsgAsync(newSecurity);
            } else {
                log.error("Delete user failed, userId:{}, email:{}", originalUser.getUserId(),
                        originalUser.getEmail());
            }
        }
        return null;
    }


    @SecurityLog(name = "换绑手机", operateType = AccountConstants.SECURITY_OPERATE_TYPE_CHANGE_MOBILE,
            userId = "#request.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @RiskTask(userId = "#request.userId")
    @Override
    public ChangeMobileResponse changeMobile(ChangeMobileRequest request) {
        final Long requestUserId = request.getUserId();
        // 手机码校验
        request.setNewMobileCode(request.getNewMobileCode().toUpperCase());
        Country country = this.iCountry.getCountryByCode(request.getNewMobileCode().toUpperCase());
        if (null == country) {
            log.warn("changeMobile,mobileCode:{} invalid", request.getNewMobileCode());
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        request.setNewMobileCode(country.getCode().toUpperCase());
        UserIndex originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == originalUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User originalUser = userMapper.queryByEmail(originalUserIndex.getEmail());
        if (null == originalUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userStatus = originalUser.getStatus();
        UserStatusEx userStatusEx=new UserStatusEx(userStatus);
        if(!userStatusEx.getIsUserMobile().booleanValue()){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }


        // 判断手机是否已经被使用
        final UserMobileIndex userMobileIndex =
                this.userMobileIndexMapper.selectByPrimaryKey(request.getNewMobile(), request.getNewMobileCode());
        if (userMobileIndex != null) {
            throw new BusinessException(GeneralCode.USER_MOBILE_EXIST);
        }

        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(requestUserId);
        if (null != userSecurity && StringUtils.isNotBlank(userSecurity.getMobile())) {
            //如果没有绑定过邮箱，那么需要更换fake email
            if(userStatusEx.getIsUserNotBindEmail()){
                int num = userMapper.deleteByEmail(originalUserIndex.getEmail());
                if (num > 0) {
                    String mobileFakeEmail= UserEmailUtils.getMobileUserEmail(request.getNewMobileCode(),request.getNewMobile()).toLowerCase();

                    User newUser = new User();
                    BeanUtils.copyProperties(originalUser, newUser);
                    newUser.setEmail(mobileFakeEmail);
                    userMapper.insert(newUser);

                    UserIndex updateUserIndex = new UserIndex();
                    updateUserIndex.setUserId(requestUserId);
                    updateUserIndex.setEmail(mobileFakeEmail);
                    userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);

                    // 临时的代码 完全迁移后移除 start
                    Map<String, Object> dataMsg = com.google.common.collect.Maps.newHashMap();
                    dataMsg.put(UserConst.USER_ID, requestUserId);
                    dataMsg.put(UserConst.EMAIL, mobileFakeEmail.toLowerCase());
                    MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.MODIFY_SUBACCOUNT_EMAIL, dataMsg);
                    log.info("iMsgNotification changeMobile:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                    this.iMsgNotification.send(msg);
                }
            }

            userMobileIndexMapper.deleteByPrimaryKey(userSecurity.getMobile(), userSecurity.getMobileCode());
            // 记录手机索引
            final UserMobileIndex mobileIndex = new UserMobileIndex();
            mobileIndex.setMobile(request.getNewMobile());
            mobileIndex.setCountry(request.getNewMobileCode());
            mobileIndex.setUserId(request.getUserId());
            this.userMobileIndexMapper.insert(mobileIndex);
            // 更新手机号
            final UserSecurity newSecurity = new UserSecurity();
            newSecurity.setUserId(request.getUserId());
            newSecurity.setMobile(request.getNewMobile());
            newSecurity.setMobileCode(request.getNewMobileCode());

            // 绑定手机消息通知 start
            Map<String, Object> dataMsg = new HashMap<String, Object>();
            dataMsg.put(UserConst.USER_ID, request.getUserId());
            dataMsg.put("mobile", request.getNewMobile());
            dataMsg.put("mobileCode", request.getNewMobileCode());
            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.BIND_MOBILE, dataMsg);
            log.info("iMsgNotification changeMobile:{}", JSON.toJSONString(msg));
            this.iMsgNotification.send(msg);
            // 绑定手机消息通知 end
            this.userSecurityMapper.updateBindInfoByUserId(newSecurity);

            // 登出用户的登录态, 这个地方做参数判断问题主要是在reset2fa中需要做延时几秒中踢出登录态，主要是为了能让前端显示reset2fa成功的状态
            if (request.isLogoutUser()) {
                log.info("change mobile success then logout userId: {}", requestUserId);
                logoutUserAll(requestUserId);
            }

            notificationToC2CHelper.sendMobileBindingChangeMsgAsync(newSecurity);
        }
        return new ChangeMobileResponse();
    }

    @Override
    public GetCapitalWithdrawVerifyParamResponse getCapitalWithdrawVerfiyParam(GetCapitalWithdrawVerifyParamRequest request) {
        if(org.apache.commons.lang3.StringUtils.isAllBlank(request.getEmailCode(),request.getMobileCode())){
            log.info("getCapitalWithdrawVerfiyParam error param");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        GetCapitalWithdrawVerifyParamResponse resp=new GetCapitalWithdrawVerifyParamResponse();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getEmailCode())){
            String emailVerifyKeyStr = RedisCacheUtils.get(request.getUserId().toString()+request.getEmailCode(), String.class, AccountConstants.CRYPTO_WITHDRAW_ADDRESS_CODE_KEY);
            CapitalWithdrawRedisVerify redisVerify = null;
            if (StringUtils.isBlank(emailVerifyKeyStr)) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
            } else {// 证明 expireTime 内还是有效的
                redisVerify = JSON.parseObject(emailVerifyKeyStr, CapitalWithdrawRedisVerify.class);
                CapitalWithdrawVerifyParam emailParam =new CapitalWithdrawVerifyParam();
                BeanUtils.copyProperties(redisVerify,emailParam);
                resp.setEmailParam(emailParam);
            }
        }

        if(org.apache.commons.lang3.StringUtils.isNotBlank(request.getMobileCode())){
            String mobileVerifyKeyStr = RedisCacheUtils.get(request.getUserId().toString()+request.getMobileCode(), String.class, AccountConstants.CRYPTO_WITHDRAW_ADDRESS_CODE_KEY);
            CapitalWithdrawRedisVerify redisVerify = null;
            if (StringUtils.isBlank(mobileVerifyKeyStr)) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
            } else {// 证明 expireTime 内还是有效的
                redisVerify = JSON.parseObject(mobileVerifyKeyStr, CapitalWithdrawRedisVerify.class);
                CapitalWithdrawVerifyParam smsParam =new CapitalWithdrawVerifyParam();
                BeanUtils.copyProperties(redisVerify,smsParam);
                resp.setSmsParam(smsParam);
            }
        }
        return resp;
    }

    private void logoutUserAll(Long userId) {
        try {
            com.binance.authcenter.vo.UserIdRequest userIdRequest = new com.binance.authcenter.vo.UserIdRequest();
            userIdRequest.setUserId(userId);
            APIResponse<LogoutResponse> response = authApi.logoutAll(APIRequest.instance(userIdRequest));
            log.info("logout user all result userId:{} {}", userId, JSON.toJSONString(response));
        }catch (Exception e) {
            log.error("logout user fail. userId:{}", userId, e);
        }
    }

    @Override
    public VerificationsDemandResponse verificationsDemand(VerificationsDemandRequest request) throws Exception {
        log.info("verificationsDemand => request:{}", request);
        Long userId = request.getUserId();
        AccountVerificationTwoEnum userChoose2Fa = null;
        VerificationsTwo smsVerificationsTwo=null;
        VerificationsTwo emailVerificationsTwo=null;
        if (StringUtils.isNotBlank(request.getYubikeyVerifyCode())) {
            this.verifySecurityKey(userId, request.getYubikeyVerifyCode());
        }
        if (StringUtils.isNotBlank(request.getMobileVerifyCode())) {
            smsVerificationsTwo = this.isSmsAuthenticator(userId, request.getMobileVerifyCode(), false, request.getMobile(), request.getMobileCode());
            userChoose2Fa = SMS;
        }
        if (StringUtils.isNotBlank(request.getEmailVerifyCode())) {
            emailVerificationsTwo = this.isEmailAuthenticator(userId, request.getEmailVerifyCode(), false);
            userChoose2Fa = EMAIL;
        }
        if (StringUtils.isNotBlank(request.getGoogleVerifyCode())) {
            this.checkGoogleAuthenticator(userId, request.getGoogleVerifyCode());
            userChoose2Fa = GOOGLE;
        }
        // 没有抛错代表验证成功，记录下用户这次使用的2fa验证类型
        if (userChoose2Fa != null) {
            iUser.addOrUpdateUserConfig(userId, USER_CONFIG_LAST_2FA_TYPE, userChoose2Fa.getCode());
        }
        if (null != emailVerificationsTwo) {
            RedisCacheUtils.del(userId.toString(), AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
            log.info("verificationsDemand => del emailCode userId={}", userId);
        }
        if (null != smsVerificationsTwo) {
            smsVerificationsTwo.delMobileCode();
            log.info("verificationsDemand => del mobileCode userId={}", userId);
        }
        return new VerificationsDemandResponse();
    }
}
