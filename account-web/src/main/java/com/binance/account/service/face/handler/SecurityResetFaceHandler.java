package com.binance.account.service.face.handler;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.face.FaceHandlerType;
import com.binance.account.service.security.IUserSecurityReset;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 重置流程人脸识别处理器
 *
 * @author liliang1
 * @date 2019-02-28 14:00
 */
@Log4j2
@Component
@FaceHandlerType(values = {FaceTransType.RESET_GOOGLE, FaceTransType.RESET_MOBIEL, FaceTransType.RESET_UNLOCK})
public class SecurityResetFaceHandler extends AbstractFaceHandler {

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private IUserSecurityReset iUserSecurityReset;

    @Override
    public FaceFlowInitResult initTransFace(String transId, Long userId, FaceTransType transType, boolean needEmail, boolean isKycLockOne) {
        UserSecurityResetType resetType = UserSecurityResetType.getByName(transType.getCode());
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (reset == null || resetType != reset.getType()) {
            log.warn("获取对应的重置流程失败, userId:{} transId:{} faceTransType:{}", userId, transId, transType);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 验证下当前记录是否符合做人脸识别的状态条件
        if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            log.info("重置流程已经处于终态，不能再进行人脸识别. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_STATUS_UNDO);
        }
        try {
            // 直接发起初始化人脸识别，如果能初始化成功，则完成初始化
            TransactionFaceLog faceLog = generateTransFaceLog(transType, userId, transId, isKycLockOne);
            if (faceLog == null) {
                log.warn("初始化重置流程人脸识别结果为null. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            if (needEmail) {
                // 如果能初始化成功，发送通知邮件，登记状态信息
                LanguageEnum language = emailNotifyLanguage(userId);
                String link = emailNotifyLink(userId, transId, language, reset);
                sendFaceNotifyEmail(userId, transId, faceLog, transType, language, link);
            }
            saveFaceStatus(reset, FaceStatus.FACE_PENDING, "已通知待人脸识别", null);
            FaceFlowInitResult result = new FaceFlowInitResult();
            result.setType(transType.getCode());
            result.setTransId(transId);
            return result;
        } catch (BusinessException e) {
            log.warn("初始化重置流程人脸识别失败. userId:{} transId:{} message:{}", userId, transId, e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("初始化重置流程人脸识别异常. userId:{}, transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    @Override
    protected String emailNotifyTemplate() {
        return Constant.USER_2FA_RESET_FACE_NOTIFY;
    }

    @Override
    public boolean isFacePassed(String transId, FaceTransType faceTransType) {
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        UserSecurityResetType resetType = UserSecurityResetType.getByName(faceTransType.getCode());
        if (reset == null || resetType != reset.getType()) {
            log.info("获取重置流程信息失败. transId:{} type:{}", transId, faceTransType);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        switch (reset.getStatus()) {
            case passed:
                throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_SUCCESS);
            case refused:
            case cancelled:
                throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_FAIL);
            default:
                return super.isFacePassed(transId, faceTransType);
        }
    }

    @Override
    public void resendFaceEmailByEmail(String email, FaceTransType faceTransType) {
        UserSecurityResetType resetType = UserSecurityResetType.getByName(faceTransType.getCode());
        if (resetType == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        User user = userMapper.queryByEmail(email);
        if (user == null) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        Long userId = user.getUserId();
        UserSecurityReset reset = userSecurityResetMapper.getLastByUserId(userId, resetType.ordinal());
        if (reset == null) {
            log.info("获取重置流程信息失败. userId:{} type:{}", userId, resetType);
            throw new BusinessException(GeneralCode.AC_RESET_RECORD_MISS);
        }
        String transId = reset.getId();
        processSendFaceEmail(userId, transId, reset, faceTransType);
    }

    @Override
    public void resendFaceEmailByTransId(String transId, FaceTransType faceTransType) {
        UserSecurityResetType resetType = UserSecurityResetType.getByName(faceTransType.getCode());
        if (resetType == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (reset == null || resetType != reset.getType()) {
            log.info("获取重置流程信息失败. transId:{}", transId);
            throw new BusinessException(GeneralCode.AC_RESET_RECORD_MISS);
        }
        Long userId = reset.getUserId();
        processSendFaceEmail(userId, transId, reset, faceTransType);
    }

    @Override
    public void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne) {
        // 由于上传的图片无法做人脸识别是，需要把回退回重新上传jumio的状态
        log.info("重置流程上传的图片由于无法做人脸识别进行重置到需重传到状态. userId:{} transId:{} faceTransType:{} isLockOne:{}",
                userId, transId, faceTransType, isLockOne);
        if (isLockOne) {
            return;
        }
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (reset == null || !Objects.equals(UserSecurityResetStatus.unsubmitted, reset.getStatus())) {
            UserSecurityResetStatus status = reset == null ? null : reset.getStatus();
            log.info("重置流程获取失败或者已经不能重新上传. userId:{} transId:{} status:{}", userId, transId, status);
            return;
        }
        reset.setScanReference(null);
        reset.setJumioToken(null);
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        userSecurityResetMapper.removeJumioInitScanRef(reset);
        log.info("重置流程成功回归到需要重新上传jumio的状态, userId:{} resetId:{}", userId, reset.getId());
    }

    /**
     * 处理当前重置流程是否能发送人脸识别邮件
     *
     * @param userId
     * @param transId
     * @param reset
     * @param faceTransType
     */
    private void processSendFaceEmail(Long userId, String transId, UserSecurityReset reset, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = transactionFaceLogMapper.findByUserIdTransId(userId, transId, faceTransType.name());
        if (faceLog == null || faceLog.isEndStatus()) {
            log.info("获取人脸识别业务记录信息失败. userId:{} transId:{} faceTransType:{}", userId, transId, faceTransType);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_STATUS_UNDO);
        }
        if (faceLog.getStatus() == TransFaceLogStatus.REVIEW) {
            log.info("当前人脸识别流程已经进入人工审核，不能发送通知邮件: userId:{} transId:{}", userId, transId);
            throw new BusinessException(AccountErrorCode.FACE_TRANS_REVIEW_CANNOT_EMAIL);
        }
        // 限制5分钟内发一次邮件
        String cacheKey = emailResendTimesCacheKey(transId);
        String cacheNum = RedisCacheUtils.get(cacheKey);
        if (StringUtils.isNotBlank(cacheNum)) {
            log.info("缓存中存在上次发送的记录，五分钟内只能发一次, 不能再发送. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_MINUTE_EMAIL);
        }
        LanguageEnum language = getResetSendFaceEmailLanguage(userId, reset.getScanReference(), reset.getType());
        String link = emailNotifyLink(userId, transId, language, reset);
        log.info("重新发送重置流程人脸识别通知邮件：userId:{} transId:{} language:{}", userId, transId, language);
        String sendResult = sendFaceNotifyEmail(userId, transId, faceLog, faceTransType, language, link);
        if (StringUtils.isBlank(sendResult)) {
            // 如果发送成功，做一个缓存，限制五分钟内只能发送一次邮件
            RedisCacheUtils.set(cacheKey, reset.getId(), 300);
        } else {
            log.info("重置流程人脸识别通知邮件发送失败. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    /**
     * 生成做人脸识别的连接
     *
     * @param userId
     * @param transId
     * @param language
     * @param reset
     * @return
     */
    private String emailNotifyLink(Long userId, String transId, LanguageEnum language, UserSecurityReset reset) {
        // 获取配置的link
        String linkBase = iFace.getFaceApiPath(null, apolloCommonConfig.getFaceEmailLinkApi(), language);
        if (StringUtils.isBlank(linkBase)) {
            log.warn("get faceid reset email link config fail. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        int haveQuestion = 0;
        if (reset.getQuestionScore() != null && reset.getQuestionScore() > 0) {
            haveQuestion = 1;
        }
        return String.format("%s?id=%s&type=%s&haveQuestion=%d", linkBase, transId, reset.getType().name(),
                haveQuestion);
    }

    /**
     * 发送人脸识别的邮件语言选择
     *
     * @param userId
     * @param scanRef
     * @param type
     * @return
     */
    private LanguageEnum getResetSendFaceEmailLanguage(Long userId, String scanRef, UserSecurityResetType type) {
        JumioInfoVo vo = jumioBusiness.getByUserAndScanRef(userId, scanRef, type.name());
        if (vo != null && StringUtils.isNotBlank(vo.getIssuingCountry())) {
            String voCountry = vo.getIssuingCountry();
            if (StringUtils.equalsIgnoreCase("CHN", voCountry) || StringUtils.equalsIgnoreCase("CN", voCountry)) {
                return LanguageEnum.ZH_CN;
            }
        }
        // 如果获取不到直接拿上一次登记的语言
        return emailNotifyLanguage(userId);
    }

    @Override
    public TransactionFaceLog validateCanDoFace(Long userId, String transId, FaceTransType faceTransType) {
        // 先验证当前业务流程是否能做人脸识别，如果可以，再检查当前人脸识别流程是否可以做人脸识别
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        UserSecurityResetType resetType = UserSecurityResetType.getByName(faceTransType.getCode());
        if (reset == null || userId == null || userId.longValue() != reset.getUserId().longValue() || resetType != reset.getType()) {
            log.warn("获取重置流程记录信息失败. userId:{} transId:{} faceTransType:{}", userId, transId, faceTransType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            log.info("当前重置记录状态以及不能在做人脸识别. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.AC_RESET_FACE_STATUS_UNDO);
        }
        // 如果自身记录状态允许做人脸识别，则检验人脸识别流程是否可以做人脸识别
        return super.validateCanDoFace(userId, transId, faceTransType);
    }

    private int saveFaceStatus(UserSecurityReset reset, FaceStatus faceStatus, String remark, String ip) {
        // 变更业务流程的人脸识别状态信息
        reset.setFaceIp(ip);
        reset.setUpdateTime(DateUtils.getNewUTCDate());
        reset.setFaceStatus(faceStatus.name());
        reset.setFaceRemark(remark);
        return userSecurityResetMapper.updateFaceStatus(reset);
    }

    @Override
    public FaceInitResponse facePcInit(String transId, FaceTransType transType) {
        FaceInitResponse response = super.facePcInit(transId, transType);
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(response.getTransId());
        if (reset != null) {
            // 变更业务流程的人脸识别状态信息
            String ip = null;
            if (WebUtils.getHttpServletRequest() != null) {
                ip = WebUtils.getRequestIp();
            }
            saveFaceStatus(reset, FaceStatus.FACE_PENDING, "待PC端人脸识别", ip);
        }
        return response;
    }
    
    @Override
    public FaceInitResponse facePrivateInit(String transId, FaceTransType transType) {
    	FaceInitResponse response = super.facePrivateInit(transId, transType);
    	UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(response.getTransId());
        if (reset != null) {
            // 变更业务流程的人脸识别状态信息
            String ip = null;
            if (WebUtils.getHttpServletRequest() != null) {
                ip = WebUtils.getRequestIp();
            }
            saveFaceStatus(reset, FaceStatus.FACE_PENDING, "待PC端人脸识别", ip);
        }
        return response;
    }

    @Override
    public FaceInitResponse faceSdkInit(String transId, FaceTransType transType) {
        FaceInitResponse response = super.faceSdkInit(transId, transType);
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(response.getTransId());
        if (reset != null) {
            // 变更业务流程的人脸识别状态信息
            String ip = null;
            if (WebUtils.getHttpServletRequest() != null) {
                ip = WebUtils.getRequestIp();
            }
            saveFaceStatus(reset, FaceStatus.FACE_PENDING, "待SDK端人脸识别", ip);
        }
        return response;
    }

    @Override
    public FacePcResponse facePcResultHandler(String transId, FaceTransType transType, FaceWebResultResponse faceWebResult) {
        FacePcResponse facePcResponse = super.facePcResultHandler(transId, transType, faceWebResult);
        // 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (facePcResponse == null || reset == null || !StringUtils.equalsIgnoreCase(transType.getCode(), reset.getType().name())) {
            log.info("重置流程PC端人脸识别结果信息获取不到重置记录. transId:{} transType:{}", transId, transType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = reset.getUserId();
        boolean isPassed = facePcResponse.isSuccess();
        String faceRemark = facePcResponse.getFaceRemark();
        TransFaceLogStatus status = facePcResponse.getStatus();
        log.info("重置流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status: {} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
        saveResetFaceResult(userId, transId, reset, isPassed, faceRemark, status);
        return facePcResponse;
    }
    
    @Override
    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
    	TransactionFaceLog transLog = super.facePcPrivateResultHandler(transType, result);
    	if(transLog == null) {
    		return null;
    	}
    	String transId = transLog.getTransId();
    	// 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (transLog == null || reset == null || !StringUtils.equalsIgnoreCase(transType.getCode(), reset.getType().name())) {
            log.info("重置流程PC端人脸识别结果信息获取不到重置记录. transId:{} transType:{}", transId, transType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = reset.getUserId();
        boolean isPassed = result.isSuccess();
        String faceRemark = transLog.getFaceRemark();
        TransFaceLogStatus status = transLog.getStatus();
        log.info("重置流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status: {} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
        saveResetFaceResult(userId, transId, reset, isPassed, faceRemark, status);
        return transLog;
    }

    /**
     * 重置流程人脸识别结果登记
     *
     * @param userId
     * @param transId
     * @param reset
     * @param facePassed
     * @param faceRemark
     */
    private void saveResetFaceResult(Long userId, String transId, UserSecurityReset reset, boolean facePassed, String faceRemark, TransFaceLogStatus status) {
        // 变更重置流程的人脸识别状态信息
        String ip = "";
        TerminalEnum terminal = null;
        try {
            ip = WebUtils.getRequestIp();
            terminal = WebUtils.getAPIRequestHeader().getTerminal();
        } catch (Exception e) {
            log.warn("获取Face验证结果请求IP和终端信息错误. userId: transId:{}", userId, transId);
        }
        final String requestIp = ip;
        final TerminalEnum requestTerminal = terminal;
        if (facePassed) {
            FaceStatus faceStatus = status == TransFaceLogStatus.PASSED ? FaceStatus.FACE_PASS : FaceStatus.FACE_REVIEW;
            saveFaceStatus(reset, faceStatus, faceRemark, requestIp);
        } else {
            saveFaceStatus(reset, FaceStatus.FACE_FAIL, faceRemark, requestIp);
        }
        if (facePassed && TransFaceLogStatus.PASSED == status) {
            // 如果人脸识别通过，检查并且走自动通过的逻辑
            log.info("人脸识别通后走自动通过认证的逻辑. userId:{} transId:{}", userId, transId);
            iUserSecurityReset.autoPassResetHandler(userId, transId, ip, requestTerminal);
        }
    }

    @Override
    public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType, FaceSdkVerifyRequest request) {
        FaceSdkResponse faceSdkResponse = super.faceSdkResultHandler(transId, userId, faceTransType, request);
        // 根据结果是否人脸识别通过，进行一些后续逻辑处理, 注意：后续处理逻辑最好处理时间要短，否则会出现用户一直等待结果的情况
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (faceSdkResponse == null || reset == null || !StringUtils.equalsIgnoreCase(faceTransType.getCode(), reset.getType().name())) {
            log.info("重置流程SDK端人脸识别结果信息获取不到重置记录. transId:{} transType:{}", transId, faceTransType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        boolean isPassed = faceSdkResponse.isSuccess();
        String faceRemark = faceSdkResponse.getMessage();
        TransFaceLogStatus status = faceSdkResponse.getStatus();
        log.info("重置流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
        saveResetFaceResult(userId, transId, reset, isPassed, faceRemark, status);
        return faceSdkResponse;
    }

    @Override
    public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = super.transFaceAudit(auditRequest, faceTransType);
        String transId = faceLog.getTransId();
        Long userId = faceLog.getUserId();
        UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(transId);
        if (reset == null || userId.longValue() != reset.getUserId().longValue()) {
            log.error("获取人脸识别审核的业务流程失败. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (!UserSecurityResetStatus.isReviewPending(reset.getStatus())) {
            log.info("当前重置流程状态已经处于终态，不能再修改. userId:{} transId:{}", userId, transId);
            return faceLog;
        }
        if (faceLog.getStatus() == TransFaceLogStatus.FAIL) {
            log.info("人工审核人脸识别认证信息拒绝, 直接把认证流程进行拒绝, userId:{} transId:{}", userId, transId);
            saveFaceStatus(reset, FaceStatus.FACE_FAIL, faceLog.getFailReason(), null);
            reset.setStatus(UserSecurityResetStatus.refused);
            reset.setAuditMsg(faceLog.getFailReason());
            reset.setAuditTime(DateUtils.getNewUTCDate());
            reset.setUpdateTime(DateUtils.getNewUTCDate());
            userSecurityResetMapper.updateByPrimaryKeySelective(reset);
            log.info("发送重置流程拒绝通知邮件. userId:{} transId:{}", userId, transId);
            iUserSecurityReset.sendResetAuthEmail(UserSecurityResetStatus.refused, reset, faceLog.getFailReason(), false);
            // JUMIO 业务状态同步
            jumioBusiness.syncJumioBizStatus(userId, reset.getScanReference(), JumioBizStatus.REFUSED);
        }else if (faceLog.getStatus() == TransFaceLogStatus.PASSED) {
            log.info("人工审核人脸识别认证过程通过，考虑自动通过流程：userId:{} transId:{}", userId, transId);
            saveResetFaceResult(userId, transId, reset, true, "人工审核人脸识别通过", TransFaceLogStatus.PASSED);
        }else {
            log.error("人工审核人脸识别状态信息错误. userId:{}, transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return faceLog;
    }
}
