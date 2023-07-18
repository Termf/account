package com.binance.account.service.face.handler;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserFaceReferenceMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.security.IFace;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.FaceTransTypeContentVo;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceErrorCode;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.vo.faceid.FaceLogVo;
import com.binance.inspector.vo.faceid.face.FaceWebGetTokenResponse;
import com.binance.inspector.vo.faceid.response.FaceSDKVerifyResponse;
import com.binance.inspector.vo.faceid.response.FaceWebInitResponse;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.WebUtils;
import com.google.common.collect.Maps;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * 不同类型人脸识别处理器的抽象
 *
 * @author liliang1
 * @date 2019-02-28 9:39
 */
@Log4j2
public abstract class AbstractFaceHandler {

    private static final String INIT_TRANS_FACE_USER_LOCK = "%s_USER_KEY_LOCK__%s";
    private static final String RESEND_EMAIL_TIMES_CACHE = "RESEND_FACE_EMAIL_TIMES_%s";

    @Resource
    protected ApolloCommonConfig apolloCommonConfig;
    @Resource
    protected TransactionFaceLogMapper transactionFaceLogMapper;
    @Resource
    protected UserIndexMapper userIndexMapper;
    @Resource
    protected UserMapper userMapper;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Resource
    protected IFace iFace;
    @Resource
    protected UserFaceReferenceMapper userFaceReferenceMapper;

    /**
     * 使用transId 初始化一个人脸识别的流程
     *
     * @param transId
     * @param userId
     * @param faceTransType
     * @param needEmail
     * @param isKycLockOne 是否为KYC单一锁定数据
     */
    public abstract FaceFlowInitResult initTransFace(String transId, Long userId, FaceTransType faceTransType, boolean needEmail, boolean isKycLockOne);

    /**
     * 使用的邮件通知邮件模板
     *
     * @return
     */
    protected abstract String emailNotifyTemplate();

    /**
     * 判断人脸识别是否已经通过
     *
     * @param transId
     * @param faceTransType
     * @return
     */
    public boolean isFacePassed(String transId, FaceTransType faceTransType) {
        TransactionFaceLog transactionFaceLog = transactionFaceLogMapper.findByTransId(transId, faceTransType.name());
        if (transactionFaceLog == null) {
            log.info("获取对应的业务记录信息失败. transId:{}", transId);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        boolean passed = transactionFaceLog.getStatus() == TransFaceLogStatus.PASSED;
        log.info("业务的人脸识别是否通过结果: transId:{} passed:{}", transId, passed);
        return passed;
    }

    /**
     * 根据用户的邮箱，发送当前用户正在做人脸识别的通知邮件
     *
     * @param email
     * @param faceTransType
     */
    public void resendFaceEmailByEmail(String email, FaceTransType faceTransType) {
        User user = userMapper.queryByEmail(email);
        if (user == null) {
            log.warn("获取用户失败. email:{}", email);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userId = user.getUserId();
        TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
        defaultResendFaceEmail(userId, faceTransType, faceLog);
    }

    /**
     * 根据transId, 发送当前正在做人脸识别的通知邮件
     *
     * @param transId
     * @param faceTransType
     */
    public void resendFaceEmailByTransId(String transId, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, faceTransType.name());
        if (faceLog == null || faceLog.isEndStatus()) {
            log.info("重发提币邮件但是业务编号错误. transId:{}", transId);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId = faceLog.getUserId();
        defaultResendFaceEmail(userId, faceTransType, faceLog);
    }

    /**
     * 默认的重发业务人脸识别邮件
     *
     * @param userId
     * @param faceTransType
     * @param faceLog
     */
    private void defaultResendFaceEmail(Long userId, FaceTransType faceTransType, TransactionFaceLog faceLog) {
        if (faceLog == null || TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
            log.info("当前用户没有需要做人脸识别的流程. userId:{} faceTransType:{}", userId, faceTransType);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        String transId = faceLog.getTransId();
        if (TransFaceLogStatus.REVIEW == faceLog.getStatus()) {
            log.info("当前人脸识别业务正在审核中的过程，不能发送人脸识别邮件. userId:{} transId:{}", userId, transId);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_REVIEW_CANNOT_EMAIL);
        }
        // 限制下五分钟内只能发送一次
        String cacheKey = emailResendTimesCacheKey(transId);
        String cacheNum = RedisCacheUtils.get(cacheKey);
        if (StringUtils.isNotBlank(cacheNum)) {
            log.info("缓存中存在上次发送的记录，五分钟内只能发一次, 不能再发送. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_MINUTE_EMAIL);
        }
        LanguageEnum language = null;
        if (WebUtils.getHttpServletRequest() != null) {
            language = WebUtils.getAPIRequestHeader() != null ? WebUtils.getAPIRequestHeader().getLanguage() : null;
        }
        if (language == null) {
            language = emailNotifyLanguage(userId);
        }
        String link = emailNotifyLink(faceLog.getTransId(), faceTransType, language);
        String result = sendFaceNotifyEmail(userId, faceLog.getTransId(), faceLog, faceTransType, language, link);
        if (StringUtils.isNotBlank(result)) {
            log.info("发送邮件失败: userId:{} transId:{} message:{}", userId, faceLog.getTransId(), result);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        } else {
            // 如果发送成功，做一个缓存，限制五分钟内只能发送一次邮件
            RedisCacheUtils.set(cacheKey, transId, 300);
        }
    }


    /**
     * 初始化一个人脸识别的业务
     * 如果已经存在对应业务编号的数据，那么则检查是否处于待验证状态，如果不是，直接报错。
     * 获取锁失败或者入库失败时返回null
     *
     * @return 初始化成功后返回对用的初始化记录
     */
    protected TransactionFaceLog generateTransFaceLog(FaceTransType faceTransType, Long userId, String transId, boolean isKycLockOne) throws Exception {
        if (faceTransType == null || userId == null || StringUtils.isBlank(transId)) {
            log.warn("请求参数错误. userId:{} transId:{} type:{}", userId, transId, faceTransType);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 发起人脸识别流程之前，需要先校验对比照片是否存在，如果不存在，则不能发起, 这步由于数据同步的问题，需要直接从主库查询
        UserFaceReference userFaceReference = iFace.getUserFaceByMasterBD(userId);
        if (userFaceReference == null || StringUtils.isAllBlank(userFaceReference.getRefImage(), userFaceReference.getCheckImage())) {
            log.info("获取当前用户的人脸识别对比照信息失败，不能进行初始化人脸识别: userId:{}", userId);
            throw new BusinessException(AccountErrorCode.FACE_VERIFICATION_MISS_REF_IMAGE);
        }
        return directGenerateFaceLog(userId, transId, faceTransType, isKycLockOne);
    }

    /**
     * 强制直接创建人脸识别流程，不做检查，需要提前做好检查
     *
     * @param userId
     * @param transId
     * @param faceTransType
     * @param isKycLockOne 是否为KYC认证中锁定具体某一个用户的认证
     * @return
     * @throws Exception
     */
    public TransactionFaceLog directGenerateFaceLog(Long userId, String transId, FaceTransType faceTransType, boolean isKycLockOne) throws Exception {
        Lock lock = RedisCacheUtils.getLock(String.format(INIT_TRANS_FACE_USER_LOCK, faceTransType.name(), userId));
        if (lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
            try {
                // 先查询下是否已经有当前业务编号的记录信息，如果已经有，则不再新建
                TransactionFaceLog faceLog = transactionFaceLogMapper.findByUserIdTransId(userId, transId, faceTransType.name());
                if (faceLog == null) {
                    //创建一个新的出来
                    TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
                    transactionFaceLog.setUserId(userId);
                    transactionFaceLog.setTransId(transId);
                    transactionFaceLog.setTransType(faceTransType.name());
                    transactionFaceLog.setStatus(TransFaceLogStatus.INIT);
                    transactionFaceLog.setCreateTime(DateUtils.getNewUTCDate());
                    transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
                    transactionFaceLog.setKycLockOne(isKycLockOne);
                    transactionFaceLogMapper.insert(transactionFaceLog);
                    if (transactionFaceLog.getId() == null || transactionFaceLog.getId() <= 0) {
                        log.warn("初始化人脸识别业务信息失败. userId:{} transId:{} type:{}", userId, transId, faceTransType);
                        return null;
                    }
                    return transactionFaceLog;
                } else if (faceLog.getStatus() == TransFaceLogStatus.INIT || faceLog.getStatus() == TransFaceLogStatus.PENDING) {
                    // 如果已经有了，并且状态不是最终状态，直接返回
                    return faceLog;
                } else {
                    log.warn("当前业务的人脸识别已经处于终态，不能再次进行初始化. userId:{} transId:{} type:{} status:{}",
                            userId, transId, faceTransType, faceLog.getStatus());
                    throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
                }
            } finally {
                lock.unlock();
            }
        } else {
            log.warn("初始化人脸识别业务信息失败. userId:{} transId:{} type:{}", userId, transId, faceTransType);
            return null;
        }
    }

    /**
     * 发送人脸识别通知邮件通知
     *
     * @param userId
     * @param transId
     * @param transactionFaceLog
     * @param faceTransType
     * @return 如果发送失败，返回对应的原因
     */
    protected String sendFaceNotifyEmail(Long userId, String transId, TransactionFaceLog transactionFaceLog,
                                         FaceTransType faceTransType, LanguageEnum language, String link) {
        String sendResult = null;
        boolean sendSuccess = true;
        User user = getUserByUserId(userId);
        String template = emailNotifyTemplate();
        if (StringUtils.isBlank(template)) {
            log.info("没有配置邮件通知模板不发生人脸识别通知邮件. userId:{} transId:{} transType:{}", userId, transId, faceTransType);
            return null;
        }
        try {
            // 发送提醒邮件
            Map<String, Object> data = Maps.newHashMap();
            if (StringUtils.isNotBlank(link)) {
                // 如果link 为空时相当于时纯通知的邮件，没有跳转连接
                data.put("link", link);
            }
            String remark = faceTransType.name() + "人脸识别通知";
            userCommonBusiness.sendEmailWithoutRequest(template, user, data, remark, language);
            sendResult = "已发送人脸识别通知邮件";
        } catch (Exception e) {
            log.error("发送人脸识别通知邮件失败. userId:{} transId:{}", userId, transId, e);
            sendResult = e.getMessage();
            sendSuccess = false;
        }
        log.info("发送人脸识别通知邮件结果: userId:{} transId:{} result:{}", userId, transId, sendResult);
        transactionFaceLog.setStatus(TransFaceLogStatus.PENDING);
        transactionFaceLog.setFailReason(sendResult);
        transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
        transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
        return sendSuccess ? null : sendResult;
    }

    /**
     * 邮件通知使用的人脸识别连接
     *
     * @param transId
     * @param faceTransType
     * @param language
     * @return
     */
    protected String emailNotifyLink(String transId, FaceTransType faceTransType, LanguageEnum language) {
        String params = String.format("?id=%s&type=%s", transId, faceTransType.getCode());
        return iFace.getFaceApiPath(null, apolloCommonConfig.getFaceEmailLinkApi(), language) + params;
    }

    /**
     * 邮件通知的使用语言，默认取的是用户最后一次登录使用的语言
     *
     * @param userId
     * @return
     */
    protected LanguageEnum emailNotifyLanguage(Long userId) {
        if (WebUtils.getHttpServletRequest() != null) {
            return WebUtils.getAPIRequestHeader().getLanguage();
        }else {
            return userCommonBusiness.getLastLoginLanguage(userId);
        }
    }

    /**
     * 用于限制邮件重发次数的缓存KEY
     *
     * @param transId
     * @return
     */
    protected String emailResendTimesCacheKey(String transId) {
        return String.format(RESEND_EMAIL_TIMES_CACHE, transId);
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    protected User getUserByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        User user = userMapper.queryByEmail(userIndex.getEmail());
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 验证是否当前人脸识别流程是否可以做人脸识别
     *
     * @param userId
     * @param transId
     * @param faceTransType
     * @return 返回验证通过的人脸识别流程信息
     */
    public TransactionFaceLog validateCanDoFace(Long userId, String transId, FaceTransType faceTransType) {
        log.info("验证人脸识别流程是否处于可验证的状态: userId:{} transId:{} faceTransType:{}", userId, transId, faceTransType);
        TransactionFaceLog faceLog = transactionFaceLogMapper.findByUserIdTransId(userId, transId, faceTransType.name());
        if (faceLog == null || userId == null || userId.longValue() != faceLog.getUserId().longValue()) {
            log.info("获取对应的业务记录失败. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        return this.validateCanDoFace(userId, transId, faceTransType, faceLog);
    }

    /**
     * 验证人脸识别是否可以进行操作
     *
     * @param userId
     * @param transId
     * @param transType
     * @param transactionFaceLog
     * @return 返回校验完成的人脸识别流程信息
     */
    private TransactionFaceLog validateCanDoFace(Long userId, String transId, FaceTransType transType, TransactionFaceLog transactionFaceLog) {
        if (transactionFaceLog == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        //判断状态是否正确
        if (!Objects.equals(TransFaceLogStatus.PENDING, transactionFaceLog.getStatus()) && !Objects.equals(TransFaceLogStatus.INIT, transactionFaceLog.getStatus())) {
            log.info("当前状态不能操作人脸识别. userId：{} transId:{}", userId, transId);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        //验证二十四小时内是否达到指定的次数，如果达到了，则返回对应的错误
        String source = null;
        if (WebUtils.getHttpServletRequest() != null) {
            // 如果请求的来源不同，进行区分下来源，这样可以在不同来源上做多次不一样的请求
            APIRequestHeader header = WebUtils.getAPIRequestHeader();
            if(header == null) {
            	source = null;
            }else {
            	source = header.getTerminal() == null ? null : header.getTerminal().name();
            }
        }
        int count = iFace.getFaceLogDailyTimes(userId, transId, transType, source);
        int limit = apolloCommonConfig.getFaceDailyLimit();
        if (count >= limit) {
            log.info("24小时内的验证次数超出限制次数. userId:{} transId:{}", userId, transId);
            throw new BusinessException(AccountErrorCode.FACE_VERIFICATION_DAILY_TIMES);
        }
        return transactionFaceLog;
    }

    public TransactionFaceLog getByMasterdb(String transId, FaceTransType transType) {
        if (StringUtils.isBlank(transId) || transType == null) {
            return null;
        }
        HintManager hintManager = null;
        TransactionFaceLog transactionFaceLog = null;
        try {
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            transactionFaceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
        return transactionFaceLog;
    }

    /**
     * PC 端人脸识别初始化
     *
     * @param transId
     * @param transType
     */
    public FaceInitResponse facePcInit(String transId, FaceTransType transType) {
        TransactionFaceLog faceLog = getByMasterdb(transId, transType);
        if (faceLog == null) {
            log.warn("获取人脸识别认证流程信息失败. transId:{} transType:{}", transId, transType);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        Long userId = faceLog.getUserId();
        // 验证业务流程和人脸识别流程是否允许进行人脸识别
        faceLog = validateCanDoFace(userId, transId, transType);
        log.info("开始初始化PC端人脸识别: userId:{} transId:{} transType:{}", userId, transId, transType);
        LanguageEnum language = LanguageEnum.EN_US;
        String baseUrl = null;
        if (WebUtils.getHttpServletRequest() != null) {
            language = WebUtils.getAPIRequestHeader().getLanguage();
            baseUrl = WebUtils.getHeader(Constant.BASE_URL);
        }
        if (language != LanguageEnum.ZH_CN) {
            language = LanguageEnum.EN_US;
        }
        String imageRef = getFaceCheckImage(userId);
        FaceWebInitResponse webInitResponse = iFace.faceWebInitHandler(userId, transId, transType, imageRef, false, true, language, baseUrl);
        if (webInitResponse == null || !webInitResponse.isSuccess()) {
            log.info("PC 端人脸识别初始化失败. userId:{} transId:{}", userId, transId);
            String message = webInitResponse == null || webInitResponse.getErrorCode() == null ? null : webInitResponse.getErrorCode().getMessage();
            saveFaceResult(faceLog, null, FaceStatus.GET_TOKEN_FAIL.name(), message);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
        } else {
            log.info("PC 端人脸识别初始化成功. userId:{} transId:{}", userId, transId);
            saveFaceResult(faceLog, null, FaceStatus.FACE_PENDING.name(), "等待PC端人脸识别验证");
            FaceInitResponse response = new FaceInitResponse();
            response.setBizNo(webInitResponse.getBizNo());
            response.setLivenessUrl(webInitResponse.getLivenessUrl());
            response.setUserId(userId);
            response.setTransId(transId);
            response.setKycLockOne(faceLog.isKycLockOne());
            return response;
        }
    }
    
    /**
     * PC 端人脸识别初始化
     *
     * @param transId
     * @param transType
     */
    public FaceInitResponse facePrivateInit(String transId, FaceTransType transType) {
        TransactionFaceLog faceLog = getByMasterdb(transId, transType);
        if (faceLog == null) {
            log.warn("获取人脸识别认证流程信息失败. transId:{} transType:{}", transId, transType);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        Long userId = faceLog.getUserId();
        // 验证业务流程和人脸识别流程是否允许进行人脸识别
        faceLog = validateCanDoFace(userId, transId, transType);
        log.info("开始初始化PC端私有云人脸识别: userId:{} transId:{} transType:{}", userId, transId, transType);
        LanguageEnum language = LanguageEnum.EN_US;
        String baseUrl = null;
        if (WebUtils.getHttpServletRequest() != null) {
        	String lang = WebUtils.getHeader("lang");
        	log.info("开始初始化PC端私有云人脸识别 head头获取语言: userId:{} transId:{} transType:{},lang:{}", userId, transId, transType,lang);
            language = StringUtils.isBlank(lang) ? language : LanguageEnum.findByLang(lang.toLowerCase());
            log.info("开始初始化PC端私有云人脸识别 head头获取语言: userId:{} transId:{} transType:{},lang:{}", userId, transId, transType,language.getCode());
            baseUrl = WebUtils.getHeader(Constant.BASE_URL);
        }
        if (!LanguageEnum.ZH_CN.equals(language)) {
            language = LanguageEnum.EN_US;
        }
        log.info("开始初始化PC端私有云人脸识别 head头获取语言: userId:{} transId:{} transType:{},lang:{}", userId, transId, transType,language.getCode());
        String imageRef = getFaceCheckImage(userId);
        FaceWebGetTokenResponse webInitResponse = iFace.faceWebInitPrivateHandler(userId, transId, transType, imageRef, false, true, language, baseUrl);
        if (webInitResponse == null || StringUtils.isBlank(webInitResponse.getLivenessUrl())) {
            log.info("PC 端私有云人脸识别初始化失败. userId:{} transId:{}", userId, transId);
            saveFaceResult(faceLog, null, FaceStatus.GET_TOKEN_FAIL.name(), "初始化token失败");
            throw new BusinessException(GeneralCode.AC_RESET_FACE_TOKEN_FAIL);
        } else {
            log.info("PC 端私有云人脸识别初始化成功. userId:{} transId:{}", userId, transId);
            saveFaceResult(faceLog, null, FaceStatus.FACE_PENDING.name(), "等待PC端人脸识别验证");
            FaceInitResponse response = new FaceInitResponse();
            response.setLivenessUrl(webInitResponse.getLivenessUrl());
            response.setUserId(userId);
            response.setTransId(transId);
            response.setKycLockOne(faceLog.isKycLockOne());
            return response;
        }
    }

    /**
     * SDK 端人脸识别初始化（初始化二维码）
     *
     * @param transId
     * @param transType
     * @return
     */
    public FaceInitResponse faceSdkInit(String transId, FaceTransType transType) {
        TransactionFaceLog faceLog = getByMasterdb(transId, transType);
        if (faceLog == null) {
            log.warn("获取人脸识别认证流程信息失败. transId:{} transType:{}", transId, transType);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        Long userId = faceLog.getUserId();
        faceLog = validateCanDoFace(userId, transId, transType);
        String qrCode = iFace.initFaceSdkQrCode(userId, transId, transType);
        log.info("人脸识别SDK端初始化二维码: transId:{} userId:{} transType:{}", transId, userId, transType);
        FaceInitResponse response = new FaceInitResponse();
        response.setQrCode(qrCode);
        response.setTransId(transId);
        response.setUserId(userId);
        response.setKycLockOne(faceLog.isKycLockOne());
        response.setQrCodeValidSeconds(apolloCommonConfig.getQrCodeValidSecond());
        if(!FaceStatus.FACE_PENDING.name().equals(faceLog.getFaceStatus())) {
        	saveFaceResult(faceLog, null, FaceStatus.FACE_PENDING.name(), "等待SDK端人脸识别验证");
        }
        return response;
    }

    /**
     * 修改保存人脸识别流程的状态信息
     *
     * @param faceLog
     * @param status
     * @param faceStatus
     * @param faceRemark
     * @return
     */
    private int saveFaceResult(TransactionFaceLog faceLog, TransFaceLogStatus status, String faceStatus, String faceRemark) {
        if (status != null) {
            faceLog.setStatus(status);
        }
        faceLog.setFaceStatus(faceStatus);
        faceLog.setFaceRemark(faceRemark);
        faceLog.setUpdateTime(DateUtils.getNewUTCDate());
        return transactionFaceLogMapper.updateByPrimaryKeySelective(faceLog);
    }

    /**
     * 获取做人脸识别的对比照片路径(默认是优先取正式的对比照，如果取不到再取临时的对比照)
     * 如果只能取正式的对比照，需要重写该方法
     *
     * @param userId
     * @return
     */
    protected String getFaceCheckImage(Long userId) {
        UserFaceReference faceReference = userFaceReferenceMapper.selectByPrimaryKey(userId);
        if (faceReference == null || StringUtils.isBlank(faceReference.getCheckImage())) {
            log.info("获取对比照片信息失败. userId:{}", userId);
            throw new BusinessException(AccountErrorCode.FACE_VERIFICATION_MISS_REF_IMAGE);
        }
        // 对比源图片，默认用当前正在进行申请流程的人脸对比图
        return faceReference.getCheckImage();
    }

    /**
     * PC 端人脸识别验证结果处理
     *
     * @param transId
     * @param transType
     * @param faceWebResult
     * @return
     */
    public FacePcResponse facePcResultHandler(String transId, FaceTransType transType, FaceWebResultResponse faceWebResult) {
        log.info("PC端人脸识别验证结果: transId:{} transType:{} result:{}", transId, transType, JSON.toJSONString(faceWebResult));
        FacePcResponse facePcResponse = new FacePcResponse();
        facePcResponse.setTransId(transId);
        facePcResponse.setFaceBizNo(faceWebResult != null ? faceWebResult.getBizNo() : null);
        // 这两个值可能时空值，当为空值的时候需要按原来的语言和默认配置来处理
        LanguageEnum language = null;
        String baseUrl = null;
        language = faceWebResult == null ? null : faceWebResult.getBaseLang();
        if (language == null) {
            try {
                language = WebUtils.getAPIRequestHeader().getLanguage();
            }catch (Exception e) {
                language = LanguageEnum.EN_US;
            }
        }
        baseUrl = faceWebResult == null ? null : faceWebResult.getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            try {
                baseUrl = WebUtils.getHeader(Constant.BASE_URL);
            }catch (Exception e) {
                // do nothing
            }
        }
        if (StringUtils.isBlank(transId) || transType == null || faceWebResult == null) {
            log.warn("PC端人脸识别验证结果异常信息.transId:{} transType:{} result:{}", transId, transType, JSON.toJSONString(faceWebResult));
            String url = getPcVerifyRedirectUrl(false, null, null, language, baseUrl);
            facePcResponse.setRedirectPath(url);
            facePcResponse.setSuccess(false);
            facePcResponse.setFaceRemark("PC段人脸识别验证结果异常");
            return facePcResponse;
        }
        try {
            TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
            if (faceLog == null) {
                log.info("PC端的人脸识别验证结果中根据业务标识查询不到对应的业务记录. transId:{} transType:{}", transId, transType);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            Long userId = faceLog.getUserId();
            facePcResponse.setUserId(userId);
            if (TransFaceLogStatus.isEndStatus(faceLog.getStatus()) || TransFaceLogStatus.REVIEW == faceLog.getStatus()) {
                log.info("PC端人脸识别结果验证后但是业务状态不是处理中，不能再变更. userId:{} transId:{} transType:{}", userId, transId, transType);
                throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
            }
            String url = getPcVerifyRedirectUrl(faceWebResult.isSuccess(), transId, transType, language, baseUrl);
            facePcResponse.setRedirectPath(url);
            facePcResponse.setSuccess(faceWebResult.isSuccess());
            facePcResponse.setKycLockOne(faceLog.isKycLockOne());
            if (faceWebResult.isSuccess()) {
                String message = "PC端人脸识别通过";
                String review = needFaceReview(userId, transId, transType, faceLog.getCreateTime());
                log.info("PC端人脸识别通过. userId:{} transId:{} type:{} review:{}", userId, transId, transType, review);
                if (StringUtils.isNotBlank(review)) {
                    // 落入人工审核
                    faceLog.setFailReason(review);
                    saveFaceResult(faceLog, TransFaceLogStatus.REVIEW, FaceStatus.FACE_PASS.name(), message);
                } else {
                    saveFaceResult(faceLog, TransFaceLogStatus.PASSED, FaceStatus.FACE_PASS.name(), message);
                }
            } else {
                String message = faceWebResult.getErrorCode() == null ? "PC端人脸识别失败" : faceWebResult.getErrorCode().getMessage();
                log.info("PC端人脸识别未通过. userId:{} transId:{} type:{} message:{}", userId, transId, transType, message);
                saveFaceResult(faceLog, null, FaceStatus.FACE_FAIL.name(), message);
            }
            // 设置下业务流程的状态，方便下级判断
            facePcResponse.setStatus(faceLog.getStatus());
            facePcResponse.setFaceRemark(faceLog.getFaceRemark());
        } catch (Exception e) {
            log.warn("PC端人脸识别验证结果处理失败. transId:{} transType:{}", transId, transType, e.getMessage());
            String url = getPcVerifyRedirectUrl(false, transId, transType, language, baseUrl);
            facePcResponse.setRedirectPath(url);
            facePcResponse.setSuccess(false);
            facePcResponse.setFaceRemark("PC端人脸识别验证结果处理失败");
        }
        return facePcResponse;
    }
    /**
     * face 私有云结果通知
     * @param transType
     * @param result
     * @return
     */
    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
    	String transId = result.getTransId();
    	log.info("PC端私有云人脸识别验证结果: transId:{} transType:{} result:{}", transId, transType, result);
    	if (StringUtils.isBlank(transId) || transType == null || result == null) {
    		log.warn("PC端私有云人脸识别验证结果异常信息.transId:{} transType:{} result:{}", transId, transType, result);
    		return null;
    	}
    	try {
            TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, transType.name());
            if (faceLog == null) {
                log.info("PC端的人脸识别验证结果中根据业务标识查询不到对应的业务记录. transId:{} transType:{}", transId, transType);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            Long userId = faceLog.getUserId();
            if (TransFaceLogStatus.isEndStatus(faceLog.getStatus()) || TransFaceLogStatus.REVIEW == faceLog.getStatus()) {
                log.info("PC端私有云人脸识别结果验证后但是业务状态不是处理中，不能再变更. userId:{} transId:{} transType:{}", userId, transId, transType);
                throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
            }
            
            if (result.isSuccess()) {
            	String message = "PC端私有云人脸识别通过";
                String review = needFaceReview(userId, transId, transType, faceLog.getCreateTime());
                log.info("PC端私有云人脸识别通过. userId:{} transId:{} type:{} review:{}", userId, transId, transType, review);
                if (StringUtils.isNotBlank(review)) {
                    // 落入人工审核
                    faceLog.setFailReason(review);
                    saveFaceResult(faceLog, TransFaceLogStatus.REVIEW, FaceStatus.FACE_PASS.name(), message);
                } else {
                    saveFaceResult(faceLog, TransFaceLogStatus.PASSED, FaceStatus.FACE_PASS.name(), message);
                }
            }else {
                String message = result.getErrorMessage() == null ? "PC端私有云人脸识别失败" : result.getErrorMessage();
                log.info("PC端私有云人脸识别未通过. userId:{} transId:{} type:{} message:{}", userId, transId, transType, message);
                saveFaceResult(faceLog, null, FaceStatus.FACE_FAIL.name(), message);
            }
            return faceLog;
    	}catch (Exception e) {
            log.warn("PC端私有云人脸识别验证结果处理失败. transId:{} transType:{}", transId, transType, e.getMessage());
            return null;
    	}
    }

    /**
     * 判断人脸识别失败时是否会落入人工审核的过程
     *
     * @param createTime 业务可以开始做人脸识别的时间
     * @return 如果返回空值，则不需要进入人工审核，如果返回的不是空值，返回具体原因
     */
    protected String needFaceReview(Long userId, String transId, FaceTransType transType, Date createTime) {
 
    	if(!apolloCommonConfig.isFaceReviewSwitch()) {
    		return null;
    	}
    	
        // 把这个用户做过的所有人脸识别记录拉出来，进行统计
        List<FaceLogVo> faceLogVoList = iFace.getFaceLogsByUser(userId, transId, transType, null);
        if (faceLogVoList == null || faceLogVoList.isEmpty()) {
            log.warn("获取不到用户操作人脸识别的流水信息: userId:{} transId:{} type:{}", userId, transId, transType);
            return null;
        }
        boolean haveCheat = false;
        int lowTimes = 0;
        boolean isLongTime = false;
        int limitHours = apolloCommonConfig.getFaceReviewHours();
        for (FaceLogVo faceLogVo : faceLogVoList) {
            if (!isLongTime && faceLogVo.getFaceStatus() == FaceStatus.FACE_PASS) {
                // 如果是通过的，检验通过的时间和初始化的时间是否超出限制
                Date compareTime = DateUtils.addHours(createTime, limitHours);
                if (DateUtils.getNewUTCDate().compareTo(compareTime) > 0) {
                    isLongTime = true;
                }
            }
            FaceErrorCode faceErrorCode = FaceErrorCode.getByName(faceLogVo.getFaceErrorCode());
            if (faceErrorCode == null) {
                continue;
            }
            switch (faceErrorCode) {
                case FACE_VERIFY_SYNTHRTIC:
                case FACE_VERIFY_MASK:
                case FACE_VERIFY_SCREEN_REPLAY:
                case FACE_VERIFY_FACE_REPLACED:
                    haveCheat = true;
                    // 不能break
                case FACE_VERIFY_CONFIDENCE_LOW:
                    lowTimes++;
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        // 是否有攻击行为
        if (haveCheat) {
            return "疑似攻击进入人工审核";
        }
        // 置信度低于设置的阈值时，看看是否达到置信度的错误次数指定次数
        int limit = apolloCommonConfig.getFaceReviewLimit();
        if (lowTimes > limit) {
            return "置信度底错误次数大于" + limit + "次进入人工审核";
        }
        // 通过的情况下，是否超过限制时长
        if (isLongTime) {
            return "从可人脸认证时间开始超" + limitHours + "小时才验证完成";
        }
        return null;
    }

    /**
     * 默认使用的PC端人脸识别跳转URL
     *
     * @param isPassed
     * @param transId
     * @param transType
     * @param language
     * @param baseUrl 允许空，当为空时按原来的逻辑取配置值处理
     * @return
     */
    protected String getPcVerifyRedirectUrl(boolean isPassed, String transId, FaceTransType transType, LanguageEnum language, String baseUrl) {
        String redirectPath = apolloCommonConfig.getFaceWebRedirectPath();
        if (transType != null && StringUtils.equalsAnyIgnoreCase(transType.name(), FaceTransType.KYC_COMPANY.name(), FaceTransType.KYC_USER.name())) {
            // KYC 认证的跳转地址不一样
            redirectPath = apolloCommonConfig.getFaceWebRedirectKycPath();
        }else if (transType != null && StringUtils.equalsIgnoreCase(transType.name(), FaceTransType.RESET_EMAIL.name())) {
            // 重置邮箱的落地页也重新配置
            redirectPath = apolloCommonConfig.getFaceWebRedirectResetEmail();
        }
        if (StringUtils.isBlank(transId) || transType == null) {
            //空值时直接返回重置的失败连接
            log.info("PC端WEB 人脸识别结果种缺失transId或者type.");
            return iFace.getFaceApiPath(baseUrl, redirectPath + "?status=0", language);
        }
        StringBuilder redirectApi = new StringBuilder();
        redirectApi.append(redirectPath);
        if (isPassed) {
            redirectApi.append("?status=1");
        } else {
            redirectApi.append("?status=0");
        }
        redirectApi.append("&type=");
        redirectApi.append(transType.getCode());
        redirectApi.append("&id=");
        redirectApi.append(transId);
        String path = iFace.getFaceApiPath(baseUrl, redirectApi.toString(), language);
        log.info("WEB人脸验证结果跳转路径: transId:{} type:{} redirectPath:{}", transId, transType, path);
        return path;
    }

    /**
     * SDK 人脸识别结果处理
     *
     * @param transId
     * @param userId
     * @param faceTransType
     * @param request
     * @return
     */
    public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType, FaceSdkVerifyRequest request) {
        log.info("SDK端人脸识别认证: transId:{} userId:{} faceTransType:{}", transId, userId, faceTransType);
        FaceSdkResponse response = new FaceSdkResponse();
        response.setUserId(userId);
        response.setTransId(transId);
        if (transId == null || userId == null || faceTransType == null || request == null) {
            log.info("SDK验证获取业务信息值失败. userId:{} transId:{} transType:{}", userId, transId, faceTransType);
            FaceTransTypeContentVo contentVo = iFace.getTransTypeContent(faceTransType, false);
            response.setSuccess(false);
            response.setMessage(GeneralCode.AC_RESET_FACE_SDK_QR_TIMEOUT.getMessage());
            response.setTitle(contentVo.getTitle());
            response.setContent(contentVo.getContent());
            response.setKycLockOne(false);
            return response;
        }
        try {
            // 先验证下当前业务是否可以做人脸识别
            TransactionFaceLog transactionFaceLog = validateCanDoFace(userId, transId, faceTransType);
            response.setKycLockOne(transactionFaceLog.isKycLockOne());
            log.info("人脸识别SDK端验证开始: userId:{} transId:{} transType:{}", userId, transId, faceTransType);
            // 获取对比照片
            String imageRef = getFaceCheckImage(userId);
            // 进行结果验证
            FaceSDKVerifyResponse sdkVerifyResponse =
                    iFace.faceSdkVerify(userId, transId, faceTransType, imageRef, false, request);
            if (sdkVerifyResponse.getErrorCode() == null
                    && StringUtils.equalsIgnoreCase(FaceStatus.FACE_PASS.name(), sdkVerifyResponse.getFaceStatus())) {

                String review = needFaceReview(userId, transId, faceTransType, transactionFaceLog.getCreateTime());
                log.info("SDK人脸识别通过, userId:{} transId:{} type:{} review:{}", userId, transId, faceTransType, review);
                if (StringUtils.isNotBlank(review)) {
                    // 需要落入人工审核
                    transactionFaceLog.setFailReason(review);
                    saveFaceResult(transactionFaceLog, TransFaceLogStatus.REVIEW, sdkVerifyResponse.getFaceStatus(), sdkVerifyResponse.getFaceRemark());
                } else {
                    saveFaceResult(transactionFaceLog, TransFaceLogStatus.PASSED, sdkVerifyResponse.getFaceStatus(), sdkVerifyResponse.getFaceRemark());
                }
                response.setSuccess(true);
                response.setMessage(sdkVerifyResponse.getFaceRemark());
                response.setFaceBizNo(sdkVerifyResponse.getBizNo());
            } else {
                String remark = sdkVerifyResponse.getFaceRemark();
                log.info("SDK人脸识别未通过, userId:{} transId:{} message:{}", userId, transId, remark);
                saveFaceResult(transactionFaceLog, null, sdkVerifyResponse.getFaceStatus(), remark);
                response.setSuccess(false);
                response.setMessage(remark);
                response.setFaceBizNo(sdkVerifyResponse.getBizNo());
            }
            // 设置下业务状态，方便下级使用
            response.setStatus(transactionFaceLog.getStatus());
            response.setKycLockOne(transactionFaceLog.isKycLockOne());
        } catch (BusinessException e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
        }
        FaceTransTypeContentVo contentVo = iFace.getTransTypeContent(faceTransType, response.isSuccess());
        response.setTitle(contentVo.getTitle());
        response.setContent(contentVo.getContent());
        log.info("SDK 端人脸识别验证的结果: userId:{} transId:{} result:{}", userId, transId, JSON.toJSONString(response));
        return response;
    }

    /**
     * 由于照片有问题需要重传照片的逻辑
     *
     * @param transId
     * @param userId
     * @param faceTransType
     * @param isLockOne 是否新版本的一个用户锁定一条KYC认证
     */
    public void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne) {
        log.info("由于照片有问题需要重新上传照片的处理: userId:{} transId:{}, faceTransType:{}", userId, transId, faceTransType);
        // 默认情况什么都不做，如果需要清理流程，在对应子类中实现
    }

    /**
     * 人脸识别认证流程审核
     *
     * @param auditRequest
     */
    public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransFaceLogStatus auditStatus = auditRequest.getStatus();
        String transId = auditRequest.getTransId();
        Long userId = auditRequest.getUserId();
        // 检验是否符合审核的条件
        TransactionFaceLog faceLog = transFaceAuditPreCheck(auditRequest, faceTransType);
        // 直接把状态信息修改到审核成功或者拒绝状态，并且输入对应的错误原因
        faceLog.setStatus(auditStatus);
        faceLog.setFailReason(auditRequest.getFailReason());
        faceLog.setUpdateTime(DateUtils.getNewUTCDate());
        int count = transactionFaceLogMapper.updateByPrimaryKeySelective(faceLog);
        if (count <= 0) {
            log.warn("变更人脸识别流程状态失败. userId:{} transId:{} type:{}", userId, transId, faceTransType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return faceLog;
    }

    /**
     * 检查人脸识别业务审核条件
     *
     * @param auditRequest
     * @param faceTransType
     * @return
     */
    private TransactionFaceLog transFaceAuditPreCheck(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransFaceLogStatus auditStatus = auditRequest.getStatus();
        String transId = auditRequest.getTransId();
        Long userId = auditRequest.getUserId();
        if (auditStatus != TransFaceLogStatus.PASSED && auditStatus != TransFaceLogStatus.FAIL) {
            log.info("人脸识别审核的状态只能是通过或者拒绝. userId:{} transId:{} type:{} auditStatus:{}", userId, transId, faceTransType, auditRequest);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "只能是审核通过或者拒绝状态");
        }
        // 如果是审核拒绝，需要输入拒绝原因
        if (auditStatus == TransFaceLogStatus.FAIL && StringUtils.isBlank(auditRequest.getFailReason())) {
            log.info("审核拒绝下必须输入拒绝原因; userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "审核拒绝情况下拒绝原因必输.");
        }
        TransactionFaceLog faceLog = transactionFaceLogMapper.findByUserIdTransId(userId, transId, faceTransType.name());
        if (faceLog == null || faceLog.getUserId().longValue() != auditRequest.getUserId().longValue()) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (faceLog.getStatus() != TransFaceLogStatus.REVIEW) {
            log.info("业务流程不处于审核状态，不能进行审核. userId:{} transId:{} currentStatus:{}", userId, transId, faceLog.getStatus());
            throw new BusinessException(GeneralCode.SYS_ERROR, "流程不处于审核状态");
        }
        return faceLog;
    }
}
