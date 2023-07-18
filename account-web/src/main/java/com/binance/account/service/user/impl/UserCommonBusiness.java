package com.binance.account.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constant.AccountCommonConstant;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.*;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.*;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserTradingAccountMapper;
import com.binance.account.domain.bo.CapitalWithdrawRedisVerify;
import com.binance.account.domain.bo.OauthRedisDto;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.integration.margin.MarginAccountApiClient;
import com.binance.account.integration.mbxgateway.MbxgatewayIOrderApiClient;
import com.binance.account.integration.streamer.StreamerOrderApiClient;
import com.binance.account.service.apimanage.IApiManageService;
import com.binance.account.service.async.MsgAsyncTask;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.utils.InvitationCodeUtil;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.BindOauthRequest;
import com.binance.inspector.common.enums.JumioError;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.models.RedisVerify;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.Assert;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.CouplingCalculationUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.master.utils.security.TokenUtils;
import com.binance.master.web.RestTemplateAbstract;
import com.binance.messaging.api.msg.MsgApi;
import com.binance.messaging.api.msg.TwilioApi;
import com.binance.messaging.api.msg.request.MsgType;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.messaging.api.msg.response.MsgResponse;
import com.binance.messaging.api.twilio.request.TwilioFeedBackRequest;
import com.binance.streamer.api.response.vo.OpenOrderVo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.binance.account.service.user.impl.UserBusiness.DEFAULT_RESULT;
import static com.binance.account.vo.security.enums.BizSceneEnum.CRYPTO_WITHDRAW;
import static com.binance.master.enums.TerminalEnum.WEB;

@Log4j2
@Service
public class UserCommonBusiness extends RestTemplateAbstract {

    @Resource
    private MsgApi msgApi;

    @Resource
    private TwilioApi twilioApi;

    @Resource
    protected UserMapper userMapper;
    @Resource
    protected ISysConfig iSysConfig;
    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    protected UserInfoMapper userInfoMapper;
    @Resource
    protected UserIndexMapper userIndexMapper;
    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    @Resource
    protected UserTradingAccountMapper userTradingAccountMapper;
    @Autowired
    private MsgAsyncTask msgAsyncTask;
    @Resource
    private MessageUtils messageUtils;
    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;
    @Resource
    private FutureAccountApiClient futureAccountApiClient;
    @Autowired
    private MbxgatewayIOrderApiClient mbxGatewayOrderApiCLient;
    @Autowired
    private StreamerOrderApiClient streamerOrderApiClient;
    @Autowired
    private MarginAccountApiClient marginAccountApiClient;
    @Resource
    private IApiManageService iApiManageService;
    @Autowired
    private RootUserIndexMapper rootUserIndexMapper;

    @Value("${account.user.id.alert.value}")
    private long userIdAlertValue;
    @Value("${account.user.id.alert.generate}")
    protected long autoUserIdGenerateNum;
    @Value("${account.init.min.user.id:10000000}")
    private long initMinUserId;
    @Value("${mainSite.domain.url}")
    protected String mainSiteDomainUrl;

    public static final int EXPIRED_TIME = 30;
    public static final int THIRD_PARTY_EMAIL_EXPIRED_TIME = 1440;// 第三方用户的重置密码邮件有效期，分钟
    public static final int EMAIL_GAP_TIME = 10;
    public static final int OAUTH_EXPIRED_TIME = 180;
    public static final int OAUTH_EMAIL_GAP_TIME = 3;
    // 30分钟内验证码不重发，还用原来的
    public static final int OAUTH_VERIFY_CODE_TIME = 30;

    @Resource
    private KycCertificateMapper kycCertificateMapper;

    @Resource
    private KycFillInfoMapper kycFillInfoMapper;


    @Autowired
    private ApolloCommonConfig commonConfig;

    @Value("#{${sms.scene.template.map:{}}}")
    private Map<String, String> smsSceneTemplateMap;

    @Value("#{${email.scene.template.map:{}}}")
    private Map<String, String> emailSceneTemplateMap;


    @Resource
    private ICountry iCountry;


    public long getUserIdAlertValue() {
        return userIdAlertValue;
    }

    public void setUserIdAlertValue(long userIdAlertValue) {
        this.userIdAlertValue = userIdAlertValue;
    }

    public long getAutoUserIdGenerateNum() {
        return autoUserIdGenerateNum;
    }

    public void setAutoUserIdGenerateNum(long autoUserIdGenerateNum) {
        this.autoUserIdGenerateNum = autoUserIdGenerateNum;
    }

    public long getInitMinUserId() {
        return initMinUserId;
    }

    public void setInitMinUserId(long initMinUserId) {
        this.initMinUserId = initMinUserId;
    }


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean updateMarginTradeAccount(Long extendId, boolean enable){
       return marginAccountApiClient.updateTradeStatus(extendId,enable);
    }


    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Long updateFutureTradeAccount(Long extendId, boolean enable, Long meTradeAccount){
        if (enable){
            return futureAccountApiClient.updateAccount(extendId,true);
        }else{
            // 撤单
            futureAccountApiClient.cancelFutureOpenOrders(String.valueOf(meTradeAccount));
            return futureAccountApiClient.updateAccount(extendId,false);
        }
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

    /**
     * 创建交易账户（这个方法有一定的风险，当撮合不可用时，调用可能报错会导致激活失败，合理的做法是
     * 上层业务记录事务日志，判断业务执行到哪儿一步，然后走补偿job继续执行）
     *
     * @param tempUserInfo
     * @return
     */
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW,
            noRollbackFor = Exception.class)
    public Long createTradingAccount(UserInfo tempUserInfo) {// 创建交易账号不回滚 防止重复创建
        try {
            Map<String, Object> paramsMap = new HashMap<>();

            final String url = String.format("%s/v1/account",
                    this.iSysConfig.selectByDisplayName("matchbox_management_root_url").getCode());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            builder.queryParam("externalId", tempUserInfo.getUserId());
            builder.queryParam("makerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getMakerCommission()));
            builder.queryParam("takerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getTakerCommission()));
            builder.queryParam("buyerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getBuyerCommission()));
            builder.queryParam("sellerCommission",
                    CouplingCalculationUtils.feeLong(tempUserInfo.getSellerCommission()));
            log.info("createTradingAccount URL：{}", builder.build().toUriString());
            JSONObject result = super.postFrom(builder.build().toUriString(), paramsMap, null, JSONObject.class);
            Long tradingAccount = result.getLong("accountId");
            log.info("createTradingAccount tradingAccount：{}", tradingAccount);
            if (tradingAccount != null) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(tempUserInfo.getUserId());
                userInfo.setTradingAccount(tradingAccount);
                this.userInfoMapper.updateByPrimaryKeySelective(userInfo);
                UserTradingAccount userTradingAccount = new UserTradingAccount();// 插入交易账户索引
                userTradingAccount.setTradingAccount(tradingAccount);
                userTradingAccount.setUserId(tempUserInfo.getUserId());
                log.info("createTradingAccount insert:{}", JSON.toJSONString(userTradingAccount));
                this.userTradingAccountMapper.insert(userTradingAccount);// 交易账户索引 激活时创建交易账户
            }
            return tradingAccount;
        } catch (Exception e) {
            log.error(String.format("createTradingAccount failed, userId:%s, exception:", tempUserInfo.getUserId()), e);
        }
        return null;
    }

    /**
     * 创建交易账号
     * @param tempUserInfo
     * @return
     */
    public Long createTradingAccountOnly(UserInfo tempUserInfo) {// 创建交易账号不回滚 防止重复创建
        try {
            Map<String, Object> paramsMap = new HashMap<>();

            final String url = String.format("%s/v1/account",
                    this.iSysConfig.selectByDisplayName("matchbox_management_root_url").getCode());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            builder.queryParam("externalId", tempUserInfo.getUserId());
            builder.queryParam("makerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getMakerCommission()));
            builder.queryParam("takerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getTakerCommission()));
            builder.queryParam("buyerCommission", CouplingCalculationUtils.feeLong(tempUserInfo.getBuyerCommission()));
            builder.queryParam("sellerCommission",
                    CouplingCalculationUtils.feeLong(tempUserInfo.getSellerCommission()));
            log.info("createTradingAccount URL：{}", builder.build().toUriString());
            JSONObject result = super.postFrom(builder.build().toUriString(), paramsMap, null, JSONObject.class);
            Long tradingAccount = result.getLong("accountId");
            log.info("createTradingAccount tradingAccount：{}", tradingAccount);
            return tradingAccount;
        } catch (Exception e) {
            log.error(String.format("createTradingAccount failed, userId:%s, exception:", tempUserInfo.getUserId()), e);
        }
        return null;
    }

    /**
     * 生成账户disableToken，并放进redis缓存
     *
     * @param userId 用户id
     * @return disableToken
     */
    public String generateAndSetDisableCode(Long userId) {
        Assert.notNull(userId, "generateAndSetDisableCode failed, userId cannot be null");
        String disableToken;
        try {
            disableToken = TokenUtils.emailRedisToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 有效期24小时
        RedisCacheUtils.set(disableToken, userId.toString(), Constant.DAY, CacheKeys.USER_DISABLE_CODE);
        return disableToken;
    }

    /**
     * 发送消息
     *
     * @param request
     * @throws Exception
     */
    public void sendMsg(SendMsgRequest request, LanguageEnum languageEnum, TerminalEnum terminalEnum) {
        log.info("languageEnum={}",languageEnum.getLang());
        APIRequestHeader originRequest = new APIRequestHeader();
        if (null == languageEnum) {
            originRequest.setLanguage(WebUtils.getAPIRequestHeader().getLanguage());
        } else {
            originRequest.setLanguage(languageEnum);
        }
        if (null == terminalEnum) {
            originRequest.setTerminal(WebUtils.getAPIRequestHeader().getTerminal());
        } else {
            originRequest.setTerminal(terminalEnum);
        }
        log.info("languageEnum={}",originRequest.getLanguage().getLang());
        // 根据用户id查找用户防钓鱼码
        request.setAntiPhishingCode(this.userInfoMapper.selectPhishingCode(Long.valueOf(request.getUserId())));
        APIResponse<MsgResponse> resp;
        try {
            resp = this.msgApi.sendMsg(APIRequest.instance(originRequest, request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("UserBusiness.sendMsg Resp:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(resp)));
        if (resp.getStatus() == Status.ERROR) {
            // throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
            throw new BusinessException(resp.getCode(), Objects.toString(resp.getErrorData(), ""));
        }
    }

    /**
     * 发送TwilioFeedback API
     *
     */
    public void sendTwilioFeedback(TwilioFeedBackRequest request, LanguageEnum languageEnum, TerminalEnum terminalEnum) {
        APIRequestHeader originRequest = new APIRequestHeader();
        if (null == languageEnum) {
            originRequest.setLanguage(WebUtils.getAPIRequestHeader().getLanguage());
        } else {
            originRequest.setLanguage(languageEnum);
        }
        if (null == terminalEnum) {
            originRequest.setTerminal(WebUtils.getAPIRequestHeader().getTerminal());
        } else {
            originRequest.setTerminal(terminalEnum);
        }

        try {
            this.twilioApi.twilioFeedBack(APIRequest.instance(originRequest, request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private static final String DEFAULT_ACTIVATE_YUBIKEY_EMIAL_LINK = "%sgateway-api/v1/public/account/web-authn/activate/{activateCode}";

    /**
     * 发送激活yubikey邮件
     *
     * @return
     */
    public String sendActivateYubiKeyEmail(User user, String activateCode, String yubiKeyNickname,
                                           String customActivateLink, String remark) {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());

        Map<String, Object> param = new HashMap<>();
        param.put("ip", WebUtils.getRequestIp());
        param.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        param.put("email", getTrueEmailOrMobile(user.getEmail(),user.getUserId()));
        param.put("userId", user.getUserId());
        param.put("register_name", yubiKeyNickname);

        String originActivateLink = String.format(DEFAULT_ACTIVATE_YUBIKEY_EMIAL_LINK,
                                                    WebUtils.getHeader(Constant.BASE_URL));
        String activateLink = emailLinkGenerator(customActivateLink, originActivateLink,
                                                ImmutableMap.of("activateCode", activateCode));
        param.put("link", activateLink);
        param.putIfAbsent("browser", WebUtils.getHeader(Constant.BASE_BROWER));
        requestMsg.setTplCode(Constant.MESSAGE_TEMPLATE_ACTIVATE_YUBIKEY);
        requestMsg.setData(param);

        msgAsyncTask.sendMsgTry(requestMsg, remark, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        return activateCode;
    }


    /**
     * 发送携带一键禁用的邮件
     *
     * @param tplCode
     * @param user
     * @param data
     * @param remark
     * @return
     */
    public String sendDisableTokenEmail(String tplCode, User user, Map<String, Object> data, String remark,
            String customForbiddenLink) {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));

        data.put("email", getTrueEmailOrMobile(user.getEmail(),user.getUserId()));

        data.put("userId", user.getUserId());

        String disableToken = this.generateAndSetDisableCode(user.getUserId());
        data.put("emailVerifyCode", disableToken);

//        String originEmailLink = String.format("%sforbiddenAccount.html?userId={userId}&emailVerifyCode={emailVerifyCode}",
//                WebUtils.getHeader(Constant.BASE_URL));
        String originEmailLink = String.format("%s%s/usercenter/security/disable-account",WebUtils.getHeader(Constant.BASE_URL),
                    StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en"));
        String forbiddenLink = emailLinkGenerator(null,
                originEmailLink,
                ImmutableMap.of("userId", user.getUserId(), "emailVerifyCode", disableToken));
        data.put("forbiddenLink", forbiddenLink);
        data.putIfAbsent("browser", WebUtils.getHeader(Constant.BASE_BROWER));
        requestMsg.setTplCode(tplCode);
        requestMsg.setData(data);

        msgAsyncTask.sendMsgTry(requestMsg, remark, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        return disableToken;
    }

    public String getTrueEmailOrMobile(String email,Long userId){
        if(email.contains("_mobileuser@binance.com")){
            UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
            if (userSecurity == null) {
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            if (StringUtils.isBlank(userSecurity.getMobile())) {
                throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
            }
            return userSecurity.getMobile();
        }else{
           return email;
        }
    }

    /**
     * 有些发送邮件请求，是从第三方页面过来的（比如Advcash用户注册），Header里没有我们的BASE_URL
     * @param tplCode
     * @param user
     * @param data
     * @param remark
     * @param customForbiddenLink
     * @return
     */
    public String sendDisableTokenEmailForThirdParty(String tplCode, User user, Map<String, Object> data, String remark,
                                        String customForbiddenLink) {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        data.put("email", getTrueEmailOrMobile(user.getEmail(),user.getUserId()));
        data.put("userId", user.getUserId());

        String disableToken = this.generateAndSetDisableCode(user.getUserId());
        data.put("emailVerifyCode", disableToken);

//        String originEmailLink = String.format("%sforbiddenAccount.html?userId={userId}&emailVerifyCode={emailVerifyCode}",
//                WebUtils.getHeader(Constant.BASE_URL));
        if (StringUtils.isBlank(mainSiteDomainUrl)) {
            log.error("sendDisableTokenEmailForThirdParty mainSiteDomainUrl null error");
            throw new BusinessException("sendDisableTokenEmailForThirdParty mainSiteDomainUrl null error");
        }
        String originEmailLink = String.format("%s%s/usercenter/security/disable-account",mainSiteDomainUrl,
                StringUtils.defaultIfEmpty(WebUtils.getHeader("lang"),"en"));
        String forbiddenLink = emailLinkGenerator(null,
                originEmailLink,
                ImmutableMap.of("userId", user.getUserId(), "emailVerifyCode", disableToken));
        data.put("forbiddenLink", forbiddenLink);
        data.putIfAbsent("browser", WebUtils.getHeader(Constant.BASE_BROWER));
        requestMsg.setTplCode(tplCode);
        requestMsg.setData(data);

        msgAsyncTask.sendMsgTry(requestMsg, remark, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        return disableToken;
    }

    /**
     * 发送没有请求的email（用于后台自主发送）
     *
     * @param tplCode
     * @param user
     * @param data
     * @param remark
     * @return
     * @throws NoSuchAlgorithmException
     */
    public void sendEmailWithoutRequest(String tplCode, User user, Map<String, Object> data, String remark,
            LanguageEnum language) {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        if (data == null) {
            data = new HashMap<>();
        }
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        data.put("email", getTrueEmailOrMobile(user.getEmail(),user.getUserId()));
        data.put("userId", user.getUserId());
        requestMsg.setTplCode(tplCode);
        requestMsg.setData(data);
        // 根据用户id查找用户防钓鱼码
        requestMsg.setAntiPhishingCode(this.userInfoMapper.selectPhishingCode(user.getUserId()));

        APIRequestHeader originRequest = new APIRequestHeader();
        originRequest.setLanguage(language);
        originRequest.setTerminal(WEB);

        APIResponse<MsgResponse> resp = null;
        try {
            resp = this.msgApi.sendMsg(APIRequest.instance(originRequest, requestMsg));
            log.info("UserBusiness.sendMsg Resp:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(resp)));
            if (resp.getStatus() == Status.ERROR) {
                throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
            }
        } catch (Exception e) {
            log.error(String.format("sendMsgTry failed, remark:%s, exception:{}", remark), e);
        }
    }

    /**
     * 发送更换邮箱验证
     *
     * @param userId
     * @param email
     * @return
     */
    public void sendUserEmailChangeLinkEmail(String tplCode, Long userId, String email, String gateWayLink,Map<String,String> rejectInfo) {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(email);
        requestMsg.setUserId(userId.toString());
        requestMsg.setIp(WebUtils.getRequestIp());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%s").append("gateway-api").append(gateWayLink);

        String link = String.format(stringBuilder.toString(), WebUtils.getHeader(Constant.BASE_URL));
        log.info("sendUserEmailChangeLinkEmail link is {}", link);
        Map<String, Object> param = new HashMap<>();
        param.put("link", link);
        param.put("time",DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        if (rejectInfo!=null) {
            param.put("info", rejectInfo.get(WebUtils.getAPIRequestHeader().getLanguage().getLang()));
        }

        // requestMsg.setTplCode(Constant.MESSAGE_TEMPLATE_USER_EMAIL_CHANGE);
        requestMsg.setTplCode(tplCode);

        String disableToken = this.generateAndSetDisableCode(userId);
        param.put("emailVerifyCode", disableToken);

        String originEmailLink = String.format("%sforbiddenAccount.html?userId={userId}&emailVerifyCode={emailVerifyCode}",
                WebUtils.getHeader(Constant.BASE_URL));
        String customForbiddenLink = null;

        String forbiddenLink = emailLinkGenerator(customForbiddenLink,
                originEmailLink,
                ImmutableMap.of("userId", userId, "emailVerifyCode", disableToken));
        param.put("forbiddenLink", forbiddenLink);


        requestMsg.setData(param);

        msgAsyncTask.sendMsgTry(requestMsg, "user email change", WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());

    }



    /**
     * 发送jumio流程审核的邮件（用于后台自主发送）
     */
    public void sendJumioCheckEmail(long userId, String country, String reason, String emailTemplateCode,
            String remark) {
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
        // 发送提醒邮件
        Map<String, Object> data = Maps.newHashMap();
        if (StringUtils.equalsIgnoreCase("CN", country)) {
            String reasonMsg = getJumioFailReason(reason, true);
            if (StringUtils.isNotBlank(reasonMsg)) {
                data.put("reason", reasonMsg);
            }
            sendEmailWithoutRequest(emailTemplateCode, dbUser, data, remark, LanguageEnum.ZH_CN);
        } else {
            String reasonMsg = getJumioFailReason(reason, false);
            if (StringUtils.isNotBlank(reasonMsg)) {
                data.put("reason", reasonMsg);
            }
            sendEmailWithoutRequest(emailTemplateCode, dbUser, data, remark, LanguageEnum.EN_US);
        }
    }

    /**
     * Jumio 错误原因提示语
     *
     * @param msg
     * @param isCn
     * @return
     */
    public String getJumioFailReason(String msg, boolean isCn) {
        if (StringUtils.isBlank(msg)) {
            return msg;
        }
        //兼容新老: 先从MessageMap中获取，如果获取不到值的情况下再按原来的逻辑走 2019-07-30（todo 以后迁移完成后都直接按这个MessageMap走）
        String message = MessageMapHelper.getMessage(msg, isCn ? LanguageEnum.ZH_CN : LanguageEnum.EN_US);
        if (!msg.equals(message)) {
            // 找到了对应的错误信息
            return message;
        }
        // 没有对应的信息时再按原来的罗就走一遍
        try {
            String result = msg;
            // JUMIO 模块化后的描述语使用
            JumioError jumioError = JumioError.getByName(msg);
            if (jumioError == null) {
                LanguageEnum language = isCn ? LanguageEnum.ZH_CN : LanguageEnum.EN_US;
                result = messageUtils.getMessage(msg, language);
            }else {
                if (isCn && StringUtils.isNotBlank(jumioError.getCnDesc())) {
                    result = jumioError.getCnDesc();
                } else {
                    result = jumioError.getEnDesc();
                }
            }
            if (StringUtils.isBlank(result) && StringUtils.isNotBlank(msg)) {
                result = msg;
            }
            return result;
        }catch (Exception e) {
            log.error("JUMIO fail reason 转换异常: ", e);
            return msg;
        }
    }

    private static String replaceEmailLink(String emailLink, final Map<String, Object> params) {
        for (Map.Entry entry : params.entrySet()) {
            emailLink = emailLink.replace(String.format("{%s}", entry.getKey()), String.valueOf(entry.getValue()));
        }
        return emailLink;
    }

    public static String emailLinkGenerator(String customEmailLink, String originEmailLink,
            Map<String, Object> params) {
        if (StringUtils.isNotBlank(customEmailLink)) {
            return replaceEmailLink(customEmailLink, params);
        } else {
            return replaceEmailLink(originEmailLink, params);
        }
    }

    /**
     * 获取用户的KYC状态
     *
     * @param user
     * @return
     */
    public KycCertificateResult getKycStatues(User user) {
        return getKycStatusByUserId(user.getUserId());
    }

    public KycCertificateResult getKycStatusByUserId(Long userId) {
    	KycCertificateResult certificateResult = new KycCertificateResult();

    	KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
    	if(kycCertificate != null) {
    		certificateResult.setNewVersion(true);
    		setKycCertificate(certificateResult, kycCertificate);
    		return certificateResult;
    	}


        // KYC 取没有过期的最后一条
        CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
        if (companyCertificate != null && CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
            // 如果是删除状态的，就当作为空值
            companyCertificate = null;
        }
        // jumioKYC 取没有过期的最后一条
        UserKyc userKyc = userKycMapper.getLast(userId);
        if (userKyc != null && (KycStatus.delete == userKyc.getStatus()||KycStatus.basic == userKyc.getStatus())) {
            // 如果是删除状态的，就当作为空值
            userKyc = null;
        }
        // 获取最后一次认证时间
        Long companyTime = (companyCertificate == null || companyCertificate.getUpdateTime() == null) ? null
                : companyCertificate.getUpdateTime().getTime();
        Long userKycTime =
                (userKyc == null || userKyc.getUpdateTime() == null) ? null : userKyc.getUpdateTime().getTime();

        if (companyTime != null) {
            if (userKycTime != null && userKycTime > companyTime) {
                setUserKycCertificate(certificateResult, userKyc);
            } else {
                setCompanyKycCertificate(certificateResult, companyCertificate);
            }
        } else if (userKycTime != null) {
            setUserKycCertificate(certificateResult, userKyc);
        }
        return certificateResult;
    }

    /**
     * 企业认证
     *
     * @param certificateResult
     * @param companyCertificate
     */
    private void setCompanyKycCertificate(KycCertificateResult certificateResult,
            CompanyCertificate companyCertificate) {
        if (certificateResult == null || companyCertificate == null || CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
            return;
        }
        LanguageEnum language = getKycCertificateLang();
        String message = companyCertificate.getInfo();
        if (language == LanguageEnum.ZH_CN) {
            message = getJumioFailReason(message, true);
        } else {
            message = getJumioFailReason(message, false);
        }
        certificateResult.setCertificateType(KycCertificateResult.TYPE_COMPANY);
        certificateResult.setCertificateId(companyCertificate.getId());
        certificateResult.setCertificateMessage(message);
        certificateResult.setCompanyName(companyCertificate.getCompanyName());
        certificateResult.setCountry(companyCertificate.getCompanyCountry());
        certificateResult.setAddress(companyCertificate.getCompanyAddress());
        certificateResult.setForbidPassed(false);
        certificateResult.setUpdateTime(companyCertificate.getUpdateTime());
        if (companyCertificate.getStatus() == CompanyCertificateStatus.passed) {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_PASS);
        } else if (companyCertificate.getStatus() == CompanyCertificateStatus.refused) {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
        } else if (companyCertificate.getStatus() == CompanyCertificateStatus.forbidPassed) {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
            certificateResult.setForbidPassed(true);
        } else {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REVIEW);
        }
    }

    private LanguageEnum getKycCertificateLang() {
        try {
            //如果是线程内起来的，这时候的是没有HttpServletRequest这个对象的
            if (RequestContextHolder.getRequestAttributes() == null) {
                return LanguageEnum.EN_US;
            }else {
                return WebUtils.getAPIRequestHeader() == null ? null : WebUtils.getAPIRequestHeader().getLanguage();
            }
        }catch (Exception e) {
            log.error("获取语言信息异常", e);
            return LanguageEnum.EN_US;
        }
    }

    /**
     * 个人认证
     *
     * @param certificateResult
     * @param userKyc
     */
    private void setUserKycCertificate(KycCertificateResult certificateResult, UserKyc userKyc) {
        if (certificateResult == null || userKyc == null || KycStatus.delete == userKyc.getStatus()) {
            return;
        }
        LanguageEnum language = getKycCertificateLang();
        String message = userKyc.getFailReason();
        if (language == LanguageEnum.ZH_CN) {
            message = getJumioFailReason(message, true);
        } else {
            message = getJumioFailReason(message, false);
        }
        certificateResult.setCertificateType(KycCertificateResult.TYPE_USER);
        certificateResult.setCertificateId(userKyc.getId());
        certificateResult.setCertificateMessage(message);
        certificateResult.setForbidPassed(false);
        certificateResult.setUpdateTime(userKyc.getUpdateTime());
        if (userKyc.getStatus() == KycStatus.passed) {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_PASS);
        } else if (userKyc.getStatus() == KycStatus.refused) {
            // 20190624 拒绝和不合规国籍通过状态的数据都归并为拒绝状态
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
        } else if (userKyc.getStatus() == KycStatus.forbidPassed) {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
            certificateResult.setForbidPassed(true);
        } else {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REVIEW);
        }
        if (userKyc.getBaseInfo() != null) {
            certificateResult.setFirstName(userKyc.getBaseInfo().getFirstName());
            certificateResult.setLastName(userKyc.getBaseInfo().getLastName());
            certificateResult.setMiddleName(userKyc.getBaseInfo().getMiddleName());
            certificateResult.setCountry(userKyc.getBaseInfo().getCountry());
            certificateResult.setCity(userKyc.getBaseInfo().getCity());
            certificateResult.setAddress(userKyc.getBaseInfo().getAddress());
            certificateResult.setDob(userKyc.getBaseInfo().getDob());
            certificateResult.setPostalCode(userKyc.getBaseInfo().getPostalCode());
        }
    }


    private void setKycCertificate(KycCertificateResult certificateResult,KycCertificate kycCertificate) {
    	LanguageEnum language = getKycCertificateLang();
    	String message = "";

    	if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
    		message = kycCertificate.getJumioTips();
    	}else if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceOcrStatus())) {
    		message = kycCertificate.getFaceOcrTips();
    	}else if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceStatus())) {
    		message = kycCertificate.getFaceStatus();
    	}

        if (language == LanguageEnum.ZH_CN) {
            message = getJumioFailReason(message, true);
        } else {
            message = getJumioFailReason(message, false);
        }

    	certificateResult.setCertificateType(KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType()) ? KycCertificateResult.TYPE_USER:KycCertificateResult.TYPE_COMPANY);
    	certificateResult.setCertificateId(kycCertificate.getUserId());
    	certificateResult.setCertificateMessage(message);
    	certificateResult.setUpdateTime(kycCertificate.getUpdateTime());
    	if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
    		certificateResult.setCertificateStatus(KycCertificateResult.STATUS_PASS);
    	}else if(KycCertificateStatus.REFUSED.name().equals(kycCertificate.getStatus())) {
    		certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
    	}else if(KycCertificateStatus.FORBID_PASS.name().equals(kycCertificate.getStatus())) {
    		certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REFUSED);
    		certificateResult.setForbidPassed(true);
    	}else {
            certificateResult.setCertificateStatus(KycCertificateResult.STATUS_REVIEW);
        }

    	KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(kycCertificate.getUserId(), KycFillType.BASE.name());
    	if(baseInfo != null ) {
    		certificateResult.setFirstName(baseInfo.getFirstName());
            certificateResult.setLastName(baseInfo.getLastName());
            certificateResult.setMiddleName(baseInfo.getMiddleName());
            certificateResult.setCompanyName(baseInfo.getCompanyName());
    	}
    }

    /**
     * 获取一个索引
     *
     * @param email
     * @return
     */
    public UserIndex getUserIndexForRegister(String email) {
        Object userIdObj = RedisCacheUtils.getRightPop(CacheKeys.REGISTER_USER_ID);
        if(userIdObj instanceof Long){
            log.info("getUserIndexForRegister long userid={}",userIdObj);
        }
        if (userIdObj == null) {
            log.error("=======userid耗尽============");
            throw new BusinessException(GeneralCode.SYS_ZUUL_ERROR);
        }
        Long userId = Long.valueOf(String.valueOf(userIdObj)) ;
        UserIndex record = new UserIndex();
        record.setUserId(userId);
        record.setEmail(email);
        int status = this.userIndexMapper.registerByUserId(record);
        if (status <= 0) {
            return getUserIndexForRegister(email);
        }
        return record;
    }


    /**
     * 包装broker sub user的信息
     */
    public User warpBrokerSubUser(UserIndex userIndex) throws NoSuchAlgorithmException {
        String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
        User user = User.buildRegisterObject(userIndex, "123456", cipherCode);
        //实际上broker账号的user并不需要密码和salt所以设置为空字符串
        user.setPassword("");
        user.setSalt("");
        //因为是broker账号所以只有交易功能，只要不激活就不能登陆，所以不需要单独标识
        Long status = user.getStatus();
        status = BitUtils.enable(status, Constant.USER_IS_BROKER_SUBUSER);
        user.setStatus(status);
        return user;
    }

    /**
     * 获取一个索引
     *
     * @param parentUserId
     * @return
     */
    public UserIndex getBrokerUserIndexForRegister(Long parentUserId, String parentEmail) {
        Object userIdObj = RedisCacheUtils.getRightPop(CacheKeys.REGISTER_USER_ID);
        if (userIdObj == null) {
            log.error("=======userid耗尽============");
            throw new BusinessException(GeneralCode.SYS_ZUUL_ERROR);
        }
        Long userId = Long.valueOf(String.valueOf(userIdObj)) ;
        UserIndex record = new UserIndex();
        record.setUserId(userId);
        record.setEmail(createVirtualBrokerEmail(parentEmail,parentUserId,userId));
        int status = this.userIndexMapper.registerByUserId(record);
        if (status <= 0) {
            return getBrokerUserIndexForRegister(parentUserId,parentEmail);
        }
        return record;
    }

    /**
     * 创建一个虚拟邮箱（幂等）
     * */
    protected String createVirtualBrokerEmail(String email,Long userId,Long brokerSubUserId){
        String[] emailArray=email.split("@");
        String virtualEmail=emailArray[0]+"_"+String.valueOf(userId)+"_"+String.valueOf(brokerSubUserId)+"_brokersubuser@"+emailArray[1];
        return virtualEmail.toLowerCase();
    }


    public void generateUserIndex(long num) {
        Lock lock = RedisCacheUtils.getLock(CacheKeys.USER_ID_GENERATE_LOCK);
        log.info("get lockbj USER_ID_GENERATE_LOCK");
        try {
            if (lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                log.info("get tryLock USER_ID_GENERATE_LOCK");
                long count = RedisCacheUtils.count(CacheKeys.REGISTER_USER_ID);
                if (count < this.userIdAlertValue) {
                    Long maxUserId = this.userIndexMapper.maxUserId();
                    if (maxUserId == null || maxUserId < initMinUserId) {
                        maxUserId = initMinUserId;
                    }
                    for (long userId = maxUserId + 1; userId < maxUserId + num; userId++) {
                       try{
                           this.generateOneUserIndex(userId);
                       }catch (Exception e){
                           log.error(String.format("generateUserIndex userId:%s, exception:", userId), e);
                       }
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("generateUserIndex failed, num:%s, exception:", num), e);
        } finally {
            if (null != lock) {
                lock.unlock();
            }
        }
    }

    public void generateOneUserIndex(Long userId) {
        UserIndex userIndex = new UserIndex();
        userIndex.setEmail(null);
        userIndex.setUserId(userId);
        this.userIndexMapper.insert(userIndex);
        log.info("生成userId:{}", userId);
        RedisCacheUtils.setLeftPush(CacheKeys.REGISTER_USER_ID, userId);
        log.info("生成userId:{},成功！", userId);
    }

    /**
     * 发送激活邮件
     *
     * @param user
     * @param terminal
     * @param customEmailLink 自定义邮件链接
     * @return
     * @throws Exception
     */
    public String[] sendActiveCode(User user, TerminalEnum terminal, String customEmailLink) throws Exception {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
        }
        String registerKeyStr = RedisCacheUtils.get(user.getEmail(), String.class, CacheKeys.REGISTER_EMAIL);
        RedisVerify redisVerify = null;
        if (StringUtils.isBlank(registerKeyStr)) {
            String registerCode = null;
            switch (terminal) {
                case WEB:
                case PC:
                case H5:
                case MAC:
                    registerCode = StringUtils.getRandomString(6);
                    break;
                default:
                    registerCode = StringUtils.getNumberRandomString(6);
                    break;
            }
            redisVerify = new RedisVerify();
            redisVerify.setToken(TokenUtils.emailRedisToken());
            redisVerify.setCode(registerCode);
            redisVerify.setTime(DateUtils.getNewUTCDate());
        } else {// 证明 expireTime 内还是有效的
            redisVerify = JSON.parseObject(registerKeyStr, RedisVerify.class);

            if (DateUtils.add(redisVerify.getTime(), Calendar.MINUTE, EMAIL_GAP_TIME).getTime() > DateUtils
                    .getNewUTCDate().getTime()) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND, new Object[] {EMAIL_GAP_TIME});
            } else if (DateUtils.add(redisVerify.getTime(), Calendar.MINUTE, EXPIRED_TIME).getTime() <= DateUtils
                    .getNewUTCDate().getTime()) {
                String registerCode = null;
                switch (terminal) {
                    case WEB:
                    case PC:
                    case H5:
                    case MAC:
                        registerCode = StringUtils.getRandomString(6);
                        break;
                    default:
                        registerCode = StringUtils.getNumberRandomString(6);
                        break;
                }
                redisVerify = new RedisVerify();
                redisVerify.setToken(TokenUtils.emailRedisToken());
                redisVerify.setCode(registerCode);
                redisVerify.setTime(DateUtils.getNewUTCDate());
            }
        }

        // 发送邮件
        SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        requestMsg.setRegister(true);
        Map<String, Object> data = new HashMap<>();
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        switch (terminal) {
            case WEB:
            case PC:
            case H5:
            case MAC:

                String emailLink = emailLinkGenerator(customEmailLink,
                        String.format(
                                "%suser/emailVerify.html?userId={userId}&emailVerifyCode={emailVerifyCode}&code={code}",
                                WebUtils.getHeader(Constant.BASE_URL)),
                        ImmutableMap.of("userId", user.getUserId(), "emailVerifyCode", redisVerify.getToken(), "code",
                                redisVerify.getCode()));
                data.put("link", emailLink);

                requestMsg.setTplCode(Constant.NODE_TYPE_EMAIL_AUTH);
                requestMsg.setData(data);
                break;
            default:
                data.put("verifyCode", redisVerify.getCode());
                requestMsg.setTplCode(Constant.NODE_TYPE_EMAIL_TERMINAL_AUTH);
                requestMsg.setData(data);
                break;
        }
        sendMsg(requestMsg, WebUtils.getAPIRequestHeader().getLanguage(), WebUtils.getAPIRequestHeader().getTerminal());
        RedisCacheUtils.set(user.getEmail(), JSON.toJSONString(redisVerify), 15 * 60,
                CacheKeys.REGISTER_EMAIL);
        return new String[] {redisVerify.getToken(), redisVerify.getCode()};
    }


    /**
     * 发送激活邮件
     *
     * @param user
     * @param terminal
     * @param customEmailLink 自定义邮件链接
     * @return
     * @throws Exception
     */
    public String[] sendActiveCodeForNewProcess(User user, TerminalEnum terminal, String customEmailLink) throws Exception {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (BitUtils.isTrue(user.getStatus(), Constant.USER_ACTIVE)) {
            throw new BusinessException(GeneralCode.USER_ALREADY_ACTIVATED);
        }
        String registerKeyStr = RedisCacheUtils.get(user.getEmail(), String.class, CacheKeys.REGISTER_EMAIL);
        RedisVerify redisVerify = null;
        if (StringUtils.isBlank(registerKeyStr)) {
            String registerCode = InvitationCodeUtil.generateDeviceVerifyCode();
            redisVerify = new RedisVerify();
            redisVerify.setToken(TokenUtils.emailRedisToken());
            redisVerify.setCode(registerCode);
            redisVerify.setTime(DateUtils.getNewUTCDate());
        } else {// 证明 expireTime 内还是有效的
            redisVerify = JSON.parseObject(registerKeyStr, RedisVerify.class);
            if (DateUtils.add(redisVerify.getTime(), Calendar.MINUTE, EMAIL_GAP_TIME).getTime() > DateUtils
                    .getNewUTCDate().getTime()) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND, new Object[] {EMAIL_GAP_TIME});
            } else if (DateUtils.add(redisVerify.getTime(), Calendar.MINUTE, EXPIRED_TIME).getTime() <= DateUtils
                    .getNewUTCDate().getTime()) {
                String registerCode = InvitationCodeUtil.generateDeviceVerifyCode();
                redisVerify = new RedisVerify();
                redisVerify.setToken(TokenUtils.emailRedisToken());
                redisVerify.setCode(registerCode);
                redisVerify.setTime(DateUtils.getNewUTCDate());
            }
        }
        // 发送邮件
        SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        requestMsg.setRegister(true);
        Map<String, Object> data = new HashMap<>();
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        data.put("verifyCode", redisVerify.getCode());
        String emailLink = emailLinkGenerator(customEmailLink,
                String.format(
                        "%suser/emailVerify.html?userId={userId}&emailVerifyCode={emailVerifyCode}&code={code}",
                        WebUtils.getHeader(Constant.BASE_URL)),
                ImmutableMap.of("userId", user.getUserId(), "emailVerifyCode", redisVerify.getToken(), "code",
                        redisVerify.getCode()));
        data.put("link", emailLink);
        requestMsg.setTplCode(AccountConstants.NODE_TYPE_EMAIL_AUTH2);
        requestMsg.setData(data);
        sendMsg(requestMsg, WebUtils.getAPIRequestHeader().getLanguage(), WebUtils.getAPIRequestHeader().getTerminal());
        RedisCacheUtils.set(user.getEmail(), JSON.toJSONString(redisVerify), 15 * 60,
                CacheKeys.REGISTER_EMAIL);
        return new String[] {redisVerify.getToken(), redisVerify.getCode()};
    }





    /**
     * 发送邮件验证码
     *
     * @param user
     * @return
     * @throws Exception
     */
    public String[] sendEmailVerifyCode(User user, BizSceneEnum bizScene, Map<String, Object> emailParams) throws Exception {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (null == bizScene) {
            log.error("sendEmailVerifyCode,bizScene loss");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }

        String emailVerifyKeyStr = RedisCacheUtils.get(user.getUserId().toString(), String.class, AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
        RedisVerify redisVerify = null;
        if (StringUtils.isBlank(emailVerifyKeyStr)) {
            String emailVerifyCode = StringUtils.getNumberRandomString(6);
            redisVerify = new RedisVerify();
            redisVerify.setCode(emailVerifyCode);
            redisVerify.setTime(DateUtils.getNewUTCDate());
        } else {// 证明 expireTime 内还是有效的
            redisVerify = JSON.parseObject(emailVerifyKeyStr, RedisVerify.class);
            //1分钟内不允许重复发送
            if (DateUtils.add(redisVerify.getTime(), Calendar.MINUTE, 1).getTime() > DateUtils
                    .getNewUTCDate().getTime()) {
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND, new Object[] {1});
            } else {
                String emailVerifyCode = StringUtils.getNumberRandomString(6);
                redisVerify = new RedisVerify();
                redisVerify.setCode(emailVerifyCode);
                redisVerify.setTime(DateUtils.getNewUTCDate());
            }
        }
        // 发送邮件
        SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        requestMsg.setRegister(false);

        // 根据场景指定短信模版
        String template=bizScene.getEmailTplCode();
        if (emailSceneTemplateMap != null && bizScene != null &&
                emailSceneTemplateMap.containsKey(bizScene.getCode())) {
            template = emailSceneTemplateMap.get(bizScene.getCode());
        }

        requestMsg.setTplCode(template);
        Map<String, Object> data = new HashMap<>();
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        data.put("verifyCode", redisVerify.getCode());
        data.put("timelimit", 30);
        if (!CollectionUtils.isEmpty(emailParams)) {
            data.putAll(emailParams);
        }

        // 各场景依赖内部参数补充
        appendEmailParamByScene(user.getUserId(), data, bizScene);

        if (bizScene == CRYPTO_WITHDRAW) {
                String amount=String.valueOf(emailParams.get("amount"));
                String address=String.valueOf(emailParams.get("address"));
                String addressTag=String.valueOf(emailParams.get("addressTag"));
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
                RedisCacheUtils.set(String.valueOf(user.getUserId()+redisVerify.getCode()), JSON.toJSONString(capitalWithdrawRedisVerify), 30 * 60, AccountConstants.CRYPTO_WITHDRAW_ADDRESS_CODE_KEY);
        }


        requestMsg.setData(data);
        sendMsg(requestMsg, WebUtils.getAPIRequestHeader().getLanguage(), WebUtils.getAPIRequestHeader().getTerminal());
        RedisCacheUtils.set(user.getUserId().toString(), JSON.toJSONString(redisVerify), 30 * 60,
                AccountConstants.SEND_EMAIL_VERIFY_CODE_KEY);
        return new String[] {redisVerify.getToken(), redisVerify.getCode()};
    }

    // 基于场景填充邮件参数
    private void appendEmailParamByScene(Long userId, Map<String, Object> emailParams, BizSceneEnum bizScene) throws Exception {
        // 默认填充禁用地址
        String forbiddenLink = genForbiddenLink();
        if (StringUtils.isNotBlank(forbiddenLink)) {
            emailParams.put("forbiddenLink", forbiddenLink);
        }
        String antiCode = iApiManageService.fetchAntiCode(userId);
        if (StringUtils.isNotBlank(antiCode)) {
            emailParams.put("antiCode", antiCode);
        }
        switch (bizScene) {
            case API_KEY_MANAGE:
                break;
            case AUTHORIZE_NEW_DEVICE:
                emailParams.put(UserDevice.LOGIN_IP, WebUtils.getRequestIp());
                emailParams.put(UserDevice.LOCATION_CITY, getLocationCity(WebUtils.getRequestIp()));
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



    /**
     * 发送短信验证码
     *
     * @param user
     * @return
     * @throws Exception
     */
    public String[] sendSmsVerifyCode(User user, BizSceneEnum bizScene,MsgType msgType, Boolean resend) throws Exception {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userId=user.getUserId();
        String email=user.getEmail();

        if (null == userId) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        // 一分钟频率控制
        String frequencyLimits =
                RedisCacheUtils.get(String.valueOf(userId), String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencyLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[]{1});
        }
        RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("sendSmsVerifyCode userId={}", userId);

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        if (StringUtils.isBlank(userSecurity.getMobile())) {
            throw new BusinessException(GeneralCode.USER_NOT_MOBILE);
        }
        String code = StringUtils.getNumberRandomString(6);
        SendMsgRequest requestSms = new SendMsgRequest();
        requestSms.setIp(WebUtils.getRequestIp());
        Country country = this.iCountry.getCountryByMobileCodeOrCountryCode(userSecurity.getMobileCode());
        requestSms.setMobileCode(country.getMobileCode());
        requestSms.setRecipient(userSecurity.getMobile());
        requestSms.setUserId(userSecurity.getUserId().toString());

        // 根据场景指定短信模版
        String template=bizScene.getSmsTplCode();
        if (smsSceneTemplateMap != null && bizScene != null &&
                smsSceneTemplateMap.containsKey(bizScene.getCode())) {
            template = smsSceneTemplateMap.get(bizScene.getCode());
        }
        requestSms.setTplCode(template);

        requestSms.setVerifyCodeId(null);
        requestSms.setMsgType(MsgType.valueOf(msgType.name()));
        requestSms.setResend(resend);

        Map<String, Object> params = new HashMap<>();
        params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, code);
        params.put("ip", WebUtils.getRequestIp());
        params.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        params.put("timelimit", 30);
        requestSms.setData(params);

        // 一分钟频率控制
        String globalmobilekey = requestSms.getMobileCode() + requestSms.getRecipient();
        String frequencymobileLimits = RedisCacheUtils.get(globalmobilekey, String.class, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        if (StringUtils.isNotBlank(frequencymobileLimits)) {
            throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[]{1});
        }
        RedisCacheUtils.set(globalmobilekey, globalmobilekey, 60L, AccountConstants.SEND_MOBILE_VERIFY_CODE_KEY);
        log.info("frequencymobileLimits userId={}", userId);


        // 发送短信
        sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
                WebUtils.getAPIRequestHeader().getTerminal());
        String mobileKey = StringUtils.getMobileKey(userSecurity.getMobile(), userSecurity.getMobileCode()).toUpperCase();

        // 存入Redis
        RedisVerify redisVerify = new RedisVerify();
        redisVerify.setTime(DateUtils.getNewUTCDate());
        redisVerify.setCode(commonConfig.convertSecretCode(userSecurity.getMobile(), code));
        redisVerify.setErrorCount(0);
        RedisCacheUtils.set(mobileKey, redisVerify, UserCommonBusiness.EXPIRED_TIME * 60L, CacheKeys.MOBILE_AUTH_TIME);
        return new String[] {redisVerify.getToken(), redisVerify.getCode()};
    }

    public String getEmailById(Long userId) {
        return userIndexMapper.selectEmailById(userId);
    }


    public User getUserIdByEmail(String email) {
        User user= userMapper.queryByEmail(email);
        if (null == user ) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user;
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

    public User getUserByUseryId(final Long userId) {
        if (null == userId) {
            return null;
        }
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            return null;
        }
        return userMapper.queryByEmail(userIndex.getEmail());
    }
    /**
     * 是否是母账户
     * */
    public boolean isParentUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
    }
    /**
     * 是否是margin账户
     * */
    public boolean isMarginUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_MARGIN_USER);
    }

    /**
     * 是否有margin账户
     * */
    public boolean isExistMarginAccount(final Long userId) {
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        Boolean isExistMarginAccount=false;
        if(null==userInfo||null==userInfo.getMarginUserId()){
            isExistMarginAccount=false;
        }else{
            isExistMarginAccount=true;
        }
        return isExistMarginAccount;
    }


    /**
     * 是否有future账户
     * */
    public boolean isExistFutureAccount(final Long userId) {
        final UserInfo userInfo = this.userInfoMapper.selectByPrimaryKey(userId);
        Boolean isExistFutureAccount=false;
        if(null==userInfo||null==userInfo.getFutureUserId()){
            isExistFutureAccount=false;
        }else{
            isExistFutureAccount=true;
        }
        return isExistFutureAccount;
    }


    /**
     * 是否有逐仓margin account
     * */
    public boolean isExistIsolatedMarginAccount(final Long userId) {
        User user = checkAndGetUserById(userId);
        UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
        return userStatusEx.getIsExistIsolatedMarginAccount().booleanValue();
    }

    /**
     * 获取用户最后一次登录使用的语言
     * @param userId
     * @return
     */
    public LanguageEnum getLastLoginLanguage(Long userId) {
        //先获取用户的最后一次登录使用的语言
        LanguageEnum language = LanguageEnum.EN_US;
        try {
            UserSecurityLog userSecurityLog = userSecurityLogMapper.getLastLoginLogByUserId(userId);
            if (userSecurityLog != null && org.apache.commons.lang3.StringUtils.isNotBlank(userSecurityLog.getIpLocation())) {
                String ipLocation = userSecurityLog.getIpLocation();
                if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(ipLocation, "China")) {
                    language = LanguageEnum.ZH_CN;
                }
            }
        }catch (Exception e) {
            log.error("获取用户最后一次登录时使用的语言信息异常. userId:{}",userId);
            language = LanguageEnum.EN_US;
        }
        return language;
    }

    /**
     * 非子母账户
     *
     * @param status
     * @return
     */
    public boolean isNormalUser(final Long status) {
        return BitUtils.isFalse(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED) && BitUtils.isFalse(status, Constant.USER_IS_SUBUSER)
                && BitUtils.isFalse(status, Constant.USER_IS_MARGIN_USER)&& BitUtils.isFalse(status, Constant.USER_IS_FUTURE_USER)
                && BitUtils.isFalse(status, Constant.USER_IS_FIAT_USER);
    }

    /**
     * 发送激活邮件
     *
     * @param user
     * @param template
     * @return
     * @throws Exception
     */
    public void sendOauthBindEmail(BindOauthRequest request, User user, String template, boolean isResendEmail)
            throws Exception {
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        OauthRedisDto oauthRedisDto =
                RedisCacheUtils.get(user.getEmail(), OauthRedisDto.class, OauthBusiness.OAUTH_LINK_EMAIL);
        if (oauthRedisDto != null) {
            // 邮件链接已存在
            if (DateUtils.add(oauthRedisDto.getTime(), Calendar.MINUTE, OAUTH_EMAIL_GAP_TIME).getTime() > DateUtils
                    .getNewUTCDate().getTime()) {
                // 邮件重发必须间隔3分钟
                throw new BusinessException(GeneralCode.USER_ACTIVE_EMAIL_REFUSE_SEND, new Object[] {OAUTH_EMAIL_GAP_TIME});
            } else if (DateUtils.add(oauthRedisDto.getTime(), Calendar.MINUTE, OAUTH_VERIFY_CODE_TIME)
                    .getTime() < DateUtils.getNewUTCDate().getTime()) {
                // 30分钟后重新生成verifyCode
                oauthRedisDto.setVerifyCode(TokenUtils.emailRedisToken());
            }
        } else {
            if (isResendEmail) {
                throw new BusinessException(AccountErrorCode.OAUTH_EXPIRED);
            } else {
                // 不存在
                oauthRedisDto = new OauthRedisDto();
                BeanUtils.copyProperties(request, oauthRedisDto);
                oauthRedisDto.setVerifyCode(TokenUtils.emailRedisToken());
                oauthRedisDto.setTemplate(template);
            }
        }
        // 设置最后一次发送邮件的时间
        oauthRedisDto.setTime(DateUtils.getNewUTCDate());

        // 发送邮件
        SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(user.getEmail());
        requestMsg.setUserId(user.getUserId().toString());
        requestMsg.setIp(WebUtils.getRequestIp());
        Map<String, Object> data = new HashMap<>();
        data.put("ip", WebUtils.getRequestIp());
        data.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
        String emailLink = emailLinkGenerator(null,
                String.format("%sgateway-api/v1/public/oauth/verify?email={email}&verifyCode={verifyCode}",
                        WebUtils.getHeader(Constant.BASE_URL)),
                ImmutableMap.of("email", URLEncoder.encode(user.getEmail(), "UTF-8"), "verifyCode",
                        oauthRedisDto.getVerifyCode()));
        data.put("link", emailLink);
        requestMsg.setTplCode(oauthRedisDto.getTemplate());
        requestMsg.setData(data);
        sendMsg(requestMsg, WebUtils.getAPIRequestHeader().getLanguage(), WebUtils.getAPIRequestHeader().getTerminal());
        RedisCacheUtils.set(user.getEmail(), oauthRedisDto, OAUTH_EXPIRED_TIME * 60, OauthBusiness.OAUTH_LINK_EMAIL);
    }

    public void insertInfoRootUserIndex(Long rootUserId, Long userId, String userType) {
        if (userId == null) {
            return;
        }
        RootUserIndex rootUserIndex = new RootUserIndex();
        rootUserIndex.setAccountType(userType);
        rootUserIndex.setRootUserId(rootUserId);
        rootUserIndex.setUserId(userId);
        rootUserIndexMapper.insertSelective(rootUserIndex);

    }

    /**
     * 是否是broker母账户
     * */
    public boolean isBrokerParentUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED);
    }

    /**
     * 是否是broker子账户
     * */
    public boolean isBrokerSubUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER);
    }


    /**
     * 是否是子账户
     * */
    public boolean isSubUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_SUBUSER);
    }


    /**
     * 是否是普通子账户
     * */
    public boolean isNormalSubUser(final Long status) {
        return BitUtils.isEnable(status, Constant.USER_IS_SUBUSER) && !isBrokerSubUser(status);
    }

    /**
     * 是否是broker子账户or母账户
     * */
    public boolean isBrokerParentUserOrSubUser(final Long status) {
        return isBrokerSubUser(status)||isBrokerParentUser(status);
    }


}
