package com.binance.account.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.DelFlag;
import com.binance.account.common.constant.UserDeviceConst;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.common.query.es.ESQueryBuilder;
import com.binance.account.common.query.es.ESResultSet;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.device.DeviceMatchReport;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.device.UserDeviceHistory;
import com.binance.account.data.entity.device.UserDeviceProperty;
import com.binance.account.data.entity.device.UserDeviceRelation;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.device.UserDeviceMapper;
import com.binance.account.data.mapper.device.UserDevicePropertyMapper;
import com.binance.account.data.mapper.device.UserDeviceRelationMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.domain.bo.DeviceAuthVerify;
import com.binance.account.domain.bo.SpecialUserDeviceAuthorizeCache;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.bigdata.AsyncBigDataProducer;
import com.binance.account.service.device.IDeviceMatchReport;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.device.IUserDeviceHistory;
import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.security.IUserIpChange;
import com.binance.account.service.security.IUserSecurityLog;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.HtmlUtils;
import com.binance.account.utils.InboxUtils;
import com.binance.account.utils.InvitationCodeUtil;
import com.binance.account.vo.device.request.AddUserDeviceForQRCodeLoginRequest;
import com.binance.account.vo.device.request.CheckNewDeviceIpRequest;
import com.binance.account.vo.device.request.ResendAuthorizeDeviceEmailRequest;
import com.binance.account.vo.device.request.UserDeviceAuthorizeRequest;
import com.binance.account.vo.device.request.UserDeviceDeleteRequest;
import com.binance.account.vo.device.request.UserDeviceRequest;
import com.binance.account.vo.device.request.VerifyAuthDeviceCodeRequest;
import com.binance.account.vo.device.response.AddUserDeviceForQRCodeLoginResponse;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.device.response.CheckNewDeviceIpResponse;
import com.binance.account.vo.device.response.CheckUserDeviceResponse;
import com.binance.account.vo.device.response.CheckWithdrawDeviceResponse;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.account.vo.device.response.ResendAuthorizeDeviceEmailResponse;
import com.binance.account.vo.device.response.UserDeviceAuthorizeResponse;
import com.binance.account.vo.device.response.UserDeviceVo;
import com.binance.account.vo.question.CreateQuestionVo;
import com.binance.account.vo.user.ex.UserSecurityKeyStatus;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.yubikey.WebAuthnFrontHandler;
import com.binance.decision.vo.user.login.request.DeviceAuthRequest;
import com.binance.decision.vo.user.login.response.DeviceAuthResponse;
import com.binance.inbox.api.InboxMessageTextApi;
import com.binance.inbox.business.PushInboxMessage;
import com.binance.master.constant.Constant;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.master.utils.version.VersionHelper;
import com.binance.notification.api.vo.SecurityNotificationEnum;
import com.binance.risk.api.RiskWithdrawApi;
import com.binance.risk.vo.withdraw.request.RiskWithdrawBlackListRequest;
import com.binance.rule.api.UserLoginRuleApi;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

/**
 * @author caixinning
 * @date 2018/5/8 11:10
 */
@SuppressWarnings("unchecked")
@Log4j2
@Service
public class UserDeviceBusiness  implements IUserDevice {

    /**
     * 最大设备数
     */
    private static final int DEVICE_MAX = 20;
    /**
     * 长度上限，数据库长度为4096，这里设置为4000，是因为有可能动态的添加device_id（约50个字符长度）
     */
    private static final int CONTENT_MAX_LENGTH = 4000;

    private static final String CACHE_ACCOUNT_DEVICE_PROPERTY = "CACHE_ACCOUNT_DEVICE_PROPERTY";
    private static final String PATTERN_DEVICE_AUTH_EMAIL = "USER_DEVICE_AUTH_EMAIL_%s";
    private static final String PATTERN_DEVICE_AUTH_EMAIL_LOCK = "USER_DEVICE_AUTH_EMAIL_LOCK_%s_%s";

    private static final String CONFIG_DEVICE_CHECK_STRICT_MODE = "device_check_strict_mode";
    private static final String CONFIG_DEVICE_CHECK_SCORE_THRESHOLD = "device_check_score_threshold";
    private static final String CONFIG_DEVICE_MIN_PROPERTY_COUNT = "device_check_min_property_count";

    @Autowired
    private UserDeviceMapper userDeviceMapper;
    @Autowired
    private UserDevicePropertyMapper userDevicePropertyMapper;
    @Autowired
    private IUserSecurityLog iUserSecurityLog;
    @Autowired
    private IUserDeviceHistory iUserDeviceHistory;
    @Autowired
    private SpecialUserDeviceAuthorizeCache specialUserDeviceAuthorizeCache;
    @Autowired
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Autowired
    private IUserIpChange userIpChange;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private UserDeviceSearchService deviceSearchService;
    @Autowired
    private UserDeviceRelationMapper deviceRelationMapper;
    @Autowired
    private RiskWithdrawApi riskWithdrawApi;
    @Autowired
    private UserLoginRuleApi userLoginRuleApi;
    @Autowired
    private IQuestion iQuestion;
    @Autowired
    private AsyncBigDataProducer asyncBigDataProducer;
    @Resource
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    private RestClient restClient;
    @Autowired
    private ApolloCommonConfig apolloCommonConfig;
    @Autowired
    private InboxMessageTextApi inboxMessageTextApi;
    @Autowired
    protected UserIndexMapper userIndexMapper;
    @Autowired
    private SecurityNotificationService securityNotificationService;
    @Autowired
    private WebAuthnFrontHandler webAuthnFrontHandler;
    @Resource
    protected UserSecurityMapper userSecurityMapper;
    @Resource
    private UserDeviceComparator userDeviceComparator;
    @Resource
    private IDeviceMatchReport deviceMatchReportBusiness;


    @Value("${device.check.withdraw.switch:true}")
    private Boolean withdrawCheckSwitch;
    @Value("${device.check.withdraw.terminal.strict:null}")
    private String withdrawTerminalStrict;
    @Value("${authorize.device.answer.question.switch:false}")
    private boolean authorizeDeviceAnswerQuestionSwitch;
    @Value("${authorize.device.force.everyone.answer.question.switch:false}")
    private boolean authorizeDeviceForceEveryoneAnswerQuestionSwitch;
    @Value("${authorize.device.call.decision.rule.switch:true}")
    private boolean authorizeDeviceCallDecisionRuleSwitch;


    @Value("${device.check.ios.version:V 1.0.0}")
    private String deviceCheckIosVersion;
    @Value("${device.check.android.version:57}")
    private String deviceCheckAdndroidVersion;
    @Value("${device.check.mac.version:1.1.3}")
    private String deviceCheckMacVersion;
    @Value("${device.check.pc.version:v1.5.0}")
    private String deviceCheckPcVersion;


    @Value("${isforce.check.device.loginToken.switch:false}")
    private boolean isForceCheckDeviceLoginToken;

    @Value("${user.device.v2.alg.switch:true}")
    private Boolean alg2Switch;


    @Value("${new.disable.logic.check.ios.version:2.16.0}")
    private String newDisableLogicCheckIosVersion;
    @Value("${new.disable.logic.check.android.version:192}")
    private String newDisableLogicCheckAdndroidVersion;


    @Value("${old.app.ios.version:2.2.0}")
    private String oldAppIosVersion;
    @Value("${old.app.android.version:1.6.0}")
    private String oldAppAdndroidVersion;


    @Value("${new.device.force.check.switch:false}")
    private Boolean newDeviceForceCheckSwitch;


    @Autowired
    private UserSecurityLogMapper userSecurityLogMapper;


    @Override
    public void preCheck(Map<String, String> deviceInfo, Long userId, String agentType) {
        if (deviceInfo == null || deviceInfo.isEmpty()) {
            return;
        }
        filterProperties(agentType, deviceInfo);
        if (StringUtils.isNotBlank(deviceInfo.get(UserDevice.DEVICE_NAME)) && HtmlUtils.isHtmlInject(deviceInfo.get(UserDevice.DEVICE_NAME))) {
            log.warn("Detected html inject in deviceInfo, userId:{}, deviceInfo:{}", userId, JSON.toJSONString(deviceInfo));
            throw new BusinessException(GeneralCode.USER_DEVICE_ERROR);
        }
    }

    @Override
    @Monitored
    public CheckUserDeviceResponse checkDevice(Long userId, String agentType, Map<String, String> content) {
        //过滤有效的属性
        filterProperties(agentType, content);
        if (content.isEmpty()) {
            return CheckUserDeviceResponse.INVALID;
        }

        List<UserDevice> deviceList = userDeviceMapper.selectAuthorizedDevices(userId, agentType);
        if (CollectionUtils.isEmpty(deviceList)) {
            return CheckUserDeviceResponse.INVALID;
        }

        boolean isHistoryIp = userIpChange.isHistoryIp(userId, content.get(UserDevice.LOGIN_IP));
        DeviceDiffer result = findMostSimilarDevice(deviceList, content, agentType, isHistoryIp);

        // 比较
        findMostSimilarDeviceAndLogResult(result, deviceList, content, agentType);

        if (result == null || !result.isSame()) {
            return CheckUserDeviceResponse.INVALID;
        }
        UserDevice matched = result.getMatched();

        if (!result.getItems().isEmpty()) {
            UserDeviceHistory history = new UserDeviceHistory(matched);
            history.setOperateType(UserDeviceHistory.OperateType.MODIFY.getCode());
            history.setMemo(String.format("update, source:user, count:%s, score:%.2f", result.getItems().size(), result.getScore()));
            AsyncTaskExecutor.execute(() -> iUserDeviceHistory.addHistory(history));
        }
        //更新设备信息
        String deviceId = StringUtils.getTimestampRandom32();

        content.put(UserDevice.DEVICE_ID, deviceId);

        matched.setActiveTime(new Date());
        matched.setUpdateTime(new Date());
        matched.setContent(JsonUtils.toJsonNotNullKey(content));
        userDeviceMapper.updateByPrimaryKeySelective(matched);

        return new CheckUserDeviceResponse(true, matched.getId(), deviceId);
    }

    @Override
    @Monitored
    public CheckWithdrawDeviceResponse checkWithdrawDevice(Long userId, String agentType, Map<String, String> content) {
        this.preCheck(content, userId, agentType);
        CheckWithdrawDeviceResponse response = new CheckWithdrawDeviceResponse();
        boolean valid = false;
        Set<String> terminalStrictSet;
        if (StringUtils.isNotBlank(withdrawTerminalStrict)) {
            terminalStrictSet = Sets.newHashSet(withdrawTerminalStrict.split(","));
        } else {
            terminalStrictSet = new HashSet<>();
        }
        if (withdrawCheckSwitch) {
            if (MapUtils.isEmpty(content)) {
                String ip = WebUtils.getRequestIp();
                log.warn("用户 {} 无提现设备指纹，走ip验证 {}", userId, agentType, ip);
                // 目前仅验证web、h5的ip
                if (userIpChange.isHistoryIp(userId, ip)) {
                    valid = true;
                }
            } else {
                List<UserDevice> deviceList = userDeviceMapper.selectAuthorizedDevices(userId, agentType);
                boolean isHistoryIp = userIpChange.isHistoryIp(userId, content.get(UserDevice.LOGIN_IP));

                DeviceDiffer result = findMostSimilarDevice(deviceList, content, agentType, isHistoryIp);
                if (result != null && result.isSame()) {
                    log.warn("用户 {} 当前提现设备与历史登录设备 id={} 匹配通过", userId, result.getMatched().getId());
                    valid = true;
                }
            }
            log.info("用户 {}-{} 设备校验完毕, 结果 {}, terminalStrictSet: {}", userId, agentType, valid, terminalStrictSet);
            if (terminalStrictSet.contains(agentType) && !valid) {
                log.info("用户 {} 设备校验失败，拉入提现黑名单 {}", userId, agentType);
                RiskWithdrawBlackListRequest request = new RiskWithdrawBlackListRequest();
                request.setUserId(userId.toString());
                request.setType(4);//提现设备异常
                request.setRemark("[自动]提现设备未授权");
                riskWithdrawApi.addWithdrawBlackList(APIRequest.instance(request));
            }
        } else {
            log.info("提现指纹验证(device.check.withdraw.switch)未开启");
            valid = true;
        }
        // 无论是否匹配，都需要记录当前的提现设备信息
        if (MapUtils.isNotEmpty(content)) {
            AddUserDeviceResponse addUserDeviceResponse = this.addDeviceHistoryForWithdraw(userId, agentType, content);
            response.setId(addUserDeviceResponse.getId());
            response.setDeviceId(addUserDeviceResponse.getDeviceId());
        }

        response.setValid(valid);

        return response;
    }

    @Override
    public AddUserDeviceResponse addDevice(Long userId, String agentType, UserDevice.Status status, String source, Map<String, String> content, String flowId) {

        filterProperties(agentType, content);
        if (content.isEmpty()) {
            return null;
        }

        String deviceId = StringUtils.getTimestampRandom32();
        content.put(UserDevice.DEVICE_ID, deviceId);

        UserDevice device = new UserDevice();
        device.setUserId(userId);
        device.setSource(source);
        device.setAgentType(agentType);
        device.setStatus(status);
        device.setContent(JsonUtils.toJsonNotNullKey(content));
        device.setFlowId(flowId);
        userDeviceMapper.insertSelective(device);

        UserDeviceHistory history = new UserDeviceHistory(device);
        history.setOperateType(UserDeviceHistory.OperateType.ADD.getCode());
        history.setMemo("add new device");
        AsyncTaskExecutor.execute(() -> iUserDeviceHistory.addHistory(history));
        //删除旧的
        AsyncTaskExecutor.execute(() -> this.clearExpiredDevice(userId));

        return new AddUserDeviceResponse(device.getId(), device.getUserId(), deviceId);
    }


    @Override
    public AddUserDeviceResponse addDevice(Long userId, String agentType, UserDevice.Status status, String source, Map<String, String> content) {
        return addDevice(userId, agentType, status, source, content, null);
    }

    @Override
    public AddUserDeviceResponse addDeviceHistoryForWithdraw(Long userId, String agentType, Map<String, String> content) {
        this.preCheck(content, userId, agentType);
        List<UserDevice> deviceList = userDeviceMapper.selectByUserIdAndAgentType(
                userId, agentType, UserDevice.Status.AUTHORIZED.ordinal(), null, null, DelFlag.OK, null, null);
        String deviceId = content.get(UserDeviceConst.DEVICE_ID);
        if (StringUtils.length(deviceId) != 32) {
            deviceId = StringUtils.getTimestampRandom32();
            content.put(UserDeviceConst.DEVICE_ID, deviceId);
        }
        AddUserDeviceResponse response;
        if (CollectionUtils.isEmpty(deviceList)) {
            response = new AddUserDeviceResponse(null, userId, deviceId);
        } else {
            UserDevice old = deviceList.get(0);
            String newContentJson = JsonUtils.toJsonNotNullKey(content);
            if (!old.getContent().equals(newContentJson)) {
                DeviceDiffer differ = compareDevice(
                        content,
                        filterProperties(agentType, JsonUtils.toObj(old.getContent(), HashMap.class)),
                        agentType,
                        false);
                UserDeviceHistory history = new UserDeviceHistory(old);
                history.setOperateType(UserDeviceHistory.OperateType.LOG.getCode());
                history.setMemo(String.format("log, source: withdraw, count:%s, score:%.2f", differ.getItems().size(), differ.getScore()));
                history.setContent(newContentJson);
                history.setCreateTime(new Date());
                iUserDeviceHistory.addHistory(history);
            }
            response = new AddUserDeviceResponse(old.getId(), userId, deviceId);
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Monitored
    public AddUserDeviceResponse associateSensitiveDevice(Long userId, String agentType, String source, Map<String, String> content) {
        this.preCheck(content, userId, agentType);
        List<UserDevice> deviceList = userDeviceMapper.selectByUserIdAndAgentType(
                userId, agentType, UserDevice.Status.AUTHORIZED.ordinal(), null, null, DelFlag.OK, null, null);
        UserDevice matched = null;
        DeviceDiffer result = null;
        for (UserDevice device : deviceList) {
            DeviceDiffer tmp = compareDevice(content, filterProperties(agentType, JsonUtils.toObj(device.getContent(), HashMap.class)), agentType, false);
            if (tmp.isSame()) {
                if (result == null) {
                    matched = device;
                    result = tmp;
                } else if (tmp.getScore() > result.getScore()) {
                    // 若有多个设备满足匹配条件，取差异最小的(得分最高的)
                    result = tmp;
                    matched = device;
                }
            }
        }
        if (StringUtils.length(content.get(UserDeviceConst.DEVICE_ID)) != 32) {
            content.put(UserDevice.DEVICE_ID, StringUtils.getTimestampRandom32());
        }
        AddUserDeviceResponse response;
        if (matched == null) {
            // 没有匹配的设备，则新增一条
            UserDevice device = new UserDevice();
            device.setUserId(userId);
            device.setAgentType(agentType);
            device.setSource(source);
            device.setContent(JsonUtils.toJsonNotNullKey(content));
            device.setStatus(UserDevice.Status.AUTHORIZED);
            userDeviceMapper.insertSelective(device);
            response = new AddUserDeviceResponse(device.getId(), userId, content.get(UserDeviceConst.DEVICE_ID));
        } else {
            // 有匹配的设备
            if (UserDeviceConst.SOURCE_REGIST.equals(matched.getSource()) || UserDeviceConst.SOURCE_LOGIN.equals(matched.getSource())
                    || "".equals(matched.getSource())) {
                // 匹配的设备来源是 login、regist（授权过），则返回该设备id、device_id，不更新数据
                response = new AddUserDeviceResponse(matched.getId(), userId, JSON.parseObject(matched.getContent()).getString(UserDeviceConst.DEVICE_ID));
                //记录history
                UserDeviceHistory history = new UserDeviceHistory(matched);
                history.setOperateType(UserDeviceHistory.OperateType.LOG.getCode());
                history.setMemo(String.format("associate sensitive device, source:%s, count:%s, score:%.2f", source, result.getItems().size(), result.getScore()));
                iUserDeviceHistory.addHistory(history);
            } else {
                // 匹配的设备来源是其它（未经授权），则更新该设备的content
                if (!matched.getContent().equals(JsonUtils.toJsonNotNullKey(content))) {
                    UserDeviceHistory history = new UserDeviceHistory(matched);
                    history.setOperateType(UserDeviceHistory.OperateType.MODIFY.getCode());
                    history.setMemo(String.format("associate sensitive device, source:%s, count:%s, score:%.2f", source, result.getItems().size(), result.getScore()));
                    iUserDeviceHistory.addHistory(history);
                }
                matched.setContent(JsonUtils.toJsonNotNullKey(content));
                matched.setActiveTime(new Date());
                userDeviceMapper.updateByPrimaryKeySelective(matched);

                response = new AddUserDeviceResponse(matched.getId(), userId, content.get(UserDeviceConst.DEVICE_ID));
            }
        }

        return response;
    }


    private boolean needAnswerQuestion(APIRequest<UserDeviceAuthorizeRequest> request, Map<String, String> deviceInfo, Long userId) {
        //授权设备总开关
        if (!authorizeDeviceAnswerQuestionSwitch) {
            log.info("authorize.device.answer.question.switch is false");
            return false;
        }

        if (iQuestion.checkUserFlowComplete(userId, request.getBody().getCode())) {
            log.info("user has completed question.");
            return false;
        }

        //强制所有人必须回答问题才能授权设备
        if (authorizeDeviceForceEveryoneAnswerQuestionSwitch) {
            log.info("authorizeDeviceForceEveryoneAnswerQuestionSwitch is true");
            return true;
        }

        DeviceAuthRequest needAnswerQuestionRequest = new DeviceAuthRequest();
        needAnswerQuestionRequest.setClientType(request.getTerminal().getCode());
        needAnswerQuestionRequest.setLan(request.getLanguage().getLang());
        needAnswerQuestionRequest.setDeviceInfo(deviceInfo);
        needAnswerQuestionRequest.setUserId(userId);
        APIResponse<DeviceAuthResponse> needAnswerQuestionResponse = userLoginRuleApi.deviceAuth(APIRequest.instance(needAnswerQuestionRequest));
        boolean needAnswerQuestion = false;
        if (needAnswerQuestionResponse != null && needAnswerQuestionResponse.getData() != null) {
            //ruleType == 1 表示高危需要回答问题
            needAnswerQuestion = (1 == needAnswerQuestionResponse.getData().getRuleType());
        }
        return needAnswerQuestion;
    }

    private static final String ANSWER_QUESTION_CALLBACK_URL = "/gateway-api/v1/public/account/user-device/authorize?userId=%s&code=%s";

    private UserDeviceAuthorizeResponse getQuestionResponse(UserDeviceRequest cache, String code) {
        UserDeviceAuthorizeResponse result = new UserDeviceAuthorizeResponse();
        String callbackUrl = String.format(ANSWER_QUESTION_CALLBACK_URL, cache.getUserId(), code);

        //使用激活码作question flow id
        String flowId = code;
        CreateQuestionVo createQuestionVo = CreateQuestionVo.builder()
                .flowId(flowId)
                .flowType(UserSecurityResetType.authDevice.name())
                .successCallback(callbackUrl)
                .failCallback(callbackUrl)
                .userId(cache.getUserId())
                .timeout(30L) //30分钟超时
                .build();
        try {
            iQuestion.createQuestionFlow(createQuestionVo);
        } catch (BusinessException e) {
            if (AccountErrorCode.QUESTION_FLOW_UNDO_EXIST == e.getErrorCode()) {
                log.info(" existing question flow: {}", flowId);
            } else {
                throw e;
            }
        } catch (Exception e) {
            log.error("create question meet error.", e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        result.setNeedAnswerQuestion(true);
        result.setQuestionFlowId(flowId);
        result.setValid(false);
        //返回flowId
        return result;
    }

    private void addDeviceIntoBigDataWhiteListIfNecessary(APIRequest<UserDeviceAuthorizeRequest> request, Long devicePk, Map<String, String> deviceInfo, Long userId) {
        DeviceAuthRequest isWhiteDevice = new DeviceAuthRequest();
        isWhiteDevice.setClientType(request.getTerminal().getCode());
        isWhiteDevice.setLan(request.getLanguage().getLang());
        isWhiteDevice.setDeviceInfo(deviceInfo);
        isWhiteDevice.setUserId(userId);
        APIResponse<DeviceAuthResponse> isWhiteDeviceResponse = userLoginRuleApi.whiteDevice(APIRequest.instance(isWhiteDevice));
        //ruleType == 0 表示加白名单
        if (isWhiteDeviceResponse != null && isWhiteDeviceResponse.getData() != null
                && 0 == isWhiteDeviceResponse.getData().getRuleType()) {
            log.info("add device into whitelist");
            addDeviceIntoBigDataWhiteList(userId, devicePk);
        } else {
            log.info("userLoginRuleApi.whiteDevice resp: {}", JSON.toJSONString(isWhiteDeviceResponse));
        }
    }

    private void addDeviceIntoBigDataWhiteListIfNecessary(String clientType, String lang, Long devicePk, Map<String, String> deviceInfo, Long userId) {
        DeviceAuthRequest isWhiteDevice = new DeviceAuthRequest();
        isWhiteDevice.setClientType(clientType);
        isWhiteDevice.setLan(lang);
        isWhiteDevice.setDeviceInfo(deviceInfo);
        isWhiteDevice.setUserId(userId);
        APIResponse<DeviceAuthResponse> isWhiteDeviceResponse = userLoginRuleApi.whiteDevice(APIRequest.instance(isWhiteDevice));
        //ruleType == 0 表示加白名单
        if (isWhiteDeviceResponse != null && isWhiteDeviceResponse.getData() != null
                && 0 == isWhiteDeviceResponse.getData().getRuleType()) {
            log.info("add device into whitelist");
            addDeviceIntoBigDataWhiteList(userId, devicePk);
        } else {
            log.info("userLoginRuleApi.whiteDevice resp: {}", JSON.toJSONString(isWhiteDeviceResponse));
        }
    }


    private void addDeviceIntoBigDataWhiteList(Long userId, Long devicePk) {
        Map<String, Object> payload = Maps.newHashMap();
        payload.put("dataType", "USER_WHITELISTED_DEVICEPK");
        payload.put("operationType", "INSERT");
        payload.put("requestTime", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        payload.put("value", ImmutableMap.of("userId", userId, "devicePk", devicePk));
        asyncBigDataProducer.produceMsgToBigData(apolloCommonConfig.getBigDataKafkaTopicBlackList(), payload);
    }

    @Override
    public UserDeviceAuthorizeResponse authorizeDevice(APIRequest<UserDeviceAuthorizeRequest> request) {
        String code = request.getBody().getCode();

        String key = String.format(PATTERN_DEVICE_AUTH_EMAIL, code);
        String cacheStr = RedisCacheUtils.get(key);
        UserDeviceRequest cache = StringUtils.isNotEmpty(cacheStr) ? JsonUtils.toObj(cacheStr, UserDeviceRequest.class) : null;
        log.info("Start authorizeDevice");
        if (cache == null) {
            return UserDeviceAuthorizeResponse.INVALID;
        }
        //前置判断
        User user = userCommonBusiness.checkAndGetUserById(cache.getUserId());
        UserStatusEx userStatusEx = new UserStatusEx(user.getStatus());
        log.info("userStatusEx={}", JsonUtils.toJsonNotNullKey(userStatusEx));
        if (userStatusEx.getIsUserDisabledLogin().booleanValue()) {
            // 已经被禁用登录
            throw new BusinessException(GeneralCode.USER_DISABLED_LOGIN, user.getUserId(), null);
        } else if (userStatusEx.getIsUserDisabled().booleanValue()) {
            // 已经被禁用
            throw new BusinessException(GeneralCode.USER_DISABLED);
        }

        if (!checkIfPassLoginYubikey(cache.getUserId())) {
            throw new BusinessException(AccountErrorCode.PLEASE_VERFIY_YUBIKEY_FIRST);
        }
        UserDeviceAuthorizeResponse result = UserDeviceAuthorizeResponse.pass();
        // check if the user need to answer question before authorizing the device.
        try {
            if (needAnswerQuestion(request, cache.getContent(), cache.getUserId())) {
                result = getQuestionResponse(cache, code);
                log.info("{} need to answer question, question: {}", cache.getUserId(), result.getQuestionFlowId());
                return result;
            }
        } catch (Exception e) {
            //出现异常不block授权流程，放行
            log.error("authorize device get question error", e);
        }
        APIRequest<PushInboxMessage> apiRequest = getAddDevicePushInboxMessageAPIRequest(request, cache, result);
        AsyncTaskExecutor.execute(() -> {
            try {
                inboxMessageTextApi.pushInbox(apiRequest);
            } catch (Exception e) {
                log.warn("send new advice inbox update error", e);
            }
        });
        List<UserDevice> unauthorized = listDevice(cache.getUserId(), cache.getAgentType(), UserDevice.Status.NOT_AUTHORIZED.ordinal(),
                null, null, false, null, null);

        DeviceDiffer differ = findMostSimilarDevice(unauthorized, cache.getContent(), cache.getAgentType(), false);
        AddUserDeviceResponse response;
        if (differ != null && differ.same) {
            log.info("authorizeDevice found existing unauthorized device, id: {}", differ.matched.getId());
            differ.matched.setFlowId(code);
            response = authorizeExistingDevice(differ.matched, cache.getContent());
        } else {
            log.info("authorizeDevice didn't find existing unauthorized device, add new");
            response = this.addDevice(cache.getUserId(), cache.getAgentType(), UserDevice.Status.AUTHORIZED, cache.getSource(), cache.getContent(), code);
        }
        if (response != null && authorizeDeviceCallDecisionRuleSwitch) {
            try {
                addDeviceIntoBigDataWhiteListIfNecessary(request, response.getId(), cache.getContent(), cache.getUserId());
            } catch (Exception e) {
                log.error("addDeviceIntoBigDataWhiteListIfNecessary() error.", e);
            }
        }

        //授权结束，清除缓存
        RedisCacheUtils.del(key);
        RedisCacheUtils.del(cache.getUserId().toString(), AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
        log.info("device auth expire:userId={}", cache.getUserId());
        result.setUserId(cache.getUserId());
        final UserIndex userIndex = userIndexMapper.selectByPrimaryKey(cache.getUserId());
        if (null == userIndex || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        result.setEmail(userIndex.getEmail());
        result.setId(response.getId());
        result.setDeviceId(response.getDeviceId());
        result.setDeviceName(cache.getContent().get(UserDevice.DEVICE_NAME));
        result.setLoginIp(cache.getContent().get(UserDevice.LOGIN_IP));
        result.setLocationCity(getLocationCity(result.getLoginIp()));
        result.setCallback(cache.getCallback());

        UserSecurityLog securityLog = new UserSecurityLog(cache.getUserId(), result.getLoginIp(), result.getLocationCity(),
                cache.getAgentType(), Constant.SECURITY_OPERATE_AUTHORIZE_DEVICE, "授权新设备");
        securityLog.touchDevice(result.getId(), result.getDeviceId());
        iUserSecurityLog.addSecurityLogAsync(securityLog);

        return result;
    }

    private APIRequest<PushInboxMessage> getAddDevicePushInboxMessageAPIRequest(APIRequest<UserDeviceAuthorizeRequest> request, UserDeviceRequest cache, UserDeviceAuthorizeResponse result) {
        String lang = WebUtils.getAPIRequestHeader().getLanguage().getLang();
        String terminalCode = request.getTerminal() == null ? "web" : request.getTerminal().getCode();
        Map<String, Object> data = Maps.newHashMap();
        data.put("ip", cache.getContent().get(UserDevice.LOGIN_IP));
        String device = cache.getContent().get(UserDevice.DEVICE_NAME);
        data.put("device", StringUtils.isBlank(device) ? "unknown" : device);
        data.put("address", getLocationCity(cache.getContent().get(UserDevice.LOGIN_IP)));
        return InboxUtils.getPushInboxMessageAPIRequest(cache.getUserId(), data, lang, terminalCode, "NEW_DEVICE_AUTHORIZE");
    }


    private AddUserDeviceResponse authorizeExistingDevice(UserDevice device, Map<String, String> content) {
        device.setStatus(UserDevice.Status.AUTHORIZED);
        String oldContent = device.getContent();
        String deviceId = JSON.parseObject(oldContent).getString(UserDevice.DEVICE_ID);
        if (StringUtils.isBlank(deviceId)) {
            deviceId = StringUtils.getTimestampRandom32();
        }
        content.put(UserDevice.DEVICE_ID, deviceId);
        device.setContent(JsonUtils.toJsonNotNullKey(content));
        device.setActiveTime(new Date());
        device.setUpdateTime(new Date());
        userDeviceMapper.updateByPrimaryKeySelective(device);
        AddUserDeviceResponse response = new AddUserDeviceResponse(device.getId(), device.getUserId(), deviceId);

        UserDeviceHistory history = new UserDeviceHistory(device);
        history.setOperateType(UserDeviceHistory.OperateType.AUTHORIZE.getCode());
        history.setMemo("authorize existing device");
        AsyncTaskExecutor.execute(() -> iUserDeviceHistory.addHistory(history));

        return response;
    }

    @Override
    public void clearExpiredDevice(Long userId) {
        List<UserDevice> deviceList = userDeviceMapper.selectByUserIdAndAgentType(userId, null, null, UserDeviceConst.SOURCE_LOGIN, null, DelFlag.OK, null, null);
        if (deviceList != null && deviceList.size() > DEVICE_MAX) {
            // 根据活跃时间排序，删除最早的设备
            deviceList.sort((o1, o2) -> o1.getActiveTime().after(o2.getActiveTime()) ? -1 : 0);
            deviceList.subList(DEVICE_MAX, deviceList.size()).forEach(device -> {
                device.setIsDel(DelFlag.DELETED);
                device.setUpdateTime(new Date());
                userDeviceMapper.updateByPrimaryKeySelective(device);

                UserDeviceHistory history = new UserDeviceHistory(device);
                history.setOperateType(UserDeviceHistory.OperateType.REMOVE.getCode());
                history.setMemo("remove, source:user, message:expired device");
                iUserDeviceHistory.addHistory(history);
            });

        }
    }

    @Override
    public void deleteDevices(APIRequest<UserDeviceDeleteRequest> request) {
        UserDeviceDeleteRequest body = request.getBody();
        Long userId = body.getUserId();
        Long devicePk = body.getDevicePk();
        String memo = String.format("remove, source:%s, message:%s", body.getSource(), body.getMemo());

        if (devicePk == null) {
            List<UserDevice> devices = userDeviceMapper.selectByUserIdAndAgentType(
                    userId, null, null, null, null, DelFlag.OK, null, null);
            if (CollectionUtils.isNotEmpty(devices)) {
                devices.forEach(item -> deleteDevice(item, memo));
            }
        } else {
            UserDevice device = userDeviceMapper.selectById(userId, devicePk);
            deleteDevice(device, memo);
        }
        // 用户自己的操作，则记录securityLog
        if (body.isFromUser()) {
            String clientIp = WebUtils.getRequestIp();
            UserSecurityLog securityLog = new UserSecurityLog(userId, clientIp, getLocationCity(clientIp),
                    request.getTerminal().getCode(), Constant.SECURITY_OPERATE_DELETE_DEVICE, String.format("删除设备, id:%s, memo:%s", devicePk, memo));
            securityLog.setDevicePk(devicePk);
            iUserSecurityLog.addSecurityLogAsync(securityLog);
        }
    }

    private void deleteDevice(UserDevice device, String memo) {
        if (device == null) {
            return;
        }
        device.setIsDel(DelFlag.DELETED);
        device.setUpdateTime(new Date());
        userDeviceMapper.updateByPrimaryKeySelective(device);
        UserDeviceHistory history = new UserDeviceHistory(device);
        history.setOperateType(UserDeviceHistory.OperateType.REMOVE.getCode());
        history.setMemo(memo);
        iUserDeviceHistory.addHistory(history);
    }

    @Override
    public UserDevice getDevice(Long userId, Long devicePk) {
        return userDeviceMapper.selectById(userId, devicePk);
    }

    @Override
    public List<UserDevice> listDevice(Long userId, String agentType, Integer statusOrdinal, String source, String excludeSource,
                                       boolean showDeleted, Integer offset, Integer limit) {
        if (showDeleted) {
            return userDeviceMapper.selectByUserIdAndAgentType(userId, agentType, statusOrdinal, source, excludeSource, null, offset, limit);
        } else {
            return userDeviceMapper.selectByUserIdAndAgentType(userId, agentType, statusOrdinal, source, excludeSource, DelFlag.OK, offset, limit);
        }
    }

    @Override
    public Long countDevice(Long userId, String agentType, Integer status, String source, String excludeSource, boolean showDeleted) {
        if (showDeleted) {
            return userDeviceMapper.countByUserIdAndAgentType(userId, agentType, status, source, excludeSource, null);
        } else {
            return userDeviceMapper.countByUserIdAndAgentType(userId, agentType, status, source, excludeSource, DelFlag.OK);
        }
    }

    @Override
    public List<UserDeviceHistory> listDeviceHistory(Long userId, Long deviceId) {
        return iUserDeviceHistory.listHistory(userId, deviceId);
    }

    @Override
    public List<UserDeviceProperty> getDevicePropertyConfig(String agentType, Byte statusCode) {
        //优先读取缓存（缓存所有数据，用的时候做过滤）
        @SuppressWarnings("unchecked")
        List<UserDeviceProperty> cache = RedisCacheUtils.get(CACHE_ACCOUNT_DEVICE_PROPERTY, List.class);
        if (CollectionUtils.isEmpty(cache)) {
            cache = userDevicePropertyMapper.selectByTypeAndStatus(null, null);
            if (!CollectionUtils.isEmpty(cache)) {
                RedisCacheUtils.set(CACHE_ACCOUNT_DEVICE_PROPERTY, cache, 3600);
            }
        }

        return cache.stream().filter(e -> {
            if (agentType != null && !Objects.equals(e.getAgentType(), agentType)) {
                return false;
            }
            if (statusCode != null && !statusCode.equals(e.getStatus())) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public Long addDeviceProperty(UserDeviceProperty property) {
        property.setStatus(UserDeviceProperty.STATUS.OPEN.getCode());
        userDevicePropertyMapper.insertSelective(property);
        clearPropertyCache();
        return property.getId();
    }

    @Override
    public void editDeviceProperty(UserDeviceProperty property) {
        property.setUpdateTime(new Date());
        userDevicePropertyMapper.updateByPrimaryKeySelective(property);
        clearPropertyCache();
    }

    @Override
    public void deleteDeviceProperty(Long id) {
        UserDeviceProperty property = userDevicePropertyMapper.selectByPrimaryKey(id);
        property.setIsDel(DelFlag.DELETED);
        property.setUpdateTime(new Date());
        userDevicePropertyMapper.updateByPrimaryKeySelective(property);
        clearPropertyCache();
    }

    @Override
    public void sendAuthEmail(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl,
                              String customForbiddenLink, String callback) {
        try {
            String lockKey = String.format(PATTERN_DEVICE_AUTH_EMAIL_LOCK, agentType, user.getUserId());
            String lockValue = RedisCacheUtils.get(lockKey);
            if (StringUtils.isNotEmpty(lockValue)) {
                log.warn("send auth-email too frequently, userId:{}, lockedTime:{}", user.getUserId(), lockValue);
                return;
            }

            //发送新设备授权邮件
            String code = StringUtils.getTimestampRandom32();
            String loginIp = content.getOrDefault(UserDevice.LOGIN_IP, "unknown");

            Map<String, Object> data = new HashMap<>(8);

            String link = UserCommonBusiness.emailLinkGenerator(customDeviceAuthorizeUrl,
                    String.format("%suser/device/authorize.html?userId={userId}&code={code}", WebUtils.getHeader(Constant.BASE_URL)),
                    ImmutableMap.of("code", code, "userId", user.getUserId()));
            data.put("link", link);

            data.put(UserDevice.LOGIN_IP, loginIp);
            data.put(UserDevice.LOCATION_CITY, getLocationCity(loginIp));
            data.put(UserDevice.DEVICE_NAME, content.getOrDefault(UserDevice.DEVICE_NAME, agentType));
            data.put(UserDevice.DEVICE_CALLBACK, callback);
            if (content != null && content.size() != 0 && content.get(UserDevice.LOGIN_IP) == null) {
                //content中loginip可能为null,添加设置保证不为null
                content.put(UserDevice.LOGIN_IP, loginIp);
            }
            userCommonBusiness.sendDisableTokenEmail(Constant.NODE_TYPE_DEVICE_AUTHORIZE, user, data, "UserDeviceBusiness.sendAuthEmail", customForbiddenLink);
            log.info("userid={} start", user.getUserId());
            String key = String.format(PATTERN_DEVICE_AUTH_EMAIL, code);
            RedisCacheUtils
                    .set(key,
                            JsonUtils.toJsonNotNullKey(new UserDeviceRequest(user.getUserId(), agentType,
                                    Integer.valueOf(0), UserDeviceConst.SOURCE_LOGIN, content, callback)),
                            15 * 60);
            RedisCacheUtils.set(lockKey, DateUtils.getNewDateUTC(), Constant.MINUTE_5);
            DeviceAuthVerify deviceAuthVerify = new DeviceAuthVerify();
            deviceAuthVerify.setCode(code);
            deviceAuthVerify.setCreateTime(new Date().getTime());
            RedisCacheUtils.set(user.getUserId().toString(), JSON.toJSONString(deviceAuthVerify), 15 * 60,
                    AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
            log.info("userid={} end", user.getUserId());

            // 特殊用户，授权链接缓存起来，便于手工授权
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_SPECIAL)) {
                log.info("Special user, cache link, userId:{}, email:{}", user.getUserId(), user.getEmail());
                specialUserDeviceAuthorizeCache.add(data);
            }
        } catch (Exception e) {
            log.error("发送新设备授权邮件出错 ", e);
        }
    }


    @Override
    public void sendAuthEmailForNewProcess(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl,
                                           String customForbiddenLink, String callback) {
        try {
            String lockKey = String.format(PATTERN_DEVICE_AUTH_EMAIL_LOCK, agentType, user.getUserId());
            String lockValue = RedisCacheUtils.get(lockKey);
            if (StringUtils.isNotEmpty(lockValue)) {
                log.warn("send auth-email too frequently, userId:{}, lockedTime:{}", user.getUserId(), lockValue);
                return;
            }

            //发送新设备授权邮件
            String code = InvitationCodeUtil.generateDeviceVerifyCode();
            String loginIp = content.getOrDefault(UserDevice.LOGIN_IP, "unknown");

            Map<String, Object> data = new HashMap<>(8);

            String link = UserCommonBusiness.emailLinkGenerator(customDeviceAuthorizeUrl,
                    String.format("%suser/device/authorize.html?userId={userId}&code={code}", WebUtils.getHeader(Constant.BASE_URL)),
                    ImmutableMap.of("code", code, "userId", user.getUserId()));
            data.put("link", link);
            data.put("verifyCode", code);
            data.put(UserDevice.LOGIN_IP, loginIp);
            data.put(UserDevice.LOCATION_CITY, getLocationCity(loginIp));
            data.put(UserDevice.DEVICE_NAME, content.getOrDefault(UserDevice.DEVICE_NAME, agentType));
            data.put(UserDevice.DEVICE_CALLBACK, callback);

            userCommonBusiness.sendDisableTokenEmail(AccountConstants.NODE_TYPE_DEVICE_AUTHORIZE2, user, data, "UserDeviceBusiness.sendAuthEmail", customForbiddenLink);
            log.info("userid={} start", user.getUserId());
            String key = String.format(PATTERN_DEVICE_AUTH_EMAIL, code + user.getUserId());
            RedisCacheUtils
                    .set(key,
                            JsonUtils.toJsonNotNullKey(new UserDeviceRequest(user.getUserId(), agentType,
                                    Integer.valueOf(0), UserDeviceConst.SOURCE_LOGIN, content, callback)),
                            15 * 60);
            RedisCacheUtils.set(lockKey, DateUtils.getNewDateUTC(), Constant.MINUTE_5);
            DeviceAuthVerify deviceAuthVerify = new DeviceAuthVerify();
            deviceAuthVerify.setCode(code);
            deviceAuthVerify.setCreateTime(new Date().getTime());
            RedisCacheUtils.set(user.getUserId().toString(), JSON.toJSONString(deviceAuthVerify), 15 * 60,
                    AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
            log.info("userid={} end", user.getUserId());

            // 特殊用户，授权链接缓存起来，便于手工授权
            if (BitUtils.isTrue(user.getStatus(), Constant.USER_SPECIAL)) {
                log.info("Special user, cache link, userId:{}, email:{}", user.getUserId(), user.getEmail());
                specialUserDeviceAuthorizeCache.add(data);
            }
        } catch (Exception e) {
            log.error("发送新设备授权邮件出错 ", e);
        }
    }

    @Override
    public void cacheDeviceAuthForLogin(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl, String customForbiddenLink, String callback) {
        try {
            //TODO排查问题用
            log.info("cacheDeviceAuthForLogin:userId={},contentinfo={}", user.getUserId(), JsonUtils.toJsonNotNullKey(content));
            String lockKey = String.format(PATTERN_DEVICE_AUTH_EMAIL_LOCK, agentType, user.getUserId());
            String lockValue = RedisCacheUtils.get(lockKey);
            if (StringUtils.isNotEmpty(lockValue)) {
                log.warn("cacheDeviceAuthForLogin too frequently, userId:{}, lockedTime:{}", user.getUserId(), lockValue);
                return;
            }
            log.info("userid={} start", user.getUserId());
            String key = String.format(PATTERN_DEVICE_AUTH_EMAIL, user.getUserId());
            RedisCacheUtils
                    .set(key,
                            JsonUtils.toJsonNotNullKey(new UserDeviceRequest(user.getUserId(), agentType,
                                    Integer.valueOf(0), UserDeviceConst.SOURCE_LOGIN, content, callback)),
                            15 * 60);
            RedisCacheUtils.set(lockKey, DateUtils.getNewDateUTC(), Constant.MINUTE_5);
            log.info("userid={} end", user.getUserId());
            log.info("start cacheDeviceAuthForLogin add");
            this.addDevice(user.getUserId(), agentType, UserDevice.Status.NOT_AUTHORIZED, UserDeviceConst.SOURCE_LOGIN, content, null);
            log.info("start cacheDeviceAuthForLogin end");

        } catch (Exception e) {
            log.error("cacheDeviceAuthForLogin error ", e);
        }
    }

    @Override
    public boolean isStrictMode(String agentType) {
        try {
            List config = sysConfigVarCacheService.getList(CONFIG_DEVICE_CHECK_STRICT_MODE);
            return config.contains(agentType);
        } catch (Exception e) {
            log.error("Get config isStrictMode error, agentType:{}", agentType, e);
            return false;
        }
    }

    @Override
    public boolean checkVersion(String agentType, String version) {
        // web端，必然需要验证设备
        if (TerminalEnum.WEB.getCode().equalsIgnoreCase(agentType) || TerminalEnum.H5.getCode().equalsIgnoreCase(agentType) || TerminalEnum.ELECTRON.getCode().equalsIgnoreCase(agentType)) {
            return true;
        }
        if (StringUtils.isEmpty(version)) {
            log.warn("app version not found, agentType:{}, version:{}", agentType, version);
            return false;
        }
        if (TerminalEnum.IOS.getCode().equalsIgnoreCase(agentType)) {
            return VersionHelper.higherOrEqual(version, deviceCheckIosVersion);
        } else if (TerminalEnum.ANDROID.getCode().equalsIgnoreCase(agentType)) {
            return VersionHelper.higherOrEqual(version, deviceCheckAdndroidVersion);
        } else if (TerminalEnum.MAC.getCode().equalsIgnoreCase(agentType)) {
            return VersionHelper.higherOrEqual(version, deviceCheckMacVersion);
        } else if (TerminalEnum.PC.getCode().equalsIgnoreCase(agentType)) {
            return VersionHelper.higherOrEqual(version, deviceCheckPcVersion);
        }
        return false;
    }

    @Override
    public int getMinPropertyCount(String agentType) {
        int minPropertyCount = 0;
        try {
            Map config = sysConfigVarCacheService.getMap(CONFIG_DEVICE_MIN_PROPERTY_COUNT);
            if (config != null && config.size() > 0) {
                minPropertyCount = (int) config.get(agentType);
            }
        } catch (Exception e) {
            log.error("getMinPropertyCount error: {}", agentType, e);
        }
        log.info("getMinPropertyCount: {}", minPropertyCount);
        return minPropertyCount;
    }

    @Override
    public List<Map<String, Object>> getSpecialUserAuthList() {
        return specialUserDeviceAuthorizeCache.getList();
    }

    @Override
    public void updateRelatedDevice(Long userId, Long devicePk, String relatedDeviceIds) {
        if (StringUtils.isBlank(relatedDeviceIds) || userId == null || devicePk == null) {
            return;
        }
        for (String di : relatedDeviceIds.split(",")) {
            if (StringUtils.length(di) == 32) {
                List<UserDevice> matched = deviceSearchService.searchDeviceByDeviceId(di);
                matched.forEach(tmp -> {
                    UserDeviceRelation relation = new UserDeviceRelation(userId, devicePk, tmp);
                    relation.setReleatedUserEmail(userCommonBusiness.getEmailById(tmp.getUserId()));
                    if (deviceRelationMapper.insertIgnoreSelective(relation) == 0) {
                        deviceRelationMapper.updateByPrimaryKeySelective(relation);
                    }
                });
            } else {
                log.warn("Invalid releatedDeviceIds: {}", relatedDeviceIds);
            }
        }
    }

    @Override
    public List<UserDeviceRelation> listRelation(Long devicePk) {
        return deviceRelationMapper.selectRelation(devicePk);
    }

    @Override
    public boolean isDeviceInBlackList(Map<String, String> deviceInfo, String clientType) {
        String riskDeviceBlackIndex = "/risk_device_black_list/_search";
        int currentPage = 1;
        int currentSize = 0;
        do {
            ESQueryBuilder builder = ESQueryBuilder.instance();
            builder.limit((currentPage - 1) * 1000, 1000);
            Map<String, Object> paramsMap = builder.build();
            ESResultSet resultSet = null;
            try {
                resultSet = ((UserDeviceBusiness) applicationContext.getBean(this.getClass())).search(riskDeviceBlackIndex, paramsMap);
            } catch (Exception e) {
                log.error("search device black list failed.");
            }
            if (resultSet == null || resultSet.getHits().size() == 0) {
                log.warn("isDeviceInBlackList(), device black list Not found.");
                return false;
            }
            // 遍历所有的设备黑名单进行匹配
            List<JSONObject> jsonBlacklists = resultSet.getHits().toJavaList(JSONObject.class);
            if (!org.springframework.util.CollectionUtils.isEmpty(jsonBlacklists)) {
                currentSize = jsonBlacklists.size();
                for (JSONObject hitJson : jsonBlacklists) {
                    JSONObject objJson = hitJson.getJSONObject("_source");
                    Iterator<String> keyIterator = objJson.keySet().iterator();
                    boolean isMatch = true;
                    while (keyIterator.hasNext()) {
                        String key = keyIterator.next();
                        if (MatchDeviceIgnoredProperties.contains(key)) {
                            continue;
                        }
                        String value = objJson.getString(key);
                        if ("device_type".equals(key)) { // 设备类型
                            if (!value.equals(clientType)) {
                                isMatch = false;
                                log.info("{} not matched field: {}, expected: {}, actual: {}",
                                        hitJson.getString("_id"), "device_type", value, clientType);
                                break;
                            }
                        } else if ("login_ip".equals(key)) { // ip 地址匹配前三段
                            String loginIp = deviceInfo.get(key);
                            String loginIpPrefix = loginIp.substring(0, loginIp.lastIndexOf("."));
                            if (!value.startsWith(loginIpPrefix)) {
                                isMatch = false;
                                log.info("{} not matched field: {}, expected: {}, actual: {}",
                                        hitJson.getString("_id"), "login_ip", value, loginIp);
                                break;
                            }
                        } else {
                            if (!value.equals(deviceInfo.get(key))) {
                                log.info("{} not matched field: {}, expected: {}, actual: {}",
                                        hitJson.getString("_id"), key, value, deviceInfo.get(key));
                                isMatch = false;
                                break;
                            }
                        }
                    }
                    if (isMatch) {
                        log.info("isDeviceInBlackList(), black list matched, deviceInfo:{}, ------------------->> hit:{}",
                                deviceInfo, objJson);
                        return true;
                    } else {
                        log.info("isDeviceInBlackList(), black list not matched.");
                    }
                }
            }
            currentPage++;
        } while (currentSize == 1000);
        return false;
    }

    // 设备黑名单信息需要排除对比的列
    private static final Set<String> MatchDeviceIgnoredProperties =
            ImmutableSet.of(
                    "id", "create_time", "related_users", "content",
                    "creator", "remark", "db_create_time", "db_modify_time"
            );

    @Retryable(
            value = {IOException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000)
    )
    ESResultSet search(String endpoint, Map<String, Object> params) throws IOException {
        Request request = new Request("GET", endpoint);
        request.setEntity(new NStringEntity(JSON.toJSONString(params), ContentType.APPLICATION_JSON));
        Response response;
        try {
            response = restClient.performRequest(request);
        } catch (IOException e) {
            log.warn("------>search() error, {}-{}, {}", endpoint, params, e);
            throw e;
        }
        try {
            if (response.getStatusLine().getStatusCode() == 200) {
                return JSONObject.parseObject(EntityUtils.toString(response.getEntity())).getObject("hits", ESResultSet.class);
            } else {
                log.warn("------>search() failed, {}-{}, response:{}", endpoint, params,
                        EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception ex) {
            log.error("------>search() Parse object error: ", ex);
        }
        return null;
    }


    private void clearPropertyCache() {
        RedisCacheUtils.del(CACHE_ACCOUNT_DEVICE_PROPERTY);
    }


    private String getLocationCity(String ip) {
        return StringUtils.replace(IP2LocationUtils.getCountryCity(ip), ", Province of China", "");
    }

    /**
     * 过滤出需要收集的property，并检验最后的总长度
     */
    private Map<String, String> filterProperties(String agentType, Map<String, String> raw) {
        Map<String, Integer> weightMap = this.getPropertyWeight(agentType);
        String[] keys = raw.keySet().toArray(new String[0]);
        for (String key : keys) {
            if (!weightMap.containsKey(key) || raw.get(key) == null) {
                raw.remove(key);
            }
        }
        if (JsonUtils.toJsonNotNullKey(raw).length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(GeneralCode.USER_DEVICE_INFO_EMPTY_WEB);
        }
        return raw;
    }

    /**
     * 获取设备属性权重
     */
    private Map<String, Integer> getPropertyWeight(String agentType) {
        List<UserDeviceProperty> properties = this.getDevicePropertyConfig(agentType, UserDeviceProperty.STATUS.OPEN.getCode());
        // h5若没有单独配置，则使用web的
        if (CollectionUtils.isEmpty(properties) && TerminalEnum.H5.getCode().equalsIgnoreCase(agentType)) {
            properties = this.getDevicePropertyConfig(TerminalEnum.WEB.getCode(), UserDeviceProperty.STATUS.OPEN.getCode());
        }
        //需要考虑不存在的agent type
        if (CollectionUtils.isEmpty(properties) && Objects.nonNull(TerminalEnum.findByCode(agentType))) {
            log.warn("Failed to load device property config, this is required for login: agentType={}", agentType);
        }
        Map<String, Integer> weightMap = new HashMap<>(properties.size());
        properties.forEach(item -> weightMap.put(item.getPropertyKey(), item.getPropertyWeight()));
        return weightMap;
    }


    /**
     * 从设备中寻找最相似的
     */
    public DeviceDiffer findMostSimilarDevice(List<UserDevice> deviceList, Map<String, String> candidate, String agentType, boolean isHistoryIp) {
        DeviceDiffer mostSimilar = null;

        for (UserDevice device : deviceList) {
            Map<String, String> deviceInfo = filterProperties(agentType, JsonUtils.toObj(device.getContent(), HashMap.class));
            DeviceDiffer result = compareDevice(candidate, deviceInfo, agentType, isHistoryIp);
            if (result.isSame()) {
                result.setMatched(device);
//                if (result == null) {
//                    result = tmp;
//                } else if (tmp.getScore() > result.getScore()) {
//                    // 若有多个设备满足匹配条件，取差异最小的(得分最高的)
//                    result = tmp;
//                }
            }

            if (mostSimilar == null || mostSimilar.getScore() < result.getScore()) {
                mostSimilar = result;
                mostSimilar.setMostSimilarDeviceInfo(deviceInfo);
            }
        }
        return mostSimilar;
    }


    /**
     * 从设备中寻找最相似的
     */
    public UserDeviceComparator.DeviceComparisonResult findMostSimilarDeviceV2(List<UserDevice> deviceList, Map<String, String> candidate, String agentType) {
        UserDeviceComparator.DeviceComparisonResult result = null;
        UserDevice matchedDevice = null;
        for (UserDevice device : deviceList) {
//            DeviceDiffer tmp = compareDevice(candidate, filterProperties(agentType, JsonUtils.toObj(device.getContent(), HashMap.class)),
//                    device.getId(), agentType, isHistoryIp);
            UserDeviceComparator.DeviceComparisonResult compareResult = userDeviceComparator.compare(candidate, JsonUtils.toObj(device.getContent(), HashMap.class), agentType);
            if (result == null) {
                result = compareResult;
                matchedDevice = device;
            } else if (result != null) {
                if (result.getScore() < compareResult.getScore()) {
                    result = compareResult;
                    matchedDevice = device;
                }
            }
        }
        if (result != null) {
            result.setMostSimilar(matchedDevice);
        }
        return result;
    }

    public UserDeviceComparator.DeviceComparisonResult compareV2(Map<String, String> content1, Map<String, String> content2, String agentType) {
        return userDeviceComparator.compare(content1, content2, agentType);
    }

    private void findMostSimilarDeviceAndLogResult(DeviceDiffer result, List<UserDevice> deviceList, Map<String, String> candidate, String agentType) {
        // 目前仅支持web
        if (alg2Switch && TerminalEnum.WEB.getCode().equals(agentType)) {
            try {
                UserDeviceComparator.DeviceComparisonResult resultV2 = findMostSimilarDeviceV2(deviceList, candidate, agentType);
                // log.info("DeviceComparisonResult: {}", JSON.toJSONString(resultV2));
                DeviceMatchReport report = new DeviceMatchReport();
                report.setCandidateDeviceInfo(JSON.toJSONString(candidate));
                report.setV1Score(result == null ? 0 : result.getScore());
                report.setV2Score(resultV2 == null ? 0 : resultV2.getScore());
                if (result != null && result.getMostSimilarDeviceInfo() != null) {
                    report.setV1MatchedDeviceInfo(JSON.toJSONString(result.getMostSimilarDeviceInfo()));
                }
                report.setV2Score(resultV2 == null ? 0 : resultV2.getScore());
                if (resultV2 != null && resultV2.getMostSimilar() != null) {
                    report.setV2MatchedDeviceInfo(resultV2.getMostSimilar().getContent());
                }
                report.setVersion(userDeviceComparator.getAlgVersion());
                deviceMatchReportBusiness.insertDeviceMatchReport(report);
            } catch (Exception e) {
                log.warn("error findMostSimilarDeviceAndLogResult", e);
            }
        }
    }

    /**
     * 比较设备指纹是否匹配（参考文档：https://confluence.fdgahl.cn/pages/viewpage.action?pageId=950761）
     *
     * @param candidate 待检测的指纹
     * @param target    已检测过的指纹
     * @return 不匹配的条目数
     */
    private DeviceDiffer compareDevice(Map<String, String> candidate, Map<String, String> target, String agentType, boolean isHistoryIp) {
        Map<String, Integer> weightMap = getPropertyWeight(agentType);
        Set<String> differentItems = new HashSet<>();

        double totalScore = 0;
        double matchScore = 0;
        for (Map.Entry<String, String> targetEntry : target.entrySet()) {
            String candidateValue = candidate.get(targetEntry.getKey());
            double propScore = weightMap.get(targetEntry.getKey()).doubleValue();
            boolean isEqual = Objects.equals(targetEntry.getValue(), candidateValue) || (targetEntry.getKey().equals(UserDevice.LOGIN_IP) && isHistoryIp);
            if (isEqual) {
                matchScore += propScore;
            } else {
                differentItems.add(targetEntry.getKey());
            }
            totalScore += propScore;
        }
        // 设备审核最终分数 = 100 * 正确字段进行累加 / 启用有效字段总分
        double finalScore = totalScore == 0 ? 0 : (matchScore * 100 / totalScore);
        return new DeviceDiffer(differentItems, finalScore);
    }


    private double getDifferScore() {
        Double score = sysConfigVarCacheService.getDouble(CONFIG_DEVICE_CHECK_SCORE_THRESHOLD);
        if (score == null) {
            return 60;
        } else {
            return score;
        }
    }

    @Getter
    @Setter
    public class DeviceDiffer {
        /**
         * true：表示为相同设备
         */
        private boolean same;
        private double score;
        private Set<String> items;
        private UserDevice matched;
        // 得分最高的设备content，不保证是匹配的设备
        private Map<String, String> mostSimilarDeviceInfo;

        public DeviceDiffer(Set<String> items, double score) {
            this.items = items;
            this.score = score;
            if (score >= getDifferScore()) {
                same = true;
            }
        }

        public DeviceDiffer(boolean same, double score) {
            this.same = same;
            this.score = score;
        }
    }

    @Override
    public FindMostSimilarUserDeviceResponse findMostSimilarDevice(Long userId, Map<String, String> candidate, String agentType) {
        boolean isHistoryIp = userIpChange.isHistoryIp(userId, candidate.get(UserDevice.LOGIN_IP));
        List<UserDevice> deviceList = userDeviceMapper.selectAuthorizedDevices(userId, agentType);

        UserDeviceBusiness.DeviceDiffer deviceDiffer = findMostSimilarDevice(deviceList, candidate, agentType, isHistoryIp);
        findMostSimilarDeviceAndLogResult(deviceDiffer, deviceList, candidate, agentType);

        FindMostSimilarUserDeviceResponse findMostSimilarUserDeviceResponse = new FindMostSimilarUserDeviceResponse();
        if (deviceDiffer == null) {
            findMostSimilarUserDeviceResponse.setScore(0);
            findMostSimilarUserDeviceResponse.setSame(false);
            return findMostSimilarUserDeviceResponse;
        }

        findMostSimilarUserDeviceResponse.setSame(deviceDiffer.isSame());
        if (deviceDiffer.matched != null) {
            UserDeviceVo userDeviceVo = new UserDeviceVo();
            BeanUtils.copyProperties(deviceDiffer.matched, userDeviceVo);
            userDeviceVo.setStatus(deviceDiffer.matched.getStatus().ordinal());
            findMostSimilarUserDeviceResponse.setMatched(userDeviceVo);
        }
        findMostSimilarUserDeviceResponse.setScore(deviceDiffer.score);
        return findMostSimilarUserDeviceResponse;
    }

    @Override
    public ResendAuthorizeDeviceEmailResponse resendAuthorizeDeviceEmail(APIRequest<ResendAuthorizeDeviceEmailRequest> request) {
        ResendAuthorizeDeviceEmailRequest resendAuthorizeDeviceEmailRequest = request.getBody();
        String email = resendAuthorizeDeviceEmailRequest.getEmail();
        Map<String, String> deviceInfo = resendAuthorizeDeviceEmailRequest.getDeviceInfo();
        User user = userCommonBusiness.getUserIdByEmail(email);
        if (!checkIfPassLogin2Fa(user.getUserId())) {
            throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
        }
        String clientType = request.getTerminal().getCode();
        preCheck(deviceInfo, user.getUserId(), clientType);
        // 设备指纹不为空，且属性数足够，才做校验
        boolean canCheckDevice = (deviceInfo != null && deviceInfo.size() >= getMinPropertyCount(clientType));
        if (checkVersion(clientType, request.getVersion())) {
            if (canCheckDevice) {
                CheckUserDeviceResponse ckRs = checkDevice(user.getUserId(), clientType, deviceInfo);
                if (!ckRs.isValid()) {
                    //通过Notification发送通知
                    securityNotificationService.saveSecurityNotification(user.getUserId(), SecurityNotificationEnum.DEVICE_AUTH, request.getLanguage());
                    // 发送授权邮件
                    sendAuthEmailForNewProcess(user, clientType, deviceInfo, null, null, null);
                }
            }
        }
        return new ResendAuthorizeDeviceEmailResponse();
    }

    @Override
    public Boolean verifyAuthDeviceCode(APIRequest<VerifyAuthDeviceCodeRequest> request) {
        VerifyAuthDeviceCodeRequest verifyAuthDeviceCodeRequest = request.getBody();
        try {
            log.info("verifyAuthDeviceCode:userId={}", verifyAuthDeviceCodeRequest.getUserId());
            if (!checkIfPassLogin2Fa(verifyAuthDeviceCodeRequest.getUserId())) {
                throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
            }
            Lock lock = RedisCacheUtils.getLock(AccountConstants.AUTH_DEVICE_EMAIL_USER_LOCK + verifyAuthDeviceCodeRequest.getUserId().toString());
            if (lock != null && lock.tryLock()) {
                try {
                    String authDeviceEmailStr = RedisCacheUtils.get(verifyAuthDeviceCodeRequest.getUserId().toString(), String.class, AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
                    ;
                    if (StringUtils.isBlank(authDeviceEmailStr)) {
                        throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
                    }
                    DeviceAuthVerify redisVerify = JSON.parseObject(authDeviceEmailStr, DeviceAuthVerify.class);
                    String originLoginToken = redisVerify.getLoginToken();
                    if (isForceCheckDeviceLoginToken && org.apache.commons.lang.StringUtils.isNotBlank(originLoginToken)
                            && !org.apache.commons.lang.StringUtils.equalsIgnoreCase(originLoginToken, verifyAuthDeviceCodeRequest.getLoginToken())) {
                        log.info("verifyAuthDeviceCode token error:userId={},originLoginToken={},requestToken={}", verifyAuthDeviceCodeRequest.getUserId(), originLoginToken,
                                verifyAuthDeviceCodeRequest.getLoginToken());
                        throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
                    }
                    if (!StringUtils.equals(redisVerify.getCode(), verifyAuthDeviceCodeRequest.getCode())) {
                        redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);
                        redisVerify.setErrorTime(DateUtils.getNewUTCTimeMillis());
                        RedisCacheUtils.set(verifyAuthDeviceCodeRequest.getUserId().toString(), JsonUtils.toJsonNotNullKey(redisVerify), -1L,
                                AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);// 不修改过期时间
                        log.info("verifyAuthDeviceCode error count:userId={},errorcount={}", verifyAuthDeviceCodeRequest.getUserId(), redisVerify.getErrorCount());
                        if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount().intValue() >= 5) {
                            RedisCacheUtils.del(verifyAuthDeviceCodeRequest.getUserId().toString(), AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
                            log.info("verifyAuthDeviceCode error expire:userId={}", verifyAuthDeviceCodeRequest.getUserId());
                            String devicecheckkey = String.format(PATTERN_DEVICE_AUTH_EMAIL, redisVerify.getCode());
                            RedisCacheUtils.del(devicecheckkey);
                            log.info("verifyAuthDeviceCode devicecheckkey expire:userId={}", verifyAuthDeviceCodeRequest.getUserId());
                            throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
                        }
                        throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_ERROR);
                    }
                    RedisCacheUtils.del(verifyAuthDeviceCodeRequest.getUserId().toString(), AccountConstants.AUTH_DEVICE_EMAIL_USER_CACHE);
                    log.info("device auth expire:userId={}", verifyAuthDeviceCodeRequest.getUserId());
                    return true;
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            } else {
                log.info("verifyAuthDeviceCode getlockFailed. userId:{}", verifyAuthDeviceCodeRequest.getUserId());
                throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
            }
        } catch (ExpiredJwtException e) {
            throw new BusinessException(GeneralCode.USER_ACTIVE_CODE_EXPIRED);
        }
    }

    @Override
    public boolean checkNewDisableLogicVersion(String agentType, String version) {
        log.info("agentType={},version={}",agentType,version);
        if(TerminalEnum.ELECTRON.getCode().equalsIgnoreCase(agentType)){
            return true;
        }
        boolean isIosOrAndroid=TerminalEnum.IOS.getCode().equalsIgnoreCase(agentType)|| TerminalEnum.ANDROID.getCode().equalsIgnoreCase(agentType);
        if(!isIosOrAndroid){
            log.warn("isIosOrAndroid false, agentType:{}, version:{}", agentType, version);
            return false;
        }
        if (StringUtils.isEmpty(version)) {
            log.warn("app version not found, agentType:{}, version:{}", agentType, version);
            return false;
        }
        if (TerminalEnum.IOS.getCode().equalsIgnoreCase(agentType)) {
            return VersionHelper.higherOrEqual(version, newDisableLogicCheckIosVersion);
        } else if (TerminalEnum.ANDROID.getCode().equalsIgnoreCase(agentType)) {
            try{
                Integer androidVersion=Integer.parseInt(version);
                Integer andoroidVersionModResult=androidVersion%1000;
                log.info("androidVersion={},andoroidVersionModResult={}",androidVersion,andoroidVersionModResult);
                boolean needCheckVersion=VersionHelper.higherOrEqual(andoroidVersionModResult.toString(), newDisableLogicCheckAdndroidVersion);
                log.info("needCheckVersion={}",needCheckVersion);
                return needCheckVersion;
            }catch (Exception e){
                log.info("unkonow version");
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isOldAppVersion(String agentType, String version) {
        log.info("isOldAppVersion:agentType={},version={}", agentType, version);
        boolean isIosOrAndroid = TerminalEnum.IOS.getCode().equalsIgnoreCase(agentType) || TerminalEnum.ANDROID.getCode().equalsIgnoreCase(agentType);
        if (!isIosOrAndroid) {
            log.warn("isOldAppVersion:isIosOrAndroid false, agentType:{}, version:{}", agentType, version);
            return false;
        }
        if (StringUtils.isEmpty(version)) {
            log.warn("isOldAppVersion:app version not found, agentType:{}, version:{}", agentType, version);
            return false;
        }
        if (TerminalEnum.IOS.getCode().equalsIgnoreCase(agentType)) {
            return !VersionHelper.higherOrEqual(version, oldAppIosVersion);
        } else if (TerminalEnum.ANDROID.getCode().equalsIgnoreCase(agentType)) {
            return !VersionHelper.higherOrEqual(version, oldAppAdndroidVersion);
        }
        return false;
    }


    public Boolean checkIfPassLogin2Fa(Long userId) {
        User user = userCommonBusiness.checkAndGetUserById(userId);
        Boolean isEnable2FA = BitUtils.isEnable(user.getStatus(), Constant.USER_MOBILE) || BitUtils.isEnable(user.getStatus(), Constant.USER_GOOGLE);
        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        List<String> origins = webAuthnFrontHandler.getUserRegisteredOrigins(user.getUserId());
        if (null != userSecurity.getYubikeyEnabledScenarios() && org.apache.commons.collections.CollectionUtils.isNotEmpty(origins)) {
            //Security key registered origins
            UserSecurityKeyStatus userSecurityKeyStatus = UserSecurityKeyStatus.build(userSecurity.getYubikeyEnabledScenarios(), origins);
            if (userSecurityKeyStatus.getLogin().booleanValue()) {
                isEnable2FA = true;
            }
        }
        String twoFaCheckResult = RedisCacheUtils.get(userId.toString(), String.class, AccountConstants.ACCOUNT_LOGIN_VERIFY_2FA_KEY);
        if (org.apache.commons.lang3.StringUtils.isBlank(twoFaCheckResult) && isEnable2FA.booleanValue()) {
            log.info("checkIfPassLogin2Fa:twoFaCheckResult={}", twoFaCheckResult);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Boolean checkIfPassLoginYubikey(Long userId) {
        log.info("checkIfPassLoginYubikey:userId={}", userId);
        String yubikeyCheckResult = RedisCacheUtils.get(userId.toString(), String.class, AccountConstants.ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY);
        if (!newDeviceForceCheckSwitch) {
            return true;
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(yubikeyCheckResult)) {
            log.info("checkIfPassLoginYubikey:yubikeyCheckResult={}", yubikeyCheckResult);
            return false;
        } else {
            return true;
        }
    }

    public TerminalEnum getUserLastLoginDevice(Long userId) throws Exception {
        if (userId == null) {
            return TerminalEnum.OTHER;
        }
        userCommonBusiness.checkAndGetUserById(userId);
        UserDevice userDevice = userDeviceMapper.selectUserLastLoginDevice(userId);
        if (userDevice == null) {
            return TerminalEnum.OTHER;
        }
        return TerminalEnum.findByCode(userDevice.getAgentType());
    }

    @Override
    public CheckNewDeviceIpResponse checkNewDeviceIp(APIRequest<CheckNewDeviceIpRequest> apiRequest) {
        CheckNewDeviceIpRequest checkNewDeviceIpRequest = apiRequest.getBody();
        Long userId = checkNewDeviceIpRequest.getUserId();
        String ip = checkNewDeviceIpRequest.getIp();
        Map<String, String> deviceInfo = checkNewDeviceIpRequest.getDeviceInfo();
        CheckNewDeviceIpResponse response = new CheckNewDeviceIpResponse();
        boolean isHistoryIp = userIpChange.isHistoryIp(userId, ip);
        log.info("isHistoryIp={}", isHistoryIp);
        if (isHistoryIp) {
            return response;
        }
        String clientType = apiRequest.getTerminal().getCode();
        String version = apiRequest.getVersion();
        CheckUserDeviceResponse rs = checkDevice(userId, clientType, deviceInfo);
        if (rs.isValid()) {
            return response;
        }
        response.setNewDeviceIp(true);
        return response;
    }

    @Override
    public APIResponse<AddUserDeviceForQRCodeLoginResponse> addDeviceForQRCodeLogin(APIRequest<AddUserDeviceForQRCodeLoginRequest> apiRequest) {
        AddUserDeviceForQRCodeLoginRequest addUserDeviceForQRCodeLoginRequest = apiRequest.getBody();
        Long userId = addUserDeviceForQRCodeLoginRequest.getUserId();
        String ip = addUserDeviceForQRCodeLoginRequest.getIp();
        Map<String, String> deviceInfo = addUserDeviceForQRCodeLoginRequest.getDeviceInfo();
        String clientType = apiRequest.getTerminal().getCode();
        String version = apiRequest.getVersion();
        preCheck(deviceInfo, userId, clientType);

        String clientIp = ip;
        String locationCity = IP2LocationUtils.getCountryCity(clientIp);
        UserSecurityLog securityLog = new UserSecurityLog(userId, clientIp, locationCity, clientType,
                Constant.SECURITY_OPERATE_TYPE_LOGIN, "扫码登录");
        String deviceName = null;
        String relatedDeviceIds = deviceInfo != null ? deviceInfo.get(UserDeviceConst.RELATED_DEVICE_IDS) : null;

        boolean hasDeviceChecked;
        // 设备指纹不为空，且属性数足够，才做校验
        boolean canCheckDevice =
                (deviceInfo != null && deviceInfo.size() >= getMinPropertyCount(clientType));
        AddUserDeviceResponse response = null;

        if (checkVersion(clientType, version)) {
            if (!canCheckDevice) {
                hasDeviceChecked = false;
            } else {
                CheckUserDeviceResponse ckRs =
                        checkDevice(userId, clientType, deviceInfo);
                if (ckRs.isValid()) {
                    hasDeviceChecked = true;
                    response = new AddUserDeviceResponse();
                    response.setId(ckRs.getId());
                    response.setDeviceId(ckRs.getDeviceId());
                    response.setUserId(userId);
                    deviceName = deviceInfo.get(UserDevice.DEVICE_NAME);
                }
                List<UserDevice> unauthorized = listDevice(userId, clientType, null,
                        null, null, false, null, null);

                DeviceDiffer differ = findMostSimilarDevice(unauthorized, deviceInfo, clientType, false);

                if (differ != null && differ.same) {
                    log.info("addDeviceForQRCodeLogin found existing unauthorized device, id: {}", differ.matched.getId());
                    response = authorizeExistingDevice(differ.matched, deviceInfo);
                    // 记录关联设备的信息
                    AsyncTaskExecutor.execute(() -> updateRelatedDevice(userId,
                            ckRs.getId(), relatedDeviceIds));
                } else {

                    response = this.addDevice(userId, clientType, UserDevice.Status.AUTHORIZED, UserDeviceConst.SOURCE_LOGIN, deviceInfo, null);
                    log.info("addDeviceForQRCodeLogin didn't find existing unauthorized device, add new");
                }

            }
        } else {
            hasDeviceChecked = false;
            // 直接记录设备信息
            log.warn("客户端版本过低，忽略设备信息: {}", userId);
        }
        if (response != null && authorizeDeviceCallDecisionRuleSwitch) {
            try {
                addDeviceIntoBigDataWhiteListIfNecessary(clientType, apiRequest.getLanguage().getLang(), response.getId(), deviceInfo, userId);
            } catch (Exception e) {
                log.error("addDeviceIntoBigDataWhiteListIfNecessary() error.", e);
            }
        }
        AddUserDeviceForQRCodeLoginResponse addUserDeviceForQRCodeLoginResponse = new AddUserDeviceForQRCodeLoginResponse();
        if (response != null) {
            securityLog.touchDevice(response.getId(), response.getDeviceId());
            this.userSecurityLogMapper.insertSelective(securityLog);
            addUserDeviceForQRCodeLoginResponse.setDeviceId(response.getDeviceId());
            addUserDeviceForQRCodeLoginResponse.setId(response.getId());
            addUserDeviceForQRCodeLoginResponse.setUserId(userId);


        }
        return APIResponse.getOKJsonResult(addUserDeviceForQRCodeLoginResponse);
    }

}
