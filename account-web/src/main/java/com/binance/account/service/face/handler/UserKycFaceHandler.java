package com.binance.account.service.face.handler;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.face.FaceHandlerType;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.kyc.request.FaceAuthRequest;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 个人认证-人脸识别处理器
 * @author liliang1
 * @date 2019-02-28 9:46
 */
@Log4j2
@Component
@FaceHandlerType(values = {FaceTransType.KYC_USER})
public class UserKycFaceHandler extends AbstractFaceHandler {

    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    private IUserKyc iUserKyc;
    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private KycFlowProcessFactory kycFlowProcessFactory;

    @Override
    public FaceFlowInitResult initTransFace(String transId, Long userId, FaceTransType transType, boolean needEmail, boolean isKycLockOne) {
//        if (!apolloCommonConfig.isKycFaceSwitch()) {
//            log.info("KYC认证是否需要做人脸识别开关关闭. userId:{} transId:{}", userId, transId);
//            return null;
//        }
        try {
            if (isKycLockOne) {
                // 对于kyc数据单用户锁定单，直接发起初始化KYC的人脸识别，如果能初始化成功，则完成初始化
                TransactionFaceLog faceLog = this.generateTransFaceLog(FaceTransType.KYC_USER, userId, transId, isKycLockOne);
                if (faceLog == null) {
                    log.warn("初始化个人认证人脸识别结果为null. userId:{} transId:{}", userId, transId);
                    throw new BusinessException(GeneralCode.SYS_ERROR);
                }
                FaceFlowInitResult result = new FaceFlowInitResult();
                result.setTransId(faceLog.getTransId());
                result.setType(transType.getCode());
                return result;
            }
            // 如果是老流程方式，按原有逻辑走
            if (userId == null || !NumberUtils.isCreatable(transId)) {
                log.warn("请求参数信息错误. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            Long kycId = Long.valueOf(transId);
            UserKyc userKyc = userKycMapper.getById(userId, kycId);
            if (userKyc == null) {
                log.warn("get user kyc record fail by id. userId:{} kycId:{}", userId, kycId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            if (KycStatus.isEndStatus(userKyc.getStatus())) {
                // 当前状态下不能做KYC的人脸识别初始化.
                log.info("当前个人KYC认证状态已经到终态，不能再发起人脸识别: userId:{} kycId:{} status:{}", userId, kycId, userKyc.getStatus());
                throw new BusinessException(AccountErrorCode.KYC_STATUS_CANNOT_FACE);
            }

            // 直接发起初始化KYC的人脸识别，如果能初始化成功，则完成初始化
            TransactionFaceLog faceLog = this.generateTransFaceLog(FaceTransType.KYC_USER, userId, kycId.toString(), isKycLockOne);
            if (faceLog == null) {
                log.warn("初始化个人认证人脸识别结果为null. userId:{} kycId:{}", userId, kycId);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            if (needEmail) {
                // 如果能初始化成功，发送通知邮件，登记KYC的信息 语言使用最近登记使用的语言
                LanguageEnum language = emailNotifyLanguage(userId);
                String link = emailNotifyLink(transId, transType, language);
                sendFaceNotifyEmail(userId, kycId.toString(), faceLog, FaceTransType.KYC_USER, language, link);
            }
            UserKyc kyc = new UserKyc();
            kyc.setId(userKyc.getId());
            kyc.setUserId(userKyc.getUserId());
            kyc.setFaceStatus(FaceStatus.FACE_PENDING.name());
            kyc.setFaceRemark("待人脸识别");
            kyc.setUpdateTime(DateUtils.getNewUTCDate());
            userKycMapper.updateFaceStatus(kyc);
            FaceFlowInitResult result = new FaceFlowInitResult();
            result.setTransId(faceLog.getTransId());
            result.setType(transType.getCode());
            return result;
        }catch (BusinessException e) {
            log.warn("初始化个人认证人脸识别失败. userId:{} transId:{} message:{}", userId, transId, e.getErrorCode());
            throw e;
        }catch (Exception e) {
            log.error("初始化个人认证人脸识别异常. userId:{}, transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    @Override
    protected String emailNotifyTemplate() {
        // KYC 个人认证 人脸识别通知邮件
        return AccountConstants.EMAIL_KYC_FACE_NOTIFY;
    }

    @Override
    protected String emailNotifyLink(String transId, FaceTransType faceTransType, LanguageEnum language) {
        // KYC 人脸识别通知邮件没有点击连接
        return null;
    }

    @Override
    public boolean isFacePassed(String transId, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(transId, faceTransType.name());
        if (faceLog == null) {
            log.warn("获取个人认证人脸识别记录信息失败. transId:{} type:{}", transId, faceTransType);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (faceLog.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，不需要验证老的kyc表信息
            return faceLog.getStatus() == TransFaceLogStatus.PASSED;
        }else {
            Long kycId = Long.valueOf(faceLog.getTransId());
            Long userId = faceLog.getUserId();
            UserKyc userKyc = userKycMapper.getById(userId, kycId);
            // 如果通过的状态，则检查业务状态是否已经结束，如果已经结束
            if (userKyc == null) {
                log.warn("获取个人认证流程信息失败. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            switch (userKyc.getStatus()) {
                case passed:
                    throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_SUCCESS);
                case expired:
                case refused:
                    throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_FAIL);
                default:
                    return faceLog.getStatus() == TransFaceLogStatus.PASSED;
            }
        }
    }

    @Override
    public TransactionFaceLog validateCanDoFace(Long userId, String transId, FaceTransType faceTransType) {
    	TransactionFaceLog transactionLog = super.validateCanDoFace(userId, transId, faceTransType);
    	if(transactionLog.isKycLockOne()) {
    		return transactionLog;
    	}

        Long kycId = Long.valueOf(transId);
        UserKyc userKyc = userKycMapper.getById(userId, kycId);
        if (userKyc == null) {
            log.warn("获取个人认证记录失败. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (KycStatus.isEndStatus(userKyc.getStatus())) {
            log.info("个人认证记录已经处于终态，不能再进行人脸识别认证. userId:{} transId:{} status:{}", userId, transId, userKyc.getStatus());
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        // 如果业务还处于审核中状态，验证人脸识别流程是否已经结束
        return transactionLog;
    }

    private int saveFaceStatus(UserKyc userKyc, FaceStatus faceStatus, String remark) {
        userKyc.setFaceStatus(faceStatus.name());
        userKyc.setFaceRemark(remark);
        userKyc.setUpdateTime(DateUtils.getNewUTCDate());
        return userKycMapper.updateFaceStatus(userKyc);
    }

    @Override
    public FaceInitResponse facePcInit(String transId, FaceTransType transType) {
        FaceInitResponse response = super.facePcInit(transId, transType);
        if(response.isKycLockOne()) {
        	return response;
        }
        Long userId = response.getUserId();
        Long id = Long.valueOf(response.getTransId());
        UserKyc kyc = userKycMapper.getById(userId, id);
        if (kyc != null) {
            saveFaceStatus(kyc, FaceStatus.FACE_PENDING, "待PC端人脸识别");
        }
        return response;
    }

    @Override
    public FaceInitResponse facePrivateInit(String transId, FaceTransType transType) {
    	FaceInitResponse response = super.facePrivateInit(transId, transType);
    	if(response.isKycLockOne()) {
        	return response;
        }
        Long userId = response.getUserId();
        Long id = Long.valueOf(response.getTransId());
        UserKyc kyc = userKycMapper.getById(userId, id);
        if (kyc != null) {
            saveFaceStatus(kyc, FaceStatus.FACE_PENDING, "待PC端人脸识别");
        }
        return response;
    }

    @Override
    public FaceInitResponse faceSdkInit(String transId, FaceTransType transType) {
        FaceInitResponse response = super.faceSdkInit(transId, transType);
        if(response.isKycLockOne()) {
        	return response;
        }
        Long userId = response.getUserId();
        Long id = Long.valueOf(response.getTransId());
        UserKyc kyc = userKycMapper.getById(userId, id);
        if (kyc != null) {
            saveFaceStatus(kyc, FaceStatus.FACE_PENDING, "待SDK端人脸识别");
        }
        return response;
    }

    @Override
    public FacePcResponse facePcResultHandler(String transId, FaceTransType transType, FaceWebResultResponse faceWebResult) {
        FacePcResponse facePcResponse = super.facePcResultHandler(transId, transType, faceWebResult);
        boolean isPassed = facePcResponse.isSuccess();
        String faceRemark = facePcResponse.getFaceRemark();
        Long userId = facePcResponse.getUserId();
        TransFaceLogStatus status = facePcResponse.getStatus();
        if (facePcResponse.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，需要按新的执行器走
            kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
        }else {
        	Long id = Long.valueOf(transId);
            UserKyc userKyc = userKycMapper.getById(userId, id);
            if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
                log.info("个人认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("个人认证流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveUserKycFaceResult(userId, id, userKyc, isPassed, faceRemark, status);
        }
        return facePcResponse;
    }

    @Override
    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
    	TransactionFaceLog transLog = super.facePcPrivateResultHandler(transType, result);
    	if(transLog == null) {
    		return null;
    	}
    	boolean isPassed = result.isSuccess();
        String faceRemark = transLog.getFaceRemark();
        Long userId = transLog.getUserId();
        TransFaceLogStatus status = transLog.getStatus();
        String transId = transLog.getTransId();
        if (transLog.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，需要按新的执行器走
            kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
        }else {
        	Long id = Long.valueOf(transId);
            UserKyc userKyc = userKycMapper.getById(userId, id);
            if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
                log.info("个人认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("个人认证流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveUserKycFaceResult(userId, id, userKyc, isPassed, faceRemark, status);
        }
        return transLog;
    }

    @Override
    public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType, FaceSdkVerifyRequest request) {
        FaceSdkResponse faceSdkResponse = super.faceSdkResultHandler(transId, userId, faceTransType, request);
        boolean isPassed = faceSdkResponse.isSuccess();
        String faceRemark = faceSdkResponse.getMessage();
        TransFaceLogStatus status = faceSdkResponse.getStatus();
        if (faceSdkResponse.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，需要按新的执行器走
            kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
        }else {
            Long id = Long.valueOf(transId);
            UserKyc userKyc = userKycMapper.getById(userId, id);
            if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
                log.info("个人认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("个人认证流程SDK端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveUserKycFaceResult(userId, id, userKyc, isPassed, faceRemark, status);
        }
        return faceSdkResponse;
    }

    private void kycLockOneFaceExecutor(Long userId, String faceTips, TransFaceLogStatus status,boolean facePassed) {
        FaceAuthRequest faceAuthRequest = new FaceAuthRequest();
        faceAuthRequest.setUserId(userId);
        faceAuthRequest.setKycType(KycCertificateKycType.USER);
        faceAuthRequest.setMessage(faceTips);
        faceAuthRequest.setStatus(status);
        faceAuthRequest.setFacePassed(facePassed);
        faceAuthRequest.setFaceStatus(null);
        kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_FACE_AUTH_RESULT).process(faceAuthRequest);
    }

    /**
     * 修改保存人脸识别通过或失败后的个人认证状态信息
     * @param userId
     * @param id
     * @param userKyc
     * @param facePassed
     * @param faceRemark
     */
    public void saveUserKycFaceResult(Long userId, Long id, UserKyc userKyc, boolean facePassed, String faceRemark, TransFaceLogStatus status) {
        if (facePassed) {
            FaceStatus faceStatus = status == TransFaceLogStatus.PASSED ? FaceStatus.FACE_PASS : FaceStatus.FACE_REVIEW;
            saveFaceStatus(userKyc, faceStatus, faceRemark);
        }else {
            saveFaceStatus(userKyc, FaceStatus.FACE_FAIL, faceRemark);
        }
        // 如果人脸识别是通过的，进行检查人脸识别通过后是否能自动审核通过
        if (facePassed && status == TransFaceLogStatus.PASSED) {
            log.info("个人KYC认证人脸识别通过，检查是否能自动通过个人认证. userId:{} kycId:{}", userId, id);
            iUserKyc.syncUserKycCanAutoPass(id, userId);
        }
    }

    @Override
    public void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne) {
        log.info("个人认证由于照片有问题需要重新上传照片的处理: userId:{} transId:{}, faceTransType:{} isLockOne:{}",
                userId, transId, faceTransType, isLockOne);
        if (isLockOne) {
            return;
        }
        Long id = Long.valueOf(transId);
        UserKyc userKyc = userKycMapper.getById(userId, id);
        if (userKyc == null || KycStatus.pending != userKyc.getStatus()) {
            KycStatus status = userKyc == null ? null : userKyc.getStatus();
            log.info("个人认证流程获取失败或者已经不能重新上传. userId:{} transId:{} status:{}", userId, transId, status);
            return;
        }
        userKyc.setCheckStatus(null);
        userKyc.setJumioId(null);
        userKyc.setScanReference(null);
        userKyc.setUpdateTime(DateUtils.getNewUTCDate());
        userKyc.setFailReason(JumioError.FACE_IMAGE_VERIFY_FAIL.name());
        userKycMapper.saveJumioId(userKyc);
        log.info("个人认证由于上传照片无法做人脸识别进行设置到重传图片状态: userId:{} transId:{}", userId, transId);
    }

    @Override
    public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = super.transFaceAudit(auditRequest, faceTransType);
        if (faceLog.isKycLockOne()) {
            // 新版本的认证，直接进入后置处理器
            kycLockOneFaceExecutor(faceLog.getUserId(), faceLog.getFailReason(), faceLog.getStatus(),true);
            return faceLog;
        }
        Long kycId = Long.valueOf(faceLog.getTransId());
        Long userId = faceLog.getUserId();
        UserKyc userKyc = userKycMapper.getById(userId, kycId);
        if (userKyc == null) {
            log.error("审核人脸识别数据时获取不到业务数据, userId:{} transId:{}", userId, kycId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (KycStatus.isEndStatus(userKyc.getStatus())) {
            log.info("个人认证已经到了最终态，不能再修改. userId:{} transId:{}", userId, kycId);
            return faceLog;
        }
        if (faceLog.getStatus() == TransFaceLogStatus.FAIL) {
            log.info("个人识别人脸识别人工审核拒绝, 直接把个人认证流程拒绝. userId:{} transId:{}", userId, kycId);
            saveFaceStatus(userKyc, FaceStatus.FACE_FAIL, faceLog.getFailReason());
            userKyc.setStatus(KycStatus.refused);
            userKyc.setFailReason(faceLog.getFailReason());
            userKyc.setUpdateTime(DateUtils.getNewUTCDate());
            userKycMapper.updateStatus(userKyc);
            log.info("人脸识别人工审核拒绝后终止个人认证流程，发送通知邮件. userId:{} transId:{}", userId, kycId);
            userCommonBusiness.sendJumioCheckEmail(userId,null,faceLog.getFailReason(), Constant.JUMIO_KYC_CHECK_FAIL, "发送KYC认证邮件");
            //同步业务状态到 INSPECTOR JUMIO
            jumioBusiness.syncJumioBizStatus(userId, userKyc.getScanReference(), JumioBizStatus.REFUSED);
        }else if (faceLog.getStatus() == TransFaceLogStatus.PASSED) {
            log.info("人工审核个人认证人脸识别通过. userId:{} transId:{}", userId, kycId);
            saveUserKycFaceResult(userId, kycId, userKyc, true, "人脸识别人工审核通过", TransFaceLogStatus.PASSED);
        }else {
            log.error("人工审核人脸识别状态信息错误. userId:{}, transId:{}", userId, kycId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return faceLog;
    }
}
