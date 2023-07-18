package com.binance.account.service.user.impl;

import static com.binance.account.constants.AccountConstants.USER_CONFIG_PREFER_LANG;
import static com.binance.account.vo.security.enums.BizSceneEnum.CRYPTO_WITHDRAW;
import static com.binance.master.constant.Constant.USER_IS_FIAT_USER;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.binance.account.data.entity.agent.*;
import com.binance.account.data.mapper.agent.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.aop.FrontTask;
import com.binance.account.aop.FrontTaskAspect;
import com.binance.account.aop.MarginValidate;
import com.binance.account.aop.RiskTask;
import com.binance.account.aop.RiskTaskAspect;
import com.binance.account.aop.SecurityLog;
import com.binance.account.aop.UserPermissionValidate;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.constant.UserDeviceConst;
import com.binance.account.common.enums.FinanceFlagEnum;
import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.account.common.enums.UserConfigTypeEnum;
import com.binance.account.common.query.es.ESQueryBuilder;
import com.binance.account.common.query.es.ESQueryCondition;
import com.binance.account.common.query.es.ESResultSet;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.certificate.AccUserKyc;
import com.binance.account.data.entity.certificate.AccUserKycExample;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.futureagent.FutureUserAgent;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.security.VerificationsTwo;
import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.ReCaptcha;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserCommonPermission;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.account.data.entity.user.UserTradingAccount;
import com.binance.account.data.mapper.certificate.AccUserKycMapper;
import com.binance.account.data.mapper.certificate.UserAddressMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.futureagent.FutureUserAgentMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.data.utils.CryptoAlgoUtils;
import com.binance.account.domain.bo.CapitalWithdrawRedisVerify;
import com.binance.account.domain.bo.FrontPushEventType;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.MsgNotification.OptType;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.assetservice.UserAssetApiClient;
import com.binance.account.integration.capital.CapitalClient;
import com.binance.account.integration.fiat.FiatAccountApiClient;
import com.binance.account.integration.margin.MarginAccountApiClient;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.integration.message.UserLanguageApiClient;
import com.binance.account.integration.report.ReportApiClient;
import com.binance.account.integration.risk.RiskSecurityApiClient;
import com.binance.account.integration.risk.RuleDecisionApiClient;
import com.binance.account.integration.streamer.StreamerOrderApiClient;
import com.binance.account.mq.OneButtonRegisterSender;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.country.impl.CountryBlacklistBusiness;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.datamigration.impl.MsgNotificationToC2CHelper;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.es.ElasticService;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.account.service.question.Utils;
import com.binance.account.service.security.IUserIpChange;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.security.impl.UserSecurityBusiness;
import com.binance.account.service.security.model.MultiFactorSceneVerify;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.IUserKycEmailNotify;
import com.binance.account.service.user.IUserPermission;
import com.binance.account.service.user.IUserSearch;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.service.userconfig.IUserConfig;
import com.binance.account.service.withdraw.WithdrawPolicyService;
import com.binance.account.util.UserEmailUtils;
import com.binance.account.utils.InboxUtils;
import com.binance.account.utils.InvitationCodeUtil;
import com.binance.account.utils.MaskUtils;
import com.binance.account.utils.MessageUtils;
import com.binance.account.utils.RegexUtils;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.country.RestrictedCountryVo;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.device.response.CheckUserDeviceResponse;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.security.request.CountAgentNumberRequest;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.security.request.GetUserIdByEmailOrMobileRequest;
import com.binance.account.vo.security.request.MarginRelationShipRequest;
import com.binance.account.vo.security.request.ReCaptchaReq;
import com.binance.account.vo.security.request.SendEmailVerifyCodeRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.GetUserIdByEmailOrMobileResponse;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserIpLikeVo;
import com.binance.account.vo.user.UserIpVo;
import com.binance.account.vo.user.UserVo;
import com.binance.account.vo.user.enums.AccountTypeEnum;
import com.binance.account.vo.user.enums.CommonUserType;
import com.binance.account.vo.user.enums.RegisterationMethodEnum;
import com.binance.account.vo.user.enums.UserPermissionOperationEnum;
import com.binance.account.vo.user.enums.UserTypeEnum;
import com.binance.account.vo.user.ex.OrderConfirmStatus;
import com.binance.account.vo.user.ex.UserIndexEx;
import com.binance.account.vo.user.ex.UserSecurityKeyStatus;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.AccountActiveUserRequest;
import com.binance.account.vo.user.request.AccountActiveUserV2Request;
import com.binance.account.vo.user.request.AccountForgotPasswordRequest;
import com.binance.account.vo.user.request.AccountResetPasswordRequest;
import com.binance.account.vo.user.request.AccountResetPasswordVerifyRequest;
import com.binance.account.vo.user.request.BaseDetailRequest;
import com.binance.account.vo.user.request.DeleteUserRequest;
import com.binance.account.vo.user.request.FuzzyMatchUserIndexRequest;
import com.binance.account.vo.user.request.FuzzyMatchUserInfoRequest;
import com.binance.account.vo.user.request.GetAccountIdByRootUserIdRequest;
import com.binance.account.vo.user.request.GetBatchUserTypeListRequest;
import com.binance.account.vo.user.request.GetReferralEmailRequest;
import com.binance.account.vo.user.request.GetUserAgentConfigRequest;
import com.binance.account.vo.user.request.GetUserAgentDetailRequest;
import com.binance.account.vo.user.request.GetUserIdByTradingAccountRequest;
import com.binance.account.vo.user.request.GetUserIdListRequest;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.request.GetUserRequest;
import com.binance.account.vo.user.request.LoginUserRequest;
import com.binance.account.vo.user.request.LoginUserRequestV2;
import com.binance.account.vo.user.request.ModifyUserEmailRequest;
import com.binance.account.vo.user.request.ModifyUserRequest;
import com.binance.account.vo.user.request.OneButtonRegisterRequest;
import com.binance.account.vo.user.request.OneButtonUserAccountActiveRequest;
import com.binance.account.vo.user.request.OrderConfrimStatusRequest;
import com.binance.account.vo.user.request.PasswordVerifyRequest;
import com.binance.account.vo.user.request.RegisterUserRequest;
import com.binance.account.vo.user.request.RegisterUserRequestV2;
import com.binance.account.vo.user.request.ResendSendActiveCodeRequest;
import com.binance.account.vo.user.request.SearchUserListRequest;
import com.binance.account.vo.user.request.SelectUserAgentLogRequest;
import com.binance.account.vo.user.request.SelectUserAgentNumRequest;
import com.binance.account.vo.user.request.SendSmsAuthCodeV2Request;
import com.binance.account.vo.user.request.SendSmsAuthCoderRequest;
import com.binance.account.vo.user.request.SnapshotShareConfigReq;
import com.binance.account.vo.user.request.ThirdPartyUserRegisterRequest;
import com.binance.account.vo.user.request.UpdateAccountUserRequest;
import com.binance.account.vo.user.request.UpdateAgentRateReq;
import com.binance.account.vo.user.request.UpdateNickNameRequest;
import com.binance.account.vo.user.request.UpdatePwdUserRequest;
import com.binance.account.vo.user.request.UpdatePwdUserV2Request;
import com.binance.account.vo.user.request.UpdateUserByEmailRequest;
import com.binance.account.vo.user.request.UserAgentConfigReq;
import com.binance.account.vo.user.request.UserAgentLinkReq;
import com.binance.account.vo.user.request.UserAgentRateReq;
import com.binance.account.vo.user.request.UserAgentSelectShareReq;
import com.binance.account.vo.user.request.UserIpRequest;
import com.binance.account.vo.user.request.UserMobileRequest;
import com.binance.account.vo.user.request.UserRegistRequest;
import com.binance.account.vo.user.request.UserStatusRequest;
import com.binance.account.vo.user.response.AccountActiveUserResponse;
import com.binance.account.vo.user.response.AccountActiveUserV2Response;
import com.binance.account.vo.user.response.AccountForgotPasswordResponse;
import com.binance.account.vo.user.response.AccountResetPasswordResponse;
import com.binance.account.vo.user.response.AccountResetPasswordVerifyResponse;
import com.binance.account.vo.user.response.BaseDetailResponse;
import com.binance.account.vo.user.response.CreateFiatUserResponse;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.account.vo.user.response.FinanceFlagResponse;
import com.binance.account.vo.user.response.FuzzyMatchUserIndexResponse;
import com.binance.account.vo.user.response.FuzzyMatchUserInfoResponse;
import com.binance.account.vo.user.response.GetBatchUserTypeListResponse;
import com.binance.account.vo.user.response.GetUserAgentConfigResponse;
import com.binance.account.vo.user.response.GetUserAgentStatResponse;
import com.binance.account.vo.user.response.GetUserCommissionDetailResponse;
import com.binance.account.vo.user.response.GetUserEmailResponse;
import com.binance.account.vo.user.response.GetUserEmailsResponse;
import com.binance.account.vo.user.response.GetUserListResponse;
import com.binance.account.vo.user.response.GetUserResponse;
import com.binance.account.vo.user.response.KycValidateResponse;
import com.binance.account.vo.user.response.LoginUserResponse;
import com.binance.account.vo.user.response.LoginUserResponseV2;
import com.binance.account.vo.user.response.OneButtonRegisterResponse;
import com.binance.account.vo.user.response.RegisterUserResponse;
import com.binance.account.vo.user.response.RegisterUserResponseV2;
import com.binance.account.vo.user.response.ResendSendActiveCodeResponse;
import com.binance.account.vo.user.response.SearchUserListResponse;
import com.binance.account.vo.user.response.SelectUserAgentLogResponse;
import com.binance.account.vo.user.response.SendSmsAuthCodeV2Response;
import com.binance.account.vo.user.response.SendSmsAuthCoderResponse;
import com.binance.account.vo.user.response.SnapshotShareConfigRes;
import com.binance.account.vo.user.response.SnapshotShareConfigsRes;
import com.binance.account.vo.user.response.SpecialUserIdResponse;
import com.binance.account.vo.user.response.UpdateAccountUserResponse;
import com.binance.account.vo.user.response.UpdatePwdUserResponse;
import com.binance.account.vo.user.response.UpdatePwdUserV2Response;
import com.binance.account.vo.user.response.UserAgentRateResponse;
import com.binance.account.vo.user.response.UserBriefInfoResponse;
import com.binance.account.vo.user.response.UserTypeResponse;
import com.binance.account.vo.user.request.*;
import com.binance.account.vo.user.response.*;
import com.binance.account.yubikey.WebAuthnFrontHandler;
import com.binance.assetservice.vo.response.UserAssetResponse;
import com.binance.assetservice.vo.response.asset.SelectUserAssetLogResponse;
import com.binance.c2c.api.MerchantApi;
import com.binance.c2c.vo.merchant.request.SyncMerchantReq;
import com.binance.c2c.vo.user.request.CreateFiatUserReq;
import com.binance.capital.vo.withdraw.request.GetWithdrawCountRequest;
import com.binance.capital.vo.withdraw.vo.WithdrawVo;
import com.binance.fiat.payment.service.api.dto.ExternalUserBindRegistRequest;
import com.binance.fiat.payment.service.api.iface.ExternalOcbsApi;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.commons.SearchResult;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.AuthStatusEnum;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.SysType;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.RedisVerify;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.PasswordUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;
import com.binance.master.utils.security.TokenUtils;
import com.binance.messaging.api.msg.request.MsgType;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.binance.risk.api.RiskWithdrawApi;
import com.binance.risk.vo.CheckUserRiskRequestVo;
import com.binance.risk.vo.withdraw.request.RiskWithdrawBlackListRequest;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.lang.Assert;
import io.shardingsphere.api.HintManager;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.keygen.KeyGenerator;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.javasimon.aop.Monitored;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.binance.account.constants.AccountConstants.USER_CONFIG_PREFER_LANG;
import static com.binance.account.vo.security.enums.BizSceneEnum.CRYPTO_WITHDRAW;
import static com.binance.master.constant.Constant.USER_IS_FIAT_USER;


@Log4j2
@Service
// @RefreshScope
public class UserBusiness implements IUser {

    private static final String[] AGENT_CODE_ARR =
            {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                    "M", "L", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private static final String AGENT_LABEL_REGEX = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    @Value("${user.nickcolor:#121212,#343434,#898989}")
    private String[] nickColors;

    @Resource
    protected UserMapper userMapper;
    @Resource
    protected UserSecurityMapper userSecurityMapper;
    @Resource
    protected UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private ICountry iCountry;
    @Resource
    protected UserIpMapper userIpMapper;
    @Resource
    protected IMsgNotification iMsgNotification;
    @Resource
    protected UserAddressMapper userAddressMapper;
    @Resource
    private IUserIpChange iUserIpChange;
    @Resource
    private UserMobileIndexMapper userMobileIndexMapper;
    @Autowired
    protected IUserDevice userDeviceBusiness;
    @Resource
    private IUserSecurity userSecurityBusiness;
    @Autowired
    private UserAgentRateMapper userAgentRateMapper;
    @Autowired
    private UserAgentConfigMapper userAgentConfigMapper;
    @Autowired
    private IUserSearch userSearch;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private ISysConfig iSysConfig;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserTradingAccountMapper userTradingAccountMapper;
    @Autowired
    private CountryBlacklistBusiness countryBlacklistBusiness;
    @Autowired
    private ApolloCommonConfig commonConfig;
    @Autowired
    private RiskWithdrawApi riskWithdrawApi;
    @Autowired
    private MatchboxApiClient matchboxApiClient;
    @Autowired
    private AccountApiClient accountApiClient;
    @Autowired
    private IUserKyc kyc;
    @Autowired
    private UserCommonValidateService userCommonValidateService;
    @Resource
    private ApplicationContext applicationContext;
    @Autowired
    private ElasticService elasticService;
    @Autowired
    private MarginAccountApiClient marginAccountApiClient;
    @Autowired
    private FiatAccountApiClient fiatAccountApiClient;
    @Autowired
    private SecurityNotificationService securityNotificationService;
    @Autowired
    private WebAuthnFrontHandler webAuthnFrontHandler;
    @Autowired
    private UserAgentLogMapper userAgentLogMapper;
    @Autowired
    private MiningAgentLogMapper miningAgentLogMapper;
    @Autowired
    private InboxMessageTextApi inboxMessageTextApi;
    @Autowired
    private MessageUtils messageUtils;
    @Autowired
    private MerchantApi merchantApi;
    @Autowired
    private SnapShotShareConfigMapper snapShotShareConfigMapper;
    protected KeyGenerator keyGenerator=new DefaultKeyGenerator();
    @Autowired
    private RuleDecisionApiClient ruleDecisionApiClient;

    @Value("${account.forget.password.switch:true}")
    private Boolean forgetPasswordSwitch;
    @Resource
    private AccUserKycMapper accUserKycMapper;

    @ApolloConfig
    private Config config;

    @Resource
    private IUserConfig iUserConfig;

    @Resource
    private IUserKycEmailNotify iUserKycEmailNotify;
    @Autowired
    private ReportApiClient reportApiClient;

    @Autowired
    private SubUserBindingMapper subUserBindingMapper;

    @Autowired
    private RiskSecurityApiClient riskSecurityApiClient;
    @Resource
    private KycApiTransferAdapter kycApiTransferAdapter;
    @Autowired
    private FutureUserAgentMapper futureUserAgentMapper;
    @Value("${rebateStrategyChangeTime:1591946295000}")
    private Long rebateStrategyChangeTime;
    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private UserSimpleBusiness userSimpleBusiness;

    @Autowired
    private ExternalOcbsApi externalOcbsApi;
    @Autowired
    private OneButtonRegisterSender oneButtonRegisterSender;
    @Autowired
    private IUserSecurity iUserSecurity;
    @Autowired
    private MsgNotificationToC2CHelper notificationToC2CHelper;
    @Autowired
    private OauthAgentRelationMapper oauthAgentRelationMapper;

    private static final String CACHE_USER_PASSWORD_ERROR_NUM = "USER_PASSWORD_ERROR_NUM_%s";
    private static final List OAUTH_TRACKSOURCE = Lists.newArrayList("BinanceOAuth","BinanceAccess");
    protected static final String DEFAULT_RESULT = "lctwmv9fdld6yfdk06g";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    public static final Integer MAX_EMAIL_LENGTH = 200;

    private static final String FAKE_EMAIL_LOGIN_KEY = "account:fake:email:login";
    private static final String FAKE_EMAIL_LOGIN_LOCKED_KEY = "account:fake:email:login:locked";
    private static final String ACCOUNT_AGENT_KEY = "account:agent:key";
    private static final String ACCOUNT_OLD_AGENT_KEY = "account:old:agent:key";

    public static final String UPDATE_EMAIL_TIME_PREFIX = "account:email:update:time:%s";
    private static final String ACCOUNT_CREATE_MARGIN_USER = "account:create:margin:user";
    private static final String ACCOUNT_CREATE_FIAT_USER = "account:create:fiat:user";
    private static final String emailPhone = "_mobileuser@binance.com";
    private static final long FIVE_DAY = 60 * 60 * 24 * 5;
    // 用户锁定时间
    private static final int USER_LOCK_TIME = 2;

    private static final int ERROR_COUNT = 5;
    // 用户id传入上限
    private static final int MAX_USER_ID_SIZE = 5000;
    // 一次用户查询个数
    private static final int MAX_SELECT_USER_COUNT = 500;

    @Value("${marginCountryBlackList:}")
    private String marginCountryBlackList;

    @Value("${futureIpCountryBlackList:}")
    private String futureIpCountryBlackList;

    @Value("${agentReferralRate}")
    private String agentReferralRate;

    @Value("${promoteLinks}")
    private String promoteLinks;

    @Value("${defaultPromoteLinkNum:20}")
    private int defaultPromoteLinkNum;

    @Value("${subuser.margin.create.limit:10}")
    private int subUserMarginCreateLimit;


    @Value("${resend.activecode.ip.limit:200}")
    private int resendActiveIpLimit;

    @Value("${agent.user.nums.switch:true}")
    private boolean agentUserNumsSwitch;

//    @Value("#{'${fiat.account.kyc.countries:CN,VN,RU}'.split(',')}")
//    private Set<String> fiatAccountKycCountries;

    @Value("${mainSite.domain.url:}")
    private String mainSiteDomainUrl;

    @Value("${third.user.traceSources:AdvCash,Flux}")
    private String thirdUserTraceSources;

    @Value("${third.user.openRegister:true}")
    private Boolean thirdUserOpenRegister;
    @Value("${user.oneButtonRegister.open:true}")
    private Boolean openOneButtonRegister;

    private static final String ONEBUTTON_REGISTER_COUNT_KEY = "register:onebutton:count";
    private static final String ONEBUTTON_REGISTER_MINUTE_COUNT_KEY = "register:onebutton:minute:count";
    private static final String ONEBUTTON_REGISTER_REQUEST_COUNT_KEY = "request:onebuttonregister:count";

    private static final String THIRDPARTY_REGISTER_MINUTE_COUNT_KEY = "register:thirdparty:minute:count";
    private static final String THIRDPARTY_REQUEST_MINUTE_COUNT_KEY = "request:thirdparty:minute:count";

    @Value("${user.oneButtonRegister.dailyCountLimit:10000}")
    private Long MAX_ONEBUTTON_REGISTER_DAILY_COUNT;
    @Value("${user.oneButtonRegister.minuteCountLimit:100}")
    private Long MAX_ONEBUTTON_REGISTER_MINUTE_COUNT;
    @Value("${oneButtonRegister.requestCountLimit:10}")
    private Long requestCountLimit;
    @Value("#{${sms.scene.template.map:{}}}")
    private Map<String, String> smsSceneTemplateMap;

    @Value("${user.thirdparty.request.minuteCountLimit:150}")
    private Long MAX_THIRDPARTY_REQUEST_MINUTE_COUNT;
    @Value("${user.thirdparty.register.minuteCountLimit:100}")
    private Long MAX_THIRDPARTY_REGISTER_MINUTE_COUNT;


    @Autowired
    private IUserPermission userPermission;



    @Value("${force.check.old.password.switch:true}")
    private boolean forceCheckOldPasswordSwitch;


    @Value("${update.pwd.new.msg.switch:false}")
    private boolean updatePwdNewMsgSwitch;


    @Autowired
    private UserLanguageApiClient userLanguageApiClient;


    @Resource
    private WithdrawPolicyService withdrawPolicyService;

    @Resource
    private UserAssetApiClient userAssetApiClient;
    @Resource
    private StreamerOrderApiClient streamerOrderApiClient;
    @Value("${account.financeFlag.timeout:600}")
    private Integer financeFlagTimeout;
    @Resource
    private CapitalClient capitalClient;


    @Value("${account.need.update.ios.version.switch:false}")
    private boolean needUpdateIosVersion;



    /**
     * 解锁账号
     *
     * @param user
     * @return
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean unlockUser(User user) {
        UserSecurity temp = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());

        // 可以解锁
        if (isUserUnlockable(temp)) {
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
            return true;
        } else {
            return false;
        }
    }

    private boolean isUserUnlockable(UserSecurity userSecurity) {
        // 添加log
        log.info("isUserUnlockable userSecurity: {}", userSecurity == null ? "null" : LogMaskUtils.maskJsonString(JSON.toJSONString(userSecurity)));
        log.info("current: " + System.currentTimeMillis());
        if (userSecurity == null) {
            return false;
        }

        // UserSecurity.lockEndTime为空但user.status为锁定的也可以解锁
        if (userSecurity.getLockEndTime() == null) {
            return true;
        }

        return userSecurity.getLockEndTime().before(new Date());

    }

    /**
     * 锁定锁账号
     *
     * @param user
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Integer passWordError(User user) {
        try {
            UserSecurity temp = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
            if (temp.getLoginFailedTime() != null
                    && DateUtils.add(temp.getLoginFailedTime(), Calendar.HOUR_OF_DAY, USER_LOCK_TIME)
                            .getTime() < DateUtils.getNewUTCTimeMillis()) {
                temp.setLoginFailedNum(0);
            }
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setUserId(user.getUserId());
            userSecurity.setLoginFailedTime(DateUtils.getNewUTCDate());
            userSecurity.setUpdateTime(DateUtils.getNewDate());
            Integer maxErrorCount = Integer.valueOf(this.iSysConfig.selectByDisplayName("login_error_limit").getCode());
            int loginFailedNum = temp.getLoginFailedNum() == null ? 0 : temp.getLoginFailedNum();
            loginFailedNum=loginFailedNum+1;
            userSecurity.setLoginFailedNum(loginFailedNum);
            if (loginFailedNum >= maxErrorCount) {// 锁定账号
                Date lockEndTime = DateUtils.getNewDateAddHour(USER_LOCK_TIME);
                userSecurity.setLockEndTime(lockEndTime);// 锁定2小时
                // Add to cache, used by unlock user job(com.binance.account.job.UnlockUserJobHandler)
                userSecurityBusiness.addLockUserCache(lockEndTime.getTime(), user.getEmail());

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
            this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
            return maxErrorCount - loginFailedNum;
        } catch (Exception e) {
            log.error(String.format("passWordError failed, userId:%s, exception:", user.getUserId()), e);
        }
        return 0;
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(type = FrontTaskAspect.REGISTER, routingKey = FrontPushEventType.REGISTER_COMPLETE_ROUTING,eventType = FrontPushEventType.REGISTER_COMPLETE)
    @RiskTask(email = "#request.body.email",type = RiskTaskAspect.REGISTER)
    @Override
    public APIResponse<RegisterUserResponse> register(APIRequest<RegisterUserRequest> request) throws Exception {
        final RegisterUserRequest requestBody = request.getBody();
        // 同一ip注册限制
        final String ip = WebUtils.getRequestIp();
        long maxIpCount = Long.parseLong(this.iSysConfig.selectByDisplayName("max_register_count").getCode());
        long ipCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class, CacheKeys.REGISTER_IP_COUNT, 0L)));
        if (ipCount >= maxIpCount) {
            throw new BusinessException(GeneralCode.USER_REGISTER_IP_EXCEED);
        }
        // 判断当前国家是否在黑名单中
        if (requestBody.getNeedCheckIp().booleanValue() && countryBlacklistBusiness.isBlackIp(ip)) {
            throw new BusinessException(GeneralCode.COUNTRY_NOT_SUPPORT);
        }
        //美国站判断ip所在州或者城市是否支持
//        if(countryBlacklistBusiness.checkIsUsNotSupportedStatesOrCity(ip)){
//            throw new BusinessException(AccountErrorCode.US_IP_FORBIDDEN_DEFAULT_PROMPT);
//        }

        RestrictedCountryVo restrictedCountryVo = iCountry.isIpInRestrictedCountry(ip);
        if (requestBody.getNeedCheckIp().booleanValue() && restrictedCountryVo.isRestricted()) {
            throw new BusinessException(AccountErrorCode.COUNTRY_RESTRICTED);
        }

        // email强制转换成小写
        final String email = requestBody.getEmail().trim().toLowerCase();

        if (!Pattern.matches(REGEX_EMAIL, email) || email.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }


        if (org.apache.commons.lang3.StringUtils.isNoneBlank(requestBody.getSafePassword(), requestBody.getConfirmSafePassword()) && !StringUtils.equals(requestBody.getSafePassword(), requestBody.getConfirmSafePassword())) {
            // 密码错误
            throw new BusinessException(AccountErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }

        User tempUser = this.userMapper.queryByEmail(email);
        if(tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {//用户存在且被删除
        	throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
        }else if (tempUser != null) {
            /*
             * long registerCount = Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class,
             * CacheKeys.DUPLICATE_REGISTER_EMAIL_COUNT, 0L)));
             *
             * if(registerCount <= 3){
             * userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_DUPLICATE_REGISTRATION, tempUser,
             * null, "重复注册发送邮件",null); try { RedisCacheUtils.increment(ip,
             * CacheKeys.DUPLICATE_REGISTER_EMAIL_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期 } catch (Exception e) {
             * log.error("注册重复发送邮件限制", e); } }
             */
            long ipUserIdCount =
                    Long.valueOf(String.valueOf(RedisCacheUtils.get(ip+tempUser.getUserId(), Long.class, AccountConstants.REGISTER_IP_COUNT_USERID, 0L)));
            if(ipUserIdCount>2){
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(GeneralCode.USER_EMAIL_USE);
            }else{
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(AccountErrorCode.ACCOUNT_HAS_BEEN_REGISTERED);
            }
        }
        log.info("register check email done, email:{}", email);

        log.info("register:获取一个用户索引");
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);// 获取一个用户索引
        log.info("register:获取密码加密盐");
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObjectV2(userIndex, requestBody.getPassword(), cipherCode,requestBody.getSafePassword());
        log.info("register:插入用户信息");
        this.userMapper.insert(user);// 插入用户登录信息
        String userEmail = user.getEmail();
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId());
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");// 防钓鱼码
        userSecurity.setSecurityLevel(1);// 安全级别
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(0);
        userSecurity.setWithdrawSecurityAutoStatus(0);
        log.info("register:插入用户安全信息");
        this.userSecurityMapper.insert(userSecurity);// 用户安全信息
        log.info("register:初始化userInfo信息");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setParent(null);
        BigDecimal agentRewardRatio =
                new BigDecimal(this.iSysConfig.selectByDisplayName("agent_reward_ratio").getCode());
        userInfo.setAgentRewardRatio(agentRewardRatio);// 经纪人返佣比例
        userInfo.setTradingAccount(null);// 用户交易账户 激活时创建
        BigDecimal makerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("maker_commission").getCode());
        BigDecimal takerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("taker_commission").getCode());
        BigDecimal buyerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("buyer_commission").getCode());
        BigDecimal sellerCommission =
                new BigDecimal(this.iSysConfig.selectByDisplayName("seller_commission").getCode());
        userInfo.setMakerCommission(makerCommission);// 被动方手续费
        userInfo.setTakerCommission(takerCommission);// 主动方手续费
        userInfo.setBuyerCommission(buyerCommission);// 买方交易手续费
        userInfo.setSellerCommission(sellerCommission);// 卖方交易手续费
        userInfo.setDailyWithdrawCap(null);// 单日最大出金总金额
        userInfo.setDailyWithdrawCountLimit(null);// 单日最大出金次数
        userInfo.setAutoWithdrawAuditThreshold(null);// 免审核额度
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(requestBody.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.NORMAL.name());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
        userInfo.setTradeLevel(0);
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = this.iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }else{
//            userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
            insertToMiningAgentLog(user,String.valueOf(requestBody.getAgentId()),requestBody.getAgentId());
        }
        if (userInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long agentId = Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(agentId);
        }
        // 新返佣设置的返佣code
        if (StringUtils.isNotBlank(requestBody.getAgentRateCode())) {
            UserAgentRate userAgentRate =
                    userAgentRateMapper.selectByAgentCode(requestBody.getAgentRateCode().toUpperCase());
            if (userAgentRate == null || userAgentRate.getUserId() == null || userAgentRate.getReferralRate() == null
                    || !userCommonValidateService.isValidateAgentId(userAgentRate.getUserId())) {
                log.warn("根据code获取userAgentRate失败,AgentRateCode:{},userAgentRate:{}", requestBody.getAgentRateCode(),
                        userAgentRate);
                throw new BusinessException(AccountErrorCode.USER_AGENT_CODE_NOT_EXIST);
            } else {
                //矿池返佣
//                if (userAgentRate.getAgentChannel() != null && userAgentRate.getAgentChannel() == 1){
//                    insertToMiningAgentLog(user,userAgentRate);
//                }
                userInfo.setAgentId(userAgentRate.getUserId());
                userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                insertToAgentLog(user, userAgentRate);
                insertToMiningAgentLog(user,userAgentRate.getAgentCode(),userAgentRate.getUserId());
            }
        } else if(requestBody.getIsFastCreatFuturesAccountProcess() && StringUtils.isNotBlank(requestBody.getFuturesReferalCode()) &&
                Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode()).equals(userInfo.getAgentId())){
            if (StringUtils.isNumeric(requestBody.getFuturesReferalCode())){
                UserInfo agentCodeRootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(Long.parseLong(requestBody.getFuturesReferalCode().trim()));
                if (agentCodeRootUserInfo == null || agentCodeRootUserInfo.getFutureUserId() == null){
                    log.warn("根据futureUserId获取futureUserInfo失败,futuresReferalCode:{}",requestBody.getFuturesReferalCode());
                }else{
                    userInfo.setAgentId(agentCodeRootUserInfo.getUserId());
                    UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(agentCodeRootUserInfo.getUserId());
                    if (userAgentRate == null){
                        log.warn("根据UserId获取selectCheckedShareCodeByUserId失败,userId:{}",agentCodeRootUserInfo.getUserId());
                    }else{
                        userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                    }
                    insertToMiningAgentLog(user,requestBody.getFuturesReferalCode(),userInfo.getAgentId());
                }
            }else{
                FutureUserAgent futureUserAgent = futureUserAgentMapper.selectByAgentCode(requestBody.getFuturesReferalCode());
                if (futureUserAgent == null){
                    log.warn("根据futureAgentcode获取futureUserAgent失败,futuresReferalCode:{}",requestBody.getFuturesReferalCode());
                }else{
                    userInfo.setAgentId(futureUserAgent.getUserId());
                    UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(futureUserAgent.getUserId());
                    if (userAgentRate == null){
                        log.warn("根据futureUserAgent-UserId获取selectCheckedShareCodeByUserId失败,userId:{}",futureUserAgent.getUserId());
                    }else{
                        userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                    }
                    insertToMiningAgentLog(user,requestBody.getFuturesReferalCode(),userInfo.getAgentId());
                }
            }

        }

        log.info("register:插入userInfo信息");
        this.userInfoMapper.insertSelective(userInfo);// 插入用户信息
        String[] sendParams = new String[2];
        try {
            log.info("register:发送激活邮件");
            if(requestBody.getIsNewRegistrationProcess().booleanValue()){
                sendParams = userCommonBusiness.sendActiveCodeForNewProcess(user, request.getTerminal(), requestBody.getCustomEmailLink());
            }else{
                sendParams = userCommonBusiness.sendActiveCode(user, request.getTerminal(), requestBody.getCustomEmailLink());
            }
        } catch (Exception e) {
            log.error(String.format("send register email failed, email:%s, exception:", userEmail), e);
        }
        try {
            RedisCacheUtils.increment(ip, CacheKeys.REGISTER_IP_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("注册ip限制", e);
        }

        try {
            RedisCacheUtils.increment(ip+user.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("注册ip userid限制", e);
        }
        // 记录设备指纹信息
        String locationCity = IP2LocationUtils.getCountryCity(ip);
        String clientType = request.getTerminal().getCode();
        AddUserDeviceResponse deviceResponse = null;
        Map<String, String> deviceInfo = requestBody.getDeviceInfo();
        if (deviceInfo != null) {
            try {
                userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
                deviceResponse = userDeviceBusiness.addDevice(user.getUserId(), clientType, UserDevice.Status.AUTHORIZED,
                        UserDeviceConst.SOURCE_REGIST, deviceInfo);
            } catch (Exception e) {
                log.error("新增设备指纹出错 userId:{}, deviceInfo:{}", user.getUserId(), requestBody.getDeviceInfo(), e);
            }
        }
        // 添加注册日志
        try {
            final UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), ip, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
            if (deviceResponse != null) {
                securityLog.touchDevice(deviceResponse.getId(), deviceResponse.getDeviceId());
            }
            UserIp userIp = new UserIp(user.getUserId(), ip);
            this.userIpMapper.insertIgnore(userIp);
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add register log failed, email:%s, exception:", userEmail), e);
        }

        try {
            if(requestBody.getIsFastCreatFuturesAccountProcess().booleanValue()){
                //记录用户通过快捷开户
                addOrUpdateUserConfig(user.getUserId(), AccountConstants.FAST_CREATE_FUTURE_ACCOUNT,
                        "true");
                //记录用户的期货返佣推荐吗
                if(org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getFuturesReferalCode())){
                    addOrUpdateUserConfig(user.getUserId(), AccountConstants.FUTURES_REFERAL_CODE,
                            requestBody.getFuturesReferalCode());
                }
                log.info("fastCreateFutureAccount done");
            }

        } catch (Exception e) {
            log.error(String.format("add fastCreateFutureAccount config failed, email:%s, exception:", userEmail), e);
        }

        // 添加偏好语言配置
        addPreferLangConfig(user.getUserId());

        log.info("register:注册结束");
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, userEmail);
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", sendParams[0]);
        dataMsg.put("code", sendParams[1]);
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        dataMsg.put("ipAddress", ip);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.REGISTER, dataMsg);
        log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new RegisterUserResponse(user.getUserId(), userEmail, user.getSalt(),
                user.getPassword(), userInfo.getAgentId(), sendParams[0], sendParams[1], null));
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(type = FrontTaskAspect.REGISTERV2, routingKey = FrontPushEventType.REGISTER_COMPLETE_ROUTING,eventType = FrontPushEventType.REGISTER_COMPLETE)
    @RiskTask(email = "#request.body.email",type = RiskTaskAspect.REGISTERV2)
    public APIResponse<RegisterUserResponseV2> registerV2(APIRequest<RegisterUserRequestV2> request) throws Exception {
        final RegisterUserRequestV2 requestBody = request.getBody();
        //1 判断注册方式
        RegisterationMethodEnum registerationMethodEnum=requestBody.getRegisterationMethod();
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            if (StringUtils.isAnyBlank(requestBody.getMobileCode(),requestBody.getMobile())) {
                log.warn("register,mobile or mobileCode blank");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            if(needUpdateIosVersion && requestBody.getMobileCode().equalsIgnoreCase("7")){
                throw new BusinessException(AccountErrorCode.CLIENT_VERSION_IS_TOO_LOW);
            }
            Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(requestBody.getMobileCode());
            if (null == country) {
                log.warn("register,mobileCode invalid");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            if (!RegexUtils.matchMobile(requestBody.getMobileCode(), requestBody.getMobile())) {
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            requestBody.setMobile(requestBody.getMobile().trim());
            requestBody.setMobileCode(country.getCode());
        }

        if(RegisterationMethodEnum.EMAIL==registerationMethodEnum){
            if (StringUtils.isBlank(requestBody.getEmail())) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
            if (!RegexUtils.matchEmail(requestBody.getEmail())) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
        }

        // 同一ip注册限制
        final String ip = WebUtils.getRequestIp();
        long maxIpCount = Long.parseLong(this.iSysConfig.selectByDisplayName("max_register_count").getCode());
        long ipCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class, CacheKeys.REGISTER_IP_COUNT, 0L)));
        if (ipCount >= maxIpCount) {
            throw new BusinessException(GeneralCode.USER_REGISTER_IP_EXCEED);
        }
        // 判断当前国家是否在黑名单中
        if (requestBody.getNeedCheckIp().booleanValue() && countryBlacklistBusiness.isBlackIp(ip)) {
            throw new BusinessException(GeneralCode.COUNTRY_NOT_SUPPORT);
        }


        RestrictedCountryVo restrictedCountryVo = iCountry.isIpInRestrictedCountry(ip);
        if (requestBody.getNeedCheckIp().booleanValue() && restrictedCountryVo.isRestricted()) {
            throw new BusinessException(GeneralCode.COUNTRY_RESTRICTED, restrictedCountryVo.getCountryCode(), null);
        }

        User tempUser = null;
        // email强制转换成小写
        String email = requestBody.getEmail();
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            if (!iCountry.isSupportMobileRegisterCountry(requestBody.getMobileCode())) {
                throw new BusinessException(GeneralCode.COUNTRY_NOT_SUPPORT);
            }
            Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(requestBody.getMobileCode());
            if (null == country) {
                log.warn("register,mobileCode invalid");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            requestBody.setMobileCode(country.getCode());
            email= UserEmailUtils.getMobileUserEmail(requestBody.getMobileCode(),requestBody.getMobile());
            //如果是手机号注册用户，需要检查这个手机号是否被人使用
            // 判断手机是否已经被使用
            UserMobileIndex userMobileIndex =
                    this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());
            // 手机号以0开头的过滤,这是为了限制历史问题，所有0开头的都有问题不能随便放进来
            if (requestBody.getMobile().startsWith("0")) {
                if (requestBody.getMobile().length() > 1) {
                    UserMobileIndex   userMobileIndex1 = this.userMobileIndexMapper.selectByPrimaryKey(
                            requestBody.getMobile().substring(1, requestBody.getMobile().length()),
                            requestBody.getMobileCode());
                    if (userMobileIndex1 != null) {
                        userMobileIndex =userMobileIndex1;
                    }
                } else {
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }

            } else {
                UserMobileIndex   userMobileIndex2 =this.userMobileIndexMapper.selectByPrimaryKey("0" + requestBody.getMobile(),
                        requestBody.getMobileCode());
                if (userMobileIndex2 != null) {
                    userMobileIndex =userMobileIndex2;
                }
            }
            if (userMobileIndex != null) {
                tempUser = this.userMapper.queryById(userMobileIndex.getUserId());
            }
        }
        email = email.trim().toLowerCase();
        if (!Pattern.matches(REGEX_EMAIL, email) || email.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        if (StringUtils.isNoneBlank(requestBody.getSafePassword(), requestBody.getConfirmSafePassword()) && !StringUtils.equals(requestBody.getSafePassword(), requestBody.getConfirmSafePassword())) {
            // 密码错误
            throw new BusinessException(AccountErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }

        tempUser = tempUser != null ? tempUser : this.userMapper.queryByEmail(email);
        if(tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {//用户存在且被删除
            throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
        }else if (tempUser != null) {
            long ipUserIdCount =
                    Long.valueOf(String.valueOf(RedisCacheUtils.get(ip+tempUser.getUserId(), Long.class, AccountConstants.REGISTER_IP_COUNT_USERID, 0L)));
            if(ipUserIdCount>2){
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(GeneralCode.USER_EMAIL_USE);
            }else{
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(AccountErrorCode.ACCOUNT_HAS_BEEN_REGISTERED);
            }
        }

        log.info("register check email done, email:{}", email);

        log.info("register:获取一个用户索引");
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);// 获取一个用户索引
        log.info("register:获取密码加密盐");
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObjectV2(userIndex, requestBody.getPassword(), cipherCode,requestBody.getSafePassword());
        log.info("register:插入用户信息");
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            user.setStatus(BitUtils.enable(user.getStatus(),AccountCommonConstant.USER_NOT_BIND_EMAIL));
            user.setStatus(BitUtils.enable(user.getStatus(),AccountCommonConstant.USER_IS_MOBILE_USER));
            user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_MOBILE));
            // 记录手机索引
            final UserMobileIndex mobileIndex = new UserMobileIndex();
            mobileIndex.setMobile(requestBody.getMobile());
            mobileIndex.setCountry(requestBody.getMobileCode());
            mobileIndex.setUserId(userIndex.getUserId());
            this.userMobileIndexMapper.insert(mobileIndex);
        }
        this.userMapper.insert(user);// 插入用户登录信息
        String userEmail = user.getEmail();
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId());
        log.info("register:插入用户信息");
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");// 防钓鱼码
        userSecurity.setSecurityLevel(1);// 安全级别
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(0);
        userSecurity.setWithdrawSecurityAutoStatus(0);
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            userSecurity.setEmail("");
            userSecurity.setMobileCode(requestBody.getMobileCode());
            userSecurity.setMobile(requestBody.getMobile());
        }
        log.info("register:插入用户安全信息");
        this.userSecurityMapper.insert(userSecurity);// 用户安全信息
        log.info("register:初始化userInfo信息");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setParent(null);
        BigDecimal agentRewardRatio =
                new BigDecimal(this.iSysConfig.selectByDisplayName("agent_reward_ratio").getCode());
        userInfo.setAgentRewardRatio(agentRewardRatio);// 经纪人返佣比例
        userInfo.setTradingAccount(null);// 用户交易账户 激活时创建
        BigDecimal makerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("maker_commission").getCode());
        BigDecimal takerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("taker_commission").getCode());
        BigDecimal buyerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("buyer_commission").getCode());
        BigDecimal sellerCommission =
                new BigDecimal(this.iSysConfig.selectByDisplayName("seller_commission").getCode());
        userInfo.setMakerCommission(makerCommission);// 被动方手续费
        userInfo.setTakerCommission(takerCommission);// 主动方手续费
        userInfo.setBuyerCommission(buyerCommission);// 买方交易手续费
        userInfo.setSellerCommission(sellerCommission);// 卖方交易手续费
        userInfo.setDailyWithdrawCap(null);// 单日最大出金总金额
        userInfo.setDailyWithdrawCountLimit(null);// 单日最大出金次数
        userInfo.setAutoWithdrawAuditThreshold(null);// 免审核额度
        userInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.NORMAL.name());
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(requestBody.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
        userInfo.setTradeLevel(0);
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = this.iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }else{
//            userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
              insertToMiningAgentLog(user,String.valueOf(requestBody.getAgentId()),requestBody.getAgentId());
        }
        if (userInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long agentId = Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(agentId);
        }

        //oauth返佣
        if(StringUtils.isNotBlank(requestBody.getTrackSource()) && OAUTH_TRACKSOURCE.contains(requestBody.getTrackSource())
                && StringUtils.isNotBlank(requestBody.getAgentRateCode())){
            OauthAgentRelation oauthAgentRelation = oauthAgentRelationMapper.selectByOauthCode(requestBody.getAgentRateCode());
            if (oauthAgentRelation == null){
                log.warn("根据agentCode获取oauthAgentRelation失败,AgentRateCode:{}", requestBody.getAgentRateCode());
            }else{
                UserAgentRate userAgentRate = userAgentRateMapper.selectByAgentCode(oauthAgentRelation.getAgentCode().toUpperCase());
                if (userAgentRate != null && userAgentRate.getUserId() != null && userAgentRate.getReferralRate() != null
                        && userCommonValidateService.isValidateAgentId(userAgentRate.getUserId())){
                    userInfo.setAgentId(userAgentRate.getUserId());
                    userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                    insertToAgentLog(user, userAgentRate);
                    insertToMiningAgentLog(user,userAgentRate.getAgentCode(),userAgentRate.getUserId());
                }else{
                    log.warn("根据code获取userAgentRate失败,AgentRateCode:{},userAgentRate:{}", oauthAgentRelation.getAgentCode(),
                            userAgentRate);
                }
            }
        }else if (StringUtils.isNotBlank(requestBody.getAgentRateCode())) {
            UserAgentRate userAgentRate =
                    userAgentRateMapper.selectByAgentCode(requestBody.getAgentRateCode().toUpperCase());
            if (userAgentRate == null || userAgentRate.getUserId() == null || userAgentRate.getReferralRate() == null
                    || !userCommonValidateService.isValidateAgentId(userAgentRate.getUserId())) {
                log.warn("根据code获取userAgentRate失败,AgentRateCode:{},userAgentRate:{}", requestBody.getAgentRateCode(),
                        userAgentRate);
                throw new BusinessException(AccountErrorCode.USER_AGENT_CODE_NOT_EXIST);
            } else {

                userInfo.setAgentId(userAgentRate.getUserId());
                userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                insertToAgentLog(user, userAgentRate);
                //矿池返佣
//                if (userAgentRate.getAgentChannel() != null && userAgentRate.getAgentChannel() == 1){
                    insertToMiningAgentLog(user,userAgentRate.getAgentCode(),userAgentRate.getUserId());
//                }
            }
        }else if(requestBody.getIsFastCreatFuturesAccountProcess() && StringUtils.isNotBlank(requestBody.getFuturesReferalCode()) &&
                Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode()).equals(userInfo.getAgentId())){
            if (StringUtils.isNumeric(requestBody.getFuturesReferalCode())){
                UserInfo agentCodeRootUserInfo = userInfoMapper.selectRootUserInfoByFutureUserId(Long.parseLong(requestBody.getFuturesReferalCode().trim()));
                if (agentCodeRootUserInfo == null || agentCodeRootUserInfo.getFutureUserId() == null){
                    log.warn("根据futureUserId获取futureUserInfo失败,futuresReferalCode:{}",requestBody.getFuturesReferalCode());
                }else{
                    userInfo.setAgentId(agentCodeRootUserInfo.getUserId());
                    UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(agentCodeRootUserInfo.getUserId());
                    if (userAgentRate == null){
                        log.warn("根据UserId获取selectCheckedShareCodeByUserId失败,userId:{}",agentCodeRootUserInfo.getUserId());
                    }else{
                        userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                    }
                    insertToMiningAgentLog(user,requestBody.getFuturesReferalCode(),userInfo.getAgentId());
                }
            }else{
                FutureUserAgent futureUserAgent = futureUserAgentMapper.selectByAgentCode(requestBody.getFuturesReferalCode());
                if (futureUserAgent == null){
                    log.warn("根据futureAgentcode获取futureUserAgent失败,futuresReferalCode:{}",requestBody.getFuturesReferalCode());
                }else{
                    userInfo.setAgentId(futureUserAgent.getUserId());
                    UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(futureUserAgent.getUserId());
                    if (userAgentRate == null){
                        log.warn("根据futureUserAgent-UserId获取selectCheckedShareCodeByUserId失败,userId:{}",futureUserAgent.getUserId());
                    }else{
                        userInfo.setReferralRewardRatio(userAgentRate.getReferralRate());
                    }
                    insertToMiningAgentLog(user,requestBody.getFuturesReferalCode(),userInfo.getAgentId());
                }
            }

        }
        log.info("register:插入userInfo信息");
        this.userInfoMapper.insertSelective(userInfo);// 插入用户信息
        String[] sendParams = new String[2];

        try {
            RedisCacheUtils.increment(ip, CacheKeys.REGISTER_IP_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("注册ip限制", e);
        }
        // 记录设备指纹信息
        String locationCity = IP2LocationUtils.getCountryCity(ip);
        String clientType = request.getTerminal().getCode();
        AddUserDeviceResponse deviceResponse = null;
        Map<String, String> deviceInfo = requestBody.getDeviceInfo();
        if (deviceInfo != null) {
            try {
                userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
                deviceResponse = userDeviceBusiness.addDevice(user.getUserId(), clientType, UserDevice.Status.AUTHORIZED,
                        UserDeviceConst.SOURCE_REGIST, deviceInfo);
            } catch (Exception e) {
                log.error("新增设备指纹出错 userId:{}, deviceInfo:{}", user.getUserId(), requestBody.getDeviceInfo(), e);
            }
        }
        // 添加注册日志
        try {
            final UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), ip, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
            if (deviceResponse != null) {
                securityLog.touchDevice(deviceResponse.getId(), deviceResponse.getDeviceId());
            }
            UserIp userIp = new UserIp(user.getUserId(), ip);
            this.userIpMapper.insertIgnore(userIp);
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add register log failed, email:%s, exception:", userEmail), e);
        }

        try {
            if(requestBody.getIsFastCreatFuturesAccountProcess().booleanValue()){
                //记录用户通过快捷开户
                addOrUpdateUserConfig(user.getUserId(), AccountConstants.FAST_CREATE_FUTURE_ACCOUNT,
                        "true");
                //记录用户的期货返佣推荐吗
                if(org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getFuturesReferalCode())){
                    addOrUpdateUserConfig(user.getUserId(), AccountConstants.FUTURES_REFERAL_CODE,
                            requestBody.getFuturesReferalCode());
                }
                log.info("fastCreateFutureAccount done");
            }

        } catch (Exception e) {
            log.error(String.format("add fastCreateFutureAccount config failed, email:%s, exception:", userEmail), e);
        }


        log.info("register:注册结束");
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, userEmail);
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", sendParams[0]);
        dataMsg.put("code", sendParams[1]);
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        dataMsg.put("ipAddress", ip);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.REGISTER, dataMsg);
        log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new RegisterUserResponseV2(user.getUserId(), userEmail, user.getSalt(),
                user.getPassword(), userInfo.getAgentId(), sendParams[0], sendParams[1], null));
    }

    private void insertToMiningAgentLog(User user, String agentCode, Long agentUserId) {
        MiningAgentLog userAgentLog = new MiningAgentLog();
        userAgentLog.setAgentCode(agentCode);
        userAgentLog.setUserId(agentUserId);
        userAgentLog.setReferralUser(user.getUserId());
        userAgentLog.setReferralEmail(user.getEmail());
        miningAgentLogMapper.insertSelective(userAgentLog);
    }


    private void insertToAgentLog(User user, UserAgentRate userAgentRate) {
        UserAgentLog userAgentLog = new UserAgentLog();
        userAgentLog.setAgentCode(userAgentRate.getAgentCode());
        userAgentLog.setUserId(userAgentRate.getUserId());
        userAgentLog.setReferralUser(user.getUserId());
        userAgentLog.setReferralEmail(user.getEmail());
        userAgentLog.setUserType(com.binance.account.constants.enums.UserTypeEnum.getAccountType(user.getStatus()));
        userAgentLogMapper.insertSelective(userAgentLog);
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
            userAgentLog.setUserType(com.binance.account.constants.enums.UserTypeEnum.getAccountType(user.getStatus()));
        }
        userAgentLogMapper.insertSelective(userAgentLog);
    }

    private int fakeEmailSimulatePswError(final String fakeEmail) {
        int loginFailedNum = 0;
        Integer fakeEmailLoginTimes = RedisCacheUtils.get(fakeEmail.trim(), Integer.class, FAKE_EMAIL_LOGIN_KEY);
        if (null != fakeEmailLoginTimes) {
            loginFailedNum = fakeEmailLoginTimes;
        }
        RedisCacheUtils.set(fakeEmail.trim(), (loginFailedNum + 1), 2 * 60 * 60L, FAKE_EMAIL_LOGIN_KEY);

        Integer maxErrorCount = Integer.valueOf(this.iSysConfig.selectByDisplayName("login_error_limit").getCode());
        return maxErrorCount - loginFailedNum;
    }

    private void passwordErrorHandler(int count, Long userId, String fakeEmail) {
        if (count <= 0) {

            // FakeEmail模拟锁定2小时
            if (StringUtils.isNotBlank(fakeEmail)) {
                RedisCacheUtils.set(fakeEmail.trim(), fakeEmail, 2 * 60 * 60L, FAKE_EMAIL_LOGIN_LOCKED_KEY);
            }

            // 已经锁定
            throw new BusinessException(GeneralCode.USER_LOCK, new Object[] {USER_LOCK_TIME});
        } else if (count <= 2) {
            // 密码错误并加上错误次数机会
            throw new BusinessException(AccountErrorCode.USER_PWD_ERROR, new Object[] {count});
        } else {
            // 密码错误
            throw new BusinessException(AccountErrorCode.USER_PWD_ERROR, new Object[] {count});
        }
    }
    @MarginValidate(email = "#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    @Monitored
    public APIResponse<LoginUserResponse> login(APIRequest<LoginUserRequest> request) throws Exception {
        final LoginUserRequest requestBody = request.getBody();
        User user = this.userMapper.queryByEmail(requestBody.getEmail());

        final String email = requestBody.getEmail();

        // 邮箱未注册、已经被删除，须模拟真实邮箱登陆，即：超过限制锁定2小时、倒计错误次数(迷惑黑客)
        if (user == null || BitUtils.isTrue(user.getStatus(), Constant.USER_DELETE)) {

            String fakeEmailLoginLocked = RedisCacheUtils.get(email.trim(), String.class, FAKE_EMAIL_LOGIN_LOCKED_KEY);
            if (StringUtils.isNotBlank(fakeEmailLoginLocked)) {
                // 已经锁定
                throw new BusinessException(GeneralCode.USER_LOCK, null, new Object[] {USER_LOCK_TIME});
            }

            int count = fakeEmailSimulatePswError(email);
            log.warn("login fakeUserEmail:{}, ip:{}, count:{}", email, WebUtils.getRequestIp(), count);

            passwordErrorHandler(count, null, email);
        }

        UserCommonPermission userCommonPermission= userPermission.getUserPermissionByUserStatus(user.getStatus());
        if(null!=userCommonPermission && !userCommonPermission.getEnableLogin()){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        if (BitUtils.isTrue(user.getStatus(), AccountCommonConstant.USER_IS_MOBILE_USER)) {
            log.info("禁止手机号用户在老版本登录");
            throw new BusinessException(AccountErrorCode.MOBILE_USER_IS_NOT_SUPPORT_FOR_LOGIN);
        }
        boolean isOldAppVersionFlag=userDeviceBusiness.isOldAppVersion(request.getTerminal().getCode(),request.getVersion());

        if (isOldAppVersionFlag) {
            log.info("isOldAppVersion");
            throw new BusinessException(AccountErrorCode.CLIENT_VERSION_IS_TOO_LOW);
        }

        // 二次验证类型
        AuthTypeEnum authType = null;
        boolean need2FA = false;
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_MOBILE)
                || BitUtils.isTrue(user.getStatus(), Constant.USER_GOOGLE)) {
            need2FA = true;
            authType = requestBody.getAuthType();
        }
        //用户绑定了security key并开启了登录验证
        if (userSecurityBusiness.isYubikeyEnabledInSpecifiedScenario(user.getUserId(), SecurityKeyApplicationScenario.login)) {
            //绑定并开启security key就不可以使用其他2fa。
            if (requestBody.getAuthType() != null && requestBody.getAuthType() != AuthTypeEnum.FIDO2) {
                throw new BusinessException(AccountErrorCode.MUST_USE_YUBIKEY_TO_AUTHENTICATE);
            }
            need2FA = true;
            authType = AuthTypeEnum.FIDO2;
            boolean isWebCLient=TerminalEnum.WEB.getCode().equalsIgnoreCase(request.getTerminal().getCode());
            if(!isWebCLient){
                log.info("forbidden app yubikey login");
                throw new BusinessException(AccountErrorCode.YUBIKEY_USER_IS_SUPPORT_FOR_LOGIN_ON_BROWSER);
            }
        }

        // 将redis的cache_user_password_error_num缓存清除
        RedisCacheUtils.del(String.format(CACHE_USER_PASSWORD_ERROR_NUM, user.getUserId()));

        log.info("Auth start");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        AuthStatusEnum authStatus = AuthStatusEnum.NOT_NEED_AUTH;
        if (!requestBody.getIsAuth()) {
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOCK) && !this.unlockUser(user)) {
                // 已经锁定
                throw new BusinessException(GeneralCode.USER_LOCK, user.getUserId(), new Object[] {USER_LOCK_TIME});
            }
            if (need2FA) {
                // 需要验证 但是还没有验证
                authStatus = AuthStatusEnum.NO_AUTH;
            }
            String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
            String confirmPassword = PasswordUtils.encode(requestBody.getPassword(), user.getSalt(), cipherCode);

            //这个flag用来判断密码check是否通过
            boolean passwordCheckPassFlag=false;
            boolean sendOldPasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getPassword());
            boolean sendSafePasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getSafePassword());
            boolean dbExistSafePasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(user.getSafePassword());


            if(forceCheckOldPasswordSwitch){
                log.info("forceCheckOldSwitch true");
                // 强制check老密码开关，这个开关的作用是为了防止代码有问题可以快速回滚
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                }
            }else if(sendSafePasswordFlag && dbExistSafePasswordFlag){
                log.info("check  safe");
                //新算法的密码check，只有用户已经有新密码并且前端也传了新密码才check，反正跳过
                String confirmSafePassword= CryptoAlgoUtils.validateAndHash512(requestBody.getSafePassword(),user.getSalt());
                if (StringUtils.equals(confirmSafePassword, user.getSafePassword())) {
                    passwordCheckPassFlag=true;
                }
            }else if(sendOldPasswordFlag && sendSafePasswordFlag && !dbExistSafePasswordFlag){
                log.info("check and update safe");
                // 新老密码都传了，但是库里没有新密码，这个时候走刷密码流程（验证老密码，刷新新密码）
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                    //并且库里面没有刷新过密码，那么刷新新密码
                    User record = new User();
                    record.setEmail(user.getEmail());
                    record.setSafePassword(CryptoAlgoUtils.validateAndHash512(requestBody.getSafePassword(),user.getSalt()));
                    this.userMapper.updateByEmail(record);
                }
            }else if(sendOldPasswordFlag && !sendSafePasswordFlag ){
                log.info("check old");
                // 老密码传了，但是新密码没传。。那么肯定是老版本客户端，那么只验证老密码
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                }else{
                    //通过新版本的web端刷新了密码，但是还是用的老版本的app会出问题，所以必须去升级
                    if(dbExistSafePasswordFlag){
                        log.info("check old and updateApp");
                        throw new BusinessException(AccountErrorCode.CLIENT_VERSION_IS_TOO_LOW);
                    }
                }
            }else{
                throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
            }


            //密码验证不通过那么走相应的error logic
            if (!passwordCheckPassFlag) {
                // 密码错误
                int count = this.passWordError(user);
                passwordErrorHandler(count, user.getUserId(), StringUtils.EMPTY);
            }


            if (BitUtils.isFalse(user.getStatus(), Constant.USER_ACTIVE)) {
                // 未激活
                throw new BusinessException(GeneralCode.USER_NOT_ACTIVE, user.getUserId(), null);
            }
            RedisCacheUtils.del(user.getUserId().toString(), AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
            log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY del:userId={}",user.getUserId());
        } else {
            this.userSecurityBusiness.verificationsTwoV2(user.getUserId(), authType, requestBody.getCode(), SecurityKeyApplicationScenario.login, true);
            authStatus = AuthStatusEnum.OK;
            RedisCacheUtils.set(user.getUserId().toString(), 1, -1, AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
            log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY set:userId={}",user.getUserId());
        }

        stopWatch.stop();
        log.info("auth end, elapsedTime:{} secs", stopWatch.getTotalTimeSeconds());

        //检查用户是否在登录白名单,如果是白名单，则跳过设备授权和ip验证
        boolean inWhiteList = commonConfig.isInLoginUserIdWhiteList(user.getUserId());
        log.info("userId={} {} white list", user.getUserId(), inWhiteList ? "in":"NOT in");
        LoginUserResponse resp = new LoginUserResponse(user.getUserId(), user.getEmail(), user.getStatus(), authStatus);
        resp.setUseSecurityKey(authType == AuthTypeEnum.FIDO2);
        // 记录安全日志
        if (AuthStatusEnum.NOT_NEED_AUTH == authStatus || AuthStatusEnum.OK == authStatus) {
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOGIN)) {
                // 已经被禁用登录
                throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN, user.getUserId(), null);
            } else if (BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
                // 已经被禁用
                throw new BusinessException(GeneralCode.USER_DISABLED, user.getUserId(), null);
            }
            // 清除登录错误次数
            AsyncTaskExecutor.execute(() -> {
                UserSecurity errorPwdCountClear = new UserSecurity();
                errorPwdCountClear.setLoginFailedNum(0);
                errorPwdCountClear.setUserId(user.getUserId());
                this.userSecurityMapper.updateByPrimaryKeySelective(errorPwdCountClear);
            });

            String clientIp = WebUtils.getRequestIp();
            stopWatch.start();
            String locationCity = IP2LocationUtils.getCountryCity(clientIp);
            stopWatch.stop();
            log.info("IP2LocationUtils.getCountryCity end, elapsedTime:{} secs", stopWatch.getTotalTimeSeconds());
            // 校验设备指纹
            String clientType = request.getTerminal().getCode();
            String deviceName = null;
            UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), clientIp, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_LOGIN, "登录");

            HashMap<String, String> deviceInfo = requestBody.getDeviceInfo();
            String relatedDeviceIds = deviceInfo != null ? deviceInfo.get(UserDeviceConst.RELATED_DEVICE_IDS) : null;
            userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
            boolean hasDeviceChecked;
            // 设备指纹不为空，且属性数足够，才做校验
            boolean canCheckDevice =
                    (deviceInfo != null && deviceInfo.size() >= userDeviceBusiness.getMinPropertyCount(clientType));
            if (userDeviceBusiness.checkVersion(clientType, request.getVersion())) {
                if (!canCheckDevice) {
                    hasDeviceChecked = false;
                } else {
                    CheckUserDeviceResponse ckRs =
                            userDeviceBusiness.checkDevice(user.getUserId(), clientType, deviceInfo);
                    if (ckRs.isValid() || inWhiteList) {
                        resp.setCurrentDeviceId(ckRs.getDeviceId());
                        securityLog.touchDevice(ckRs.getId(), ckRs.getDeviceId());
                        deviceName = deviceInfo.get(UserDevice.DEVICE_NAME);
                        // 记录关联设备的信息
                        AsyncTaskExecutor.execute(() -> userDeviceBusiness.updateRelatedDevice(user.getUserId(),
                                ckRs.getId(), relatedDeviceIds));
                    } else {
                        //通过Notification发送通知
                    	securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.DEVICE_AUTH, request.getLanguage());
                    	// 发送授权邮件
                        if(requestBody.getIsNewLoginProcess().booleanValue()){
                            userDeviceBusiness.sendAuthEmailForNewProcess(user, clientType, deviceInfo,
                                    requestBody.getCustomDeviceAuthorizeUrl(), requestBody.getCustomForbiddenLink(),
                                    requestBody.getCallback());
                        }else{
                            userDeviceBusiness.sendAuthEmail(user, clientType, deviceInfo,
                                    requestBody.getCustomDeviceAuthorizeUrl(), requestBody.getCustomForbiddenLink(),
                                    requestBody.getCallback());
                        }

                        throw new BusinessException(GeneralCode.USER_DEVICE_UNAUTHORIZED);
                    }
                    //如果是黑设备登录，则把user id加入提币黑名单。
                    asyncAddUserIntoWithdrawBlackListIfNecessary(ckRs.getId(), deviceInfo, clientType, user);
                    hasDeviceChecked = true;
                }
            } else {
                hasDeviceChecked = false;
                // 直接记录设备信息
                log.warn("客户端版本过低，忽略设备信息: {}, {}", user.getUserId(), user.getEmail());
            }
            // 设备指纹兜底逻辑
            if (!hasDeviceChecked && !inWhiteList) {
                this.iUserIpChange.sensitiveIpCheck(user, clientIp, authType,
                        requestBody.getCustomIpChangeConfirmLink(), requestBody.getCustomForbiddenLink(),
                        userDeviceBusiness.isStrictMode(clientType));
            }

            // 生成认证令牌
            String otherCipher = RedisCacheUtils.get(CacheKeys.OTHER_CIPHER, DEFAULT_RESULT, true);
            // 认证令牌
            resp.setToken(
                    TokenUtils.createJWT(JSON.toJSONString(resp), DateUtils.getNewDateAddMinute(120), otherCipher));
            // 2小时内多次换ip登录
            SysConfig sysConfig = this.iSysConfig.selectByDisplayName("login_forbidden_ip_count");
            if (sysConfig != null) {
                String ips = RedisCacheUtils.get(user.getUserId().toString(), String.class, CacheKeys.USER_LOGIN_IP);
                String separatorChar = "@";
                if (StringUtils.isBlank(ips)) {
                    ips = clientIp;
                } else {
                    if (StringUtils.indexOf(ips, clientIp) < 0) {
                        ips = ips + separatorChar + clientIp;
                    }
                }
                String[] loginIps = StringUtils.split(ips, separatorChar);
                int loginIpsCount = loginIps != null ? loginIps.length : 0;
                int loginForbiddenIpCount = Integer.parseInt(sysConfig.getCode());
                // 账号频繁登录，请两小时后再试。
                if (loginIpsCount >= loginForbiddenIpCount && !inWhiteList) {
                    throw new BusinessException(GeneralCode.USER_LOGIN_FREQUENTLY, user.getUserId(), null);
                }
                // 有效期2小时
                RedisCacheUtils.set(user.getUserId().toString(), ips, 2 * 60 * 60, CacheKeys.USER_LOGIN_IP);
            } else {
                log.warn("没有配置  login_forbidden_ip_count");
            }

            this.userSecurityLogMapper.insertSelective(securityLog);

            resp.setLogId(securityLog.getId());
            // ip发生变化，发送登录成功邮件
            if (!iUserIpChange.isHistoryIp(user.getUserId(), clientIp) && !inWhiteList) {
                this.userIpMapper.insert(new UserIp(user.getUserId(), clientIp));

                Map<String, Object> data = new HashMap<>();
                data.put("currentIp", clientIp);
                data.put("browser",
                        StringUtils.isNotBlank(deviceName) ? deviceName : WebUtils.getHeader(Constant.BASE_BROWER));
                String inboxDeviceName = StringUtils.isNotBlank(deviceName) ? deviceName : WebUtils.getHeader(Constant.BASE_BROWER);
                data.put("deviceName", StringUtils.isBlank(inboxDeviceName)?"unknown":inboxDeviceName);
                String disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_EMAIL_IP, user, data,
                        "登录发送ip变更邮件：", requestBody.getCustomForbiddenLink());
                String lang= WebUtils.getAPIRequestHeader().getLanguage().getLang();
                String terminalCode = request.getTerminal()==null?"web":request.getTerminal().getCode();
                APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(user.getUserId(), data, lang, terminalCode,"LOGIN_IP_CHANGE");
                AsyncTaskExecutor.execute(() -> {
                    try {
                        inboxMessageTextApi.pushInbox(apiRequest);
                    }catch (Exception e){
                        log.warn("send inbox ip update error",e);
                    }
                });
                // 如果是中国用户，发送短信
                List<Long> userIds = new ArrayList<>();
                userIds.add(user.getUserId());
                List<UserSecurity> userSecuritys = userSecurityMapper.selectUserSecurityByUserIds(userIds);
                // this.msgApi.sendMsg(request)
                if (CollectionUtils.isNotEmpty(userSecuritys)) {
                    UserSecurity userSecurity = userSecuritys.get(0);
                    try {
                        if (("cn".equals(userSecurity.getMobileCode()) || "CN".equals(userSecurity.getMobileCode()))
                                && StringUtils.isNotEmpty(userSecurity.getMobile())) {
                            SendMsgRequest requestSms = new SendMsgRequest();
                            requestSms.setIp(clientIp);
                            requestSms.setMobileCode("+86");
                            requestSms.setRecipient(userSecurity.getMobile());
                            requestSms.setUserId(userSecurity.getUserId().toString());
                            requestSms.setTplCode(Constant.NODE_TYPE_SMS_IP);

                            requestSms.setNeedIpCheck(false);
                            requestSms.setNeedSendTimesCheck(false);

                            Map<String, Object> params = new HashMap<>();
                            params.put("currentIp", clientIp);
                            params.put("email", getHideEmail(user.getEmail()));
                            params.put("time",
                                    DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
                            requestSms.setData(params);
                            // 发送短信
                            userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                                    WebUtils.getAPIRequestHeader().getTerminal());

                        }
                    } catch (Exception e) {
                        log.warn("exception:{}", e);
                    }
                }


                resp.setIpLocation(securityLog.getIpLocation());
                resp.setDisableToken(disableToken);
            }
        }
        // 记录登录语言
        final String userLastLoginLanguage= WebUtils.getHeader(Constant.LANG);
        if(null==userLastLoginLanguage|| org.apache.commons.lang3.StringUtils.isBlank(userLastLoginLanguage)){
            addOrUpdateUserConfig(user.getUserId(), "userLastLoginLanguage",
                    LanguageEnum.EN_US.getLang());
        }else{
            addOrUpdateUserConfig(user.getUserId(), "userLastLoginLanguage",
                    userLastLoginLanguage);
        }
        log.info("userLastLoginLanguage={}",userLastLoginLanguage);
        AsyncTaskExecutor.execute(() -> {
            try {
                if(org.apache.commons.lang3.StringUtils.isNotBlank(userLastLoginLanguage)){
                    userLanguageApiClient.saveOrUpdate(user.getUserId().toString(),userLastLoginLanguage);
                }else{
                    userLanguageApiClient.saveOrUpdate(user.getUserId().toString(),LanguageEnum.EN_US.getLang());
                }
            }catch (Exception e){
                log.warn("send language to messagee error",e);
            }
        });

        //login success
        RedisCacheUtils.del(user.getUserId().toString(), AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
        log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY login del:userId={}",user.getUserId());
        return APIResponse.getOKJsonResult(resp);
    }

    @MarginValidate(email = "#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    @Monitored
    public APIResponse<LoginUserResponseV2> loginV2(APIRequest<LoginUserRequestV2> request) throws Exception {
        final LoginUserRequestV2 requestBody = request.getBody();
        User user = this.userMapper.queryByEmail(requestBody.getEmail());

        final String email = requestBody.getEmail();
        // 邮箱未注册、已经被删除，须模拟真实邮箱登陆，即：超过限制锁定2小时、倒计错误次数(迷惑黑客)
        if (user == null || BitUtils.isTrue(user.getStatus(), Constant.USER_DELETE)) {

            String fakeEmailLoginLocked = RedisCacheUtils.get(email.trim(), String.class, FAKE_EMAIL_LOGIN_LOCKED_KEY);
            if (StringUtils.isNotBlank(fakeEmailLoginLocked)) {
                // 已经锁定
                throw new BusinessException(GeneralCode.USER_LOCK, new Object[] {USER_LOCK_TIME});
            }

            int count = fakeEmailSimulatePswError(email);
            log.warn("login fakeUserEmail:{}, ip:{}, count:{}", email, WebUtils.getRequestIp(), count);

            passwordErrorHandler(count, null, email);
        }
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());

        UserCommonPermission userCommonPermission= userPermission.getUserPermissionByUserStatus(user.getStatus());
        if(null!=userCommonPermission && !userCommonPermission.getEnableLogin()){
            log.info("userCommonPermission validate fail, userType={}", userCommonPermission.getUserType());
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 二次验证类型
        boolean need2FA = true;
        boolean bindMobile = BitUtils.isTrue(user.getStatus(), Constant.USER_MOBILE);
        boolean bindEmail = BitUtils.isFalse(user.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL);
        boolean bindGoogle = BitUtils.isTrue(user.getStatus(), Constant.USER_GOOGLE);
        boolean bindYubikey = userSecurityBusiness.isYubikeyEnabledInSpecifiedScenario(user.getUserId(), SecurityKeyApplicationScenario.login);
        boolean onlyBindEmail = bindEmail && !bindMobile && !bindGoogle && !bindYubikey ;
        boolean onlyBindMobile = bindMobile && !bindEmail && !bindGoogle && !bindYubikey ;
        boolean onlyBindMobileOrEmail = onlyBindMobile || onlyBindEmail;
        if(onlyBindMobileOrEmail){
            need2FA=false;
        }
        // 将redis的cache_user_password_error_num缓存清除
        RedisCacheUtils.del(String.format(CACHE_USER_PASSWORD_ERROR_NUM, user.getUserId()));

        log.info("Auth start");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        AuthStatusEnum authStatus = AuthStatusEnum.NOT_NEED_AUTH;
        if (!requestBody.getIsAuth()) {
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOCK) && !this.unlockUser(user)) {
                // 已经锁定
                throw new BusinessException(GeneralCode.USER_LOCK, new Object[] {USER_LOCK_TIME});
            }
            //如果用户就绑定了邮箱或者手机的其中一个那么可以跳过2次验证
            if (need2FA) {
                // 需要验证 但是还没有验证
                authStatus = AuthStatusEnum.NO_AUTH;
            }
            String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
            String confirmPassword = PasswordUtils.encode(requestBody.getPassword(), user.getSalt(), cipherCode);
            //这个flag用来判断密码check是否通过
            boolean passwordCheckPassFlag=false;
            boolean sendOldPasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getPassword());
            boolean sendSafePasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getSafePassword());
            boolean dbExistSafePasswordFlag=org.apache.commons.lang3.StringUtils.isNotBlank(user.getSafePassword());


            if(forceCheckOldPasswordSwitch){
                log.info("forceCheckOldSwitch true");
                // 强制check老密码开关，这个开关的作用是为了防止代码有问题可以快速回滚
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                }
            }else if(sendSafePasswordFlag && dbExistSafePasswordFlag){
                log.info("check  safe");
                //新算法的密码check，只有用户已经有新密码并且前端也传了新密码才check，反正跳过
                String confirmSafePassword= CryptoAlgoUtils.validateAndHash512(requestBody.getSafePassword(),user.getSalt());
                if (StringUtils.equals(confirmSafePassword, user.getSafePassword())) {
                    passwordCheckPassFlag=true;
                }
            }else if(sendOldPasswordFlag && sendSafePasswordFlag && !dbExistSafePasswordFlag){
                log.info("check and update safe");
                // 新老密码都传了，但是库里没有新密码，这个时候走刷密码流程（验证老密码，刷新新密码）
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                    //并且库里面没有刷新过密码，那么刷新新密码
                    User record = new User();
                    record.setEmail(user.getEmail());
                    record.setSafePassword(CryptoAlgoUtils.validateAndHash512(requestBody.getSafePassword(),user.getSalt()));
                    this.userMapper.updateByEmail(record);
                }else if(StringUtils.isNoneBlank(requestBody.getMobile(),requestBody.getMobileCode())){
                    //密码不对并且是手机号登录，让用户去授权
                    throw new BusinessException(AccountErrorCode.PLEASE_AUTHORIZE_MOBILE_LOGIN);

                }
            }else if(sendOldPasswordFlag && !sendSafePasswordFlag ){
                log.info("check old");
                // 老密码传了，但是新密码没传。。那么肯定是老版本客户端，那么只验证老密码
                if (StringUtils.equals(confirmPassword, user.getPassword())) {
                    // 密码通过的话
                    passwordCheckPassFlag=true;
                }else{
                    //通过新版本的web端刷新了密码，但是还是用的老版本的app会出问题，所以必须去升级
                    if(dbExistSafePasswordFlag){
                        log.info("check old and updateApp");
                        throw new BusinessException(AccountErrorCode.CLIENT_VERSION_IS_TOO_LOW);
                    }
                }
            }else{
                throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
            }


            //密码验证不通过那么走相应的error logic
            if (!passwordCheckPassFlag) {
                // 密码错误
                int count = this.passWordError(user);
                passwordErrorHandler(count, user.getUserId(), StringUtils.EMPTY);
            }
            if (BitUtils.isFalse(user.getStatus(), Constant.USER_ACTIVE)) {
                // 未激活
                throw new BusinessException(GeneralCode.USER_NOT_ACTIVE, user.getUserId(), null);
            }
            RedisCacheUtils.del(user.getUserId().toString(), AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
            log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY del:userId={}",user.getUserId());
        } else {
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOGIN)) {
                // 已经被禁用登录
                throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN, user.getUserId(), null);
            } else if (BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
                // 已经被禁用
                throw new BusinessException(GeneralCode.USER_DISABLED, user.getUserId(), null);
            }
            MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                    .userId(user.getUserId())
                    .bizScene(BizSceneEnum.LOGIN)
                    .emailVerifyCode(requestBody.getEmailVerifyCode())
                    .googleVerifyCode(requestBody.getGoogleVerifyCode())
                    .mobileVerifyCode(requestBody.getMobileVerifyCode())
                    .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                    .build();
            this.userSecurityBusiness.verifyMultiFactors(verify);
            authStatus = AuthStatusEnum.OK;
            RedisCacheUtils.set(user.getUserId().toString(), 1, -1, AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
            log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY set:userId={}",user.getUserId());


        }

        stopWatch.stop();
        log.info("auth end, elapsedTime:{} secs", stopWatch.getTotalTimeSeconds());

        //检查用户是否在登录白名单,如果是白名单，则跳过设备授权和ip验证
        boolean inWhiteList = commonConfig.isInLoginUserIdWhiteList(user.getUserId());
        log.info("userId={} {} white list", user.getUserId(), inWhiteList ? "in":"NOT in");
        LoginUserResponseV2 resp = new LoginUserResponseV2(user.getUserId(), user.getEmail(), user.getStatus(), authStatus);
        //用户绑定了security key并开启了登录验证
        if (userSecurityBusiness.isYubikeyEnabledInSpecifiedScenario(user.getUserId(), SecurityKeyApplicationScenario.login)) {
            resp.setUseSecurityKey(true);
            RedisCacheUtils.set(user.getUserId().toString(), 1, -1, AccountConstants.ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY);
            log.info("ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY set:userId={}",user.getUserId());
            boolean isWebCLient=TerminalEnum.WEB.getCode().equalsIgnoreCase(request.getTerminal().getCode());
            if(!isWebCLient){
                log.info("forbidden app yubikey login");
                throw new BusinessException(AccountErrorCode.YUBIKEY_USER_IS_SUPPORT_FOR_LOGIN_ON_BROWSER);
            }
            if(org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getYubikeyVerifyCode())&& AuthStatusEnum.OK==authStatus){
                RedisCacheUtils.del(user.getUserId().toString(), AccountConstants.ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY);
                log.info("ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY del:userId={}",user.getUserId());
            }else{
                RedisCacheUtils.set(user.getUserId().toString(), 1, -1, AccountConstants.ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY);
                log.info("ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY set:userId={}",user.getUserId());
            }

        }
        resp.setOnlyBindMobileOrEmail(onlyBindMobileOrEmail);


        boolean isNewUserDisableLogicFlag=userDeviceBusiness.checkNewDisableLogicVersion(request.getTerminal().getCode(),request.getVersion());
        // 现在等于每次半登录态的时候就要check 是否是新设备
        if (true) {
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOGIN)) {
                // 已经被禁用登录
                throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN, user.getUserId(), null);
            } else if (!isNewUserDisableLogicFlag &&BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
                // 已经被禁用
                throw new BusinessException(GeneralCode.USER_DISABLED, user.getUserId(), null);
            }else if (isNewUserDisableLogicFlag && BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
                //新的禁用逻辑，如果是app端的话会返回半登陆态
                resp.setNewDisableLogicFLag(true);
            }
            // 清除登录错误次数
            AsyncTaskExecutor.execute(() -> {
                UserSecurity errorPwdCountClear = new UserSecurity();
                errorPwdCountClear.setLoginFailedNum(0);
                errorPwdCountClear.setUserId(user.getUserId());
                this.userSecurityMapper.updateByPrimaryKeySelective(errorPwdCountClear);
            });

            String clientIp = WebUtils.getRequestIp();
            stopWatch.start();
            String locationCity = IP2LocationUtils.getCountryCity(clientIp);
            stopWatch.stop();
            log.info("IP2LocationUtils.getCountryCity end, elapsedTime:{} secs", stopWatch.getTotalTimeSeconds());
            // 校验设备指纹
            String clientType = request.getTerminal().getCode();
            String deviceName = null;
            UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), clientIp, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_LOGIN, "登录");

            HashMap<String, String> deviceInfo = requestBody.getDeviceInfo();
            String relatedDeviceIds = deviceInfo != null ? deviceInfo.get(UserDeviceConst.RELATED_DEVICE_IDS) : null;
            userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
            boolean hasDeviceChecked;
            // 设备指纹不为空，且属性数足够，才做校验
            boolean canCheckDevice =
                    (deviceInfo != null && deviceInfo.size() >= userDeviceBusiness.getMinPropertyCount(clientType));
            if (userDeviceBusiness.checkVersion(clientType, request.getVersion())) {
                if (!canCheckDevice) {
                    hasDeviceChecked = false;
                } else {
                    CheckUserDeviceResponse ckRs =
                            userDeviceBusiness.checkDevice(user.getUserId(), clientType, deviceInfo);
                    if (ckRs.isValid() || inWhiteList) {
                        resp.setCurrentDeviceId(ckRs.getDeviceId());
                        securityLog.touchDevice(ckRs.getId(), ckRs.getDeviceId());
                        deviceName = deviceInfo.get(UserDevice.DEVICE_NAME);
                        // 记录关联设备的信息
                        AsyncTaskExecutor.execute(() -> userDeviceBusiness.updateRelatedDevice(user.getUserId(),
                                ckRs.getId(), relatedDeviceIds));
                    } else {
                        //通过Notification发送通知
                        securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.DEVICE_AUTH, request.getLanguage());
                        // 缓存新设备信息
                        userDeviceBusiness.cacheDeviceAuthForLogin(user, clientType, deviceInfo,
                                requestBody.getCustomDeviceAuthorizeUrl(), requestBody.getCustomForbiddenLink(),
                                requestBody.getCallback());

                        //throw new BusinessException(GeneralCode.USER_DEVICE_UNAUTHORIZED);
                        resp.setNewDeviceFLag(true);
                    }
                    //如果是黑设备登录，则把user id加入提币黑名单。
                    asyncAddUserIntoWithdrawBlackListIfNecessary(ckRs.getId(), deviceInfo, clientType, user);
                    hasDeviceChecked = true;
                }
            } else {
                hasDeviceChecked = false;
                // 直接记录设备信息
                log.warn("客户端版本过低，忽略设备信息: {}, {}", user.getUserId(), user.getEmail());
            }
            // 设备指纹兜底逻辑
            if (!hasDeviceChecked && !inWhiteList) {
                //TODO 需要兼容
                this.iUserIpChange.sensitiveIpCheck(user, clientIp, AuthTypeEnum.GOOGLE,
                        requestBody.getCustomIpChangeConfirmLink(), requestBody.getCustomForbiddenLink(),
                        userDeviceBusiness.isStrictMode(clientType));
            }

            // 生成认证令牌
            String otherCipher = RedisCacheUtils.get(CacheKeys.OTHER_CIPHER, DEFAULT_RESULT, true);
            // 认证令牌
            resp.setToken(
                    TokenUtils.createJWT(JSON.toJSONString(resp), DateUtils.getNewDateAddMinute(120), otherCipher));
            // 2小时内多次换ip登录
            SysConfig sysConfig = this.iSysConfig.selectByDisplayName("login_forbidden_ip_count");
            if (sysConfig != null) {
                String ips = RedisCacheUtils.get(user.getUserId().toString(), String.class, CacheKeys.USER_LOGIN_IP);
                String separatorChar = "@";
                if (StringUtils.isBlank(ips)) {
                    ips = clientIp;
                } else {
                    if (StringUtils.indexOf(ips, clientIp) < 0) {
                        ips = ips + separatorChar + clientIp;
                    }
                }
                String[] loginIps = StringUtils.split(ips, separatorChar);
                int loginIpsCount = loginIps != null ? loginIps.length : 0;
                int loginForbiddenIpCount = Integer.parseInt(sysConfig.getCode());
                // 账号频繁登录，请两小时后再试。
                if (loginIpsCount >= loginForbiddenIpCount && !inWhiteList) {
                    throw new BusinessException(GeneralCode.USER_LOGIN_FREQUENTLY, user.getUserId(), null);
                }
                // 有效期2小时
                RedisCacheUtils.set(user.getUserId().toString(), ips, 2 * 60 * 60, CacheKeys.USER_LOGIN_IP);
            } else {
                log.warn("没有配置  login_forbidden_ip_count");
            }

            this.userSecurityLogMapper.insertSelective(securityLog);

            resp.setLogId(securityLog.getId());
            // ip发生变化，发送登录成功邮件
            if (!iUserIpChange.isHistoryIp(user.getUserId(), clientIp) && !inWhiteList) {
                this.userIpMapper.insert(new UserIp(user.getUserId(), clientIp));

                Map<String, Object> data = new HashMap<>();
                data.put("currentIp", clientIp);
                data.put("browser",
                        StringUtils.isNotBlank(deviceName) ? deviceName : WebUtils.getHeader(Constant.BASE_BROWER));
                String inboxDeviceName = StringUtils.isNotBlank(deviceName) ? deviceName : WebUtils.getHeader(Constant.BASE_BROWER);
                data.put("deviceName", StringUtils.isBlank(inboxDeviceName)?"unknown":inboxDeviceName);
                String disableToken =null;
                if(!userStatusEx.getIsUserNotBindEmail().booleanValue()){
                    disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_EMAIL_IP, user, data,
                            "登录发送ip变更邮件：", requestBody.getCustomForbiddenLink());
                }else{
                    if(user.getEmail().contains("_mobileuser@binance.com")){
                        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
                        if (userSecurity != null && StringUtils.isNotBlank(userSecurity.getMobile())) {
                            data.put("email",userSecurity.getMobile());
                        }
                    }else{
                        data.put("email",user.getEmail());
                    }
                    data.put("ip", WebUtils.getRequestIp());
                    data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
                }
                String lang= WebUtils.getAPIRequestHeader().getLanguage().getLang();
                String terminalCode = request.getTerminal()==null?"web":request.getTerminal().getCode();
                APIRequest<PushInboxMessage> apiRequest = InboxUtils.getPushInboxMessageAPIRequest(user.getUserId(), data, lang, terminalCode,"LOGIN_IP_CHANGE");
                AsyncTaskExecutor.execute(() -> {
                    try {
                        inboxMessageTextApi.pushInbox(apiRequest);
                    }catch (Exception e){
                        log.warn("send inbox ip update error",e);
                    }
                });
                // 如果是中国用户，发送短信
                List<Long> userIds = new ArrayList<>();
                userIds.add(user.getUserId());
                List<UserSecurity> userSecuritys = userSecurityMapper.selectUserSecurityByUserIds(userIds);
                // this.msgApi.sendMsg(request)
                if (CollectionUtils.isNotEmpty(userSecuritys)) {
                    UserSecurity userSecurity = userSecuritys.get(0);
                    try {
                        if (("cn".equals(userSecurity.getMobileCode()) || "CN".equals(userSecurity.getMobileCode()))
                                && StringUtils.isNotEmpty(userSecurity.getMobile())) {
                            SendMsgRequest requestSms = new SendMsgRequest();
                            requestSms.setIp(clientIp);
                            requestSms.setMobileCode("+86");
                            requestSms.setRecipient(userSecurity.getMobile());
                            requestSms.setUserId(userSecurity.getUserId().toString());
                            requestSms.setTplCode(Constant.NODE_TYPE_SMS_IP);

                            requestSms.setNeedIpCheck(false);
                            requestSms.setNeedSendTimesCheck(false);

                            Map<String, Object> params = new HashMap<>();
                            params.put("currentIp", clientIp);
                            params.put("email", getHideEmail(user.getEmail()));
                            params.put("time",
                                    DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
                            requestSms.setData(params);
                            // 发送短信
                            userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                                    WebUtils.getAPIRequestHeader().getTerminal());

                        }
                    } catch (Exception e) {
                        log.warn("exception:{}", e);
                    }
                }


                resp.setIpLocation(securityLog.getIpLocation());
                resp.setDisableToken(disableToken);
            }
        }
        // 记录登录语言
        final String userLastLoginLanguage=WebUtils.getHeader(Constant.LANG);
        if(null==userLastLoginLanguage|| org.apache.commons.lang3.StringUtils.isBlank(userLastLoginLanguage)){
            addOrUpdateUserConfig(user.getUserId(), "userLastLoginLanguage",
                    LanguageEnum.EN_US.getLang());
        }else{
            addOrUpdateUserConfig(user.getUserId(), "userLastLoginLanguage",
                    userLastLoginLanguage);
        }
        log.info("userLastLoginLanguage={}",userLastLoginLanguage);
        AsyncTaskExecutor.execute(() -> {
            try {
                if(org.apache.commons.lang3.StringUtils.isNotBlank(userLastLoginLanguage)){
                    userLanguageApiClient.saveOrUpdate(user.getUserId().toString(),userLastLoginLanguage);
                }else{
                    userLanguageApiClient.saveOrUpdate(user.getUserId().toString(),LanguageEnum.EN_US.getLang());
                }
            }catch (Exception e){
                log.warn("send language to messagee error",e);
            }
        });

        //login success
        RedisCacheUtils.del(user.getUserId().toString(), AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
        log.info("ACCOUNT_LOGIN_VERIFY_2FA_KEY login del:userId={}",user.getUserId());
        return APIResponse.getOKJsonResult(resp);
    }




    public void asyncAddUserIntoWithdrawBlackListIfNecessary(Long devicePk, Map<String, String> deviceInfo, String clientType, User user) {
        ((UserBusiness) applicationContext.getBean(this.getClass())).addUserIntoWithdrawBlackListIfNecessary(devicePk, deviceInfo, clientType, user);
    }

    @Async
    void addUserIntoWithdrawBlackListIfNecessary(Long devicePk, Map<String, String> deviceInfo, String clientType, User user) {
        boolean addWithdrawBlackListSwitch = BooleanUtils.toBoolean(commonConfig.getLoginAddWithdrawBlackListSwitch());
        log.info("add user into withdraw black list when login, switch={}", addWithdrawBlackListSwitch);
        if (addWithdrawBlackListSwitch) {
            boolean isDeviceBlack = false;
            try {
                isDeviceBlack = userDeviceBusiness.isDeviceInBlackList(deviceInfo, clientType);
            } catch (Exception e) {
                log.error("check device in black list error.", e);
            }
            if (isDeviceBlack) {
                RiskWithdrawBlackListRequest riskWithdrawBlackListRequest = new RiskWithdrawBlackListRequest();
                riskWithdrawBlackListRequest.setUserId(String.valueOf(user.getUserId()));
                riskWithdrawBlackListRequest.setEmail(user.getEmail());
                riskWithdrawBlackListRequest.setType(5/*黑设备*/);
                riskWithdrawBlackListRequest.setIfKycRestore(0/*完成KYC之后是否自动删除黑名单：1-自动删除,0-不删除*/);
                riskWithdrawBlackListRequest.setRemark("Device id: " + devicePk);
                APIResponse addWithdrawBlackListResp = riskWithdrawApi.addWithdrawBlackList(APIRequest.instance(riskWithdrawBlackListRequest));
                if (addWithdrawBlackListResp != null && addWithdrawBlackListResp.getStatus() == APIResponse.Status.OK) {
                    log.info("add user={} into withdraw black list done.", user.getUserId());
                } else {
                    log.error("add user={} into withdraw black list failed, error code={}",
                            user.getUserId(), addWithdrawBlackListResp == null ? "null" : addWithdrawBlackListResp.getCode());
                }
            } else {
                log.info("Won't add user={} into withdraw black list.", user.getUserId());
            }
        }
    }

    private String getHideEmail(String email) {
        int lastIndexOf = email.lastIndexOf("@");
        if (lastIndexOf > 4) {
            int length = email.substring(4, email.lastIndexOf("@")).length();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < length; i++) {
                sb.append("*");
            }
            return email.replaceAll(email.substring(4, email.lastIndexOf("@")), sb.toString());
        } else {
            return email.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4");
        }
    }

    @Override
    @MarginValidate(userId = "#request.body.userId")
    @SecurityLog(name = "修改密码", operateType = Constant.SECURITY_OPERATE_TYPE_UPDATE_PASSWORD,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId",type = RiskTaskAspect.CHANGE_PASSWORD)
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<UpdatePwdUserResponse> updatePwd(APIRequest<UpdatePwdUserRequest> request) throws Exception {
        final UpdatePwdUserRequest requestBody = request.getBody();
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        //User user = this.userMapper.queryByEmail(userIndex.getEmail());
        User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOGIN)) {
            // 已经被禁用登录
            throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN);
        } else if (BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
            throw new BusinessException(GeneralCode.USER_DISABLED);// 已经被禁用
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOCK) && !this.unlockUser(user)) {
            throw new BusinessException(GeneralCode.USER_LOCK, new Object[] {USER_LOCK_TIME});// 已经锁定
        }
        VerificationsTwo verificationsTwo = this.userSecurityBusiness.verificationsTwoV2(
                user.getUserId(), requestBody.getAuthType(), requestBody.getCode(),
                SecurityKeyApplicationScenario.resetPassword, false);

        // 获取缓存中旧密码输错次数，登录后清除缓存
        String cacheKey = String.format(CACHE_USER_PASSWORD_ERROR_NUM, String.valueOf(user.getUserId()));
        String strNum = RedisCacheUtils.get(cacheKey);
        int num = Integer.valueOf(strNum == null ? "0" : strNum);
        // 检查原始密码是否正确
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        final String password = PasswordUtils.encode(requestBody.getOldPassword(), user.getSalt(), cipherCode);
        if (!StringUtils.equals(password, user.getPassword())) {
            // 新旧密码不一致，原逻辑：超过错误限定次数，锁定账号；现修改逻辑为：提示输错超过限定次数，请退出登录后再修改
            num += 1;
            if (num <= 3) {
                // 设置旧密码输错次数的缓存
                RedisCacheUtils.set(cacheKey, String.valueOf(num), Constant.DAY);
                throw new BusinessException(GeneralCode.USER_UPDATE_PWD_ERROR);// 原密码错误
            }
        }
        // 旧密码输错三次且一直未重新登录，则提示重新登录再修改
        if (num >= 3 && !updatePwdNewMsgSwitch) {
            throw new BusinessException(GeneralCode.USER_UPDATE_PASSWORD_ERROR);// 原密码输错三次，提示重新登录后再修改密码
        }
        if (num >= 3 && updatePwdNewMsgSwitch) {
            throw new BusinessException(AccountErrorCode.PLEASE_GO_TO_WEBSIDE_FOR_OPERATION);// 原密码输错三次，提示重新登录后再修改密码
        }
        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, user.getUserId(), requestBody.getDeviceInfo());
        // 修改密码
        user.setSalt(StringUtils.uuid());
        user.setPassword(PasswordUtils.encode(requestBody.getNewPassword(), user.getSalt(), cipherCode));
        user.setUpdateTime(DateUtils.getNewUTCDate());

        userSecurityBusiness.updateUserPassword(user.getEmail(), user.getSalt(), user.getPassword(),riskEngineResult, UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX);

        if (verificationsTwo != null) {
            verificationsTwo.delMobileCode();
        }
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_RESET_PASSWORD_USABLE:Constant.NODE_TYPE_RESET_PASSWORD, user, null,
                "修改密码发送邮件：", requestBody.getCustomForbiddenLink());
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UPDATE_PWD, dataMsg);
        log.info("iMsgNotification updatePwd:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new UpdatePwdUserResponse(user.getUserId(), user.getEmail(), user.getSalt(),
                user.getPassword(), disableToken));
    }

    @Override
    @MarginValidate(userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId",type = RiskTaskAspect.CHANGE_PASSWORD)
    @SecurityLog(name = "修改密码", operateType = Constant.SECURITY_OPERATE_TYPE_UPDATE_PASSWORD,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<UpdatePwdUserV2Response> updatePwdV2(APIRequest<UpdatePwdUserV2Request> request) throws Exception {
        final UpdatePwdUserV2Request requestBody = request.getBody();
        if (StringUtils.isAnyBlank(requestBody.getOldPassword(), requestBody.getNewPassword(), requestBody.getOldSafePassword(),
                requestBody.getNewSafePassword())) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);// 四个密码一个不能少
        }
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        //User user = this.userMapper.queryByEmail(userIndex.getEmail());
        User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOGIN)) {
            // 已经被禁用登录
            throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN);
        } else if (BitUtils.isTrue(user.getStatus(), Constant.USER_DISABLED)) {
            throw new BusinessException(GeneralCode.USER_DISABLED);// 已经被禁用
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_LOCK) && !this.unlockUser(user)) {
            throw new BusinessException(GeneralCode.USER_LOCK, new Object[] {USER_LOCK_TIME});// 已经锁定
        }

        //2fa验证
        MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                .userId(requestBody.getUserId())
                .bizScene(BizSceneEnum.MODIFY_PASSWORD)
                .emailVerifyCode(requestBody.getEmailVerifyCode())
                .googleVerifyCode(requestBody.getGoogleVerifyCode())
                .mobileVerifyCode(requestBody.getMobileVerifyCode())
                .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                .build();
        this.userSecurityBusiness.verifyMultiFactors(verify);

        // 获取缓存中旧密码输错次数，登录后清除缓存
        String cacheKey = String.format(CACHE_USER_PASSWORD_ERROR_NUM, String.valueOf(user.getUserId()));
        String strNum = RedisCacheUtils.get(cacheKey);
        int num = Integer.valueOf(strNum == null ? "0" : strNum);
        // 检查原始密码是否正确
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        final String oldEncodePassword = PasswordUtils.encode(requestBody.getOldPassword(), user.getSalt(), cipherCode);
        final String oldSafePassword = CryptoAlgoUtils.validateAndHash512(requestBody.getOldSafePassword(), user.getSalt());
        boolean matchOldEncodePassword = StringUtils.equals(oldEncodePassword, user.getPassword());
        boolean matchOldSafePassword = StringUtils.equals(oldSafePassword, user.getSafePassword());
        if (!matchOldSafePassword) {
            // 新旧密码不一致，原逻辑：超过错误限定次数，锁定账号；现修改逻辑为：提示输错超过限定次数，请退出登录后再修改
            num += 1;
            if (num <= 3) {
                // 设置旧密码输错次数的缓存
                RedisCacheUtils.set(cacheKey, String.valueOf(num), Constant.DAY);
                throw new BusinessException(GeneralCode.USER_UPDATE_PWD_ERROR);// 原密码错误
            }
        }
        // 旧密码输错三次且一直未重新登录，则提示重新登录再修改
        if (num >= 3) {
            throw new BusinessException(GeneralCode.USER_UPDATE_PASSWORD_ERROR);// 原密码输错三次，提示重新登录后再修改密码
        }
        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.UNBIND_UPDATE_PWD, user.getUserId(), requestBody.getDeviceInfo());
        // 修改密码
        user.setSalt(StringUtils.uuid());
        user.setPassword(PasswordUtils.encode(requestBody.getNewPassword(), user.getSalt(), cipherCode));
        user.setUpdateTime(DateUtils.getNewUTCDate());

        userSecurityBusiness.updateUserPasswordAndSafePwd(user.getEmail(), user.getSalt(), user.getPassword(),riskEngineResult, UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX, requestBody.getNewSafePassword());

        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_RESET_PASSWORD_USABLE:Constant.NODE_TYPE_RESET_PASSWORD, user, null,
                "修改密码发送邮件：", requestBody.getCustomForbiddenLink());
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, userIndex.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("disableToken", disableToken);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.UPDATE_PWD, dataMsg);
        log.info("iMsgNotification updatePwd:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new UpdatePwdUserV2Response(user.getUserId(), user.getEmail(), user.getSalt(),
                user.getPassword(), disableToken));
    }

    @Override
    @SecurityLog(name = "修改账号", operateType = Constant.SECURITY_OPERATE_TYPE_UPDATE_ACCOUNT,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<UpdateAccountUserResponse> updateAccount(APIRequest<UpdateAccountUserRequest> request) {
        final UpdateAccountUserRequest requestBody = request.getBody();
        final String requestEmail = requestBody.getEmail().trim().toLowerCase();
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final String oldEmail = userIndex.getEmail();
        User user = this.userMapper.queryByEmail(oldEmail);// 老的数据
        User temp = this.userMapper.queryByEmail(requestEmail);// 新的邮箱
        if (temp != null || userIndex.getEmail().equalsIgnoreCase(requestEmail)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }
        this.userMapper.deleteByEmail(oldEmail);
        user.setEmail(requestEmail);
        user.setUpdateTime(DateUtils.getNewUTCDate());
        this.userMapper.insert(user);
        userIndex.setEmail(requestEmail);
        this.userIndexMapper.updateByPrimaryKeySelective(userIndex);
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId());
        userSecurity.setEmail(requestEmail);
        this.userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
        return APIResponse.getOKJsonResult(new UpdateAccountUserResponse(userIndex.getUserId(), userIndex.getEmail()));
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<AccountActiveUserResponse> internalAccountActive(
            @RequestBody() APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        log.info("internalAccountActive, userId:{}", requestBody.getUserId());
        User tempUser;
        Long tradingAccount;
        try {
            UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                // 账号不存在
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            tempUser = this.userMapper.queryByEmail(userIndex.getEmail());
            if(tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {//用户存在且被删除
                throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
            }
            if (BitUtils.isTrue(tempUser.getStatus(), Constant.USER_ACTIVE)) {
                throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
            }
            User user = new User();
            user.setEmail(tempUser.getEmail());
            // 默认禁用app交易
            user.setStatus(tempUser.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE);
            this.userMapper.updateByEmail(user);
            UserInfo tempInfo = this.userInfoMapper.selectByPrimaryKey(tempUser.getUserId());
            if (tempInfo.getTradingAccount() == null) {
                // 创建交易账户
                tradingAccount = userCommonBusiness.createTradingAccount(tempInfo);
            } else {
                tradingAccount = tempInfo.getTradingAccount();
            }
        } catch (ExpiredJwtException e) {
            throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
        }
        // 临时的代码 完全迁移后移除 start
        sendAccountActiveMqMsg(tempUser, tradingAccount);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(
                new AccountActiveUserResponse(tempUser.getUserId(), tempUser.getEmail(), tradingAccount));
    }

    private void sendAccountActiveMqMsg(User tempUser, Long tradingAccount) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, tempUser.getUserId());
        dataMsg.put("tradingAccount", tradingAccount);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.ACCOUNT_ACTIVE, dataMsg);
        log.info("iMsgNotification accountActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
        this.iMsgNotification.send(msg);
    }

    @Override
    // @SecurityLog(name="激活账号",operateType=Constant.SECURITY_OPERATE_TYPE_ACCOUNT_ACTIVE,email="#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<AccountActiveUserResponse> accountActive(APIRequest<AccountActiveUserRequest> request) {
        final AccountActiveUserRequest requestBody = request.getBody();
        log.info("accountActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(request)));
        User tempUser = null;
        Long tradingAccount = null;
        String useremail=requestBody.getEmail();
        try {
            String registerKeyStr = null;
            if (StringUtils.isNotBlank(requestBody.getEmail())) {
                registerKeyStr = RedisCacheUtils.get(requestBody.getEmail(), String.class, CacheKeys.REGISTER_EMAIL);
            } else {
                UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
                if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
                }
                requestBody.setEmail(userIndex.getEmail());
                useremail=userIndex.getEmail();
                registerKeyStr = RedisCacheUtils.get(userIndex.getEmail(), String.class, CacheKeys.REGISTER_EMAIL);
            }
            if (StringUtils.isBlank(registerKeyStr)) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
            }
            Lock lock = RedisCacheUtils.getLock(AccountConstants.ACTIVE_EMAIL_USER_LOCK+useremail);
            if (lock != null && lock.tryLock()) {
                try {
                    RedisVerify redisVerify = JSON.parseObject(registerKeyStr, RedisVerify.class);
                   /* if (redisVerify.getErrorTime() != null
                            && (redisVerify.getErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils.getNewUTCTimeMillis()) {
                        log.info("activecode retry gap less than 3s");
                        throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
                    }*/
                    tempUser = this.userMapper.queryByEmail(requestBody.getEmail());
                    if (!StringUtils.equals(redisVerify.getCode(), requestBody.getCode())) {
                        redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);
                        redisVerify.setErrorTime(DateUtils.getNewUTCTimeMillis());
                        RedisCacheUtils.set(tempUser.getEmail(), JsonUtils.toJsonNotNullKey(redisVerify), -1L,
                                CacheKeys.REGISTER_EMAIL);// 不修改过期时间
                        log.info("activecode error count:userId={},errorcount={}",tempUser.getUserId(),redisVerify.getErrorCount());
                        if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount().intValue() >= ERROR_COUNT) {
                            RedisCacheUtils.del(useremail,CacheKeys.REGISTER_EMAIL);
                            log.info("activecode error expire:userId={} ",tempUser.getUserId());
                            throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
                        }
                        throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_ERROR);
                    }
                    if (BitUtils.isTrue(tempUser.getStatus(), Constant.USER_ACTIVE)) {
                        throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
                    }
                    User user = new User();
                    user.setEmail(tempUser.getEmail());
                    user.setStatus(tempUser.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE);// 默认禁用app交易
                    this.userMapper.updateByEmail(user);
                    UserInfo tempInfo = this.userInfoMapper.selectByPrimaryKey(tempUser.getUserId());
                    if (tempInfo.getTradingAccount() == null) {
                        tradingAccount = userCommonBusiness.createTradingAccount(tempInfo);// 创建交易账户
                    } else {
                        tradingAccount = tempInfo.getTradingAccount();
                    }
                    RedisCacheUtils.del(requestBody.getEmail(), CacheKeys.REGISTER_EMAIL);
                    //美国站 添加用户Basic邮件通知任务。主站不受印象。
                    iUserKycEmailNotify.addBasicNotifyTask(tempUser.getUserId(), tempUser.getEmail());

                    // 添加登录日志
                    // 用户注册完激活进来后，由于未经过登录，页面显示lastLoginIp和Time为空，所以激活时增加login日志
                    try {
                        final String ip = WebUtils.getRequestIp();
                        String locationCity = IP2LocationUtils.getCountryCity(ip);
                        String clientType = request.getTerminal().getCode();
                        final UserSecurityLog securityLog = new UserSecurityLog(tempUser.getUserId(), ip, locationCity, clientType,
                                Constant.SECURITY_OPERATE_TYPE_LOGIN, "登录");
                        UserIp userIp = new UserIp(tempUser.getUserId(), ip);
                        this.userIpMapper.insertIgnore(userIp);
                        this.userSecurityLogMapper.insertSelective(securityLog);
                    } catch (Exception e) {
                        log.error(String.format("add login log failed, email:%s, exception:", tempUser.getEmail()), e);
                    }

                }finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }else {
                log.info("user active get lock failed");
                throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
            }

        } catch (ExpiredJwtException e) {
            throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
        }
//        catch (InterruptedException e) {
//            // do nothing
//            log.info("useractive lock InterruptedException");
//            throw new BusinessException(GeneralCode.SYS_ERROR);
//
//        }
        // 临时的代码 完全迁移后移除 start
        sendAccountActiveMqMsg(tempUser, tradingAccount);
        AsyncTaskExecutor.execute(() -> {
            try{
                User currentUser=userMapper.queryByEmail(requestBody.getEmail());
                UserStatusEx userStatusEx=new UserStatusEx(currentUser.getStatus());
               if(userStatusEx.getIsSubUser()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    SubUserBinding subUserBinding=subUserBindingMapper.selectBySubUserId(currentUser.getUserId());
                    if(null!=subUserBinding){
                        dataMsg.put("parentUserId", subUserBinding.getParentUserId());
                        dataMsg.put("subUserId", currentUser.getUserId());
                        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
                        log.info("iMsgNotification sendUserProductFeeMsgFroActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                        iMsgNotification.send(msg);
                    }
                }else {
                    log.info("rootUserId is not parentUser or subUser:{}", currentUser.getUserId());

                }
            }catch (Exception e){
                log.error("sendUserProductFeeMsgFroActive exception", e);

            }
        });
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(
                new AccountActiveUserResponse(tempUser.getUserId(), tempUser.getEmail(), tradingAccount));
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<AccountActiveUserV2Response> accountActiveV2(APIRequest<AccountActiveUserV2Request> request) throws Exception {
        final AccountActiveUserV2Request requestBody = request.getBody();
        log.info("accountActiveV2:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(request)));
        User tempUser = null;
        Long tradingAccount = null;
        String useremail;
        try {
            UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
            if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
            }
            useremail=userIndex.getEmail();
            tempUser = this.userMapper.queryByEmail(useremail);
            if (BitUtils.isTrue(tempUser.getStatus(), Constant.USER_ACTIVE)) {
                throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
            }

            // 多因子校验
            MultiFactorSceneVerify verify = MultiFactorSceneVerify.builder()
                    .userId(requestBody.getUserId())
                    .bizScene(BizSceneEnum.ACCOUNT_ACTIVATE)
                    .emailVerifyCode(requestBody.getEmailVerifyCode())
                    .googleVerifyCode(requestBody.getGoogleVerifyCode())
                    .mobileVerifyCode(requestBody.getMobileVerifyCode())
                    .yubikeyVerifyCode(requestBody.getYubikeyVerifyCode())
                    .build();
            userSecurityBusiness.verifyMultiFactors(verify);

            // 默认禁用app交易
            Long updateStatus=tempUser.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE;
            int updateResult=  this.userMapper.enableStatusOptimisticLockUpdate(tempUser.getEmail(),updateStatus);
            if (updateResult!=1) {
                throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
            }
            UserInfo tempInfo = this.userInfoMapper.selectByPrimaryKey(tempUser.getUserId());
            if (tempInfo.getTradingAccount() == null) {
                tradingAccount = userCommonBusiness.createTradingAccount(tempInfo);// 创建交易账户
            } else {
                tradingAccount = tempInfo.getTradingAccount();
            }

            // 添加登录日志
            // 用户注册完激活进来后，由于未经过登录，页面显示lastLoginIp和Time为空，所以激活时增加login日志
            try {
                final String ip = WebUtils.getRequestIp();
                String locationCity = IP2LocationUtils.getCountryCity(ip);
                String clientType = request.getTerminal().getCode();
                final UserSecurityLog securityLog = new UserSecurityLog(tempUser.getUserId(), ip, locationCity, clientType,
                        Constant.SECURITY_OPERATE_TYPE_LOGIN, "登录");
                UserIp userIp = new UserIp(tempUser.getUserId(), ip);
                this.userIpMapper.insertIgnore(userIp);
                this.userSecurityLogMapper.insertSelective(securityLog);
            } catch (Exception e) {
                log.error(String.format("add login log failed, email:%s, exception:", tempUser.getEmail()), e);
            }
        } catch (ExpiredJwtException e) {
            throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
        } catch (InterruptedException e) {
            // do nothing
            log.info("useractive lock InterruptedException");
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        // 临时的代码 完全迁移后移除 start
        sendAccountActiveMqMsg(tempUser, tradingAccount);
        AsyncTaskExecutor.execute(() -> {
            try{
                User currentUser=userMapper.queryByEmail(useremail);
                UserStatusEx userStatusEx=new UserStatusEx(currentUser.getStatus());
                if(userStatusEx.getIsSubUser()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    SubUserBinding subUserBinding=subUserBindingMapper.selectBySubUserId(currentUser.getUserId());
                    if(null!=subUserBinding){
                        dataMsg.put("parentUserId", subUserBinding.getParentUserId());
                        dataMsg.put("subUserId", currentUser.getUserId());
                        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
                        log.info("iMsgNotification sendUserProductFeeMsgFroActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                        iMsgNotification.send(msg);
                    }
                }else {
                    log.info("rootUserId is not parentUser or subUser:{}", currentUser.getUserId());
                }
            }catch (Exception e){
                log.error("sendUserProductFeeMsgFroActive exception", e);
            }
        });
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new AccountActiveUserV2Response(tempUser.getUserId(), tempUser.getEmail(), tradingAccount));
    }

    @Override
    public String getConfigByConfigType(Long userId,String configType)  {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType(configType);
        UserConfig result=userInfoMapper.selectLatestUserConfig(uc);
        if(null==result){
            return null;
        }
        return result.getConfigName();
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<GetUserResponse> getUserByEmail(APIRequest<GetUserRequest> request) {
        final GetUserRequest requestBody = request.getBody();
        final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());

        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(user.getUserId());

        UserVo userVo = null;
        if (user != null) {
            userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVo.setPassword(null);
            userVo.setSalt(null);
        }

        UserSecurityVo userSecurityVo = conver2SecurityVo(userSecurity);

        UserInfoVo userInfoVo = null;
        if (userInfo != null) {
            userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(userInfo, userInfoVo);
        }
        return APIResponse.getOKJsonResult(new GetUserResponse(userVo, userSecurityVo, userInfoVo));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    @Monitored
    public APIResponse<GetUserResponse> getUserById(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final User user = this.userMapper.queryByEmail(userIndex.getEmail());

        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());

        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(requestBody.getUserId());

        UserVo userVo = null;
        if (user != null) {
            userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVo.setPassword(null);
            userVo.setSalt(null);
        }
        UserSecurityVo userSecurityVo = conver2SecurityVo(userSecurity);
        UserInfoVo userInfoVo = null;
        if (userInfo != null) {
            userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(userInfo, userInfoVo);
        }

        log.info("getUserById done, userId:{}", user==null ? "null":user.getUserId());
        return APIResponse.getOKJsonResult(new GetUserResponse(userVo, userSecurityVo, userInfoVo));
    }

    @Override
    public APIResponse<GetUserResponse> checkAndGetUserById(APIRequest<com.binance.account.vo.user.request.UserIdRequest> request) throws Exception {
        Long userId = Long.valueOf(request.getBody().getUserId());
        User user = userCommonBusiness.checkAndGetUserById(userId);

        UserVo userVo = new UserVo();
        userVo.setUserId(user.getUserId());
        userVo.setStatus(user.getStatus());
        return APIResponse.getOKJsonResult(new GetUserResponse(userVo, null, null));
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @SecurityLog(name = "忘记密码发送邮件", operateType = Constant.SECURITY_OPERATE_TYPE_FORGET_PSW,
            email = "#request.body.email")
    public APIResponse<AccountForgotPasswordResponse> forgotPasswordSendEmail(
            APIRequest<AccountForgotPasswordRequest> request) throws Exception {// 忘记密码发送邮件
        final AccountForgotPasswordRequest requestBody = request.getBody();
        final String email = requestBody.getEmail();
        if(org.apache.commons.lang3.StringUtils.isBlank(email)) {
        	log.warn("forgotPasswordSendEmail中的email为null");
        	throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        //final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        final User user = this.userMapper.queryByExistentEmail(email);
        if (user == null) {
            log.info(String.format("forgotPasswordSendEmail email为%s的用户不存在", requestBody.getEmail()));
            return APIResponse.getOKJsonResult(null);
            // throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND, new Object[]
            // {EMAIL_GAP_TIME});
        }
        final Long status = user.getStatus();
        String token = RedisCacheUtils.get(user.getEmail(), String.class, CacheKeys.RESET_PASSWORD_EMAIL);
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
        securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.FORGET_PWD, request.getLanguage());
        // 发送邮件
        Map<String, Object> data = new HashMap<>();
        String emailLink = UserCommonBusiness.emailLinkGenerator(requestBody.getCustomEmailLink(),
                String.format("%sresetPassword.html?vc={vc}&email={email}", WebUtils.getHeader(Constant.BASE_URL)),
                ImmutableMap.of("vc", token, UserConst.EMAIL, URLEncoder.encode(user.getEmail(), "UTF-8")));
        data.put("link", emailLink);

        String disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_EMAIL_PWD, user, data,
                "忘记密码发送邮件：", null);

        RedisCacheUtils.set(user.getEmail(), token, UserCommonBusiness.EXPIRED_TIME * 60,
                CacheKeys.RESET_PASSWORD_EMAIL);
        log.info("forgotPasswordSendEmail 处理完成！");
        return APIResponse.getOKJsonResult(
                new AccountForgotPasswordResponse(user.getUserId(), user.getEmail(), status, token, disableToken));
    }

    @Override
    @SecurityLog(name = "重置密码", operateType = Constant.SECURITY_OPERATE_TYPE_RESET_PASSWORD,
            email = "#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<AccountResetPasswordResponse> resetPassword(APIRequest<AccountResetPasswordRequest> request)
            throws Exception {
        final AccountResetPasswordRequest requestBody = request.getBody();
        final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_LINK_EXPIRED);
        }
        final Long status = user.getStatus();
        String token = RedisCacheUtils.get(user.getEmail(), String.class, CacheKeys.RESET_PASSWORD_EMAIL);
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(GeneralCode.USER_LINK_EXPIRED);
        }
        if (!StringUtils.equals(token, requestBody.getToken())) {
            throw new BusinessException(GeneralCode.USER_INVALID_LINK_CODE);
        }
        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.FORGET_PWD, user.getUserId(), requestBody.getDeviceInfo());


        // 重置密码
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        user.setSalt(StringUtils.uuid());
        user.setPassword(PasswordUtils.encode(requestBody.getPassword(), user.getSalt(), cipherCode));

        userSecurityBusiness.updateUserPassword(user.getEmail(), user.getSalt(), user.getPassword(), riskEngineResult,forgetPasswordSwitch?UserSecurityBusiness.FORGET_PSW_TIME_PREFIX:UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX);

        // 重置密码错误次数
        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLockEndTime(DateUtils.getNewUTCDateAddHour(-2));
        this.userSecurityMapper.updateByPrimaryKey(userSecurity);
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_RESET_PASSWORD_USABLE:Constant.NODE_TYPE_RESET_PASSWORD, user, null,
                "重置密码异常：", requestBody.getCustomForbiddenLink());

        // 使EmailVerifyCode失效

        // 如果是第三方注册的用户，通知Fiat
        try {
            if (StringUtils.isNotBlank(thirdUserTraceSources)) {
                List<String> traceSourceList = Arrays.asList(thirdUserTraceSources.split(","));
                final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(user.getUserId());
                if (StringUtils.isNotBlank(userInfo.getTrackSource()) && traceSourceList.contains(userInfo.getTrackSource())) {
                    ExternalUserBindRegistRequest apiRequest = new ExternalUserBindRegistRequest();
                    apiRequest.setEmail(user.getEmail());
                    log.info("resetPassword 第三方注册用户通知Fiat request={}", JSONObject.toJSONString(apiRequest));
                    externalOcbsApi.externalUserBindFormRegist(APIRequest.instance(apiRequest));
                    log.info("resetPassword 第三方注册用户通知Fiat success, userId={}", user.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("resetPassword 第三方注册用户通知Fiat error, userId=" + user.getUserId(), e);
        }

        return APIResponse.getOKJsonResult(new AccountResetPasswordResponse(user.getUserId(), user.getEmail(), status,
                user.getSalt(), user.getPassword(), disableToken));
    }


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> enableUserStatus(APIRequest<UserStatusRequest> request) {
        final UserStatusRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final long statusNew = requestBody.getStatus();
        if (BitUtils.isEnable(statusNew, Constant.USER_MOBILE)) {// 手机验证
            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());
            if (StringUtils.isBlank(userSecurity.getMobile())) {
                throw new BusinessException(GeneralCode.USER_NOT_MOBILE);// 用户没有绑定手机
            }
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        final User userTemp = new User();
        userTemp.setEmail(userIndex.getEmail());
        userTemp.setStatus(BitUtils.enable(status, requestBody.getStatus()));
        final UserSecurityLog log = new UserSecurityLog();
        log.setUserId(requestBody.getUserId());
        log.setIp(WebUtils.getRequestIp());
        TerminalEnum terminal = WebUtils.getTerminal();
        log.setClientType(terminal!=null?terminal.getCode():"other");
        log.setIpLocation(IP2LocationUtils.getCountryCity(WebUtils.getRequestIp()));
        log.setOperateTime(DateUtils.getNewUTCDate());
        if (requestBody.getStatus().equals(Constant.USER_DISABLED)) {
            log.setOperateType("user_disable");
            log.setDescription("disableUserStatus接口禁用用户");
        }else if (requestBody.getStatus().equals(Constant.USER_LOGIN)){
            log.setOperateType("disable_login");
            log.setDescription("disableUserStatus接口禁用登陆");
        }else if (requestBody.getStatus().equals(Constant.USER_TRADE)){
            log.setOperateType("disable_user_trade");
            log.setDescription("disableUserStatus接口禁用用户交易");
        }else{
            log.setOperateType("other_disable_status");
            log.setDescription("disableUserStatus接口更改用户状态:"+requestBody.getStatus());
        }
        this.userSecurityLogMapper.insertSelective(log);
        return APIResponse.getOKJsonResult(this.userMapper.updateUserStatusByEmail(userTemp));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> disableUserStatus(APIRequest<UserStatusRequest> request) {
        final UserStatusRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
        final User user = new User();
        user.setEmail(userIndex.getEmail());
        user.setStatus(BitUtils.disable(status, requestBody.getStatus()));
        final UserSecurityLog log = new UserSecurityLog();
        log.setUserId(requestBody.getUserId());
        log.setIp(WebUtils.getRequestIp());
        TerminalEnum terminal = WebUtils.getTerminal();
        log.setClientType(terminal!=null?terminal.getCode():"other");
        log.setIpLocation(IP2LocationUtils.getCountryCity(WebUtils.getRequestIp()));
        log.setOperateTime(DateUtils.getNewUTCDate());
        if (requestBody.getStatus().equals(Constant.USER_TRADE)){
            log.setOperateType("disable_user_trade");
            log.setDescription("接口启用用户交易");
        }
        this.userSecurityLogMapper.insertSelective(log);
        return APIResponse.getOKJsonResult(this.userMapper.updateUserStatusByEmail(user));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @RiskTask(email = "#request.body.email")
    @Override
    public APIResponse<Integer> updateUserByEmail(APIRequest<UpdateUserByEmailRequest> request) {
        final UpdateUserByEmailRequest requestBody = request.getBody();

        User user = new User();
        BeanUtils.copyProperties(requestBody.getUser(), user);

        return APIResponse.getOKJsonResult(this.userMapper.updateByEmailSelective(user));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<ResendSendActiveCodeResponse> resendSendActiveCode(
            APIRequest<ResendSendActiveCodeRequest> request) throws Exception {
        final ResendSendActiveCodeRequest requestBody = request.getBody();
        String ip = WebUtils.getRequestIp();
        long maxIpCount = resendActiveIpLimit;
        long ipCount = Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class, AccountConstants.ACCOUNT_RESEND_ACTIVECODE_IP_KEY, 0L)));
        if (ipCount >= maxIpCount) {
            throw new BusinessException(GeneralCode.TOO_MANY_REQUESTS);
        }else{
            try {
                RedisCacheUtils.increment(ip, AccountConstants.ACCOUNT_RESEND_ACTIVECODE_IP_KEY, 1L, 1L, TimeUnit.HOURS);// 有效期
            } catch (Exception e) {
                log.error("resendSendActiveCode", e);
            }
        }
        User temp = this.userMapper.queryByEmail(requestBody.getEmail());
        String[] sendParams =null;
        if(requestBody.getIsNewLoginProcess().booleanValue()){
            sendParams = userCommonBusiness.sendActiveCodeForNewProcess(temp, request.getTerminal(), requestBody.getCustomEmailLink());
        }else{
            sendParams = userCommonBusiness.sendActiveCode(temp, request.getTerminal(), requestBody.getCustomEmailLink());
        }
        return APIResponse.getOKJsonResult(
                new ResendSendActiveCodeResponse(temp.getUserId(), temp.getEmail(), sendParams[0], sendParams[1]));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<SendSmsAuthCoderResponse> sendSmsAuthCode(APIRequest<SendSmsAuthCoderRequest> request) {
        final SendSmsAuthCoderRequest requestBody = request.getBody();

        if (StringUtils.isNotBlank(requestBody.getEmail())) {
            //User tempUser = this.userMapper.queryByEmail(requestBody.getEmail());
            User tempUser = this.userMapper.queryByExistentEmail(requestBody.getEmail());
            if (tempUser == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            requestBody.setUserId(tempUser.getUserId());
        }
        if(null==requestBody.getUserId()){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 一分钟频率控制
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(requestBody.getUserId()), String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        RedisCacheUtils.set(String.valueOf(requestBody.getUserId()), String.valueOf(requestBody.getUserId()), 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("sendSmsAuthCode userId={}",requestBody.getUserId());

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (StringUtils.isBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }
        String code = RandomStringUtils.randomNumeric(6);
        // this.msgApi.sendMsg(request)
        SendMsgRequest requestSms = new SendMsgRequest();
        requestSms.setIp(WebUtils.getRequestIp());
        Country country = this.iCountry.getCountryByCode(userSecurity.getMobileCode());
        requestSms.setMobileCode(country.getMobileCode());
        requestSms.setRecipient(userSecurity.getMobile());
        requestSms.setUserId(userSecurity.getUserId().toString());
        requestSms.setTplCode(Constant.NODE_TYPE_MOBILE_VERIFY);

        requestSms.setVerifyCodeId(requestBody.getVerifyCodeId());

        requestSms.setMsgType(MsgType.valueOf(requestBody.getMsgType().name()));
        requestSms.setResend(requestBody.getResend());

        Map<String, Object> params = new HashMap<>();
        params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, code);
        requestSms.setData(params);

        // 一分钟频率控制
        String globalmobilekey=requestSms.getMobileCode()+requestSms.getRecipient();
        String frequencymobileLimits = RedisCacheUtils.get(globalmobilekey, String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencymobileLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
        }
        RedisCacheUtils.set(globalmobilekey, globalmobilekey, 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("frequencymobileLimits userId={}",requestBody.getUserId());


        // 发送短信
        userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        String mobileKey = StringUtils.getMobileKey(userSecurity.getMobile(), userSecurity.getMobileCode()).toUpperCase();

        // 存入Redis
        RedisVerify redisVerify = new RedisVerify();
        redisVerify.setTime(DateUtils.getNewUTCDate());
        redisVerify.setCode(commonConfig.convertSecretCode(userSecurity.getMobile(),code));
        redisVerify.setErrorCount(0);
        RedisCacheUtils.set(mobileKey, redisVerify, UserCommonBusiness.EXPIRED_TIME * 60L, CacheKeys.MOBILE_AUTH_TIME);

        return APIResponse.getOKJsonResult(new SendSmsAuthCoderResponse(userSecurity.getUserId(), null,
                userSecurity.getMobile(), userSecurity.getMobileCode()));
    }

    @Override
    public APIResponse<SendSmsAuthCodeV2Response> sendSmsAuthCodeV2(APIRequest<SendSmsAuthCodeV2Request> request) throws Exception {
        final SendSmsAuthCodeV2Request requestBody = request.getBody();

        boolean isSendNewMobile = requestBody.getBizScene() == BizSceneEnum.BIND_MOBILE || requestBody.getBizScene() == BizSceneEnum.ACCOUNT_ACTIVATE
                || requestBody.getBizScene() == BizSceneEnum.RESET_APPLY_MOBILE;
        if (isSendNewMobile) {
            if (StringUtils.isAnyBlank(requestBody.getMobileCode(), requestBody.getMobile())) {
                throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
            }
        }
        if (null == requestBody.getUserId() && StringUtils.isNotBlank(requestBody.getEmail())) {
            User tempUser = this.userMapper.queryByExistentEmail(requestBody.getEmail());
            if (tempUser == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            requestBody.setUserId(tempUser.getUserId());
        }
        if (null == requestBody.getUserId()) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //针对绑定手机场景特殊处理
        if(requestBody.getBizScene() == BizSceneEnum.BIND_MOBILE){
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
        }
        // 一分钟频率控制
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(requestBody.getUserId()), String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[]{1});
        }
        RedisCacheUtils.set(String.valueOf(requestBody.getUserId()), String.valueOf(requestBody.getUserId()), 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("sendSmsAuthCode userId={}", requestBody.getUserId());

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (!isSendNewMobile && StringUtils.isBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }
        String code = StringUtils.getNumberRandomString(6);
        SendMsgRequest requestSms = new SendMsgRequest();
        requestSms.setIp(WebUtils.getRequestIp());

        String mobileCode = isSendNewMobile ? requestBody.getMobileCode() : userSecurity.getMobileCode();
        String mobile = isSendNewMobile ? requestBody.getMobile() : userSecurity.getMobile();
        Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(mobileCode);
        requestSms.setMobileCode(country.getMobileCode());
        requestSms.setRecipient(mobile);
        requestSms.setUserId(userSecurity.getUserId().toString());

        // 根据场景指定短信模版
        String template;
        if (smsSceneTemplateMap != null && requestBody.getBizScene() != null &&
                smsSceneTemplateMap.containsKey(requestBody.getBizScene().getCode())) {
            template = smsSceneTemplateMap.get(requestBody.getBizScene().getCode());
        } else {
            template = Constant.NODE_TYPE_MOBILE_VERIFY;
        }
        requestSms.setTplCode(template);

        requestSms.setVerifyCodeId(requestBody.getVerifyCodeId());
        requestSms.setMsgType(MsgType.valueOf(requestBody.getMsgType().name()));
        requestSms.setResend(requestBody.getResend());

        Map<String, Object> params = new HashMap<>();
        params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, code);
        params.put("ip", WebUtils.getRequestIp());
        params.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        params.put("timelimit", 30);
        if (requestBody.getParams() != null && requestBody.getParams().size() > 0){
            params.putAll(requestBody.getParams());
        }
        appendSmsParamByScene(requestBody.getUserId(),params,requestBody.getBizScene());


        requestSms.setData(params);

        // 一分钟频率控制
        String globalmobilekey = requestSms.getMobileCode() + requestSms.getRecipient();
        String frequencymobileLimits = RedisCacheUtils.get(globalmobilekey, String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencymobileLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[]{1});
        }
        RedisCacheUtils.set(globalmobilekey, globalmobilekey, 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("frequencymobileLimits userId={}", requestBody.getUserId());
        if (requestBody.getBizScene() == CRYPTO_WITHDRAW) {
            String amount=String.valueOf(params.get("amount"));
            String address=String.valueOf(params.get("address"));
            String addressTag=String.valueOf(params.get("addressTag"));
            if(org.apache.commons.lang3.StringUtils.isAnyBlank(amount,amount)){
                log.info("CRYPTO_WITHDRAW error param");
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            CapitalWithdrawRedisVerify capitalWithdrawRedisVerify=new CapitalWithdrawRedisVerify();
            capitalWithdrawRedisVerify.setAddress(address);
            capitalWithdrawRedisVerify.setAmount(amount);
            if(org.apache.commons.lang3.StringUtils.isNotBlank(addressTag)){
                capitalWithdrawRedisVerify.setAddressTag(addressTag);
            }
            String capitalWithdrawkey=requestBody.getUserId().toString()+code;
            RedisCacheUtils.set(capitalWithdrawkey, JSON.toJSONString(capitalWithdrawRedisVerify), 30 * 60, AccountConstants.CRYPTO_WITHDRAW_ADDRESS_CODE_KEY);
        }

        userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        String mobileKey = StringUtils.getMobileKey(mobile, mobileCode).toUpperCase();

        // 存入Redis
        RedisVerify redisVerify = new RedisVerify();
        redisVerify.setTime(DateUtils.getNewUTCDate());
        redisVerify.setCode(commonConfig.convertSecretCode(mobile, code));
        redisVerify.setErrorCount(0);
        RedisCacheUtils.set(mobileKey, redisVerify, UserCommonBusiness.EXPIRED_TIME * 60L, CacheKeys.MOBILE_AUTH_TIME);

        return APIResponse.getOKJsonResult(new SendSmsAuthCodeV2Response(userSecurity.getUserId(), null,
                mobile, mobileCode));
    }

    // 基于场景填充邮件参数
    private void appendSmsParamByScene(Long userId, Map<String, Object> smsParams, BizSceneEnum bizScene) throws Exception {
        // 默认填充禁用地址
        String forbiddenLink = genForbiddenLink();
        if (StringUtils.isNotBlank(forbiddenLink)) {
            smsParams.put("forbiddenLink", forbiddenLink);
        }
        final UserSecurity userSecurity =  userSecurityMapper.selectByPrimaryKey(userId);
        String antiCode = userSecurity.getAntiPhishingCode();
        if (StringUtils.isNotBlank(antiCode)) {
            smsParams.put("antiCode", antiCode);
        }
        switch (bizScene) {
            case API_KEY_MANAGE:
                break;
            case AUTHORIZE_NEW_DEVICE:
                smsParams.put(UserDevice.LOGIN_IP, WebUtils.getRequestIp());
                smsParams.put(UserDevice.LOCATION_CITY, getLocationCity(WebUtils.getRequestIp()));
                break;
        }
    }

    private String getLocationCity(String ip){
        return StringUtils.replace(IP2LocationUtils.getCountryCity(ip),", Province of China", "");
    }

    private String genForbiddenLink() {
        return String.format("%s%s/my/security/account-activity/disable-account",WebUtils.getHeader(Constant.BASE_URL),
                StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en"));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<GetUserEmailResponse> getUserEmailByUserId(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return APIResponse.getOKJsonResult(new GetUserEmailResponse(userIndex.getEmail()));
    }

    @Override
    public APIResponse<GetUserEmailsResponse> getUserEmailByUserIds(APIRequest<GetUserListRequest> request) {
        final GetUserListRequest requestBody = request.getBody();
        List<Long> userIds = requestBody.getUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final List<UserIndex> userIndexList = this.userIndexMapper.selectByUserIds(userIds);
        List<UserIndexEx> userIndexExes = userIndexList.stream().map(k -> new UserIndexEx(k.getUserId(), k.getEmail()))
                .collect(Collectors.toList());
        return APIResponse.getOKJsonResult(new GetUserEmailsResponse(userIndexExes));
    }

    @Override
    public APIResponse<GetUserEmailsResponse> getUserIdsByEmails(APIRequest<GetUserIdListRequest> request) {
        final GetUserIdListRequest requestBody = request.getBody();
        List<String> emails = requestBody.getEmails();
        if (CollectionUtils.isEmpty(emails)) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final List<UserIndex> userIndexList = this.userIndexMapper.selectByEmails(emails);
        List<UserIndexEx> userIndexExes = userIndexList.stream().map(k -> new UserIndexEx(k.getUserId(), k.getEmail()))
                .collect(Collectors.toList());
        return APIResponse.getOKJsonResult(new GetUserEmailsResponse(userIndexExes));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<Boolean> passwordCheck(APIRequest<PasswordVerifyRequest> request) throws Exception {
        final PasswordVerifyRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());

        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        final String password = PasswordUtils.encode(requestBody.getPassword(), user.getSalt(), cipherCode);
        if (StringUtils.equals(password, user.getPassword())) {
            return APIResponse.getOKJsonResult(true);
        }
        return APIResponse.getOKJsonResult(false);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<SearchUserListResponse> searchUserList(APIRequest<SearchUserListRequest> request) {
        final SearchUserListRequest requestBody = request.getBody();
        // 参数校验
        if (StringUtils.isNotBlank(requestBody.getEmail()) && CollectionUtils.isNotEmpty(requestBody.getEmails())) {
            throw new BusinessException("邮箱和批量邮箱不能同时输入");// 后台接口，错误信息直接中文显示
        }
        return this.userSearch.searchUserList(requestBody);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<BaseDetailResponse> baseDetail(APIRequest<BaseDetailRequest> request)throws Exception {
        final BaseDetailRequest requestBody = request.getBody();
        BaseDetailResponse baseDetailResponse = new BaseDetailResponse();

        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        // 账号不存在
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user;

        // 强制从主库读，避免读写延迟问题，确保页面展示正确的用户状态
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();

            //user = this.userMapper.queryByEmail(userIndex.getEmail());
            user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }

        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        baseDetailResponse.setEmail(userIndex.getEmail().trim().toLowerCase());
        baseDetailResponse.setStatus(user.getStatus());
        baseDetailResponse.setUserStatusEx(new UserStatusEx(user.getStatus()));

        // UserInfo
        UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(user.getUserId());
        if (userInfo != null) {
            baseDetailResponse.setAgentId(userInfo.getAgentId());
            baseDetailResponse.setReferralRewardRatio(userInfo.getReferralRewardRatio());
            baseDetailResponse.setAgentRewardRatio(userInfo.getAgentRewardRatio());
            baseDetailResponse.setMakerCommission(userInfo.getMakerCommission());
            baseDetailResponse.setTakerCommission(userInfo.getTakerCommission());
            baseDetailResponse.setBuyerCommission(userInfo.getBuyerCommission());
            baseDetailResponse.setSellerCommission(userInfo.getSellerCommission());
            baseDetailResponse.setDailyWithdrawCap(userInfo.getDailyWithdrawCap());
            baseDetailResponse.setTradeLevel(userInfo.getTradeLevel());
            baseDetailResponse.setRemark(userInfo.getRemark());
            baseDetailResponse.setMarginUserId(userInfo.getMarginUserId());
            baseDetailResponse.setFiatUserId(userInfo.getFiatUserId());
            baseDetailResponse.setOrderConfirmStatus(OrderConfirmStatus.build(userInfo.getOrderConfirmStatus()));
            baseDetailResponse.setNickName(userInfo.getNickName());
            baseDetailResponse.setNickColor(userInfo.getNickColor());
        }

        // User Security
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (userSecurity != null) {
            baseDetailResponse.setMobile(userSecurity.getMobile());
            baseDetailResponse.setMobileCode(userSecurity.getMobileCode());
            baseDetailResponse.setSecurityLevel(userSecurity.getSecurityLevel());
            baseDetailResponse.setAntiPhishingCode(userSecurity.getAntiPhishingCode());
            baseDetailResponse.setInitFundPasswordOrNot(org.apache.commons.lang3.StringUtils.isNotBlank(userSecurity.getFundPassword()));
            //Security key registered origins
            List<String> origins = webAuthnFrontHandler.getUserRegisteredOrigins(user.getUserId());
            UserSecurityKeyStatus userSecurityKeyStatus = UserSecurityKeyStatus.build(userSecurity.getYubikeyEnabledScenarios(), origins);
            baseDetailResponse.setSecurityKeyStatus(userSecurityKeyStatus);
        }

        // User Security Log
        UserSecurityLog userSecurityLog = this.userSecurityLogMapper.getLastLoginLogByUserId(requestBody.getUserId());
        if (userSecurityLog != null) {
            UserSecurityLogVo userSecurityLogVo = new UserSecurityLogVo();
            BeanUtils.copyProperties(userSecurityLog, userSecurityLogVo);

            baseDetailResponse.setLastUserSecurityLog(userSecurityLogVo);
        }

        // KYC

        KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);

        baseDetailResponse.setCertificateType(certificateResult.getCertificateType());
        baseDetailResponse.setCertificateMessage(certificateResult.getCertificateMessage());
        baseDetailResponse.setCertificateStatus(certificateResult.getCertificateStatus());
        baseDetailResponse.setFirstName(certificateResult.getFirstName());
        baseDetailResponse.setMiddleName(certificateResult.getMiddleName());
        baseDetailResponse.setLastName(certificateResult.getLastName());
        baseDetailResponse.setCompanyName(certificateResult.getCompanyName());

        // User Address
        SysConfig addressVerificationConfig = this.iSysConfig.selectByDisplayName("address_verification_switch");
        if (addressVerificationConfig != null && "ON".equalsIgnoreCase(addressVerificationConfig.getCode())) {
            UserAddress userAddress =
                    this.userAddressMapper.getLast(user.getUserId(), UserAddress.Status.PASSED.ordinal());
            if (userAddress != null) {
                baseDetailResponse.setCertificateAddress(userAddress.getFullAddress());
            }
        }
        // leverwithdraw
        List<BigDecimal> leverwithdrawList=  withdrawPolicyService.getDailyWithdrawLimitsByUserSecurityAndUserInfo(userInfo,userSecurity);
        baseDetailResponse.setLevelWithdraw(leverwithdrawList);
        return APIResponse.getOKJsonResult(baseDetailResponse);
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public APIResponse<UserBriefInfoResponse> briefInfo(APIRequest<UserIdReq> request) {
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(request.getBody().getUserId());
        // 账号不存在
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user;

        // 强制从主库读，避免读写延迟问题，确保页面展示正确的用户状态
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();

            //user = this.userMapper.queryByEmail(userIndex.getEmail());
            user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }

        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserBriefInfoResponse response = new UserBriefInfoResponse();
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (userSecurity != null) {
            response.setMobile(userSecurity.getMobile());
            if (StringUtils.isNotBlank(userSecurity.getMobileCode())) {
                // response.setCountryCode(userSecurity.getMobileCode());
                final Country countryByCode = iCountry.getCountryByCode(userSecurity.getMobileCode());
                if (countryByCode != null) {
                    response.setMobileCode(countryByCode.getMobileCode());
                }
            }
        }

        KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);
        if(certificateResult != null) {
            response.setFirstName(certificateResult.getFirstName());
            response.setMiddleName(certificateResult.getMiddleName());
            response.setLastName(certificateResult.getLastName());
            /** 0:审核中 , 1:通过 2: 拒绝 null:未验证 */
            response.setIsKycPass(Integer.valueOf(1).equals(certificateResult.getCertificateStatus()));
            if (response.getIsKycPass()) {
                try {
                    final UserKycCountryResponse kycCountry = kycApiTransferAdapter.getKycCountry(request.getBody().getUserId());
                    if (kycCountry != null) {
                        response.setCountryCode(kycCountry.getCountryCode());
                    } else {
                        log.error("failed to get user's kyc country. kycCountry==null");
                    }
                } catch (Exception e) {
                    log.error("failed to get user's kyc country.", e);
                }
            }
        }

        UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
        response.setStatus(BooleanUtils.isTrue(userStatusEx.getIsUserDisabled()) ? "DISABLED" : "ENABLE");
        return APIResponse.getOKJsonResult(response);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<Long> getUserIdByEmail(APIRequest<GetUserRequest> request) {
        final GetUserRequest requestBody = request.getBody();
        User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        return APIResponse.getOKJsonResult(user.getUserId());
    }

    @Override
    public APIResponse<GetUserListResponse> getUserListByUserIds(APIRequest<GetUserListRequest> request) {
        final GetUserListRequest requestBody = request.getBody();
        List<Long> userIds = requestBody.getUserIds();
        int size = userIds.size();
        if (size > MAX_USER_ID_SIZE) {// 设置userId传入个数上限不大于5000
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(),
                    "userId传入个数太多");
        }

        Gson json = new Gson();
        String jsonUserIds = json.toJson(userIds);

        StopWatch one = new StopWatch();
        one.start();
        // 批量查userList
        int i = 0;
        final List<User> userList = new ArrayList<>(size);
        while (true) {
            if (i + MAX_SELECT_USER_COUNT < size) {
                userList.addAll(this.userMapper
                        .selectByUserIds(requestBody.getUserIds().subList(i, i + MAX_SELECT_USER_COUNT)));
            } else {
                userList.addAll(this.userMapper.selectByUserIds(requestBody.getUserIds().subList(i, size)));
                break;
            }
            i = i + MAX_SELECT_USER_COUNT;
        }
        one.stop();
        log.info("userList end, elapsedTime: {} seconds,userIds:{}", one.getTotalTimeSeconds(), jsonUserIds);

        StopWatch two = new StopWatch();
        two.start();
        // 批量查UserSecurityList
        i = 0;
        final List<UserSecurity> userSecurityList = new ArrayList<>(size);
        while (true) {
            if (i + MAX_SELECT_USER_COUNT < size) {
                userSecurityList.addAll(this.userSecurityMapper
                        .selectUserSecurityByUserIds(requestBody.getUserIds().subList(i, i + MAX_SELECT_USER_COUNT)));
            } else {
                userSecurityList.addAll(
                        this.userSecurityMapper.selectUserSecurityByUserIds(requestBody.getUserIds().subList(i, size)));
                break;
            }
            i = i + MAX_SELECT_USER_COUNT;
        }
        two.stop();
        log.info("userSecurityList end, elapsedTime: {} seconds,userIds:{}", two.getTotalTimeSeconds(), jsonUserIds);

        StopWatch three = new StopWatch();
        three.start();
        // 批量查UserInfoList
        i = 0;
        final List<UserInfo> userInfoList = new ArrayList<>(size);
        while (true) {
            if (i + MAX_SELECT_USER_COUNT < size) {
                userInfoList.addAll(this.userInfoMapper
                        .selectUserInfoList(requestBody.getUserIds().subList(i, i + MAX_SELECT_USER_COUNT)));
            } else {
                userInfoList.addAll(this.userInfoMapper.selectUserInfoList(requestBody.getUserIds().subList(i, size)));
                break;
            }
            i = i + MAX_SELECT_USER_COUNT;
        }
        three.stop();
        log.info("userInfoList end, elapsedTime: {} seconds,userIds:{}", three.getTotalTimeSeconds(), jsonUserIds);

        // user加到userVos中
        List<UserVo> userVos = new ArrayList<>();
        for (User user : userList) {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVos.add(userVo);
        }

        // UserSecurity加到UserSecurityVos
        List<UserSecurityVo> userSecurityVos = new ArrayList<>();
        userSecurityList.stream().forEach(usl -> {
            UserSecurityVo usVo = new UserSecurityVo();
            BeanUtils.copyProperties(usl, usVo);
            userSecurityVos.add(usVo);
        });

        // UserInfo加到UserInfoVos
        List<UserInfoVo> userInfoVos = new ArrayList<>();
        userInfoList.stream().forEach(uil -> {
            UserInfoVo uiVo = new UserInfoVo();
            BeanUtils.copyProperties(uil, uiVo);
            userInfoVos.add(uiVo);
        });

        return APIResponse.getOKJsonResult(new GetUserListResponse(userVos, userSecurityVos, userInfoVos));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    @Monitored
    public APIResponse<Long> getUserIdByTradingAccount(APIRequest<GetUserIdByTradingAccountRequest> request) {
        final GetUserIdByTradingAccountRequest requestBody = request.getBody();
        Long userId = userTradingAccountMapper.queryUserIdByTradingAccount(requestBody.getTradingAccount());
        log.info("getUserIdByTradingAccount done, userId:{}, tradingAccountId:{}", userId,
                requestBody.getTradingAccount());
        return APIResponse.getOKJsonResult(userId);
    }

    @SecurityLog(name = "APP忘记密码", operateType = Constant.SECURITY_OPERATE_TYPE_APP_FORGET_PSW,
            email = "#request.body.email")
    @Override
    public APIResponse<AccountForgotPasswordResponse> sendAppForgotPasswordEmail(
            APIRequest<AccountForgotPasswordRequest> request) throws Exception {
        final AccountForgotPasswordRequest requestBody = request.getBody();
        //final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        final User user = this.userMapper.queryByExistentEmail(requestBody.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        RedisVerify redisVerify =
                RedisCacheUtils.get(user.getEmail(), RedisVerify.class, CacheKeys.APP_RESET_PASSWORD_EMAIL);
        if (redisVerify == null) {
            redisVerify = new RedisVerify();
            redisVerify.setToken(TokenUtils.emailRedisToken());
            redisVerify.setTime(DateUtils.getNewUTCDate());
            redisVerify.setCode(StringUtils.getNumberRandomString(6));
        } else {
            if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount() > ERROR_COUNT) {
                throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
            }
            // 10分钟内不重复发送
            if (TokenUtils.isEmailVerifyCodeExpire(redisVerify.getToken(), UserCommonBusiness.EMAIL_GAP_TIME)) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND,
                        new Object[] {UserCommonBusiness.EMAIL_GAP_TIME});
            } else if (!TokenUtils.isEmailVerifyCodeExpire(redisVerify.getToken(), UserCommonBusiness.EXPIRED_TIME)) {
                // 30分钟有效期
                redisVerify.setToken(TokenUtils.emailRedisToken());
                redisVerify.setCode(StringUtils.getNumberRandomString(6));
            }
        }
        //发送忘记密码通知
        securityNotificationService.saveSecurityNotification(user.getUserId(),SecurityNotificationEnum.FORGET_PWD,
                request.getLanguage());
        // 发送邮件
        Map<String, Object> data = new HashMap<>();
        data.put("link", redisVerify.getCode());

        String disableToken = userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_TML_EMAIL_PWD, user, data,
                "APP忘记密码发送邮件：", requestBody.getCustomEmailLink());
        RedisCacheUtils.set(user.getEmail(), redisVerify, UserCommonBusiness.EXPIRED_TIME * 60L,
                CacheKeys.APP_RESET_PASSWORD_EMAIL);
        log.info("sendAppForgotPasswordEmail done, email:{}", user.getEmail());
        return APIResponse.getOKJsonResult(new AccountForgotPasswordResponse(user.getUserId(), user.getEmail(),
                user.getStatus(), redisVerify.getCode(), disableToken));
    }



    @Override
    public APIResponse<AccountResetPasswordVerifyResponse> resetAppPasswordVerify(
            APIRequest<AccountResetPasswordVerifyRequest> request) {
        final AccountResetPasswordVerifyRequest requestBody = request.getBody();
        final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        this.resetAppPasswordVerify(requestBody.getToken(), user);
        return APIResponse.getOKJsonResult(new AccountResetPasswordVerifyResponse(user.getUserId()));
    }

    private void resetAppPasswordVerify(final String token, final User user) {
        RedisVerify redisVerify =
                RedisCacheUtils.get(user.getEmail(), RedisVerify.class, CacheKeys.APP_RESET_PASSWORD_EMAIL);
        if (redisVerify == null) {
            throw new BusinessException(GeneralCode.USER_RESET_PSW_CODE_EXPIRED);
        }
        if (redisVerify.getErrorTime() != null
                && (redisVerify.getErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils.getNewUTCTimeMillis()) {
            throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
        }
        if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount() > ERROR_COUNT) {
            throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
        }
        if (!StringUtils.equals(redisVerify.getCode(), token)) {
            redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);
            redisVerify.setErrorTime(DateUtils.getNewUTCTimeMillis());
            RedisCacheUtils.set(user.getEmail(), redisVerify, -1L, CacheKeys.APP_RESET_PASSWORD_EMAIL);// 不修改过期时间
            throw new BusinessException(GeneralCode.USER_INVALID_RESET_PSW_CODE);
        }
    }

    @SecurityLog(name = "APP重置密码", operateType = Constant.SECURITY_OPERATE_TYPE_APP_RESET_PSW,
            email = "#request.body.email")
    @Override
    public APIResponse<AccountResetPasswordResponse> resetAppPassword(APIRequest<AccountResetPasswordRequest> request)
            throws Exception {
        final AccountResetPasswordRequest requestBody = request.getBody();
        final User user = this.userMapper.queryByEmail(requestBody.getEmail());
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        this.resetAppPasswordVerify(requestBody.getToken(), user);

        //风控是否可以提币,true可以提币、false禁止提币
        boolean riskEngineResult = ruleDecisionApiClient.unifyCheckWithdrawRule(RuleDecisionApiClient.FORGET_PWD, user.getUserId(), requestBody.getDeviceInfo());


        // 重置密码
        final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        user.setSalt(StringUtils.uuid());
        user.setPassword(PasswordUtils.encode(requestBody.getPassword(), user.getSalt(), cipherCode));

        userSecurityBusiness.updateUserPassword(user.getEmail(), user.getSalt(), user.getPassword(), riskEngineResult,forgetPasswordSwitch?UserSecurityBusiness.FORGET_PSW_TIME_PREFIX:UserSecurityBusiness.UPDATE_PSW_TIME_PREFIX);

        // 重置密码错误次数
        final UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLockEndTime(DateUtils.getNewUTCDateAddHour(-2));
        userSecurityMapper.updateByPrimaryKey(userSecurity);
        String disableToken = userCommonBusiness.sendDisableTokenEmail(riskEngineResult?AccountConstants.NODE_TYPE_RESET_PASSWORD_USABLE:Constant.NODE_TYPE_RESET_PASSWORD, user, null,
                "重置密码异常：", requestBody.getCustomForbiddenLink());

        // 使EmailVerifyCode失效
        RedisCacheUtils.del(user.getEmail(), CacheKeys.APP_RESET_PASSWORD_EMAIL);

        return APIResponse.getOKJsonResult(new AccountResetPasswordResponse(user.getUserId(), user.getEmail(),
                user.getStatus(), user.getSalt(), user.getPassword(), disableToken));
    }

    @SecurityLog(name = "修改用户信息", operateType = Constant.SECURITY_OPERATE_TYPE_MODIFY_USER,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> modifyUser(APIRequest<ModifyUserRequest> request) {

        final ModifyUserRequest requestBody = request.getBody();
        final Long requestUserId = requestBody.getUserId();
        String requestEmail = requestBody.getEmail();
        final Boolean isResetMobileNo = requestBody.getIsResetMobileNo();
        final Boolean isSpecialUser = requestBody.getIsSpecialUser();
        final Boolean isSeedUser = requestBody.getIsSeedUser();
        final String remark = requestBody.getRemark();
        if (StringUtils.isNotBlank(remark) && remark.length() > 64){
            throw new BusinessException(AccountErrorCode.REMARK_FIELD_EXCEEDS_LIMIT);
        }
        UserIndex originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == originalUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User originalUser = userMapper.queryByEmail(originalUserIndex.getEmail());
        if (null == originalUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userStatus = originalUser.getStatus();

        // 更新Email
        if (StringUtils.isNotBlank(requestEmail)) {
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
                    userStatus = BitUtils.disable(userStatus, AccountCommonConstant.USER_NOT_BIND_EMAIL);
                    newUser.setStatus(userStatus);
                    userMapper.insert(newUser);

                    UserIndex updateUserIndex = new UserIndex();
                    updateUserIndex.setUserId(requestUserId);
                    updateUserIndex.setEmail(requestEmail);
                    userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);

                    UserSecurity userSecurity = new UserSecurity();
                    userSecurity.setUserId(requestUserId);
                    userSecurity.setEmail(requestEmail);
                    userSecurityMapper.updateByPrimaryKeySelective(userSecurity);
                } else {
                    log.error("Delete user failed, userId:{}, email:{}", originalUser.getUserId(),
                            originalUser.getEmail());
                }
            }
        }
        UserStatusEx userStatusEx=new UserStatusEx(userStatus);
        log.info("requestUserId={},userStatusEx={}",requestUserId,JsonUtils.toJsonNotNullKey(userStatusEx));
        boolean isBindEmailAndBindMobile=userStatusEx.getIsUserMobile()&& !userStatusEx.getIsUserNotBindEmail().booleanValue();
        log.info("isBindEmailAndBindMobile={}",isBindEmailAndBindMobile);
        // 重置MobileNo
        if (isResetMobileNo && isBindEmailAndBindMobile) {
            UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(requestUserId);
            if (null != userSecurity && StringUtils.isNotBlank(userSecurity.getMobile())) {
                final User user = new User();
                user.setEmail(userSecurity.getEmail());
                userStatus = BitUtils.disable(userStatus, Constant.USER_MOBILE);
                user.setStatus(userStatus);
                userMapper.updateByEmail(user);

                userMobileIndexMapper.deleteByPrimaryKey(userSecurity.getMobile(), userSecurity.getMobileCode());

                final UserSecurity security = new UserSecurity();
                security.setUserId(requestUserId);
                security.setMobile(null);
                security.setMobileCode(null);
                userSecurityMapper.updateMobileByUserId(security);
            }
        }
        originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        // isSpecialUser & isSeedUser
        User updatedUser = new User();
        updatedUser.setEmail(originalUserIndex.getEmail());
        if (isSpecialUser) {
            userStatus = BitUtils.enable(userStatus, Constant.USER_SPECIAL);
        } else {
            userStatus = BitUtils.disable(userStatus, Constant.USER_SPECIAL);
        }
        if (isSeedUser) {
            userStatus = BitUtils.enable(userStatus, Constant.USER_SEND);
        } else {
            userStatus = BitUtils.disable(userStatus, Constant.USER_SEND);
        }
        updatedUser.setStatus(userStatus);
        userMapper.updateByEmail(updatedUser);

        // remark
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(requestUserId);
        if (null != userInfo) {
            userInfo.setRemark(StringUtils.defaultString(remark));
            userInfoMapper.updateByPrimaryKeySelective(userInfo);
        }

        return APIResponse.getOKJsonResult(1);
    }

    @SecurityLog(name = "修改用户昵称", operateType = Constant.SECURITY_OPERATE_TYPE_MODIFY_USER,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateNickName(APIRequest<UpdateNickNameRequest> request) {
        final UpdateNickNameRequest requestBody = request.getBody();
        final Long requestUserId = requestBody.getUserId();
        // remark
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(requestUserId);
        if (null == userInfo) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserInfo record = new UserInfo();
        record.setUserId(userInfo.getUserId());
        record.setNickName(requestBody.getNickName());
        if (StringUtils.isBlank(userInfo.getNickColor())) {
            // 背景色只设置一次
            int random = RandomUtils.nextInt(0, nickColors.length);
            record.setNickColor(nickColors[random]);
        }
        int count = userInfoMapper.updateByPrimaryKeySelective(record);
        try {
            // 同步商户昵称信息到c2c 若非商户c2c会丢弃
            SyncMerchantReq syncMerchantReq = new SyncMerchantReq();
            BeanUtils.copyProperties(record, syncMerchantReq);
            APIResponse<Integer> apiResponse = merchantApi.syncMerchantInfo(APIRequest.instance(syncMerchantReq));
            if (apiResponse.getStatus() == APIResponse.Status.ERROR) {
                log.error("同步商户昵称失败,apiResponse={}", apiResponse);
            }
        } catch (Exception e) {
            log.warn("同步商户昵称失败,userId=" + record.getUserId(), e);
        }

        notificationToC2CHelper.sendNickNameChangeMsgAsync(record);
        return APIResponse.getOKJsonResult(count);
    }



    @SecurityLog(name = "修改用户邮箱", operateType = Constant.SECURITY_OPERATE_TYPE_MODIFY_USER,
            userId = "#request.body.userId")
    @RiskTask(userId = "#request.body.userId",type = RiskTaskAspect.UPDATE_EMAIL,email= "#request.body.email")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> modifyUserEmail(APIRequest<ModifyUserEmailRequest> request) throws Exception {

        final ModifyUserEmailRequest requestBody = request.getBody();
        final Long requestUserId = requestBody.getUserId();
        String requestEmail = requestBody.getEmail();

        UserIndex originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == originalUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User originalUser = userMapper.queryByEmail(originalUserIndex.getEmail());
        if (null == originalUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 更新Email
        requestEmail=requestEmail.toLowerCase();
        if (!Pattern.matches(REGEX_EMAIL, requestEmail)) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }
        User existedUserByEmail = userMapper.queryByEmail(requestEmail);
        if (null != existedUserByEmail && existedUserByEmail.getUserId().longValue() != requestUserId.longValue()) {
            throw new BusinessException(GeneralCode.USER_EMAIL_USE);
        }else if (null != existedUserByEmail && existedUserByEmail.getUserId().longValue() == requestUserId.longValue()) {
            //新旧邮箱一致
            return APIResponse.getOKJsonResult(1);
        } else{
            int num = userMapper.deleteByEmail(originalUserIndex.getEmail());
            if (num > 0) {
                final String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
                User newUser = new User();
                newUser.setUserId(requestUserId);
                newUser.setEmail(requestEmail);
                newUser.setStatus(originalUser.getStatus());
                newUser.setInsertTime(originalUser.getInsertTime());
                newUser.setSalt(StringUtils.uuid());
                newUser.setPassword(PasswordUtils.encode(requestBody.getNewPassword(), newUser.getSalt(), cipherCode));
                //有传safePassword才能存
                if(org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getNewSafePassword())){
                    newUser.setSafePassword(CryptoAlgoUtils.validateAndHash512(requestBody.getNewSafePassword(),newUser.getSalt()));
                }
                int result = userMapper.insert(newUser);
                if(result == 1){
                    long updateTime = System.currentTimeMillis();
                    RedisCacheUtils.set(String.format(UPDATE_EMAIL_TIME_PREFIX, requestUserId), updateTime, FIVE_DAY);
                    log.info("modifyUserEmail updateTime:{}, will be expired in:{}seconds", updateTime, FIVE_DAY);
                }

                UserIndex updateUserIndex = new UserIndex();
                updateUserIndex.setUserId(requestUserId);
                updateUserIndex.setEmail(requestEmail);
                userIndexMapper.updateByPrimaryKeySelective(updateUserIndex);

                UserSecurity userSecurity = new UserSecurity();
                userSecurity.setUserId(requestUserId);
                userSecurity.setEmail(requestEmail);
                userSecurityMapper.updateByPrimaryKeySelective(userSecurity);

                // 临时的代码 完全迁移后移除 start
                Map<String, Object> dataMsg = com.google.common.collect.Maps.newHashMap();
                dataMsg.put(UserConst.USER_ID, requestUserId);
                dataMsg.put(UserConst.EMAIL, requestEmail.toLowerCase());
                MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.MODIFY_SUBACCOUNT_EMAIL, dataMsg);
                log.info("iMsgNotification modifyAccountEmail:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                this.iMsgNotification.send(msg);
            } else {
                log.error("Delete user failed, userId:{}, email:{}", originalUser.getUserId(),
                        originalUser.getEmail());
            }
        }

        return APIResponse.getOKJsonResult(1);
    }


    @SecurityLog(name = "删除用户信息", operateType = Constant.SECURITY_OPERATE_TYPE_DELETE_USER,
            userId = "#request.body.userId")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> deleteUser(APIRequest<DeleteUserRequest> request) {
        final DeleteUserRequest requestBody = request.getBody();
        final Long requestUserId = requestBody.getUserId();

        UserIndex originalUserIndex = userIndexMapper.selectByPrimaryKey(requestUserId);
        if (null == originalUserIndex) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User originalUser = userMapper.queryByEmail(originalUserIndex.getEmail());
        if (null == originalUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Long userStatus = originalUser.getStatus();
        User updatedUser = new User();
        updatedUser.setEmail(originalUserIndex.getEmail());
        userStatus = BitUtils.enable(userStatus, Constant.USER_DELETE);
        updatedUser.setStatus(userStatus);
        return APIResponse.getOKJsonResult(userMapper.updateByEmail(updatedUser));
    }

    @Override
    public APIResponse<Boolean> isExist(APIRequest<String> request) {
        User user = this.userMapper.queryByEmail(StringUtils.trimToEmpty(request.getBody()).toLowerCase());
        if (user == null) {
            return APIResponse.getOKJsonResult(false);
        }
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<Boolean> ipIsExist(APIRequest<UserIpRequest> request) {
        UserIpRequest userIp = request.getBody();
        Boolean result = false;
        if (userIpMapper.getIpCount(userIp.getUserId(), userIp.getIp()) > 0) {
            result = true;
        }
        return APIResponse.getOKJsonResult(result);
    }


    @Override
    public APIResponse<List<KycValidateResponse>> kycValidateInfo(APIRequest<BaseDetailRequest> request) {
        final BaseDetailRequest requestBody = request.getBody();
        List<KycValidateResponse> response = new ArrayList<KycValidateResponse>();

        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        // 账号不存在
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user;

        // 强制从主库读，避免读写延迟问题，确保页面展示正确的用户状态
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();

            user = this.userMapper.queryByEmail(userIndex.getEmail());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }

        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        // isValidated: -1 未做 0 审核中 1 通过 2拒绝
        // 获取Jumio结果
        SysConfig kycVerificationConfig = this.iSysConfig.selectByDisplayName("jumio_enable");
        if (kycVerificationConfig != null && "true".equalsIgnoreCase(kycVerificationConfig.getCode())) {
            KycValidateResponse kycRes = new KycValidateResponse();
            KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);
            kycRes.setGroup("KYC");
            kycRes.setKycOrAddress("kyc");
            Integer certificateStatus = certificateResult.getCertificateStatus();

            if (certificateStatus != null) {
                kycRes.setIsValidated(String.valueOf(certificateStatus));
                if (certificateResult.getCertificateMessage() != null) {
                    kycRes.setFailReason(certificateResult.getCertificateMessage());
                }
            } else {
                kycRes.setIsValidated("-1");
            }

            response.add(kycRes);
        }

        // ug basic verify 仅仅乌干达用
        if ("UG".equalsIgnoreCase(config.getProperty("TARGET_EXCHANGE", ""))) {
            AccUserKycExample example = new AccUserKycExample();
            example.createCriteria().andUserIdEqualTo(request.getBody().getUserId());
            example.setOrderByClause("create_time desc");
            List<AccUserKyc> accUserKycList = accUserKycMapper.selectByExample(example);
            KycValidateResponse kycRes = new KycValidateResponse();
            kycRes.setIsValidated("-1");
            kycRes.setGroup("KYC");
            kycRes.setKycOrAddress("kyc-basic");
            if (CollectionUtils.isNotEmpty(accUserKycList)) {
                // 如果kyc通过默认basic也通过了
                AccUserKyc accUserKyc = accUserKycList.get(0);
                if (accUserKyc.getStatus() == 1) {
                    kycRes.setIsValidated(Integer.valueOf(1).toString());
                } else if (accUserKyc.getFillFirstName() != null && accUserKyc.getFillLastName() != null
                        && accUserKyc.getFillDob() != null && accUserKyc.getNationality() != null) {
                    kycRes.setIsValidated(Integer.valueOf(1).toString());
                }
            }
            response.add(kycRes);
        }


        // User Address
        SysConfig addressVerificationConfig = this.iSysConfig.selectByDisplayName("address_verification_switch");
        if (addressVerificationConfig != null && "ON".equalsIgnoreCase(addressVerificationConfig.getCode())) {
            UserAddress userAddressPass =
                    this.userAddressMapper.getLast(user.getUserId(), UserAddress.Status.PASSED.ordinal());
            KycValidateResponse addressRes = new KycValidateResponse();
            String isVerified = "-1";
            String address = "";

            if (null != userAddressPass) {
                isVerified = "1";
                address = userAddressPass.getFullAddress();
            } else {
                UserAddress lastUserAddress = this.userAddressMapper.getLast(user.getUserId(), null);
                if (lastUserAddress != null) {
                    if (UserAddress.Status.PENDING.equals(lastUserAddress.getStatus()) || UserAddress.Status.WAITING.equals(lastUserAddress.getStatus())) {
                        isVerified = "0";
                    } else if (UserAddress.Status.REFUSED.equals(lastUserAddress.getStatus())) {
                        isVerified = "2";
                        addressRes.setFailReason(lastUserAddress.getFailReason());
                    }

                    address = lastUserAddress.getFullAddress();
                }
            }

            addressRes.setGroup("ADDRESS");
            addressRes.setIsValidated(isVerified);
            addressRes.setKycOrAddress("address");
            addressRes.setFullAddress(address);
            response.add(addressRes);
        }

        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Integer> todayRegist(APIRequest<UserRegistRequest> request) {
        final UserRegistRequest requestBody = request.getBody();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startTime", requestBody.getStartTime());
        paramMap.put("endTime", requestBody.getEndTime());
        int total = userMapper.getTodayRegist(paramMap);
        return APIResponse.getOKJsonResult(total);
    }

    @Override
    public APIResponse<Integer> saveUserIp(APIRequest<UserIpRequest> request) {
        final UserIpRequest requestBody = request.getBody();
        try {
            int count = userIpMapper.getIpCount(requestBody.getUserId(), requestBody.getIp());
            if (count > 0) {
                return APIResponse.getOKJsonResult(count);// 存在直接返回
            }
            // 不存在则insert
            UserIp uip = new UserIp();
            uip.setUserId(requestBody.getUserId());
            uip.setIp(requestBody.getIp());
            return APIResponse.getOKJsonResult(userIpMapper.insertIgnore(uip));
        } catch (Exception e) {
            log.error("save userIp error,userId-->{},error-->{}", requestBody.getUserId(), e);
            return APIResponse.getErrorJsonResult(-1);
        }
    }


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<GetUserResponse> getUserByMobile(APIRequest<UserMobileRequest> request) {
        final UserMobileRequest requestBody = request.getBody();
        UserMobileIndex userMobileIndex = null;
        if(org.apache.commons.lang3.StringUtils.isNotBlank(requestBody.getMobileCode())){
            userMobileIndex= this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(),requestBody.getMobileCode());
        }else{
            userMobileIndex= this.userMobileIndexMapper.selectByMobile(requestBody.getMobile());
        }

        if (userMobileIndex == null) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }

        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userMobileIndex.getUserId());
        if (userIndex == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        final User user = this.userMapper.queryByEmail(userIndex.getEmail());

        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userIndex.getUserId());

        UserVo userVo = null;
        if (user != null) {
            userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            userVo.setSalt(null);
            userVo.setPassword(null);
        }
        UserSecurity userSecurity=userSecurityMapper.selectByPrimaryKey(userIndex.getUserId());

        UserSecurityVo userSecurityVo = conver2SecurityVo(userSecurity);

        UserInfoVo userInfoVo = null;
        if (userInfo != null) {
            userInfoVo = new UserInfoVo();
            BeanUtils.copyProperties(userInfo, userInfoVo);
        }
        return APIResponse.getOKJsonResult(new GetUserResponse(userVo, userSecurityVo, userInfoVo));
    }

    private UserSecurityVo conver2SecurityVo(UserSecurity userSecurity) {
        if (userSecurity == null) {
            return null;
        }
        UserSecurityVo userSecurityVo = new UserSecurityVo();
        BeanUtils.copyProperties(userSecurity, userSecurityVo);
        if (!commonConfig.isWithdrawFaceSwitchOn()) {
            userSecurityVo.setWithdrawSecurityFaceStatus(UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO);
        }
        return userSecurityVo;
    }

    @Override
    public APIResponse<UserIpLikeVo> ipLikeCheck(APIRequest<UserIpLikeVo> request) {
        UserIpLikeVo vo = request.getBody();
        if (vo == null || vo.getUserId() == null || vo.getIpList() == null || vo.getIpList().isEmpty()) {
            log.warn("没有需要检查的IP信息");
            return APIResponse.getOKJsonResult(vo);
        }
        // 获取用户的所有IP;
        List<UserIp> userIps = userIpMapper.getIpByUser(vo.getUserId());
        if (userIps == null || userIps.isEmpty()) {
            // 获取不到的时候，按默认值返回
            return APIResponse.getOKJsonResult(vo);
        }
        for (UserIpLikeVo.IpLikeVo ipLikeVo : vo.getIpList()) {
            String tempIp = ipLikeVo.getIp();
            // 如果tempIp 为空值不检查，
            if (StringUtils.isBlank(tempIp)) {
                continue;
            }
            for (UserIp userIp : userIps) {
                // 取IP的前三段与tempIp 进行比对，如果前三端于用户的IP能匹配，认为能匹配上
                String ipData = tempIp.substring(0, tempIp.lastIndexOf('.') + 1);
                if (userIp.getIp().startsWith(ipData)) {
                    // 前三段匹配上，设置匹配成功, 退出当前内部循环
                    ipLikeVo.setExist(1);
                    break;
                }
            }
        }
        return APIResponse.getOKJsonResult(vo);
    }

    @Override
    public List<UserIpVo> getUserIpList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<UserIp> userIps = userIpMapper.getIpByUser(userId);
        if (userIps == null || userIps.isEmpty()) {
            return Collections.emptyList();
        }
        return userIps.stream()
                .map(item -> {
                    UserIpVo vo = new UserIpVo();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private static Map<String, String> changeMap = Maps.newHashMap();
    static {
        changeMap.put("123", "83");
        changeMap.put("124", "84");
        changeMap.put("125", "85");
        changeMap.put("127", "81");
        changeMap.put("129", "82");
        changeMap.put("162", "32");
        changeMap.put("163", "33");
        changeMap.put("164", "34");
        changeMap.put("165", "35");
        changeMap.put("166", "36");
        changeMap.put("167", "37");
        changeMap.put("168", "38");
        changeMap.put("169", "39");
        changeMap.put("120", "70");
        changeMap.put("121", "79");
        changeMap.put("122", "77");
        changeMap.put("126", "76");
        changeMap.put("128", "78");
        changeMap.put("188", "58");
        changeMap.put("186", "56");
        changeMap.put("199", "59");

        changeMap.put("0123", "083");
        changeMap.put("0124", "084");
        changeMap.put("0125", "085");
        changeMap.put("0127", "081");
        changeMap.put("0129", "082");
        changeMap.put("0162", "032");
        changeMap.put("0163", "033");
        changeMap.put("0164", "034");
        changeMap.put("0165", "035");
        changeMap.put("0166", "036");
        changeMap.put("0167", "037");
        changeMap.put("0168", "038");
        changeMap.put("0169", "039");
        changeMap.put("0120", "070");
        changeMap.put("0121", "079");
        changeMap.put("0122", "077");
        changeMap.put("0126", "076");
        changeMap.put("0128", "078");
        changeMap.put("0188", "058");
        changeMap.put("0186", "056");
        changeMap.put("0199", "059");
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED)
    @Override
    public APIResponse<Boolean> fixVNUser(APIRequest<UserIdRequest> request) {
        UserIdRequest body = request.getBody();
        Long userId = body.getUserId();

        if (userId == null) {
            // 全量处理
            log.info("userId 为空");
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserSecurity user = userSecurityMapper.selectByPrimaryKey(userId);
        if (user == null) {
            log.info("找不到用户");
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        if (StringUtils.isEmpty(user.getMobileCode())) {
            log.info("mobileCode为空");
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        if (!user.getMobileCode().equals("vn") && !user.getMobileCode().equals("VN")
                && !user.getMobileCode().equals("vnm") && !user.getMobileCode().equals("VNM")) {
            log.info("不是越南用户");
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        for (Map.Entry<String, String> map : changeMap.entrySet()) {
            if (user.getMobile().trim().startsWith(map.getKey())) {
                String oldMobile = user.getMobile().trim();
                // change to new format
                String newMobile =
                        map.getValue() + user.getMobile().trim().substring(map.getKey().length(), oldMobile.length());

                user.setMobile(newMobile);

                userSecurityMapper.updateMobileByUserId(user);
                // 修改user_security完毕

                UserMobileIndex index = userMobileIndexMapper.selectByMobile(oldMobile);
                if (index == null) {
                    // 找不到index？？
                    log.info("找不到index");
                    return APIResponse.getOKJsonResult(false);
                } else {
                    userMobileIndexMapper.deleteByPrimaryKey(index.getMobile(), index.getCountry());

                    index.setMobile(newMobile);
                    userMobileIndexMapper.insert(index);

                    log.info("fixVNUser处理完毕：{}", userId);
                }
            }
        }

        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<UserStatusEx> getUserStatusByUserId(APIRequest<UserIdRequest> request) {
        final UserIdRequest requestBody = request.getBody();

        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        User user;
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            user = this.userMapper.queryByEmail(userIndex.getEmail());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        if (null == user) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return APIResponse.getOKJsonResult(new UserStatusEx(user.getStatus()));
    }

    @Override
    public APIResponse<Integer> saveRecaptcha(APIRequest<ReCaptchaReq> request) {
        final ReCaptchaReq requestBody = request.getBody();
        try {
            ReCaptcha reCaptcha = new ReCaptcha();
            reCaptcha.setUserId(requestBody.getUserId());
            reCaptcha.setEmail(requestBody.getEmail());
            reCaptcha.setScore(requestBody.getScore());
            reCaptcha.setAction(requestBody.getAction());
            if (requestBody.getSuccess() != null) {
                reCaptcha.setSuccess(String.valueOf(requestBody.getSuccess()));
            }
            reCaptcha.setChallengeTs(requestBody.getChallengeTs());
            reCaptcha.setErrorCodes(requestBody.getErrorCodes());

            return APIResponse.getOKJsonResult(userMapper.saveRecaptcha(reCaptcha));
        } catch (Exception e) {
            log.error("saveRecaptcha error:{},email:{}", e, requestBody.getEmail());
            return APIResponse.getErrorJsonResult(0);
        }
    }

    @Override
    @Monitored
    public APIResponse<Long> countAgentNumber(APIRequest<CountAgentNumberRequest> request) throws Exception {
        CountAgentNumberRequest requestBody = request.getBody();
        Long agentId=requestBody.getAgentId();
        //这里也没什么逻辑，拿到推荐人id执行下sql统计一下

        Long agentNumber=this.getTotalByAgent(agentId);
        return APIResponse.getOKJsonResult(agentNumber);
    }

    /**
     * margin账户的创建方法
     * 逻辑很简单，任何一部事务操作失败会回滚上下文中的所有操作，这样的话数据比较干净
     * 所以这个接口不应该去做幂等，而是应该通过数据库唯一索引触发事务回滚
     * 注意：AopContext.currentProxy()是为了强制开启spring动态代理来托管事务,反正会失效
     */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @FrontTask(routingKey = FrontPushEventType.OPEN_ACCOUNT_COMPLETE_ROUTING,userId = "#request.body.userId",eventType = FrontPushEventType.OPEN_ACCOUNT_COMPLETE,accountType = FrontPushEventType.OPEN_ACCOUNT_COMPLETE_MARGIN)
    @UserPermissionValidate(userId = "#request.body.userId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_MARGIN)
    public APIResponse<CreateMarginUserResponse> createMarginAccount(APIRequest<CreateMarginAccountRequest> request) throws Exception {
        final CreateMarginAccountRequest requestBody = request.getBody();
        //校验并且获取主账户信息  ,rootuser这里解释为主账户
        Pair<User, UserInfo> rootTuple = checkAndGetUserByIdForMarginVersion(requestBody.getUserId(),requestBody.getParentUserId());
        //获取主账号相关信息
        User rootUser = rootTuple.getLeft();
        UserInfo rootUserInfo = rootTuple.getRight();
        if(null!=rootUserInfo.getMarginUserId()){
            CreateMarginUserResponse createMarginUserResponse = new CreateMarginUserResponse();
            createMarginUserResponse.setRootUserId(rootUserInfo.getUserId());
            createMarginUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
            UserStatusEx rootUserStatusEx=new UserStatusEx(rootUser.getStatus());
            UserInfo oldMarginUserInfo = userInfoMapper.selectByPrimaryKey(rootUserInfo.getMarginUserId());
            if(null==oldMarginUserInfo||null==oldMarginUserInfo.getTradingAccount()){
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            createMarginUserResponse.setMarginUserId(oldMarginUserInfo.getUserId());
            createMarginUserResponse.setMarginTradingAccount(oldMarginUserInfo.getTradingAccount());
            createMarginUserResponse.setIsSubUser(rootUserStatusEx.getIsSubUser());
            createMarginUserResponse.setIsBrokerSubUser(rootUserStatusEx.getIsBrokerSubUser());
            return APIResponse.getOKJsonResult(createMarginUserResponse);
        }
        //开始创建margin账号相关信息
        String vritualEmail = createVirtualEmail(rootUser.getEmail(), rootUser.getUserId());
        User marginUser = ((UserBusiness) AopContext.currentProxy()).createMarginUser(vritualEmail);
        // 创建margin账号Security信息
        ((UserBusiness) AopContext.currentProxy()).createMarginUserSecurity(marginUser.getUserId(), marginUser.getEmail());
        // 创建margin账号info信息
        UserInfo marginUserInfo = ((UserBusiness) AopContext.currentProxy()).createMarginUserInfo(rootUserInfo, marginUser.getUserId());
        // 更新root表中的marginuserid （这步操作是幂等的）
        rootUserInfo.setMarginUserId(marginUser.getUserId());
        userInfoMapper.updateByPrimaryKeySelective(rootUserInfo);
        //更新主账户的状态(这步操作也是幂等的),这是用来标明这个账户是否拥有margin账户
        rootUser.setStatus(BitUtils.enable(rootUser.getStatus(), Constant.USER_IS_EXIST_MARGIN_ACCOUNT));
        userMapper.updateByEmailSelective(rootUser);
        // 创建margin交易账户
        //这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long marginTradingAccount = matchboxApiClient.postAccount(marginUserInfo, MatchBoxAccountTypeEnum.MARGIN);
        marginUserInfo.setTradingAccount(marginTradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(marginUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(marginTradingAccount);
        userTradingAccount.setUserId(marginUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        log.info("UserBusiness.postAccount insert:{}", JSON.toJSONString(userTradingAccount));
        UserStatusEx rootUserStatusEx=new UserStatusEx(rootUser.getStatus());
        //需要调用api回写给margin系统
        Boolean isSubUser=rootUserStatusEx.getIsSubUser();

        if(null!=requestBody.getNeedSyncMargin() && requestBody.getNeedSyncMargin().booleanValue() ){
            marginAccountApiClient.newMarginAccount(rootUserInfo.getUserId(),rootUserInfo.getTradingAccount(),
                    marginUserInfo.getUserId(), marginTradingAccount,isSubUser);
        }
        if(isSubUser){
            SubUserBinding updateSubUserBinding=new SubUserBinding();
            updateSubUserBinding.setParentUserId(rootUserInfo.getParent());
            updateSubUserBinding.setSubUserId(rootUserInfo.getUserId());
            updateSubUserBinding.setMarginUserId(marginUser.getUserId());
            log.info("updateSelectiveBySubUserIdAndParentUserId={}",JsonUtils.toJsonNotNullKey(updateSubUserBinding));
            subUserBindingMapper.updateSelectiveBySubUserIdAndParentUserId(updateSubUserBinding);
        }

        //还需要更新是否燃烧bnb的标志位

        AsyncTaskExecutor.execute(() -> {
            HintManager hintManager = null;
            try {
                TimeUnit.SECONDS.sleep(1);
                hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
                log.info("updateMarginBNBFee start userid={}",rootUser.getUserId());
                if (BitUtils.isTrue(rootUser.getStatus(), Constant.USER_FEE)) {
                    userSecurityBusiness.setMarginBnbFee(rootUser.getUserId(),true);
                }else{
                    userSecurityBusiness.setMarginBnbFee(rootUser.getUserId(),false);
                }
                log.info("updateMarginBNBFee finish userid={}",rootUser.getUserId());
            } catch (Exception e) {
                log.error("updateMarginBNBFee sleep exception", e);
            }finally {
                if (null != hintManager) {
                    hintManager.close();
                }
            }
            try {
                // 加入推荐记录表
                insertToAgentLog(requestBody.getUserId(), marginUser.getUserId(), marginUser.getEmail());
            } catch (Exception e) {
                log.error("insertToAgentLog exception", e);
            }
            try {
                userCommonBusiness.insertInfoRootUserIndex(requestBody.getUserId(),marginUser.getUserId(), com.binance.account.constants.enums.UserTypeEnum.MARGIN.name());
            }catch (Exception e){
                log.error("insertInfoRootUserIndex margin exception", e);
            }
            try{
                UserStatusEx userStatusEx=new UserStatusEx(rootUser.getStatus());
                if(userStatusEx.getIsSubUserFunctionEnabled()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    dataMsg.put("parentUserId", rootUser.getUserId());
                    MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE_ONLY_PARENT_MARGIN, dataMsg);
                    log.info("iMsgNotification sendUserProductFeeMsgFroMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                    iMsgNotification.send(msg);
                }else if(userStatusEx.getIsSubUser()){
                    Map<String, Object> dataMsg = new HashMap<>();
                    SubUserBinding subUserBinding=subUserBindingMapper.selectBySubUserId(rootUser.getUserId());
                    if(null!=subUserBinding){
                        dataMsg.put("parentUserId", subUserBinding.getParentUserId());
                        dataMsg.put("subUserId", rootUser.getUserId());
                        MsgNotification msg = new MsgNotification(SysType.PNK_ADMIN, MsgNotification.OptType.USER_PRODUCT_FEE, dataMsg);
                        log.info("iMsgNotification sendUserProductFeeMsgFroMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
                        iMsgNotification.send(msg);
                    }
                }else {
                    log.info("rootUserId is not parentUser or subUser:{}", rootUser.getUserId());

                }
            }catch (Exception e){
                log.error("sendUserProductFeeMsgFroMargin exception", e);

            }

        });
        //发送消息回写给pnk库 后续需要删除，临时双写
        sendRegisterMqMsgForMargin(marginUser, marginUserInfo);
        CreateMarginUserResponse createMarginUserResponse = new CreateMarginUserResponse();
        createMarginUserResponse.setRootUserId(rootUserInfo.getUserId());
        createMarginUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createMarginUserResponse.setMarginUserId(marginUserInfo.getUserId());
        createMarginUserResponse.setMarginTradingAccount(marginTradingAccount);
        createMarginUserResponse.setIsSubUser(rootUserStatusEx.getIsSubUser());
        createMarginUserResponse.setIsBrokerSubUser(rootUserStatusEx.getIsBrokerSubUser());
        return APIResponse.getOKJsonResult(createMarginUserResponse);
    }

    /**
     * fiat账户的创建方法 逻辑很简单，任何一部事务操作失败会回滚上下文中的所有操作，这样的话数据比较干净 所以这个接口不应该去做幂等，而是应该通过数据库唯一索引触发事务回滚
     * 注意：AopContext.currentProxy()是为了强制开启spring动态代理来托管事务,反正会失效
     */
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @UserPermissionValidate(userId = "#request.body.userId",userPermissionOperation = UserPermissionOperationEnum.ENABLE_CREATE_FIAT)
    public APIResponse<CreateFiatUserResponse> createFiatAccount(APIRequest<UserIdRequest> request, boolean ignoreKycCountryCheck)
            throws Exception {
        log.info("createFiatAccount start. userId={}", request.getBody().getUserId());
        Long total = RedisCacheUtils.get(String.valueOf(request.getBody().getUserId()), Long.class,
                ACCOUNT_CREATE_FIAT_USER);
        if (total != null) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        RedisCacheUtils.set(String.valueOf(request.getBody().getUserId()), 1L, 10L, ACCOUNT_CREATE_FIAT_USER);
        final UserIdRequest requestBody = request.getBody();
        // 校验并且获取主账户信息 ,rootuser这里解释为主账户
        Pair<User, UserInfo> rootTuple = checkAndGetUserByIdForFiatVersion(requestBody.getUserId(), ignoreKycCountryCheck);

        // 获取主账号相关信息
        User rootUser = rootTuple.getLeft();
        UserInfo rootUserInfo = rootTuple.getRight();

        // 已经创建法币账户直接幂等返回
        if (null != rootUserInfo.getFiatUserId()) {
            UserInfo existFiatUser = userInfoMapper.selectByPrimaryKey(rootUserInfo.getFiatUserId());
            CreateFiatUserResponse createFiatUserResponse = new CreateFiatUserResponse();
            createFiatUserResponse.setRootUserId(rootUserInfo.getUserId());
            createFiatUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
            createFiatUserResponse.setFiatUserId(existFiatUser.getUserId());
            createFiatUserResponse.setFiatTradingAccount(existFiatUser.getTradingAccount());
            return APIResponse.getOKJsonResult(createFiatUserResponse);
        }

        // 开始创建fiat账号相关信息
        String vritualEmail = createFiatVirtualEmail(rootUser.getEmail(), rootUser.getUserId());
        User fiatUser = ((UserBusiness) AopContext.currentProxy()).createFiatUser(vritualEmail);
        // 创建fiat账号Security信息
        ((UserBusiness) AopContext.currentProxy()).createVirtualUserSecurity(fiatUser.getUserId(), fiatUser.getEmail());
        // 创建margin账号info信息
        UserInfo fiatUserInfo =
                ((UserBusiness) AopContext.currentProxy()).createVirtualUserInfo(rootUserInfo, fiatUser.getUserId());
        // 更新root表中的fiat_userid （这步操作是幂等的）
        rootUserInfo.setFiatUserId(fiatUser.getUserId());
        userInfoMapper.updateByPrimaryKeySelective(rootUserInfo);
        // 更新主账户的状态(这步操作也是幂等的),这是用来标明这个账户是否拥有margin账户
        rootUser.setStatus(BitUtils.enable(rootUser.getStatus(), Constant.USER_IS_EXIST_FIAT_ACCOUNT));
//        userMapper.updateByEmailSelective(rootUser);
        userMapper.enableStatus(rootUser.getEmail(), Constant.USER_IS_EXIST_FIAT_ACCOUNT);
        // 创建FIAT交易账户，类型跟margin一致
        // 这里不需要考虑创建账户失败的case，因为我在postAccount方法里面已经处理了，出错直接抛出异常，所以上层调用不要再判断了
        Long fiatTradingAccount = matchboxApiClient.postAccount(fiatUserInfo, MatchBoxAccountTypeEnum.C2C);
        fiatUserInfo.setTradingAccount(fiatTradingAccount);
        this.userInfoMapper.updateByPrimaryKeySelective(fiatUserInfo);
        UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
        userTradingAccount.setTradingAccount(fiatTradingAccount);
        userTradingAccount.setUserId(fiatUserInfo.getUserId());
        this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        // 需要调用api回写给c2c系统
        CreateFiatUserReq createFiatUserReq = new CreateFiatUserReq();
        createFiatUserReq.setUserId(rootUserInfo.getUserId());
        createFiatUserReq.setTradingAccount(rootUserInfo.getTradingAccount());
        createFiatUserReq.setFiatUserId(rootUserInfo.getFiatUserId());
        createFiatUserReq.setFiatTradingAccount(fiatTradingAccount);
        createFiatUserReq.setEmail(rootUser.getEmail());
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(rootUserInfo.getUserId());
        createFiatUserReq.setMobile(userSecurity.getMobile());
        try {
            fiatAccountApiClient.newFiatAccount(createFiatUserReq);
        } catch (Exception e) {
            // 吃掉异常，防止因为回写失败导致回滚，c2c会有补偿机制。
            log.warn("createFiatAccount 回写c2c失败", e);
        }
        log.info("UserBusiness.postAccount insert:{}", JSON.toJSONString(userTradingAccount));
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        if(isSubUser){
            SubUserBinding updateSubUserBinding=new SubUserBinding();
            updateSubUserBinding.setParentUserId(rootUserInfo.getParent());
            updateSubUserBinding.setSubUserId(rootUserInfo.getUserId());
            updateSubUserBinding.setFiatUserId(fiatUser.getUserId());
            log.info("updateSelectiveBySubUserIdAndParentUserId={}",JsonUtils.toJsonNotNullKey(updateSubUserBinding));
            subUserBindingMapper.updateSelectiveBySubUserIdAndParentUserId(updateSubUserBinding);
        }

        AsyncTaskExecutor.execute(() -> {
            try {
                // 加入推荐记录表
                insertToAgentLog(requestBody.getUserId(), fiatUser.getUserId(), fiatUser.getEmail());
            } catch (Exception e) {
                log.error("insertToAgentLog exception", e);
            }
            try {
                userCommonBusiness.insertInfoRootUserIndex(requestBody.getUserId(),fiatUser.getUserId(), com.binance.account.constants.enums.UserTypeEnum.FIAT.name());
            }catch (Exception e){
                log.error("insertInfoRootUserIndex fiat exception", e);
            }
            notificationToC2CHelper.sendActiveAccountMsgAsync(rootUserInfo, fiatUserInfo, userSecurity);
        });
        // 发送消息回写给pnk库 后续需要删除，临时双写
        sendRegisterMqMsgForFiat(fiatUser, fiatUserInfo);
        CreateFiatUserResponse createFiatUserResponse = new CreateFiatUserResponse();
        createFiatUserResponse.setRootUserId(rootUserInfo.getUserId());
        createFiatUserResponse.setRootTradingAccount(rootUserInfo.getTradingAccount());
        createFiatUserResponse.setFiatUserId(fiatUserInfo.getUserId());
        createFiatUserResponse.setFiatTradingAccount(fiatTradingAccount);
        return APIResponse.getOKJsonResult(createFiatUserResponse);
    }

    /**
     * 发送用户注册MQ消息至PNK同步数据（针对创建margin账户）
     *
     * @param user
     * @param userInfo
     */
    public void sendRegisterMqMsgForMargin(User user, UserInfo userInfo) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", "");
        dataMsg.put("code", "");
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        if (WebUtils.getHttpServletRequest() != null) {
            dataMsg.put("ipAddress", WebUtils.getRequestIp());
        }
        dataMsg.put("tradingAccount", userInfo.getTradingAccount());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.CREATE_MARGIN, dataMsg);
        log.info("iMsgNotification sendRegisterMqMsgForMargin:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }

    protected void sendRegisterMqMsgForFiat(User user, UserInfo userInfo) {
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, user.getEmail());
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", "");
        dataMsg.put("code", "");
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        if (WebUtils.getHttpServletRequest() != null) {
            dataMsg.put("ipAddress", WebUtils.getRequestIp());
        }
        dataMsg.put("tradingAccount", userInfo.getTradingAccount());
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.CREATE_FIAT, dataMsg);
        log.info("iMsgNotification sendRegisterMqMsgForMargin:{}",
                LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        iMsgNotification.send(msg);
    }

    @Override
    public APIResponse<Boolean> isMarginRelationShip(APIRequest<MarginRelationShipRequest> request) throws Exception {
        final MarginRelationShipRequest requestBody = request.getBody();
        Long firstUserId=requestBody.getFirstUserId();
        Long secondUserId=requestBody.getSecondUserId();
        try{
            if (null == firstUserId ||null == secondUserId) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(firstUserId);
            if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            final User fisrtUser = userMapper.queryByEmail(userIndex.getEmail());
            if (null == fisrtUser) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            //两种情况，要么first是主账号，要么second是主账号
            Boolean isMarginRelationShip=false;
            if (BitUtils.isEnable(fisrtUser.getStatus(), AccountConstants.USER_IS_MARGIN_USER)) {
                final UserInfo secondUserInfo =  userInfoMapper.selectByPrimaryKey(secondUserId);
                isMarginRelationShip=firstUserId.equals(secondUserInfo.getMarginUserId());
            }else{
                final UserInfo fisrtUserInfo =  userInfoMapper.selectByPrimaryKey(firstUserId);
                isMarginRelationShip=secondUserId.equals(fisrtUserInfo.getMarginUserId());
            }
            return APIResponse.getOKJsonResult(isMarginRelationShip);
        }catch (Exception e){
            log.error("isMarginRelationShip firstIdUserid="+request.getBody().getFirstUserId()+",error:", e);
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
    }

    @Override
    public APIResponse<List<GetBatchUserTypeListResponse>> getBatchUserTypeList(APIRequest<GetBatchUserTypeListRequest> request) throws Exception {
        GetBatchUserTypeListRequest getBatchUserTypeListRequest= request.getBody();
        List<Long> userIdList=getBatchUserTypeListRequest.getUserIdList();
        List<User> userList= userMapper.selectByUserIds(userIdList);
        if(CollectionUtils.isEmpty(userIdList)){
            log.info("getBatchUserTypeList: query userList is empty,request userIdList={}",userIdList);
            return APIResponse.getOKJsonResult(Lists.newArrayList());
        }
        List<GetBatchUserTypeListResponse> resultList= Lists.transform(userList, new Function<User, GetBatchUserTypeListResponse>() {
            @Override
            public GetBatchUserTypeListResponse apply(@Nullable User user) {
                GetBatchUserTypeListResponse result=new GetBatchUserTypeListResponse();
                result.setUserId(user.getUserId());
                result.setEmail(user.getEmail());
                if (BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {
                    result.setUserType(CommonUserType.PARENT.name());
                } else if (BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)) {
                    result.setUserType(CommonUserType.SUB.name());
                }else if(BitUtils.isEnable(user.getStatus(), Constant.USER_IS_MARGIN_USER)){
                    result.setUserType(CommonUserType.MARGIN.name());
                } else {
                    result.setUserType(CommonUserType.NORMAL.name());
                }
                return result;
            }
        });
        return APIResponse.getOKJsonResult(resultList);
    }

    @Override
    public APIResponse<Long> getAccountIdByRootUserId(APIRequest<GetAccountIdByRootUserIdRequest> request) throws Exception {
        GetAccountIdByRootUserIdRequest getAccountIdByRootUserIdRequest= request.getBody();
        Long rootUserId=getAccountIdByRootUserIdRequest.getRootUserId();
        AccountTypeEnum accountType=getAccountIdByRootUserIdRequest.getAccountType();
        UserInfo rootUserInfo=userInfoMapper.selectByPrimaryKey(rootUserId);
        if(Objects.isNull(rootUserInfo)||Objects.isNull(rootUserInfo.getTradingAccount())){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        switch (accountType) {
            case SPOT:
               return APIResponse.getOKJsonResult(rootUserInfo.getTradingAccount());
            case MARGIN:
                Long marginUserId=rootUserInfo.getMarginUserId();
                if(marginUserId == null){
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                UserInfo marginUserInfo=userInfoMapper.selectByPrimaryKey(marginUserId);
                if(Objects.isNull(marginUserInfo)||Objects.isNull(marginUserInfo.getTradingAccount())){
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                return APIResponse.getOKJsonResult(marginUserInfo.getTradingAccount());
            case FUTURE:
                Long futureUserId = rootUserInfo.getFutureUserId();
                if (Objects.isNull(futureUserId)) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                if (Objects.isNull(futureUserInfo) || Objects.isNull(futureUserInfo.getMeTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                return APIResponse.getOKJsonResult(futureUserInfo.getMeTradingAccount());
            case FUTURE_DELIVERY:
                // 交割合约和默认的永续合约，使用同一futureUserId
                Long futureUserId1 = rootUserInfo.getFutureUserId();
                if (Objects.isNull(futureUserId1)) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                UserInfo futureUserInfo1 = userInfoMapper.selectByPrimaryKey(futureUserId1);
                if (Objects.isNull(futureUserInfo1) || Objects.isNull(futureUserInfo1.getDeliveryTradingAccount())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                return APIResponse.getOKJsonResult(futureUserInfo1.getDeliveryTradingAccount());
            default:
                throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
    }

    /**
     * 校验用户信息（margin version）
     *
     * @return Pair 返回的是一个元组，主要是不想再单独包个对象了，为了简单
     */
    protected Pair<User, UserInfo> checkAndGetUserByIdForMarginVersion(Long userId,Long parentUserId) throws Exception {
        if (null == userId) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        UserKycApprove userKyc = userKycApproveMapper.selectByPrimaryKey(userId);
        Boolean checkRiskResult=riskSecurityApiClient.checkUserRisk(userId, CheckUserRiskRequestVo.RiskScenario.Margin);
        if(null!=checkRiskResult && checkRiskResult.booleanValue()&& null==userKyc){
            throw new BusinessException(AccountErrorCode.PLEASE_FINISH_KYC_FIRST_BEFORE_OPENNING_MARGIN_ACCOUNT);
        }
        final User rootUser = userMapper.queryByEmail(userIndex.getEmail());
        if (null == rootUser) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        //检查传进来的userid是否是margin的userid
        //当前账号不能是margin 账号
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_MARGIN_USER)) {
            throw new BusinessException(AccountErrorCode.MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT);
        }
        //当前账号不能是future
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_FUTURE_USER)) {
            throw new BusinessException(AccountErrorCode.SUB_USER_CANNOT_CREATE_FUTURE_ACCOUNT);
        }
        //当前账号没有激活
        if (!BitUtils.isEnable(rootUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(AccountErrorCode.ACTIVE_MARGIN_ACCOUNT_FAILED);
        }

        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(userId);

        parentUserId=rootUserInfo.getParent();
        if(null!=parentUserId){
            HintManager hintManager =HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            final UserIndex parentUserIndex = userIndexMapper.selectByPrimaryKey(parentUserId);
            if (null == parentUserIndex || StringUtils.isBlank(parentUserIndex.getEmail())) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            final User parentUser = userMapper.queryByEmail(parentUserIndex.getEmail());
            if (null == parentUser) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            if (BitUtils.isFalse(parentUser.getStatus(), Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED) &&
                    BitUtils.isEnable(parentUser.getStatus(), Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {
                List<Long> subUserIds = subUserBindingMapper.selectSubUserIdsByParent(parentUserId);
                if(null!=subUserIds && subUserIds.size()>0){
                    Long marginUserNum= userMapper.countSubMarginUser(subUserIds);
                    log.info("create marginlimit:parentid={},marginUserNum={}",parentUserId,marginUserNum);
                    if(null!=marginUserNum && marginUserNum.intValue()>=subUserMarginCreateLimit){
                        log.info("create margin over limit :parentid={},marginUserNum={}",parentUserId,marginUserNum);
                        throw new BusinessException(AccountErrorCode.CREATE_SUBUSER_MARGIN_OVERLIMIT);
                    }
                }
            }
            if (null != hintManager) {
                    hintManager.close();
            }
        }


        //当前账号是否是子账号
        Boolean isSubUser=BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER);
        Boolean needCheckSubUserValidate= isSubUser && Objects.isNull(parentUserId);

        //判断用户的kyc国家是否在黑名单

        if(needCheckSubUserValidate){
                checkCountryBackListByIp(userId);
        }

        if(!isSubUser){
                checkKycCountryBackList(userId);
        }

        Pair<User, UserInfo> twoTuple = Pair.of(rootUser, rootUserInfo);
        return twoTuple;
    }/**/

    /**
     * 校验用户信息（fiat version）
     *
     * @return Pair 返回的是一个元组，主要是不想再单独包个对象了，为了简单
     */
    protected Pair<User, UserInfo> checkAndGetUserByIdForFiatVersion(Long userId, boolean ignoreKycCountryCheck) throws Exception {
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
        // 检查传进来的userid是否是fiat的userid
        // 当前账号不能是fiat 账号
        if (BitUtils.isEnable(rootUser.getStatus(), USER_IS_FIAT_USER)) {
            throw new BusinessException(AccountErrorCode.FIAT_USER_CANNOT_CREATE_FIAT_ACCOUNT);
        }
        // 当前账号没有激活
        if (!BitUtils.isEnable(rootUser.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(AccountErrorCode.ACTIVE_FIAT_ACCOUNT_FAILED);
        }
        UserInfo rootUserInfo = userInfoMapper.selectByPrimaryKey(userId);
        // 已经创建过fiat账号的就别再反复创建了 ----此段校验已移出上层
        // if (null != rootUserInfo.getFiatUserId()) {
        //     throw new BusinessException(AccountErrorCode.DONT_CREATE_FIAT_ACCOUNT_AGAIN);
        // }

        // 当前账号是否绑定中国手机号码
        // if (userSecurity == null || !StringUtils.equalsIgnoreCase("CN", userSecurity.getMobileCode())) {
        // throw new BusinessException(AccountErrorCode.BIND_CHINA_MOBILE);
        // }
        // 判断用户的kyc国家是否在黑名单,子账户不判断kyc，因为子账户做不了kyc
        Long kycUserId = userId;
        if (BitUtils.isEnable(rootUser.getStatus(), Constant.USER_IS_SUBUSER)) {
            // 如果是子账户，需要判断母账户是否做过中国kyc
            kycUserId = rootUserInfo.getParent();
        }
        if (!ignoreKycCountryCheck) {
            isKycOk(kycUserId);
        }

        Pair<User, UserInfo> twoTuple = Pair.of(rootUser, rootUserInfo);
        return twoTuple;
    }

    /**
     * 判断当前用户是否通过kyc
     */
    protected Boolean checkUserWhetherPassKyc(Long userId) throws Exception {
        APIRequest<UserIdRequest> originRequest = new APIRequest<UserIdRequest>();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        UserIdRequest request = new UserIdRequest();
        request.setUserId(userId);
        return kyc.checkUserWhetherPassKyc(APIRequest.instance(originRequest, request)).getData();
    }


    /**
     * 判断当前用户的ip是否在国家黑名单
     */
    protected void checkCountryBackListByIp(Long userId) throws Exception {
        final String ip = WebUtils.getRequestIp();
        log.info("checkCountryBackListByIp userId={},ip={}", userId, ip);
        if (org.apache.commons.lang3.StringUtils.isBlank(futureIpCountryBlackList)) {
            return;
        }
        String[] countryBlackArry = futureIpCountryBlackList.split(",");
        List<String> countryBlackList = Lists.newArrayList(countryBlackArry);
        String countryCode = null;
        try {
            countryCode = IP2LocationUtils.getCountryShort(ip);
        } catch (Exception e) {
            log.error("checkCountryBackListByIp.getCountryShort error:", e);
            return;
        }
        if (countryBlackList.contains(countryCode)) {
            throw new BusinessException(AccountErrorCode.MARGIN_IP_COUNTRY_NOT_SPPORT);
        }
    }

    /**
     * 判断当前用户的国家是否在国家黑名单
     */
    protected void checkKycCountryBackList(Long userId) throws Exception {
        UserKycCountryResponse userKycCountryResponse= kycApiTransferAdapter.getKycCountry(userId);
        String countryCode=null;
        if (null != userKycCountryResponse
                && org.apache.commons.lang3.StringUtils.isNotBlank(userKycCountryResponse.getCountryCode())) {
            countryCode=userKycCountryResponse.getCountryCode();
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(countryCode)) {
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

    /**
     * 判断用户kyc country是否允许创建fiat account
     */
    protected void isKycOk(Long userId) throws Exception {
        UserKycCountryResponse userKycCountryResponse = kycApiTransferAdapter.getKycCountry(userId);
//        if (userKycCountryResponse != null && userKycCountryResponse.getCountryCode() != null
//                && fiatAccountKycCountries.contains(userKycCountryResponse.getCountryCode().toUpperCase())) {
//            return;
//        }
        if (userKycCountryResponse != null) {
            return;
        }
        log.info("isKycOk() block user: {}", userId);
        throw new BusinessException(AccountErrorCode.KYC_STATUS_NOT_PASSED);
    }


    /**
     * 创建用户（不幂等，但是会回滚）
     *
     * @param vritualEmail
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createMarginUser(final String vritualEmail) throws NoSuchAlgorithmException {
        //这里加了事务回滚，所以如果报错数据直接回滚
        User marginUser = null;
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(vritualEmail);
        marginUser = onlyCreateMarginUser(userIndex);
        return marginUser;

    }

    /**
     * 创建用户（不幂等，但是会回滚）
     *
     * @param virtualEmail
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected User createFiatUser(final String virtualEmail) throws NoSuchAlgorithmException {
        // 这里加了事务回滚，所以如果报错数据直接回滚
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(virtualEmail);
        User fiatUser = onlyCreateUser(userIndex, USER_IS_FIAT_USER);
        return fiatUser;

    }

    /**
     * 创建margin账户逻辑
     */
    protected User onlyCreateMarginUser(UserIndex userIndex) throws NoSuchAlgorithmException {
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, "123456", cipherCode);
        //实际上margin账号的user并不需要密码和salt所以设置为空字符串
        user.setPassword("");
        user.setSalt("");
        //因为是margin账号所以只有交易功能，还有需要标志成margin
        Long status = user.getStatus();
        status = BitUtils.enable(status, AccountConstants.USER_IS_MARGIN_USER);
        user.setStatus(status);
        userMapper.insert(user);
        return user;
    }

    /**
     * 创建虚拟账户，类似fiat margin 之类的账户
     */
    protected User onlyCreateUser(UserIndex userIndex, long userType) throws NoSuchAlgorithmException {
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, "123456", cipherCode);
        // 实际上虚拟账号，例如fiat margin的user并不需要密码和salt所以设置为空字符串
        user.setPassword("");
        user.setSalt("");
        // 因为是margin账号所以只有交易功能，还有需要标志成margin
        Long status = user.getStatus();
        status = BitUtils.enable(status, userType);
        user.setStatus(status);
        userMapper.insert(user);
        return user;
    }

    /**
     * 创建一个虚拟邮箱（幂等）
     * */
    protected String createVirtualEmail(String email,Long userId){
        String[] emailArray=email.split("@");
        String virtualEmail=emailArray[0]+"_"+String.valueOf(userId)+"_margin@"+emailArray[1];
        return virtualEmail.toLowerCase();
    }


    /**
     * 创建一个虚拟邮箱（不幂等）
     * */
    protected String createVirtualEmailForMarginFix(String email,Long userId){
        String[] emailArray=email.split("@");
        String virtualEmail=emailArray[0]+"_"+String.valueOf(userId)+"_"+DateUtils.getNewTimeMillis()+"_margin@"+emailArray[1];
        return virtualEmail.toLowerCase();
    }

    protected String createFiatVirtualEmail(String email, Long userId) {
        String[] emailArray = email.split("@");
        String virtualEmail = emailArray[0] + "_" + String.valueOf(userId) + "_fiat@" + emailArray[1];
        return virtualEmail.toLowerCase();
    }

    /**
     * 创建Margin用户Security信息（不幂等，但是会回滚）
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createMarginUserSecurity(final Long userId, final String userEmail) {
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
     * 创建类似fiat用户Security信息（不幂等，但是会回滚）
     *
     * @param userId
     * @param userEmail
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void createVirtualUserSecurity(final Long userId, final String userEmail) {
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
     * 创建margin用户信息（不幂等，但是会回滚）
     *
     * @param rootUserInfo
     * @param marginUserId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createMarginUserInfo(UserInfo rootUserInfo, Long marginUserId) {
        //逻辑很简单，从主账号的userinfo把信息都copy过来就完事了
        UserInfo marginUserInfo = new UserInfo();
        marginUserInfo.setUserId(marginUserId);
        // 经纪人返佣比例
        marginUserInfo.setAgentRewardRatio(rootUserInfo.getAgentRewardRatio());
        // 被推荐人返佣比例
        marginUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        // 用户交易账号 激活时创建
        marginUserInfo.setTradingAccount(null);
        // 被动方手续费
        marginUserInfo.setMakerCommission(rootUserInfo.getMakerCommission());
        // 主动方手续费
        marginUserInfo.setTakerCommission(rootUserInfo.getTakerCommission());
        // 买方交易手续费
        marginUserInfo.setBuyerCommission(rootUserInfo.getBuyerCommission());
        // 卖方交易手续费
        marginUserInfo.setSellerCommission(rootUserInfo.getSellerCommission());
        // 单日最大出金总金额
        marginUserInfo.setDailyWithdrawCap(rootUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        marginUserInfo.setDailyWithdrawCountLimit(rootUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        marginUserInfo.setAutoWithdrawAuditThreshold(rootUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        marginUserInfo.setTradeLevel(rootUserInfo.getTradeLevel());
        // 新返佣比例
        marginUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        marginUserInfo.setNickName("");
        marginUserInfo.setRemark("");
        marginUserInfo.setTrackSource(rootUserInfo.getTrackSource());
        marginUserInfo.setInsertTime(DateUtils.getNewDate());
        marginUserInfo.setUpdateTime(DateUtils.getNewDate());
        marginUserInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.MARGIN.name());
        // 推荐人
        marginUserInfo.setAgentId(rootUserInfo.getAgentId());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            marginUserInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(rootUserInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            marginUserInfo.setAgentId(null);
        }
        if (marginUserInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            marginUserInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insertSelective(marginUserInfo);
        return marginUserInfo;
    }

    /**
     * 创建虚拟账户，类似fiat用户信息（不幂等，但是会回滚）
     *
     * @param rootUserInfo
     * @param virtualUserId
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected UserInfo createVirtualUserInfo(UserInfo rootUserInfo, Long virtualUserId) {
        // 逻辑很简单，从主账号的userinfo把信息都copy过来就完事了
        UserInfo virtualUserInfo = new UserInfo();
        virtualUserInfo.setUserId(virtualUserId);
        // 经纪人返佣比例
        virtualUserInfo.setAgentRewardRatio(rootUserInfo.getAgentRewardRatio());
        // 被推荐人返佣比例
        virtualUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        // 用户交易账号 激活时创建
        virtualUserInfo.setTradingAccount(null);
        // 被动方手续费
        virtualUserInfo.setMakerCommission(rootUserInfo.getMakerCommission());
        // 主动方手续费
        virtualUserInfo.setTakerCommission(rootUserInfo.getTakerCommission());
        // 买方交易手续费
        virtualUserInfo.setBuyerCommission(rootUserInfo.getBuyerCommission());
        // 卖方交易手续费
        virtualUserInfo.setSellerCommission(rootUserInfo.getSellerCommission());
        // 单日最大出金总金额
        virtualUserInfo.setDailyWithdrawCap(rootUserInfo.getDailyWithdrawCap());
        // 单日最大出金次数
        virtualUserInfo.setDailyWithdrawCountLimit(rootUserInfo.getDailyWithdrawCountLimit());
        // 免审核额度
        virtualUserInfo.setAutoWithdrawAuditThreshold(rootUserInfo.getAutoWithdrawAuditThreshold());
        // 交易等级
        virtualUserInfo.setTradeLevel(rootUserInfo.getTradeLevel());
        // 新返佣比例
        virtualUserInfo.setReferralRewardRatio(rootUserInfo.getReferralRewardRatio());
        virtualUserInfo.setNickName("");
        virtualUserInfo.setRemark("");
        virtualUserInfo.setTrackSource(rootUserInfo.getTrackSource());
        virtualUserInfo.setInsertTime(DateUtils.getNewDate());
        virtualUserInfo.setUpdateTime(DateUtils.getNewDate());
        // 推荐人
        virtualUserInfo.setAgentId(rootUserInfo.getAgentId());
        virtualUserInfo.setAccountType(com.binance.account.constants.enums.UserTypeEnum.FIAT.name());
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            virtualUserInfo.setAgentId(null);
        }
        // 这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId = userCommonValidateService.isValidateAgentId(rootUserInfo.getAgentId());
        // 不合法就置为空
        if (!isValidateAgentId) {
            virtualUserInfo.setAgentId(null);
        }
        if (virtualUserInfo.getAgentId() == null) {
            log.info("register:设置默认推荐人");
            Long defaultAgentId = Long.valueOf(iSysConfig.selectByDisplayName("default_agent").getCode());
            virtualUserInfo.setAgentId(defaultAgentId);
        }
        log.info("register:插入userInfo信息");
        // 插入用户信息
        userInfoMapper.insertSelective(virtualUserInfo);
        return virtualUserInfo;
    }
    /**
     * 这个功能的实现方式绝对不能用join的方式会造成扫20张分表并且走笛卡尔积，这是要命的，多来几把数据库都上西天了
     * 代码里面自己join
     * */
    @Override
    public APIResponse<List<GetUserResponse>> getUserAgentDetail(APIRequest<GetUserAgentDetailRequest> request) throws Exception {
        final GetUserAgentDetailRequest requestBody = request.getBody();
        try {
            Map params = Maps.newHashMap();
            params.put("agentId", requestBody.getAgentId());
            params.put("offset", requestBody.getOffset());
            //实际上我们的用户都是有默认推荐人的，如果是默认推荐人，那么他下面的数据量会特别大,所以我这边要限制一下
            params.put("rows", requestBody.getRows());
            List<UserInfo> userInfoList=  userInfoMapper.selectUserInfoByAgentId(params);
            List<Long> userIds =Lists.transform(userInfoList, new Function<UserInfo, Long>() {
                @Override
                public Long apply(UserInfo userInfo) {
                    return userInfo.getUserId();
                }
            });
            params.put("userIds",userIds);
            List<User>  userList=  userMapper.selectUserPageByUserIds(params);
            List<UserSecurity> userSecurityList= userSecurityMapper.selectUserSecurityPageByUserIds(params);
            Map<Long, User> userListMap=Maps.uniqueIndex(userList, new Function<User, Long>() {
                @Override
                public Long apply(User user) {
                    return user.getUserId();
                }
            });
            Map<Long, UserInfo> userInfoListMap=Maps.uniqueIndex(userInfoList, new Function<UserInfo, Long>() {
                @Override
                public Long apply(UserInfo userInfo) {
                    return userInfo.getUserId();
                }
            });
            Map<Long, UserSecurity> userSecurityListMap=Maps.uniqueIndex(userSecurityList, new Function<UserSecurity, Long>() {
                        @Override
                public Long apply(UserSecurity userSecurity) {
                    return userSecurity.getUserId();
                }
            });
            List<GetUserResponse> getUserResponseList=Lists.transform(userIds, new Function<Long, GetUserResponse>() {
                @Override
                public GetUserResponse apply(Long userId) {
                    UserVo userVo = new UserVo();
                    BeanUtils.copyProperties(userListMap.get(userId), userVo);
                    UserInfoVo userInfoVo =new UserInfoVo();
                    BeanUtils.copyProperties(userInfoListMap.get(userId), userInfoVo);
                    UserSecurityVo userSecurityVo = conver2SecurityVo(userSecurityListMap.get(userId));
                    return new GetUserResponse(userVo, userSecurityVo, userInfoVo);
                }
            });
            return APIResponse.getOKJsonResult(getUserResponseList);
        }catch (Exception e){
            log.error("getUserAgentDetail error", e);
            return APIResponse.getErrorJsonResult("getUserAgentDetail error" + e.getMessage());
        }
    }

    @Override
    public APIResponse<FuzzyMatchUserIndexResponse> fuzzyMatchUserIndex(APIRequest<FuzzyMatchUserIndexRequest> request) throws Exception {
        FuzzyMatchUserIndexRequest fuzzyMatchUserIndexRequest=request.getBody();
        String fuzzyEmail=fuzzyMatchUserIndexRequest.getEmail();
        Integer offset=fuzzyMatchUserIndexRequest.getOffset();
        Integer rows=fuzzyMatchUserIndexRequest.getRows();
        //组装es请求
        List<ESQueryCondition> mustConditions =Lists.newArrayList();
        mustConditions.add(ESQueryCondition.wildcard("email", "*"+fuzzyEmail + "*"));
        ESQueryBuilder builder = ESQueryBuilder.instance().must(mustConditions)
                .limit(offset, rows);
        Map<String, Object> params = builder.build();
        ESResultSet resultSet =null;
        List<UserIndexEx> userIndexExList=Lists.newArrayList();
        try {
            resultSet = elasticService.search("/user_index/_search", params);
            List<JSONObject> jsonUserIndexList = resultSet.getHits().toJavaList(JSONObject.class);
             userIndexExList=Lists.transform(jsonUserIndexList, new Function<JSONObject, UserIndexEx>() {
                @Override
                public UserIndexEx apply(@Nullable JSONObject jsonObject) {
                    JSONObject objJson = jsonObject.getJSONObject("_source");
                    UserIndexEx userIndexEx=new UserIndexEx();
                    userIndexEx.setUserId(objJson.getLong("user_id"));
                    userIndexEx.setEmail(objJson.getString("email"));
                    return userIndexEx;
                }
            });
        } catch (Exception e) {
            log.error("fuzzyMatchUserIndex error :",e);
            return APIResponse.getErrorJsonResult("fuzzyMatchUserIndex error" + e.getMessage());
        }
        return APIResponse.getOKJsonResult(new FuzzyMatchUserIndexResponse(userIndexExList,Integer.valueOf(resultSet.getTotal()).longValue()));
    }

    @Override
    public APIResponse<FuzzyMatchUserInfoResponse> fuzzyMatchUserInfo(APIRequest<FuzzyMatchUserInfoRequest> request) throws Exception {
        FuzzyMatchUserInfoRequest fuzzyMatchUserInfoRequest=request.getBody();
        String fuzzyRemark=fuzzyMatchUserInfoRequest.getRemark();
        Integer offset=fuzzyMatchUserInfoRequest.getOffset();
        Integer rows=fuzzyMatchUserInfoRequest.getRows();

        //组装es请求
        List<ESQueryCondition> mustConditions =Lists.newArrayList();
        mustConditions.add(ESQueryCondition.wildcard("remark", "*"+fuzzyRemark + "*"));
        ESQueryBuilder builder = ESQueryBuilder.instance().must(mustConditions)
                .limit(offset, rows);
        Map<String, Object> params = builder.build();
        ESResultSet resultSet =null;
        List<UserInfoVo> userInfoVoList=Lists.newArrayList();
        try {
            resultSet = elasticService.search("/user_info/_search", params);
            //对返回值进行转换
            List<JSONObject> jsonUserInfoList = resultSet.getHits().toJavaList(JSONObject.class);
            userInfoVoList=Lists.transform(jsonUserInfoList, new Function<JSONObject, UserInfoVo>() {
                @Override
                public UserInfoVo apply(@Nullable JSONObject jsonObject) {
                    JSONObject objJson = jsonObject.getJSONObject("_source");
                    UserInfoVo userInfoVo=new UserInfoVo();
                    userInfoVo.setUserId(objJson.getLong("user_id"));//用户Id
                    userInfoVo.setParent(objJson.getLong("parent"));//主账户
                    userInfoVo.setAgentId(objJson.getLong("agent_id"));//推荐人id
                    userInfoVo.setAgentRewardRatio(objJson.getBigDecimal("agent_reward_ratio"));//经纪人返佣比例
                    userInfoVo.setTradingAccount(objJson.getLong("trading_account"));//用户交易账户(现货撮合当中的)
                    userInfoVo.setMakerCommission(objJson.getBigDecimal("maker_commission"));//被动方手续费
                    userInfoVo.setTakerCommission(objJson.getBigDecimal("taker_commission"));//主动方手续费
                    userInfoVo.setBuyerCommission(objJson.getBigDecimal("buyer_commission"));//买方交易手续费
                    userInfoVo.setSellerCommission(objJson.getBigDecimal("seller_commission"));//卖方交易手续费
                    userInfoVo.setDailyWithdrawCap(objJson.getBigDecimal("daily_withdraw_cap"));//单日最大出金总金额
                    userInfoVo.setDailyWithdrawCountLimit(objJson.getInteger("daily_withdraw_count_limit"));//单日最大出金次数
                    userInfoVo.setAutoWithdrawAuditThreshold(objJson.getBigDecimal("auto_withdraw_audit_threshold"));//免审核额度
                    userInfoVo.setNickName(objJson.getString("nick_name"));//昵称
                    userInfoVo.setRemark(objJson.getString("remark"));//备注
                    userInfoVo.setTrackSource(objJson.getString("track_source"));//注册渠道
                    userInfoVo.setUpdateTime(objJson.getDate("update_time"));//更新时间
                    userInfoVo.setInsertTime(objJson.getDate("insert_time"));//创建时间
                    userInfoVo.setTradeLevel(objJson.getInteger("trade_level"));//交易级别
                    return userInfoVo;
                }
            });
        } catch (Exception e) {
            log.error("fuzzyMatchUserInfo error :",e);
            return APIResponse.getErrorJsonResult("fuzzyMatchUserInfo error" + e.getMessage());
        }
        return APIResponse.getOKJsonResult(new FuzzyMatchUserInfoResponse(userInfoVoList,Integer.valueOf(resultSet.getTotal()).longValue()));

    }
    @Monitored
    @Override
    public APIResponse<GetUserCommissionDetailResponse> getUserCommissionDetail(APIRequest<GetUserAgentDetailRequest> request) throws Exception {
        final GetUserAgentDetailRequest requestBody = request.getBody();
        try {
            Map params = Maps.newHashMap();
            params.put("agentId", requestBody.getAgentId());
            params.put("offset", requestBody.getOffset());
            //实际上我们的用户都是有默认推荐人的，如果是默认推荐人，那么他下面的数据量会特别大,所以我这边要限制一下
            params.put("rows", requestBody.getRows());
            Long agentCount= this.getTotalByAgent(requestBody.getAgentId());
            if(Long.valueOf(0L).equals(agentCount)){
                return APIResponse.getOKJsonResult(new GetUserCommissionDetailResponse(agentCount,Lists.newArrayList()));
            }
            List<UserInfo> userInfoList=  userInfoMapper.selectUserInfoByAgentId(params);
            List<Long> userIds =Lists.transform(userInfoList, new Function<UserInfo, Long>() {
                @Override
                public Long apply(UserInfo userInfo) {
                    return userInfo.getUserId();
                }
            });
            params.put("userIds",userIds);
            List<User>  userList=  userMapper.selectUserPageByUserIds(params);
            Map<Long, UserInfo> userInfoListMap=Maps.uniqueIndex(userInfoList, new Function<UserInfo, Long>() {
                @Override
                public Long apply(UserInfo userInfo) {
                    return userInfo.getUserId();
                }
            });
            GetUserCommissionDetailResponse getUserCommissionDetailResponse=new GetUserCommissionDetailResponse();
            getUserCommissionDetailResponse.setAgentCount(agentCount);
            List<GetUserCommissionDetailResponse.Agent> agentList=Lists.transform(userList, new Function<User, GetUserCommissionDetailResponse.Agent>() {
                @Override
                public GetUserCommissionDetailResponse.Agent apply(@Nullable User input) {
                    GetUserCommissionDetailResponse.Agent  agent=new GetUserCommissionDetailResponse.Agent();
                    agent.setEmail(input.getEmail());
                    agent.setTime(input.getInsertTime());
                    agent.setTs(userInfoListMap.get(input.getUserId()).getTrackSource());
                    return agent;
                }
            });
            getUserCommissionDetailResponse.setAgents(agentList);
            return APIResponse.getOKJsonResult(getUserCommissionDetailResponse);
        }catch (Exception e){
            log.error("getUserCommissionDetail error", e);
            return APIResponse.getErrorJsonResult("getUserCommissionDetail error" + e.getMessage());
        }
    }

    public Long getTotalByAgent(Long userId) {
        Assert.notNull(userId);
        Long total = RedisCacheUtils.get(String.valueOf(userId),Long.class,ACCOUNT_AGENT_KEY);
        if (total == null){
            Long agentNumber=userInfoMapper.countAgentNumber(userId);
            RedisCacheUtils.set(String.valueOf(userId), agentNumber, 60*60L,ACCOUNT_AGENT_KEY);
            return agentNumber;
        }
        return total;
    }

    public Long getTotalByOldAgent(Long userId) {
        Assert.notNull(userId);
        Long total = RedisCacheUtils.get(String.valueOf(userId), Long.class, ACCOUNT_OLD_AGENT_KEY);
        if (total == null) {
            Long agentNumber = userInfoMapper.countOldAgentNum(userId);
            RedisCacheUtils.set(String.valueOf(userId), agentNumber, 60 * 60L, ACCOUNT_OLD_AGENT_KEY);
            return agentNumber;
        }
        return total;
    }

    @Override
    public APIResponse<SpecialUserIdResponse> getSpecialUserIds() throws Exception {
        List<Long> userIds = userMapper.selectSpecialUserId();
        SpecialUserIdResponse response = new SpecialUserIdResponse();
        response.setUserIds(userIds);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<UserTypeResponse> getUserTypeByUserId(APIRequest<UserIdReq> request) throws Exception {
        UserIdReq requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        UserTypeEnum userTypeEnum = getUserType(userId);
        UserTypeResponse userTypeResponse=new UserTypeResponse(userTypeEnum);
        return APIResponse.getOKJsonResult(userTypeResponse);
    }

    public UserTypeEnum getUserType(Long userId){
        //先不要判断broker账号，因为broker权限和字母账号一样，再做区分没有意义，而且
        //会引起调用方错误
        UserTypeEnum userTypeEnum=UserTypeEnum.NORMAL;
        User user = userCommonBusiness.checkAndGetUserById(userId);
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        if(userStatusEx.getIsAssetSubUser()){
            userTypeEnum=UserTypeEnum.ASSET_SUB;
        }else if(userStatusEx.getIsMarginUser()){
            userTypeEnum=UserTypeEnum.MARGIN;
        }else if(userStatusEx.getIsSubUserFunctionEnabled()){
            userTypeEnum=UserTypeEnum.PARENT;
        }else if(userStatusEx.getIsFutureUser()){
            userTypeEnum=UserTypeEnum.FUTURE;
        }else if(userStatusEx.getIsFiatUser()){
            userTypeEnum=UserTypeEnum.FIAT;
        }else if(userStatusEx.getIsSubUser()){
            userTypeEnum=UserTypeEnum.SUB;
        }
        return userTypeEnum;
    }

    @Override
    public APIResponse<String> createUserAgentRate(APIRequest<UserAgentRateReq> request) throws Exception {
        UserAgentRateReq agentRateReq = request.getBody();
        validateRate(agentRateReq);
        if (validateLabelHaveSpecialChar(agentRateReq.getLabel())) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_LABEL_HAVE_SPECIAL_CHAR);
        }
        User user = userCommonBusiness.checkAndGetUserById(agentRateReq.getUserId());
        if (!isNormalUser(user)) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_WITHOUT_PERMISSION);
        }
        Integer existNum = userAgentRateMapper.countByUserId(agentRateReq.getUserId());
        UserAgentConfig userAgentConfig = userAgentConfigMapper.selectByUserId(agentRateReq.getUserId());
        Integer limitNum = getLimitNum(userAgentConfig, defaultPromoteLinkNum);
        if (existNum != null && existNum >= limitNum) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_LINK_BEYOND_LIMIT);
        }
        UserAgentRate userAgentRate = new UserAgentRate();
        userAgentRate.setAgentLevel(agentRateReq.getAgentLevel());
        userAgentRate.setAgentCode(
                StringUtils.isBlank(agentRateReq.getAgentCode()) ? getRandomAgentCode() : agentRateReq.getAgentCode());
        userAgentRate.setReferralRate(agentRateReq.getReferralRate());
        userAgentRate.setUserId(agentRateReq.getUserId());
        userAgentRate.setLabel(agentRateReq.getLabel());
        userAgentRate.setIsDelete(0);
        try {
            long result = userAgentRateMapper.insertSelective(userAgentRate);
            if (result <= 0) {
                return APIResponse.getErrorJsonResult(AccountErrorCode.USER_INSERT_AGENT_RATE_FAIL);
            }
            return APIResponse.getOKJsonResult(promoteLinks + userAgentRate.getAgentCode());
        } catch (DuplicateKeyException e) {
            // 重新获取agentCode，重新插入一次 可能会覆盖userId、agent的重复key
            log.error("UserBusiness.createUserAgentRate DuplicateKeyException", e);
            userAgentRate.setAgentCode(getRandomAgentCode());
            long result = userAgentRateMapper.insertSelective(userAgentRate);
            if (result <= 0) {
                return APIResponse.getErrorJsonResult(AccountErrorCode.USER_INSERT_AGENT_RATE_FAIL);
            }
            return APIResponse.getOKJsonResult(promoteLinks + userAgentRate.getAgentCode());
        }

    }

    @Override
    public APIResponse<String> createMiningAgentRate(@RequestBody @Validated APIRequest<Long> request) throws Exception{
        Long userId = request.getBody();
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
        if (userInfo == null || userInfo.getMiningUserId() == null){
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }
        //矿池返佣
        Lock lock = RedisCacheUtils.getLock(AccountConstants.MINING_AGENT_CREATE_LOCK+userId);
        if (lock != null && lock.tryLock()) {
            try {
                Map<String, Object> searchParam = Maps.newHashMap();
                searchParam.put("userId", userId);
                searchParam.put("agentChannel",1);//默认矿池
                List<UserAgentRate> userAgentRates = userAgentRateMapper.selectByUserIdAgentCode(searchParam);
                if (CollectionUtils.isNotEmpty(userAgentRates) && userAgentRates.size() > 0) {
                    return APIResponse.getOKJsonResult(userAgentRates.get(0).getAgentCode());
                }
                UserAgentRate userAgentRate = new UserAgentRate();
                userAgentRate.setAgentLevel(2);
                userAgentRate.setAgentCode(getRandomAgentCode());
                userAgentRate.setReferralRate(new BigDecimal("0"));
                userAgentRate.setUserId(userId);
                userAgentRate.setLabel("MINING");
                userAgentRate.setIsDelete(0);
                userAgentRate.setAgentChannel(1);
                long result = userAgentRateMapper.insertSelective(userAgentRate);
                if (result <= 0) {
                    return APIResponse.getErrorJsonResult(AccountErrorCode.USER_INSERT_AGENT_RATE_FAIL);
                }
                return APIResponse.getOKJsonResult(userAgentRate.getAgentCode());
            }finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }else {
            log.info("create mining agent get lock failed");
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }

    }

    private Integer getLimitNum(UserAgentConfig userAgentConfig, int defaultPromoteLinkNum) {
        if (userAgentConfig != null && userAgentConfig.getMaxLink() != null) {
            return userAgentConfig.getMaxLink();
        }
        return defaultPromoteLinkNum;
    }

    @Override
    public Long checkTestnetEmailIfPassKyc(String email) throws Exception {
        User user = this.userMapper.queryByEmail(email);
        if (user == null) {
            return null;// 账号不存在
        }
        if (!checkUserWhetherPassKyc(user.getUserId())) {
            return null;// 账号不存在
        }
        return user.getUserId();
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<Integer> updateOrderConfirmStatus(APIRequest<OrderConfrimStatusRequest> request) {
        final OrderConfrimStatusRequest requestBody = request.getBody();
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(requestBody.getUserId());
        UserInfo param = new UserInfo();
        long status;
        Long originStatus = userInfo.getOrderConfirmStatus() != null ? userInfo.getOrderConfirmStatus()
                : OrderConfirmStatus.DEFAULT_STATUS;
        if (requestBody.isStatus()) {
            // 启用
            status = BitUtils.enable(originStatus, requestBody.getOrderConfirmType().getBitVal());
        } else {
            // 停用
            status = BitUtils.disable(originStatus, requestBody.getOrderConfirmType().getBitVal());
        }
        param.setOrderConfirmStatus(status);
        param.setUserId(userInfo.getUserId());
        return APIResponse.getOKJsonResult(this.userInfoMapper.updateByPrimaryKeySelective(param));
    }

    public boolean validateLabelHaveSpecialChar(String str) {
        Pattern p = Pattern.compile(AGENT_LABEL_REGEX);
        Matcher m = p.matcher(str);
        return m.find();
    }

    @Override
    public APIResponse<UserStatusEx> getUserStatusByAgentCode(APIRequest<String> request) throws Exception {
        if (StringUtils.isBlank(request.getBody())) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_CODE_NOT_EXIST);
        }
        UserAgentRate userAgentRate = userAgentRateMapper.selectByAgentCode(request.getBody().trim().toUpperCase());
        if (userAgentRate == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
        UserIdRequest userIdRequest = new UserIdRequest();
        userIdRequest.setUserId(userAgentRate.getUserId());
        apiRequest.setBody(userIdRequest);
        return getUserStatusByUserId(apiRequest);
    }


    @Override
    public APIResponse<UserAgentRateResponse> getUserAgentRateByCode(APIRequest<String> request) throws Exception {
        if (StringUtils.isBlank(request.getBody())) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_CODE_NOT_EXIST);
        }
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            UserAgentRate userAgentRate = userAgentRateMapper.selectByAgentCode(request.getBody().trim().toUpperCase());
            if (userAgentRate == null) {
                return APIResponse.getOKJsonResult();
            }
            UserAgentRateResponse response = new UserAgentRateResponse();
            response.setId(userAgentRate.getId());
            response.setAgentLevel(userAgentRate.getAgentLevel());
            response.setReferralRate(userAgentRate.getReferralRate());
            response.setUserId(userAgentRate.getUserId());
            return APIResponse.getOKJsonResult(response);
        } catch (Exception e) {
            log.warn("UserBusiness.getUserAgentRateByCode error", e);
            throw new BusinessException(AccountErrorCode.USER_QUERY_AGENT_RATE_FAIL);
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
    }

    @Override
    public APIResponse updateLabelByAgentCode(APIRequest<UpdateAgentRateReq> request) throws Exception {
        UpdateAgentRateReq agentRateReq = request.getBody();
        if (validateLabelHaveSpecialChar(agentRateReq.getLabel())) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_AGENT_LABEL_HAVE_SPECIAL_CHAR);
        }
        UserAgentRate userAgentRate = userAgentRateMapper.selectByAgentCode(agentRateReq.getAgentCode());
        if (userAgentRate == null || !Objects.equals(userAgentRate.getUserId(), agentRateReq.getUserId())) {
            return APIResponse.getErrorJsonResult(AccountErrorCode.USER_UPDATE_AGENT_AGENTCODE_ERROR);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("agentCode", agentRateReq.getAgentCode());
        params.put("label", agentRateReq.getLabel());
        if (userAgentRateMapper.updateAgentLabel(params) > 0) {
            return APIResponse.getOKJsonResult();
        }
        return APIResponse.getErrorJsonResult("更新label失败");
    }

    @Override
    public APIResponse<GetUserAgentStatResponse> getOldPromoteByUserId(APIRequest<Long> request) throws Exception {
        Long userId = request.getBody();
        GetUserAgentStatResponse response = new GetUserAgentStatResponse();
        response.setReferralRate("0%");
        response.setPromoteUrl(promoteLinks + userId);
        Long oldAgentNum = getTotalByOldAgent(userId);
        response.setPeopleNums(oldAgentNum == null ? 0 : Integer.parseInt(String.valueOf(oldAgentNum)));
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Integer> getRemainingAgentLinkNum(APIRequest<Long> request) throws Exception {
        Long userId = request.getBody();
        Integer num = userAgentRateMapper.countByUserId(userId);
        UserAgentConfig userAgentConfig = userAgentConfigMapper.selectByUserId(userId);
        Integer limitNum = getLimitNum(userAgentConfig, defaultPromoteLinkNum);
        return APIResponse.getOKJsonResult(num == null ? limitNum : limitNum - num);
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @Override
    public APIResponse<Boolean> saveOrupdateUserAgentConfig(APIRequest<UserAgentConfigReq> request) throws Exception {
        UserAgentConfigReq body = request.getBody();
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(body.getUserId());
        if (userInfo == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // if (body.getMaxAgentRate() != null && body.getMaxAgentRate().doubleValue() < 0.2 &&
        // body.getMaxAgentRate().doubleValue() < 1){
        // throw new BusinessException(AccountErrorCode.USER_AGENT_RATE_NOT_WITHIN_LIMIT);
        // }
        UserAgentConfig userAgentConfig = userAgentConfigMapper.selectByUserId(body.getUserId());
        UserAgentConfig param = getUserAgentConfig(body);
        if (userAgentConfig == null) {
            param.setCreateUser(body.getUpdateUser());
            param.setInsertTime(new Date());
            userAgentConfigMapper.insertSelective(param);
        } else {
            param.setUpdateUser(body.getUpdateUser());
            param.setUpdateTime(new Date());
            userAgentConfigMapper.updateByUserId(param);
        }
        // 本期不做了
        // if (body.getMaxAgentRate() != null){
        // UserInfo updateParam = new UserInfo();
        // updateParam.setUserId(userInfo.getUserId());
        // updateParam.setAgentRewardRatio(body.getMaxAgentRate());
        // userInfoMapper.updateByPrimaryKeySelective(updateParam);
        // }
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<SearchResult<GetUserAgentConfigResponse>> selectUserAgentConfig(
            APIRequest<GetUserAgentConfigRequest> request) throws Exception {
        GetUserAgentConfigRequest body = request.getBody();
        Map<String, Object> param = new HashMap<>();
        param.put("userId", body.getUserId());
        Integer total = userAgentConfigMapper.countByUserId(param);
        if (total == 0) {
            return APIResponse.getOKJsonResult(new SearchResult<>(Lists.newArrayList(), 0));
        }
        param.put("start", (body.getPage() - 1) * body.getRows());
        param.put("offset", body.getRows());
        List<UserAgentConfig> list = userAgentConfigMapper.selectByPage(param);
        List<GetUserAgentConfigResponse> result = toAgentConfigList(list);
        return APIResponse.getOKJsonResult(new SearchResult<GetUserAgentConfigResponse>(result, total));
    }

    @Override
    public APIResponse<SearchResult<SelectUserAgentLogResponse>> selectMiningUserAgentLog(APIRequest<SelectMiningAgentLogRequest> request) throws Exception {
        SelectMiningAgentLogRequest body = request.getBody();
        Map<String, Object> param = new HashMap<>();
        param.put("userId", body.getUserId());
        param.put("agentCode",body.getAgentCode());
        param.put("start", (body.getPage() - 1) * body.getRows());
        param.put("offset", body.getRows());
        if (body.getStartTime() != null){
            param.put("startTime", new Date(body.getStartTime()));
        }
        if (body.getEndTime() != null){
            param.put("endTime", new Date(body.getEndTime()));
        }
        Long total = miningAgentLogMapper.countByUserIdAgentCode(param);
        if (total == 0) {
            return APIResponse.getOKJsonResult(new SearchResult<>(Lists.newArrayList(), 0));
        }

        List<MiningAgentLog> list = miningAgentLogMapper.selectByUserIdAgentCode(param);
        List<SelectUserAgentLogResponse> result = toAgentLogList(list);
        return APIResponse.getOKJsonResult(new SearchResult<SelectUserAgentLogResponse>(result, total));
    }

    @Override
    public APIResponse<Long> selectMiningUserAgentNum(APIRequest<SelectUserAgentNumRequest> request)throws Exception{
        SelectUserAgentNumRequest body = request.getBody();
        Map<String, Object> param = new HashMap<>();
        param.put("agentCode",body.getAgentCode());
        if (body.getStartTime() != null){
            param.put("startTime", new Date(body.getStartTime()));
        }
        if (body.getEndTime() != null){
            param.put("endTime", new Date(body.getEndTime()));
        }
        Long total = miningAgentLogMapper.countByUserIdAgentCode(param);
        return APIResponse.getOKJsonResult(total);
    }


    private List<SelectUserAgentLogResponse> toAgentLogList(List<MiningAgentLog> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<SelectUserAgentLogResponse> result = new ArrayList<>(list.size());
        for (MiningAgentLog log : list) {
            SelectUserAgentLogResponse response = new SelectUserAgentLogResponse();
            response.setUserId(log.getUserId());
            response.setAgentCode(log.getAgentCode());
            response.setReferralUser(log.getReferralUser());
            result.add(response);
        }
        return result;
    }

    private List<GetUserAgentConfigResponse> toAgentConfigList(List<UserAgentConfig> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<GetUserAgentConfigResponse> result = new ArrayList<>(list.size());
        for (UserAgentConfig config : list) {
            GetUserAgentConfigResponse response = new GetUserAgentConfigResponse();
            response.setUserId(config.getUserId());
            response.setMaxAgentRate(config.getMaxAgentRate());
            response.setMaxLink(config.getMaxLink());
            result.add(response);
        }
        return result;
    }


    private UserAgentConfig getUserAgentConfig(UserAgentConfigReq body) {
        UserAgentConfig config = new UserAgentConfig();
        config.setUserId(body.getUserId());
        config.setMaxLink(body.getMaxLink());
        // config.setMaxAgentRate(body.getMaxAgentRate());
        return config;
    }

    @Override
    public APIResponse<SearchResult<GetUserAgentStatResponse>> getUserPromoteByUserId(
            APIRequest<UserAgentLinkReq> request) throws Exception {
        UserAgentLinkReq userAgentLink = request.getBody();
        if (StringUtils.isBlank(userAgentLink.getAgentCode()) && userAgentLink.getUserId() == null) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        Map<String, Object> searchParam = Maps.newHashMap();
        searchParam.put("userId", userAgentLink.getUserId());
        searchParam.put("start", (userAgentLink.getPage() - 1) * userAgentLink.getRows());
        searchParam.put("offset", userAgentLink.getRows());
        if (StringUtils.isNotBlank(userAgentLink.getAgentCode())) {
            searchParam.put("agentCode", userAgentLink.getAgentCode());
        }
        searchParam.put("agentChannel", 0);
        List<UserAgentRate> userAgentRates = userAgentRateMapper.selectByUserIdAgentCode(searchParam);

        if (CollectionUtils.isEmpty(userAgentRates) || userAgentRates.size() <= 0) {
            return APIResponse.getOKJsonResult(new SearchResult<>(Lists.newArrayList(), 0));
        }
        Integer total = userAgentRateMapper.countByUserIdAgentCode(searchParam);
        List<GetUserAgentStatResponse> list = new ArrayList<>();
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userAgentRates.get(0).getUserId());
        for (UserAgentRate userAgentRate : userAgentRates) {
            GetUserAgentStatResponse stat = new GetUserAgentStatResponse();
            stat.setId(userAgentRate.getId());
            stat.setAgentCode(userAgentRate.getAgentCode());
            Map<String, Object> params = Maps.newHashMap();
            params.put("agentId", userAgentRate.getUserId());
            params.put("rate", userAgentRate.getReferralRate());
            if (agentUserNumsSwitch){
                if (StringUtils.isNumeric(userAgentRate.getAgentCode())) {
                    Integer olduserIdAgentNums = reportApiClient.selectOldAgentReffrNums(userAgentRate.getUserId());
                    stat.setPeopleNums(olduserIdAgentNums==null?0:(olduserIdAgentNums<0?0:olduserIdAgentNums));
                }else{
                    Map<String, Long> agentCodeNumMap = Maps.newHashMap();
                    List<String> agentCodes = userAgentRates.stream().map(UserAgentRate::getAgentCode).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(agentCodes)){
                        agentCodeNumMap = reportApiClient.selectUserAgentNumByAgentCodes(agentCodes);
                    }
                    if (agentCodeNumMap.containsKey(userAgentRate.getAgentCode())){
                        Long nums = agentCodeNumMap.get(userAgentRate.getAgentCode());
                        stat.setPeopleNums(nums == null?0:Integer.parseInt(String.valueOf(nums)));
                    }else{
                        stat.setPeopleNums(0);
                    }
                }
            }else{
                if (StringUtils.isNumeric(userAgentRate.getAgentCode())) {
                    Map<Long, Long> userIdAgentNumsMap = reportApiClient.selectUserAgentNumByUserIds(Lists.newArrayList(Long.parseLong(userAgentRate.getAgentCode())));
                    if (userIdAgentNumsMap == null || !userIdAgentNumsMap.containsKey(Long.parseLong(userAgentRate.getAgentCode()))){
                        stat.setPeopleNums(0);
                    }else{
                        Long nums = userIdAgentNumsMap.get(Long.parseLong(userAgentRate.getAgentCode()));
                        stat.setPeopleNums(nums == null?0:Integer.parseInt(String.valueOf(nums)));
                    }
                } else {
                    Integer peopleNums = userAgentLogMapper.countByAgentCode(userAgentRate.getAgentCode());
                    stat.setPeopleNums(peopleNums == null ? 0 : peopleNums);
                }
            }
            stat.setPromoteUrl(promoteLinks + userAgentRate.getAgentCode());
            stat.setStatus(userAgentRate.getIsDelete() == 0 ? 1 : 0);
            stat.setAgentRate(transToPercent(userInfo.getAgentRewardRatio()));
            stat.setReferralRate(transToPercent(userAgentRate.getReferralRate()));
            stat.setLabel(userAgentRate.getLabel());
            stat.setUserId(userAgentRate.getUserId());
            stat.setSelectShare(userAgentRate.getSelectShare());
            list.add(stat);
        }
        if (!agentUserNumsSwitch){
            for (GetUserAgentStatResponse stat :list){
                if (StringUtils.isNumeric(stat.getAgentCode())) {
                    int result = (stat.getPeopleNums()==null?0:stat.getPeopleNums()) - getAgentCodeNumFromAgentLog(stat.getUserId());
                    stat.setPeopleNums(result<0?0:result);
                }
            }
        }
        return APIResponse.getOKJsonResult(new SearchResult<>(list, total == null ? 0 : total));
    }

    public int getAgentCodeNumFromAgentLog(Long userId){
        Map<String, Object> searchParam = Maps.newHashMap();
        searchParam.put("userId", userId);
        List<UserAgentRate> allUserAgentRates = userAgentRateMapper.selectByUserIdAgentCode(searchParam);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(allUserAgentRates)){
            return 0;
        }
        List<String> agentCodes = allUserAgentRates.stream().map(UserAgentRate::getAgentCode).collect(Collectors.toList());
        agentCodes.remove(String.valueOf(userId));
        if (CollectionUtils.isEmpty(agentCodes) || agentCodes.size() ==0){
            return 0;
        }
        searchParam.put("agentCodes",agentCodes);
        Integer nums = userAgentLogMapper.countByAgentCodes(searchParam);
        return nums==null?0:nums;
    }

    @Override
    public APIResponse<Boolean> saveOrupdateSnapshotShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception {
        SnapshotShareConfigReq snapshotShareConfigReq = request.getBody();
        if (StringUtils.isAllBlank(snapshotShareConfigReq.getLanguage(),snapshotShareConfigReq.getContent(),snapshotShareConfigReq.getIcon(),snapshotShareConfigReq.getTitle(),snapshotShareConfigReq.getUrl())){
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        SnapShotShareConfig config = getSnapShotShareConfig(snapshotShareConfigReq);
        if (snapshotShareConfigReq.getId() == null){
            snapShotShareConfigMapper.insertSelective(config);
        }else{
            config.setId(snapshotShareConfigReq.getId());
            snapShotShareConfigMapper.updateByPrimaryKeySelective(config);
        }
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<Boolean> deleteSnapshotShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception {
        SnapshotShareConfigReq snapshotShareConfigReq = request.getBody();
        if (snapshotShareConfigReq.getId() == null){
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }
        snapShotShareConfigMapper.deleteByPrimaryKey(snapshotShareConfigReq.getId());
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<SnapshotShareConfigsRes> selectAllSnapShareConfig(APIRequest<SnapshotShareConfigReq> request) throws Exception {
        SnapshotShareConfigReq body = request.getBody();
        List<SnapShotShareConfig> snapShotShareConfigs = snapShotShareConfigMapper.selectAllSnapShotConfig(body.getLanguage(),body.getType());
        return APIResponse.getOKJsonResult(toSnapShotShareConfigRes(body.getUserId(), snapShotShareConfigs));
    }

    @Override
    public APIResponse<List<SnapshotShareConfigRes>> selectAllSnapShareConfigForPnk(APIRequest<SnapshotShareConfigReq> request) throws Exception {
        List<SnapShotShareConfig> snapShotShareConfigs = snapShotShareConfigMapper.selectAllSnapShotConfig(null,request.getBody().getType());
        return APIResponse.getOKJsonResult(toSnapShotShareConfigResForPnk(snapShotShareConfigs));
    }

    private List<SnapshotShareConfigRes> toSnapShotShareConfigResForPnk(List<SnapShotShareConfig> snapShotShareConfigs) {
            List<SnapshotShareConfigRes> result = new ArrayList<>(snapShotShareConfigs.size());
            for (SnapShotShareConfig config:snapShotShareConfigs){
                SnapshotShareConfigRes res = new SnapshotShareConfigRes();
                res.setId(config.getId());
                res.setLanguage(config.getLanguage());
                res.setTitle(config.getTitle());
                res.setContent(config.getContent());
                res.setIcon(config.getIcon());
                res.setType(config.getType());
                res.setUrl(config.getUrl());
                //如果reffer配置，则
                result.add(res);
            }
        return result;
    }

    private SnapshotShareConfigsRes toSnapShotShareConfigRes(Long userId, List<SnapShotShareConfig> snapShotShareConfigs) {
        SnapshotShareConfigsRes snapshotShareConfigsRes = new SnapshotShareConfigsRes();
        if (CollectionUtils.isNotEmpty(snapShotShareConfigs) && snapShotShareConfigs.size() > 0){
            List<SnapshotShareConfigRes> result = toSnapShotShareConfigResForPnk(snapShotShareConfigs);
            snapshotShareConfigsRes.setResList(result);
        }
        UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(userId);
        snapshotShareConfigsRes.setAgentCode((userAgentRate != null?userAgentRate.getAgentCode():String.valueOf(userId)));
        snapshotShareConfigsRes.setUploadUrlUniqueKey(String.valueOf(keyGenerator.generateKey().longValue()));
        return snapshotShareConfigsRes;
    }

    private SnapShotShareConfig getSnapShotShareConfig(SnapshotShareConfigReq snapshotShareConfigReq) {
        SnapShotShareConfig config = new SnapShotShareConfig();
        config.setLanguage(snapshotShareConfigReq.getLanguage());
        config.setTitle(snapshotShareConfigReq.getTitle());
        config.setContent(snapshotShareConfigReq.getContent());
        config.setIcon(snapshotShareConfigReq.getIcon());
        config.setType(snapshotShareConfigReq.getType());
        config.setUrl(snapshotShareConfigReq.getUrl());
        return config;
    }

    @Override
    public APIResponse<Boolean> selectOneAsShareCode(APIRequest<UserAgentSelectShareReq> request) throws Exception {
        UserAgentSelectShareReq userAgentSelectShareReq = request.getBody();
        Map<String, Object> searchParam = Maps.newHashMap();
        searchParam.put("userId", userAgentSelectShareReq.getUserId());
        searchParam.put("agentCode", userAgentSelectShareReq.getAgentCode());
        List<UserAgentRate> userAgentRates = userAgentRateMapper.selectByUserIdAgentCode(searchParam);
        if (CollectionUtils.isEmpty(userAgentRates) || userAgentRates.size() != 1) {
            return APIResponse.getOKJsonResult(false);
        }
        userAgentRateMapper.deleteAllShareCodeByUserId(searchParam);
        userAgentRateMapper.updateOneAsAgentCode(searchParam);
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<GetUserAgentStatResponse> selectCheckedShareCodeByUserId(APIRequest<Long> request) throws Exception {
        UserAgentRate userAgentRate = userAgentRateMapper.selectCheckedShareCodeByUserId(request.getBody());
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(request.getBody());
        if (userInfo == null){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (userAgentRate == null){
            userAgentRate = userAgentRateMapper.selectByAgentCode(String.valueOf(request.getBody()));
            if (userAgentRate == null){
                return APIResponse.getOKJsonResult(null);
            }
            userAgentRate.setSelectShare(1);
        }
        GetUserAgentStatResponse stat = new GetUserAgentStatResponse();
        stat.setId(userAgentRate.getId());
        stat.setAgentCode(userAgentRate.getAgentCode());
        Map<String, Object> params = Maps.newHashMap();
        params.put("agentId", userAgentRate.getUserId());
        params.put("rate", userAgentRate.getReferralRate());
        if (StringUtils.isNumeric(userAgentRate.getAgentCode())) {
            Map<Long, Long> userIdAgentNumsMap = reportApiClient.selectUserAgentNumByUserIds(Lists.newArrayList(Long.parseLong(userAgentRate.getAgentCode())));
            if (userIdAgentNumsMap == null || !userIdAgentNumsMap.containsKey(Long.parseLong(userAgentRate.getAgentCode()))){
                stat.setPeopleNums(0);
            }else{
                Long nums = userIdAgentNumsMap.get(Long.parseLong(userAgentRate.getAgentCode()));
                stat.setPeopleNums(nums == null?0:Integer.parseInt(String.valueOf(nums)));
            }
        } else {
            Integer peopleNums = userAgentLogMapper.countByAgentCode(userAgentRate.getAgentCode());
            stat.setPeopleNums(peopleNums == null ? 0 : peopleNums);
        }
        stat.setPromoteUrl(promoteLinks + userAgentRate.getAgentCode());
        stat.setStatus(userAgentRate.getIsDelete() == 0 ? 1 : 0);
        stat.setAgentRate(transToPercent(userInfo.getAgentRewardRatio()));
        stat.setReferralRate(transToPercent(userAgentRate.getReferralRate()));
        stat.setLabel(userAgentRate.getLabel());
        stat.setUserId(userAgentRate.getUserId());
        stat.setSelectShare(userAgentRate.getSelectShare());
        return APIResponse.getOKJsonResult(stat);
    }


    @Override
    public APIResponse<SearchResult<String>> getReferralUserEmail(APIRequest<GetReferralEmailRequest> request)
            throws Exception {
        GetReferralEmailRequest emailRequest = request.getBody();
        Map<String, Object> userParam = Maps.newHashMap();
        if (emailRequest.getRows() == null || emailRequest.getPage() == null) {
            userParam.put("start", 0);
            userParam.put("offset", 20);
        } else {
            userParam.put("start", (emailRequest.getPage() - 1) * emailRequest.getRows());
            userParam.put("offset", emailRequest.getRows());
        }
        SearchResult<String> searchResult = new SearchResult<>();
        userParam.put("agentCode", emailRequest.getAgentCode());
        userParam.put("userId", emailRequest.getUserId());
        if (StringUtils.isNumeric(emailRequest.getAgentCode())){
            //检验关系
            if (!emailRequest.getUserId().equals(Long.parseLong(emailRequest.getAgentCode()))) {
                return APIResponse.getOKJsonResult(new SearchResult<>(Lists.newArrayList(), 0));
            }
            SearchResult<String> result = reportApiClient.selectRefferalEmailByAgentId(emailRequest.getUserId(), (emailRequest.getPage() - 1) * emailRequest.getRows(), emailRequest.getRows());
            return APIResponse.getOKJsonResult(result);
        }
        List<UserAgentLog> userAgentLogs = userAgentLogMapper.selectByAgentCode(userParam);
        if (CollectionUtils.isEmpty(userAgentLogs)) {
            return APIResponse.getOKJsonResult(new SearchResult<>(Lists.newArrayList(), 0));
        }
        List<String> emails = new ArrayList<>(userAgentLogs.size());
        for (UserAgentLog userAgentLog : userAgentLogs) {
            if (StringUtils.isNotBlank(userAgentLog.getReferralEmail())) {
                if (userAgentLog.getReferralEmail().contains(emailPhone)){
                    emails.add(emailToPhone(userAgentLog.getReferralEmail()));
                }else{
                    emails.add(MaskUtils.maskHalfOpenEmail(userAgentLog.getReferralEmail()));
                }

            }
        }
        Integer total = userAgentLogMapper.countByAgentCode(emailRequest.getAgentCode());
        searchResult.setRows(emails);
        searchResult.setTotal(total == null ? 0 : total);
        return APIResponse.getOKJsonResult(searchResult);
    }

    private static String emailToPhone(String email){
        String phone = email.substring(0, email.lastIndexOf(emailPhone));
        if (StringUtils.isNotBlank(phone)){
            //去除掉cn,取出手机号
            String phoneNumber = phone.replaceAll("[^(0-9)]", "");
            if (StringUtils.isNotBlank(phoneNumber) && phoneNumber.length() > 7){
                return phoneNumber.substring(0,3)+"****"+phoneNumber.substring(phoneNumber.length()-4);
            }else if (StringUtils.isNotBlank(phoneNumber) && phoneNumber.length() <= 7){
                return "*******"+phoneNumber.substring(phoneNumber.length()-4);
            }
        }
        return MaskUtils.maskHalfOpenEmail(phone);
    }

    @Override
    public APIResponse<Long> countRealUserAgentNumber(APIRequest<CountAgentNumberRequest> request) throws Exception {
        CountAgentNumberRequest requestBody = request.getBody();
        Long agentId=requestBody.getAgentId();
        //这里也没什么逻辑，拿到推荐人id执行下sql统计一下
        Map<Long, Long> userIdNumsMap = reportApiClient.selectUserAgentNumByUserIds(Lists.newArrayList(agentId));
        if (userIdNumsMap == null || !userIdNumsMap.containsKey(agentId)){
            return APIResponse.getOKJsonResult(0L);
        }
        return APIResponse.getOKJsonResult(userIdNumsMap.get(agentId));
    }

    private List<String> getEmailsByAgentParam(Map<String, Object> userParam) {
        List<UserInfo> userInfoList = userInfoMapper.selectOldAgentByAgentIdAndReferral(userParam);
        if (CollectionUtils.isEmpty(userInfoList)) {
            return Lists.newArrayList();
        }
        List<Long> userIds = userInfoList.stream().map(UserInfo::getUserId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        List<UserIndex> userIndexs = userIndexMapper.selectByUserIds(userIds);
        if (CollectionUtils.isEmpty(userIndexs)) {
            return Lists.newArrayList();
        }
        List<String> emails = new ArrayList<>(userIndexs.size());
        for (UserIndex userIndex : userIndexs) {
            if (StringUtils.isNotBlank(userIndex.getEmail())) {
                emails.add(MaskUtils.maskEmail(userIndex.getEmail()));
            }
        }
        return emails;
    }

    private String transToPercent(BigDecimal num) {
        if (num == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("0%");
        return df.format(num);
    }

    /**
     * 创建一个8位String,第一个是字母，后续7个为字母或数字
     *
     * @return
     */
    private String getRandomAgentCode() {
        StringBuilder randomStr = new StringBuilder();
        String first = AGENT_CODE_ARR[(int) (Math.random() * 26) + 10];
        randomStr.append(first);
        for (int i = 0; i < 7; i++) {
            randomStr.append(AGENT_CODE_ARR[(int) (Math.random() * 36)]);
        }
        if (userAgentRateMapper.selectByAgentCode(randomStr.toString()) == null) {
            return randomStr.toString();
        }
        return getRandomAgentCode();
    }

    private boolean isNormalUser(User user) {
        return user != null && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_SUBUSER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_MARGIN_USER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_IS_FUTURE_USER)
                && !BitUtils.isEnable(user.getStatus(), Constant.USER_DELETE)
                && BitUtils.isEnable(user.getStatus(), Constant.USER_ACTIVE);
    }


    public void validateRate(UserAgentRateReq agentRateReq) {
        UserInfo userInfo = userInfoMapper.selectByPrimaryKey(agentRateReq.getUserId());
        // 小于500BNB即level=2,则只能0,0.05,0.1
        if ((0.2 == agentRateReq.getReferralRate().doubleValue()
                || 0.15 == agentRateReq.getReferralRate().doubleValue())
                && (userInfo != null && userInfo.getAgentRewardRatio() != null
                        && userInfo.getAgentRewardRatio().doubleValue() < 0.4)) {
            throw new BusinessException(AccountErrorCode.USER_AGENT_RATE_WITHOUT_500BNB);
        }
        String[] split = agentReferralRate.split(",");
        for (String str : split) {
            if (new BigDecimal(str).doubleValue() == agentRateReq.getReferralRate().doubleValue()) {
                return;
            }
        }
        throw new BusinessException(AccountErrorCode.USER_AGENT_RATE_WITH_ERROR_RATE);
    }

    @Override
    public Integer addOrUpdateUserConfig(Long userId, String configType, String configName) throws Exception {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType(configType);
        uc.setConfigName(configName);
        try {
            String keyCode = String.valueOf(userId) + "_" + configType;
            // 没有默认配置项，则添加默认配置项 清除缓存
            iUserConfig.insertOrupdateUserConfig(keyCode, uc);
            return 1;
        } catch (Exception e) {
            log.error("addOrUpdateUserConfig,configType:{}, userId:{}", configType, userId);
            log.error("addOrUpdateUserConfig error-->{}", e);
            return 0;
        }
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public APIResponse<RegisterUserResponse> thirdPartyUserRegister(APIRequest<ThirdPartyUserRegisterRequest> request) throws Exception {
        final ThirdPartyUserRegisterRequest requestBody = request.getBody();
        log.info("thirdPartyUserRegister start request={}", JSONObject.toJSONString(requestBody));
        if (!thirdUserOpenRegister) {
            log.info("thirdPartyUserRegister close thirdParty user register");
            throw new BusinessException("thirdParty user register was closed");
        }

        String dayMinute = new SimpleDateFormat("yyyyMMddHHmm").format(DateUtils.getNewUTCDate());
        String limitCacheKey = requestBody.getTrackSource()+":"+dayMinute;
        // 同一trackSource请求频率限制
        long requestMinuteCount = RedisCacheUtils.get(limitCacheKey, Long.class, THIRDPARTY_REQUEST_MINUTE_COUNT_KEY, 0L);
        if (requestMinuteCount > MAX_THIRDPARTY_REQUEST_MINUTE_COUNT) {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
        }
        // 同一trackSource注册频率限制
        long registerMinuteCount = RedisCacheUtils.get(limitCacheKey, Long.class, THIRDPARTY_REGISTER_MINUTE_COUNT_KEY, 0L);
        if (registerMinuteCount > MAX_THIRDPARTY_REGISTER_MINUTE_COUNT) {
            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);    
        }

        // 同一ip注册限制
        String userIp = requestBody.getUserIp();// 第三方用户的real ip
        if (StringUtils.isNotBlank(userIp)) {
            long maxIpCount = Long.parseLong(this.iSysConfig.selectByDisplayName("max_register_count").getCode());
            long ipCount = Long.valueOf(String.valueOf(RedisCacheUtils.get(userIp, Long.class, CacheKeys.REGISTER_IP_COUNT, 0L)));
            if (ipCount >= maxIpCount) {
                throw new BusinessException(GeneralCode.USER_REGISTER_IP_EXCEED);
            }
        }

        try {
            RedisCacheUtils.increment(limitCacheKey, THIRDPARTY_REQUEST_MINUTE_COUNT_KEY, 1L, 1l, TimeUnit.MINUTES);// 有效期
        } catch (Exception e) {
            log.error("注册请求频率限制", e);
        }

        // email强制转换成小写
        final String email = requestBody.getEmail().trim().toLowerCase();

        if (!Pattern.matches(REGEX_EMAIL, email) || email.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        User tempUser = this.userMapper.queryByEmail(email);
        if(tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {//用户存在且被删除
            throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
        }else if (tempUser != null) {
            log.info("thirdPartyUserRegister fail, email exist");
            throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
        }
        log.info("thirdPartyUserRegister check email done, email:{}", email);

        log.info("thirdPartyUserRegister:获取一个用户索引");
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);// 获取一个用户索引
        log.info("thirdPartyUserRegister:获取密码加密盐");
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        log.info("thirdPartyUserRegister:生成随机密码");
        String password = InvitationCodeUtil.generatePassword();
        User user = User.buildRegisterObject(userIndex, password, cipherCode);
        user.setStatus(user.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE);// 默认激活账号
        log.info("thirdPartyUserRegister:插入用户信息");
        this.userMapper.insert(user);// 插入用户登录信息
        String userEmail = user.getEmail();
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId());
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");// 防钓鱼码
        userSecurity.setSecurityLevel(1);// 安全级别
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(0);
        userSecurity.setWithdrawSecurityAutoStatus(0);
        log.info("thirdPartyUserRegister:插入用户安全信息");
        this.userSecurityMapper.insert(userSecurity);// 用户安全信息
        log.info("thirdPartyUserRegister:初始化userInfo信息");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setParent(null);
        BigDecimal agentRewardRatio =
                new BigDecimal(this.iSysConfig.selectByDisplayName("agent_reward_ratio").getCode());
        userInfo.setAgentRewardRatio(agentRewardRatio);// 经纪人返佣比例
        userInfo.setTradingAccount(null);// 用户交易账户 激活时创建
        BigDecimal makerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("maker_commission").getCode());
        BigDecimal takerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("taker_commission").getCode());
        BigDecimal buyerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("buyer_commission").getCode());
        BigDecimal sellerCommission =
                new BigDecimal(this.iSysConfig.selectByDisplayName("seller_commission").getCode());
        userInfo.setMakerCommission(makerCommission);// 被动方手续费
        userInfo.setTakerCommission(takerCommission);// 主动方手续费
        userInfo.setBuyerCommission(buyerCommission);// 买方交易手续费
        userInfo.setSellerCommission(sellerCommission);// 卖方交易手续费
        userInfo.setDailyWithdrawCap(null);// 单日最大出金总金额
        userInfo.setDailyWithdrawCountLimit(null);// 单日最大出金次数
        userInfo.setAutoWithdrawAuditThreshold(null);// 免审核额度
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(requestBody.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        userInfo.setTradeLevel(0);
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = this.iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }
        if (userInfo.getAgentId() == null) {
            log.info("thirdPartyUserRegister:设置默认推荐人");
            Long agentId = Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(agentId);
        }

        // 注册时即激活，创建交易账号
        Long tradingAccount = userCommonBusiness.createTradingAccountOnly(userInfo);// 创建交易账户
        if (tradingAccount != null) {
            userInfo.setTradingAccount(tradingAccount);

            UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
            userTradingAccount.setTradingAccount(tradingAccount);
            userTradingAccount.setUserId(user.getUserId());
            log.info("thirdPartyUserRegister createTradingAccount insert:{}", JSON.toJSONString(userTradingAccount));
            this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
        }
        log.info("thirdPartyUserRegister:插入userInfo信息");
        this.userInfoMapper.insertSelective(userInfo);// 插入用户信息

        try {
            RedisCacheUtils.increment(limitCacheKey, THIRDPARTY_REGISTER_MINUTE_COUNT_KEY, 1L, 1l, TimeUnit.MINUTES);// 有效期
        } catch (Exception e) {
            log.error("注册频率限制", e);
        }

        String locationCity = null;
        if (StringUtils.isNotBlank(userIp)) {
            try {
                RedisCacheUtils.increment(userIp, CacheKeys.REGISTER_IP_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
            } catch (Exception e) {
                log.error("注册ip限制", e);
            }
            locationCity = IP2LocationUtils.getCountryCity(userIp);
        } else {
            userIp = "0.0.0.0";
        }

        // 记录设备指纹信息
        String clientType = TerminalEnum.WEB.getCode();
        AddUserDeviceResponse deviceResponse = null;
        Map<String, String> deviceInfo = requestBody.getDeviceInfo();
        if (deviceInfo != null) {
            try {
                userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
                deviceResponse = userDeviceBusiness.addDevice(user.getUserId(), clientType, UserDevice.Status.AUTHORIZED,
                        UserDeviceConst.SOURCE_REGIST, deviceInfo);
            } catch (Exception e) {
                log.error("thirdPartyUserRegister新增设备指纹出错 userId:{}, deviceInfo:{}", user.getUserId(), requestBody.getDeviceInfo(), e);
            }
        }
        // 添加注册日志
        try {
            final UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), userIp, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
            if (deviceResponse != null) {
                securityLog.touchDevice(deviceResponse.getId(), deviceResponse.getDeviceId());
            }
            UserIp userIpDO = new UserIp(user.getUserId(), userIp);
            this.userIpMapper.insertIgnore(userIpDO);
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add thirdPartyUserRegister log failed, email:%s, exception:", userEmail), e);
        }

        try {
            log.info("thirdPartyUserRegister:发送重置密码邮件");
            AccountForgotPasswordRequest forgotPasswordRequest = new AccountForgotPasswordRequest();
            forgotPasswordRequest.setEmail(email);
            thirdPartySendResetPasswordEmail(APIRequest.instance(request, forgotPasswordRequest));
        } catch (Exception e) {
            log.error(String.format("send thirdPartyUserRegister email failed, email:%s, exception:", userEmail), e);
        }

        log.info("thirdPartyUserRegister:注册结束");
        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, userEmail);
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        dataMsg.put("tradingAccount", tradingAccount);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.REGISTER_AND_ACTIVE, dataMsg);
        log.info("thirdPartyUserRegister iMsgNotification registerAndActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        log.info("thirdPartyUserRegister end success, request={}", JSONObject.toJSONString(requestBody));
        return APIResponse.getOKJsonResult(new RegisterUserResponse(user.getUserId(), userEmail, null,
                null, userInfo.getAgentId(), null, null, null));
    }

    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public APIResponse<AccountForgotPasswordResponse> thirdPartySendResetPasswordEmail(APIRequest<AccountForgotPasswordRequest> request) throws Exception {
        final AccountForgotPasswordRequest requestBody = request.getBody();
        log.info("thirdPartySendResetPasswordEmail start, request={}", LogMaskUtils.maskJsonString2(JSONObject.toJSONString(request)));
        final String email = requestBody.getEmail();
        if(org.apache.commons.lang3.StringUtils.isBlank(email)) {
            log.warn("thirdPartySendResetPasswordEmail中的email为null");
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 强制从主库读，避免读写延迟问题
        User user;
        HintManager hintManager = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            user = this.userMapper.queryByExistentEmail(email);
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        if (user == null) {
            log.info(String.format("thirdPartySendResetPasswordEmail email为%s的用户不存在", requestBody.getEmail()));
            return APIResponse.getOKJsonResult(null);
        }
        final Long status = user.getStatus();
        // 通过notification发送通知
        securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.FORGET_PWD, request.getLanguage());
        // 发送邮件
        Map<String, Object> data = new HashMap<>();
        // /en可以写死
        if (StringUtils.isBlank(mainSiteDomainUrl)) {
             log.error("thirdPartySendResetPasswordEmail mainSiteDomainUrl null error");
             throw new BusinessException("thirdPartySendResetPasswordEmail mainSiteDomainUrl null error");
        }
        String emailLink = UserCommonBusiness.emailLinkGenerator(requestBody.getCustomEmailLink(),
                String.format("%sen/user/reset-password/1", mainSiteDomainUrl),
                new HashMap<>());
        data.put("link", emailLink);

        String disableToken = userCommonBusiness.sendDisableTokenEmailForThirdParty(AccountCommonConstant.NODE_TYPE_THIRD_PARTY_RESET_PASSWORD, user, data,
                "重置密码发送邮件：", null);

        log.info("thirdPartySendResetPasswordEmail 处理完成！");
        return APIResponse.getOKJsonResult(
                new AccountForgotPasswordResponse(user.getUserId(), user.getEmail(), status, null, disableToken));
    }

    /**
     * 添加偏好语言
     */
    private void addPreferLangConfig(Long userId) {
        String lang;
        try {
            if (null == WebUtils.getAPIRequestHeader() || null == WebUtils.getAPIRequestHeader().getLanguage()) {
                lang = LanguageEnum.EN_US.getLang();
            } else {
                lang = WebUtils.getAPIRequestHeader().getLanguage().getLang();
            }
            UserConfig uc = new UserConfig();
            uc.setUserId(userId);
            uc.setConfigType(USER_CONFIG_PREFER_LANG);
            uc.setConfigName(lang);
            String keyCode = String.valueOf(userId) + "_" + USER_CONFIG_PREFER_LANG;
            // 没有默认配置项，则添加默认配置项 清除缓存
            iUserConfig.insertOrupdateUserConfig(keyCode, uc);
            log.info("addPreferLangConfig,userId:{},lang:{}", userId, lang);
        } catch (Exception e) {
            log.error("addPreferLangConfig error", e);
        }
    }
    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<OneButtonRegisterResponse> oneButtonRegister(APIRequest<OneButtonRegisterRequest> request) throws Exception {
        final OneButtonRegisterRequest requestBody = request.getBody();
        log.info("oneButtonRegister start request={}", JSONObject.toJSONString(requestBody));
        if (!openOneButtonRegister) {
            log.info("oneButtonRegister close oneButton user register");
            throw new BusinessException("user one button register was closed");
        }

        final String ip = WebUtils.getRequestIp();
        // 该方法包括注册和发送验证码两部分，同一ip注册限制不能cover全部，增加同一ip访问次数限制
        long ipRequestCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class, ONEBUTTON_REGISTER_REQUEST_COUNT_KEY, 0L)));
        if (ipRequestCount >= requestCountLimit) {
            throw new BusinessException(GeneralCode.TOO_MANY_REQUESTS);
        }

        //1 判断注册方式
        RegisterationMethodEnum registerationMethodEnum=requestBody.getRegisterationMethod();
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            if (StringUtils.isAnyBlank(requestBody.getMobileCode(),requestBody.getMobile())) {
                log.warn("register,mobile or mobileCode blank");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(requestBody.getMobileCode());
            if (null == country) {
                log.warn("register,mobileCode invalid");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            if (!RegexUtils.matchMobile(requestBody.getMobileCode(), requestBody.getMobile())) {
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            requestBody.setMobile(requestBody.getMobile().trim());
            requestBody.setMobileCode(country.getMobileCode());
        }

        if(RegisterationMethodEnum.EMAIL==registerationMethodEnum){
            if (StringUtils.isBlank(requestBody.getEmail())) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
            if (!RegexUtils.matchEmail(requestBody.getEmail())) {
                throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
            }
        }

        User tempUser = null;
        // email强制转换成小写
        String email = requestBody.getEmail();
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            if (!iCountry.isSupportMobileRegisterCountry(requestBody.getMobileCode())) {
                throw new BusinessException(GeneralCode.COUNTRY_NOT_SUPPORT);
            }
            Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(requestBody.getMobileCode());
            if (null == country) {
                log.warn("register,mobileCode invalid");
                throw new BusinessException(AccountErrorCode.USER_MOBILE_NOT_CORRECT);
            }
            requestBody.setMobileCode(country.getCode());
            email= UserEmailUtils.getMobileUserEmail(requestBody.getMobileCode(),requestBody.getMobile());
            //如果是手机号注册用户，需要检查这个手机号是否被人使用
            // 判断手机是否已经被使用
            UserMobileIndex userMobileIndex =
                    this.userMobileIndexMapper.selectByPrimaryKey(requestBody.getMobile(), requestBody.getMobileCode());

            // 手机号以0开头的过滤,这是为了限制历史问题，所有0开头的都有问题不能随便放进来
            if (requestBody.getMobile().startsWith("0")) {
                if (requestBody.getMobile().length() > 1) {
                    UserMobileIndex   userMobileIndex1 = this.userMobileIndexMapper.selectByPrimaryKey(
                            requestBody.getMobile().substring(1, requestBody.getMobile().length()),
                            requestBody.getMobileCode());
                    if (userMobileIndex1 != null) {
                        userMobileIndex =userMobileIndex1;
                    }
                } else {
                    throw new BusinessException(GeneralCode.SYS_VALID);
                }

            } else {
                UserMobileIndex   userMobileIndex2 =this.userMobileIndexMapper.selectByPrimaryKey("0" + requestBody.getMobile(),
                        requestBody.getMobileCode());
                if (userMobileIndex2 != null) {
                    userMobileIndex =userMobileIndex2;
                }
            }
            if (userMobileIndex != null) {
                tempUser = this.userMapper.queryById(userMobileIndex.getUserId());
            }
        }
        email = email.trim().toLowerCase();
        if (!Pattern.matches(REGEX_EMAIL, email) || email.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException(GeneralCode.USER_EMAIL_NOT_CORRECT);
        }

        tempUser = tempUser != null ? tempUser : this.userMapper.queryByEmail(email);
        if(tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {//用户存在且被删除
            throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
        }else if (tempUser != null) {
            // 如果用户已存在，判断是不是一键注册用户，且未激活
            Boolean isOneButton = BitUtils.isEnable(tempUser.getStatus(), AccountCommonConstant.ONE_BUTTON_REGISTER_USER) && !BitUtils.isEnable(tempUser.getStatus(), AccountCommonConstant.USER_ACTIVE);
            if (isOneButton) {
                // 如果是，发送激活验证码
                sendActivteCode(request, tempUser);
                return APIResponse.getOKJsonResult(new OneButtonRegisterResponse(tempUser.getUserId(), tempUser.getEmail()));
            }
            // 如果不是，按照注册逻辑，提示邮箱已被注册

            long ipUserIdCount =
                    Long.valueOf(String.valueOf(RedisCacheUtils.get(ip+tempUser.getUserId(), Long.class, AccountConstants.REGISTER_IP_COUNT_USERID, 0L)));
            if(ipUserIdCount>2){
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(GeneralCode.USER_EMAIL_USE);
            }else{
                try {
                    RedisCacheUtils.increment(ip+tempUser.getUserId(), AccountConstants.REGISTER_IP_COUNT_USERID, 1L, 24L, TimeUnit.HOURS);// 有效期
                } catch (Exception e) {
                    log.error("注册ip userid限制", e);
                }
                throw new BusinessException(AccountErrorCode.ACCOUNT_HAS_BEEN_REGISTERED);
            }
        }

        log.info("oneButtonRegister check email done, email:{}", email);

        // 同一ip注册限制
        long maxIpCount = Long.parseLong(this.iSysConfig.selectByDisplayName("max_register_count").getCode());
        long ipCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(ip, Long.class, CacheKeys.REGISTER_IP_COUNT, 0L)));
        if (ipCount >= maxIpCount) {
            throw new BusinessException(GeneralCode.USER_REGISTER_IP_EXCEED);
        }
        // 每日最大数量限制
        String today = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.getNewUTCDate());
        long oneButtonRegisterCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(today, Long.class, ONEBUTTON_REGISTER_COUNT_KEY, 0L)));
        if (oneButtonRegisterCount >= MAX_ONEBUTTON_REGISTER_DAILY_COUNT) {
            throw new BusinessException(GeneralCode.SYS_ACCESS_LIMITED);
        }
        // 每分钟最大数量限制
        String dateTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(DateUtils.getNewUTCDate());
        long oneButtonRegisterMinuteCount =
                Long.valueOf(String.valueOf(RedisCacheUtils.get(dateTime, Long.class, ONEBUTTON_REGISTER_MINUTE_COUNT_KEY, 0L)));
        if (oneButtonRegisterMinuteCount >= MAX_ONEBUTTON_REGISTER_MINUTE_COUNT) {
            throw new BusinessException(GeneralCode.SYS_ACCESS_LIMITED);
        }

        log.info("oneButtonRegister:获取一个用户索引");
        UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);// 获取一个用户索引
        log.info("oneButtonRegister:生成随机密码");
        String password = InvitationCodeUtil.generatePassword();
        String safePassword = DigestUtils.sha512Hex(password);
        log.info("oneButtonRegister:获取密码加密盐");
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObjectV2(userIndex, password, cipherCode, safePassword);
//        User user = User.buildRegisterObject(userIndex, password, cipherCode);

        log.info("oneButtonRegister:插入用户信息");
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            user.setStatus(BitUtils.enable(user.getStatus(),AccountCommonConstant.USER_NOT_BIND_EMAIL));
            user.setStatus(BitUtils.enable(user.getStatus(),AccountCommonConstant.USER_IS_MOBILE_USER));
            user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_MOBILE));
            // 记录手机索引
            final UserMobileIndex mobileIndex = new UserMobileIndex();
            mobileIndex.setMobile(requestBody.getMobile());
            mobileIndex.setCountry(requestBody.getMobileCode());
            mobileIndex.setUserId(userIndex.getUserId());
            this.userMobileIndexMapper.insert(mobileIndex);
        }
        user.setStatus(BitUtils.enable(user.getStatus(), AccountCommonConstant.ONE_BUTTON_REGISTER_USER));// 一键注册用户
        this.userMapper.insert(user);// 插入用户登录信息
        String userEmail = user.getEmail();
        UserSecurity userSecurity = new UserSecurity();
        userSecurity.setUserId(user.getUserId());
        log.info("oneButtonRegister:插入用户信息");
        userSecurity.setEmail(userEmail);
        userSecurity.setAntiPhishingCode("");// 防钓鱼码
        userSecurity.setSecurityLevel(1);// 安全级别
        userSecurity.setMobileCode("");
        userSecurity.setMobile("");
        userSecurity.setLoginFailedNum(0);
        userSecurity.setLoginFailedTime(DateUtils.getNewDate());
        userSecurity.setAuthKey("");
        userSecurity.setLastLoginTime(DateUtils.getNewDate());
        userSecurity.setLockEndTime(DateUtils.getNewDate());
        userSecurity.setInsertTime(DateUtils.getNewDate());
        userSecurity.setUpdateTime(DateUtils.getNewDate());
        userSecurity.setWithdrawSecurityStatus(0);
        userSecurity.setWithdrawSecurityAutoStatus(0);
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            userSecurity.setEmail("");
            userSecurity.setMobileCode(requestBody.getMobileCode());
            userSecurity.setMobile(requestBody.getMobile());
        }
        log.info("oneButtonRegister:插入用户安全信息");
        this.userSecurityMapper.insert(userSecurity);// 用户安全信息
        log.info("oneButtonRegister:初始化userInfo信息");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setParent(null);
        BigDecimal agentRewardRatio =
                new BigDecimal(this.iSysConfig.selectByDisplayName("agent_reward_ratio").getCode());
        userInfo.setAgentRewardRatio(agentRewardRatio);// 经纪人返佣比例
        userInfo.setTradingAccount(null);
        BigDecimal makerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("maker_commission").getCode());
        BigDecimal takerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("taker_commission").getCode());
        BigDecimal buyerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("buyer_commission").getCode());
        BigDecimal sellerCommission =
                new BigDecimal(this.iSysConfig.selectByDisplayName("seller_commission").getCode());
        userInfo.setMakerCommission(makerCommission);// 被动方手续费
        userInfo.setTakerCommission(takerCommission);// 主动方手续费
        userInfo.setBuyerCommission(buyerCommission);// 买方交易手续费
        userInfo.setSellerCommission(sellerCommission);// 卖方交易手续费
        userInfo.setDailyWithdrawCap(null);// 单日最大出金总金额
        userInfo.setDailyWithdrawCountLimit(null);// 单日最大出金次数
        userInfo.setAutoWithdrawAuditThreshold(null);// 免审核额度
        userInfo.setNickName("");
        userInfo.setRemark("");
        userInfo.setTrackSource(requestBody.getTrackSource());
        userInfo.setInsertTime(DateUtils.getNewDate());
        userInfo.setUpdateTime(DateUtils.getNewDate());
        userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
        userInfo.setTradeLevel(0);
        // 返佣开关关闭的话，无视推荐人
        String ref_switch = this.iSysConfig.selectByDisplayName("ref_switch").getCode();
        if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
            userInfo.setAgentId(null);
        }
        //这里的校验逻辑只是说，如果发现agent不合法那么需要置为空
        Boolean isValidateAgentId= userCommonValidateService.isValidateAgentId(userInfo.getAgentId());
        //不合法就置为空
        if (!isValidateAgentId) {
            userInfo.setAgentId(null);
        }
        if (userInfo.getAgentId() == null) {
            log.info("oneButtonRegister:设置默认推荐人");
            Long agentId = Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode());
            userInfo.setAgentId(agentId);
        }

        log.info("oneButtonRegister:插入userInfo信息");
        this.userInfoMapper.insertSelective(userInfo);// 插入用户信息
        String[] sendParams = new String[2];

        try {
            RedisCacheUtils.increment(ip, CacheKeys.REGISTER_IP_COUNT, 1L, 24L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("注册ip限制", e);
        }

        // 一键注册用户每日最大数量限制
        try {
            RedisCacheUtils.increment(today, ONEBUTTON_REGISTER_COUNT_KEY, 1L, 24L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("一键注册每日注册数量限制", e);
        }
        // 一键注册用户每分钟最大数量限制
        try {
            RedisCacheUtils.increment(dateTime, ONEBUTTON_REGISTER_MINUTE_COUNT_KEY, 1L, 2l, TimeUnit.MINUTES);// 有效期
        } catch (Exception e) {
            log.error("一键注册每分钟注册数量限制", e);
        }

        String locationCity = IP2LocationUtils.getCountryCity(ip);
        String clientType = request.getTerminal().getCode();
        // 添加注册日志
        try {
            final UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), ip, locationCity, clientType,
                    Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
            UserIp userIp = new UserIp(user.getUserId(), ip);
            this.userIpMapper.insertIgnore(userIp);
            this.userSecurityLogMapper.insertSelective(securityLog);
        } catch (Exception e) {
            log.error(String.format("add register log failed, email:%s, exception:", userEmail), e);
        }
        log.info("oneButtonRegister:注册结束");

        log.info("oneButtonRegister:发送激活验证码");
        try {
            sendActivteCode(request, user);
        } catch (Exception e) {
            // 用户注册完成后，发送激活码异常不处理
            log.error("oneButtonRegister sendActivteCode error", e);
        }

        // 临时的代码 完全迁移后移除 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put(UserConst.USER_ID, user.getUserId());
        dataMsg.put(UserConst.EMAIL, userEmail);
        dataMsg.put("salt", user.getSalt());
        dataMsg.put("password", user.getPassword());
        dataMsg.put("registerToken", sendParams[0]);
        dataMsg.put("code", sendParams[1]);
        dataMsg.put("agentId", userInfo.getAgentId());
        dataMsg.put("trackSource", userInfo.getTrackSource());
        dataMsg.put("ipAddress", ip);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, OptType.REGISTER, dataMsg);
        log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
        this.iMsgNotification.send(msg);
        // 临时的代码 完全迁移后移除 end
        return APIResponse.getOKJsonResult(new OneButtonRegisterResponse(user.getUserId(), userEmail));
    }

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public APIResponse<AccountActiveUserV2Response> oneButtonUserAccountActive(APIRequest<OneButtonUserAccountActiveRequest> request) throws Exception {
        final OneButtonUserAccountActiveRequest requestBody = request.getBody();

        // 邮箱或手机至少有一个
        if (StringUtils.isAnyBlank(requestBody.getMobile(), requestBody.getMobileCode()) && StringUtils.isBlank(requestBody.getEmail())) {
            throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
        }

        // 根据邮箱、手机号查userId
        GetUserIdByEmailOrMobileRequest getUserIdRequest = new GetUserIdByEmailOrMobileRequest();
        getUserIdRequest.setMobile(requestBody.getMobile());
        getUserIdRequest.setMobileCode(requestBody.getMobileCode());
        getUserIdRequest.setEmail(requestBody.getEmail());
        GetUserIdByEmailOrMobileResponse getUserIdResponse = iUserSecurity.getUserIdByMobileOrEmail(getUserIdRequest);
        final Long userId = getUserIdResponse.getUserId();

        // 验证是否是一键注册用户
        User user = userMapper.queryById(userId);
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (BitUtils.isFalse(user.getStatus(), AccountCommonConstant.ONE_BUTTON_REGISTER_USER)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 账号激活
        final APIRequest originRequest = new APIRequest<>();
        originRequest.setLanguage(request.getLanguage());
        originRequest.setTerminal(request.getTerminal());
        originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
        AccountActiveUserV2Request activeUserV2Request = new AccountActiveUserV2Request();
        BeanUtils.copyProperties(requestBody, activeUserV2Request);
        activeUserV2Request.setUserId(userId);
        APIResponse<AccountActiveUserV2Response> apiResponse = ((UserBusiness) applicationContext.getBean(this.getClass())).accountActiveV2(APIRequest.instance(originRequest, activeUserV2Request));
        Utils.CheckResponse(apiResponse);

        // 激活成功后，发送重置密码邮件
        try {
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            oneButtonUserForgotPassword(APIRequest.instance(originRequest, userIdRequest));
        } catch (Exception e) {
            log.error("oneButtonUserForgotPassword error", e);
        }

        return apiResponse;
    }

    @Override
    public APIResponse<Void> oneButtonUserForgotPassword(APIRequest<UserIdRequest> request) throws Exception {
        final UserIdRequest requestBody = request.getBody();
        final Long userId = requestBody.getUserId();
        log.info("oneButtonUserForgotPassword request userId={}", userId);

        User user = userMapper.queryById(userId);
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (BitUtils.isFalse(user.getStatus(), AccountCommonConstant.ONE_BUTTON_REGISTER_USER)) {
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        // 如果是手机号用户，发送短信修改密码提醒；如果是邮箱用户，发送重置密码邮件
        if (BitUtils.isTrue(user.getStatus(), AccountCommonConstant.USER_MOBILE)) {
            // 发送短信
            // 一分钟频率控制
            String frequencyLimits =
                    RedisCacheUtils.get(String.valueOf(userId), String.class, AccountConstants.ONEBUTTONUSER_RESETPSW_SMS_KEY);
            if (StringUtils.isNotBlank(frequencyLimits)) {
                throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] {1});
            }
            RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, AccountConstants.ONEBUTTONUSER_RESETPSW_SMS_KEY);
            log.info("oneButtonUserForgotPassword send sms start userId={}",userId);

            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            if (StringUtils.isBlank(userSecurity.getMobile())) {
                throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
            }
            SendMsgRequest requestSms = new SendMsgRequest();
            requestSms.setIp(WebUtils.getRequestIp());
            Country country = this.iCountry.getCountryByCode(userSecurity.getMobileCode());
            requestSms.setMobileCode(country.getMobileCode());
            requestSms.setRecipient(userSecurity.getMobile());
            requestSms.setUserId(userSecurity.getUserId().toString());
            requestSms.setTplCode(AccountConstants.ONEBUTTONUSER_RESETPSW_SMS_CODE);
            requestSms.setMsgType(MsgType.TEXT);
            requestSms.setResend(false);

            Map<String, Object> data = new HashMap<>();
            String lang = StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en");
            String link = String.format("%s%s/user/reset-password/1", mainSiteDomainUrl, lang);
            data.put("link", link);
            requestSms.setData(data);

            // 发送短信
            userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                    WebUtils.getAPIRequestHeader().getTerminal());
            log.info("oneButtonUserForgotPassword send sms success userId={}",userId);
        } else if (BitUtils.isFalse(user.getStatus(), AccountCommonConstant.USER_NOT_BIND_EMAIL)) {
            // 发送邮件
            log.info("oneButtonUserForgotPassword send email start userId={}", userId);

            // 发送邮件
            Map<String, Object> data = new HashMap<>();
            String lang = StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en");
            String emailLink = String.format("%s%s/user/reset-password/1", mainSiteDomainUrl, lang);
            data.put("link", emailLink);

            // code 用 AccountConstants.ONEBUTTONUSER_RESETPSW_EMAIL_CODE  邮件模版还没配 TODO
            String disableToken = userCommonBusiness.sendDisableTokenEmail(AccountConstants.ONEBUTTONUSER_RESETPSW_EMAIL_CODE, user, data,
                    "一键注册用户发送重置密码邮件：", null);


            log.info("oneButtonUserForgotPassword send email success userId={}", userId);
        } else {
            log.warn("oneButtonUserForgotPassword mobile or email binding is needed, userId={}", userId);
        }

        return  APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<List<UserVo>> getUserStatusByUserIds(APIRequest<GetUserListRequest> request) {
        List<Long> userIds = request.getBody().getUserIds();
        // 限制userIds长度为100
        userIds = userIds.size() > 100 ? userIds.subList(0, 100) : userIds;
        
        List<User> users =  userMapper.selectByUserIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            return APIResponse.getOKJsonResult(Lists.newArrayList());    
        }

        List<UserVo> result = users.stream().map(x -> {
            UserVo userVo = new UserVo();
            userVo.setUserId(x.getUserId());
            userVo.setEmail(x.getEmail());
            userVo.setStatus(x.getStatus());
            return userVo;
        }).collect(Collectors.toList());
        
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<Boolean> finishLVTSAQ(APIRequest<UserIdReq> request) {
        Long userId = request.getBody().getUserId();
        log.info("finishLVTSAQ userId={}", userId);
        UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User user = this.userMapper.queryByExistentEmail(userIndex.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        UserConfig config = new UserConfig();
        config.setUserId(userId);
        config.setConfigType(UserConfigTypeEnum.LVT_SAQ.name());
        config.setConfigName(Boolean.TRUE.toString());
        config.setUpdateTime(new Date());
        userInfoMapper.insertOrUpdateUserConfig(config);
        return APIResponse.getOKJsonResult(true);
    }

    @Override
    public APIResponse<UserSAQResponse> queryLVTSAQStatus(APIRequest<UserIdReq> request) {
        Long userId = request.getBody().getUserId();

        UserConfig queryConfig = new UserConfig();
        queryConfig.setUserId(userId);
        queryConfig.setConfigType(UserConfigTypeEnum.LVT_SAQ.name());
        UserConfig config = userInfoMapper.selectLatestUserConfig(queryConfig);

        UserSAQResponse response = new UserSAQResponse();
        response.setUserId(userId);
        response.setType(UserConfigTypeEnum.LVT_SAQ.name());

        // 仅当数据库中为true返回true
        boolean isFinished = config != null && Boolean.parseBoolean(config.getConfigName());
        response.setStatus(String.valueOf(isFinished));

        return APIResponse.getOKJsonResult(response);
    }

    private void sendActivteCode(APIRequest<OneButtonRegisterRequest> request, User tempUser) throws Exception {
        final OneButtonRegisterRequest requestBody = request.getBody();
        RegisterationMethodEnum registerationMethodEnum=requestBody.getRegisterationMethod();
        if(RegisterationMethodEnum.MOBILE==registerationMethodEnum){
            // 如果是手机号注册，发送短信验证码
            SendSmsAuthCodeV2Request sendSmsAuthCodeV2Request = new SendSmsAuthCodeV2Request();
            sendSmsAuthCodeV2Request.setUserId(tempUser.getUserId());
            sendSmsAuthCodeV2Request.setEmail(tempUser.getEmail());
            sendSmsAuthCodeV2Request.setMobile(requestBody.getMobile());
            sendSmsAuthCodeV2Request.setMobileCode(requestBody.getMobileCode());
            sendSmsAuthCodeV2Request.setBizScene(BizSceneEnum.ACCOUNT_ACTIVATE);// 使用激活的场景

            sendSmsAuthCodeV2(APIRequest.instance(sendSmsAuthCodeV2Request));
        } else {
            // 如果是邮箱注册，发送邮件验证码
            SendEmailVerifyCodeRequest sendEmailVerifyCodeRequest = new SendEmailVerifyCodeRequest();
            sendEmailVerifyCodeRequest.setUserId(tempUser.getUserId());
            sendEmailVerifyCodeRequest.setBizScene(BizSceneEnum.ACCOUNT_ACTIVATE);// 使用激活的场景
            iUserSecurity.sendEmailVerifyCode(sendEmailVerifyCodeRequest);
        }

        // 一键注册请求次数+1
        final String ip = WebUtils.getRequestIp();
        try {
            RedisCacheUtils.increment(ip, ONEBUTTON_REGISTER_REQUEST_COUNT_KEY, 1L, 1L, TimeUnit.HOURS);// 有效期
        } catch (Exception e) {
            log.error("一键注册ip请求次数限制", e);
        }
    }

    @Override
    public APIResponse<FinanceFlagResponse> financeFlag(APIRequest<UserIdReq> request) throws Exception {
        // 接口的逻辑是，将所有financeFlag保存在UserConfig中，请求先查UserConfig。如果某一项为true，则不再查询；如果某一项为false，则去实时重新查一遍对应数据
        final Long userId = request.getBody().getUserId();
        log.info("financeFlag request userId={}", userId);
        // 先查缓存，避免为false时一直去实时查询
        FinanceFlagResponse response = RedisCacheUtils.get(userId.toString(), FinanceFlagResponse.class, AccountConstants.ACCOUNT_FINANCE_FLAG_KEY);
        if (response != null) {
            // 刚上线时，redis中缓存还是旧对象，没有新属性，此时给个默认的false
            if (response.getHasWithdrawRecord() == null) {
                response.setHasWithdrawRecord(false);    
            }
            return APIResponse.getOKJsonResult(response);
        }

//        Lock lock = RedisCacheUtils.getLock(AccountConstants.ACCOUNT_FINANCE_FLAG_LOCK_KEY+userId);
//        if (lock != null && lock.tryLock()) {
            try {
                UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
                // 账号不存在
                if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }
                User user = this.userMapper.queryByEmail(userIndex.getEmail());
                if (null == user) {
                    throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                }

                Map<String, Object> params = new HashMap<>();
                params.put("userId", userId);
                List<FinanceFlagEnum> configTypes = Arrays.asList(FinanceFlagEnum.values());
                params.put("configTypes", configTypes);
                List<UserConfig> userConfigs = userInfoMapper.selectUserConfigList(params);
                Map<String, String> configMap = userConfigs.stream().collect(Collectors.toMap(UserConfig::getConfigType, UserConfig::getConfigName));
                // 如果数据库里没有，或者不全，init一下
                if (MapUtils.isEmpty(configMap) || configMap.size() < FinanceFlagEnum.values().length) {
                    initFinanceFlag(userId);
                }

                Boolean hasFinanceRecord = StringUtils.isNotBlank(configMap.get(FinanceFlagEnum.HAS_FINANCE_RECORD.name())) ?
                        Boolean.valueOf(configMap.get(FinanceFlagEnum.HAS_FINANCE_RECORD.name())) : false;
                Boolean kycPass = StringUtils.isNotBlank(configMap.get(FinanceFlagEnum.KYC_PASS.name())) ?
                        Boolean.valueOf(configMap.get(FinanceFlagEnum.KYC_PASS.name())) : false;
                Boolean hasAsset = StringUtils.isNotBlank(configMap.get(FinanceFlagEnum.HAS_ASSET.name())) ?
                        Boolean.valueOf(configMap.get(FinanceFlagEnum.HAS_ASSET.name())) : false;
                Boolean hasSpotOrder = StringUtils.isNotBlank(configMap.get(FinanceFlagEnum.HAS_SPOT_ORDER.name())) ?
                        Boolean.valueOf(configMap.get(FinanceFlagEnum.HAS_SPOT_ORDER.name())) :false;
                Boolean hasWithdrawRecord = StringUtils.isNotBlank(configMap.get(FinanceFlagEnum.HAS_WITHDRAW_RECORD.name())) ?
                        Boolean.valueOf(configMap.get(FinanceFlagEnum.HAS_WITHDRAW_RECORD.name())) : false;

                List<String> needUpdateConfigType = new ArrayList<>();// 上面4个config，需要更新数据库的

                // 如果不存在config记录，或者config的value是false，发起实时查询
                // 财务记录
                if (!hasFinanceRecord) {
                    SelectUserAssetLogResponse assetLog = userAssetApiClient.getAssetLogByParam(userId, null, null, null);
                    if (assetLog != null) {
                        hasFinanceRecord = true;

                        needUpdateConfigType.add(FinanceFlagEnum.HAS_FINANCE_RECORD.name());
                    }
                }

                // kyc状态
                if (!kycPass) {
                    KycCertificateResult certificateResult = userCommonBusiness.getKycStatues(user);
                    if (certificateResult != null) {
                        kycPass = Integer.valueOf(1).equals(certificateResult.getCertificateStatus());

                        if (kycPass) {
                            needUpdateConfigType.add(FinanceFlagEnum.KYC_PASS.name());
                        }
                    }
                }

                // 用户资产
                if (!hasAsset) {
                    UserAssetResponse userAssetResponse = userAssetApiClient.getPrivateUserAsset(userId.toString(), null);
                    if (userAssetResponse != null && CollectionUtils.isNotEmpty(userAssetResponse.getUserAssetList())) {
                        for (UserAssetResponse.UserAsset userAsset : userAssetResponse.getUserAssetList()) {
                            if (userAsset.getFree().add(userAsset.getFreeze()).add(userAsset.getLocked()).add(userAsset.getWithdrawing()).compareTo(new BigDecimal(0)) != 0) {
                                hasAsset = true;
                                needUpdateConfigType.add(FinanceFlagEnum.HAS_ASSET.name());
                                break;
                            }
                        }
                    }
                }

                // 现货订单
                if (!hasSpotOrder) {
                    hasSpotOrder = streamerOrderApiClient.hasOrder(userId, null);
                    if (hasSpotOrder) {
                        needUpdateConfigType.add(FinanceFlagEnum.HAS_SPOT_ORDER.name());
                    }
                }

                // 提币记录
                if (!hasWithdrawRecord) {
                    GetWithdrawCountRequest getWithdrawCountRequest = new GetWithdrawCountRequest();
                    getWithdrawCountRequest.setUserId(userId);
                    getWithdrawCountRequest.setPage(1);
                    getWithdrawCountRequest.setOffset(0);// 查一条就可以
                    List<WithdrawVo> withdrawVoList = capitalClient.withdrawList(getWithdrawCountRequest);
                    if (CollectionUtils.isNotEmpty(withdrawVoList)) {
                        hasWithdrawRecord = true;

                        needUpdateConfigType.add(FinanceFlagEnum.HAS_WITHDRAW_RECORD.name());
                    }
                }

                // 更新数据库
                if (CollectionUtils.isNotEmpty(needUpdateConfigType)) {
                    userInfoMapper.updateUserConfigToTrue(userId, needUpdateConfigType);
                }

                response = new FinanceFlagResponse(hasFinanceRecord, kycPass, hasAsset, hasSpotOrder, hasWithdrawRecord);
                RedisCacheUtils.set(userId.toString(), response, financeFlagTimeout, AccountConstants.ACCOUNT_FINANCE_FLAG_KEY);
                log.info("financeFlag userId={} response={}", userId, JSONObject.toJSONString(response));
                return APIResponse.getOKJsonResult(response);    
            } catch (DuplicateKeyException e) {
                // 并发场景会出现DuplicateKeyException，不影响
                log.warn("financeFlag DuplicateKeyException error, {}", e.getMessage());
                throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
            } catch (Exception e) {
                log.error(String.format("financeFlag failed, userId:%s, exception:", userId), e);
                if (e instanceof BusinessException) {
                    throw e;                    
                }
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
//            finally {
//                if (lock != null) {
//                    lock.unlock();
//                }
//            }
//        } else {
//            log.info("financeFlag get lock failed");
//            throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
//        }
    }

    private void initFinanceFlag(Long userId) {
        // 初始化数据
        UserConfig financeUc = new UserConfig();
        financeUc.setUserId(userId);
        financeUc.setConfigType(FinanceFlagEnum.HAS_FINANCE_RECORD.name());
        financeUc.setConfigName("false");

        UserConfig kycUc = new UserConfig();
        kycUc.setUserId(userId);
        kycUc.setConfigType(FinanceFlagEnum.KYC_PASS.name());
        kycUc.setConfigName("false");

        UserConfig assetUc = new UserConfig();
        assetUc.setUserId(userId);
        assetUc.setConfigType(FinanceFlagEnum.HAS_ASSET.name());
        assetUc.setConfigName("false");

        UserConfig spotOrderUc = new UserConfig();
        spotOrderUc.setUserId(userId);
        spotOrderUc.setConfigType(FinanceFlagEnum.HAS_SPOT_ORDER.name());
        spotOrderUc.setConfigName("false");

        UserConfig withdrawUc = new UserConfig();
        withdrawUc.setUserId(userId);
        withdrawUc.setConfigType(FinanceFlagEnum.HAS_WITHDRAW_RECORD.name());
        withdrawUc.setConfigName("false");

        List<UserConfig> list = Arrays.asList(financeUc, kycUc, assetUc, spotOrderUc, withdrawUc);
        userInfoMapper.batchInsertIgnoreUserConfig(list);
    }

}
