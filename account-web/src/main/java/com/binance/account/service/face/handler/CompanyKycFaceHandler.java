package com.binance.account.service.face.handler;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.error.AccountErrorCode;
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
 * 企业认证人脸识别处理器
 *
 * @author liliang1
 * @date 2019-02-28 10:39
 */
@Log4j2
@Component
@FaceHandlerType(values = {FaceTransType.KYC_COMPANY})
public class CompanyKycFaceHandler extends AbstractFaceHandler {

    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
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
                // 单kyc用户锁定模式下，直接发起初始化KYC的人脸识别，如果能初始化成功，则完成初始化
                TransactionFaceLog faceLog = this.generateTransFaceLog(FaceTransType.KYC_COMPANY, userId, transId, isKycLockOne);
                if (faceLog == null) {
                    log.warn("初始化企业认证人脸识别结果为null. userId:{} transId:{}", userId, transId);
                    throw new BusinessException(GeneralCode.SYS_ERROR);
                }
                FaceFlowInitResult result = new FaceFlowInitResult();
                result.setTransId(faceLog.getTransId());
                result.setType(transType.getCode());
                return result;
            }
            // 如果不是单kyc模式直接按原有逻辑
            if (userId == null || !NumberUtils.isCreatable(transId)) {
                log.warn("请求参数信息错误. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            Long kycId = Long.valueOf(transId);
            CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, kycId);
            if (certificate == null) {
                log.warn("当前企业认证记录获取失败. userId:{} kycId:{}", userId, kycId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            if (CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
                log.info("当前企业认证的状态已经到终态，不能再发起人脸识别：userId:{} kycId:{} status:{}", userId, kycId, certificate.getStatus());
                throw new BusinessException(AccountErrorCode.KYC_STATUS_CANNOT_FACE);
            }
            // 直接发起初始化KYC的人脸识别，如果能初始化成功，则完成初始化
            TransactionFaceLog faceLog = this.generateTransFaceLog(FaceTransType.KYC_COMPANY, userId, kycId.toString(), isKycLockOne);
            if (faceLog == null) {
                log.warn("初始化企业认证人脸识别结果为null. userId:{} kycId:{}", userId, kycId);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            if (needEmail) {
                // 如果能初始化成功，发送通知邮件，登记KYC的信息
                LanguageEnum language = emailNotifyLanguage(userId);
                String link = emailNotifyLink(transId, transType, language);
                sendFaceNotifyEmail(userId, kycId.toString(), faceLog, FaceTransType.KYC_COMPANY, language, link);
            }
            CompanyCertificate companyCertificate = new CompanyCertificate();
            companyCertificate.setId(certificate.getId());
            companyCertificate.setUserId(certificate.getUserId());
            saveFaceStatus(companyCertificate, FaceStatus.FACE_PENDING, "已通知待人脸识别");
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
        // KYC 企业认证 人脸识别通知邮件
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
            log.warn("获取公司认证人脸识别记录信息失败. transId:{} type:{}", transId, faceTransType);
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        if (faceLog.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，不需要验证老的kyc表信息
            return faceLog.getStatus() == TransFaceLogStatus.PASSED;
        }else {
            Long userId = faceLog.getUserId();
            Long certificateId = Long.valueOf(faceLog.getTransId());
            CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, certificateId);
            if (certificate == null) {
                log.warn("获取公司认证流程信息失败. userId:{} transId:{}", userId, transId);
                throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
            }
            switch (certificate.getStatus()) {
                case passed:
                    throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_SUCCESS);
                case refused:
                case expired:
                    throw new BusinessException(AccountErrorCode.CHECK_FLOW_END_OF_FAIL);
                default:
                    // 业务未结束的情况下，看当前人脸识别是否通过
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
        Long certificateId = Long.valueOf(transId);
        CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, certificateId);
        if (certificate == null) {
            log.warn("获取公司认证记录失败. userId:{} transId:{}", userId, transId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
            log.info("公司认证记录已经处于终态，不能再进行人脸识别认证. userId:{} transId:{} status:{}", userId, transId, certificate.getStatus());
            throw new BusinessException(AccountErrorCode.FACE_TRANS_STATUS_ERROR);
        }
        // 如果业务还处于审核中状态，验证人脸识别流程是否已经结束
        return transactionLog;
    }

    private int saveFaceStatus(CompanyCertificate companyCertificate, FaceStatus faceStatus, String remark) {
        companyCertificate.setFaceStatus(faceStatus.name());
        companyCertificate.setFaceRemark(remark);
        companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
        return companyCertificateMapper.updateFaceStatus(companyCertificate);
    }

    @Override
    public FaceInitResponse facePcInit(String transId, FaceTransType transType) {
        FaceInitResponse response = super.facePcInit(transId, transType);
        if(response.isKycLockOne()) {
        	return response;
        }
        Long userId = response.getUserId();
        Long id = Long.valueOf(response.getTransId());
        CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
        if (companyCertificate != null) {
            saveFaceStatus(companyCertificate, FaceStatus.FACE_PENDING, "待PC端人脸识别");
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
         CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
         if (companyCertificate != null) {
             saveFaceStatus(companyCertificate, FaceStatus.FACE_PENDING, "待PC端人脸识别");
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
        CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
        if (companyCertificate != null) {
            saveFaceStatus(companyCertificate, FaceStatus.FACE_PENDING, "待SDK端人脸识别");
        }
        return response;
    }

    @Override
    public FacePcResponse facePcResultHandler(String transId, FaceTransType transType, FaceWebResultResponse faceWebResult) {
        FacePcResponse facePcResponse = super.facePcResultHandler(transId, transType, faceWebResult);
        Long userId = facePcResponse.getUserId();
        String faceRemark = facePcResponse.getFaceRemark();
        boolean isPassed = facePcResponse.isSuccess();
        TransFaceLogStatus status = facePcResponse.getStatus();
        if (facePcResponse.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，需要按新的执行器走
            kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
        }else {
            Long id = Long.valueOf(transId);
            CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
            if (certificate == null || CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
                log.info("FACE PC => 企业认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("FACE PC => 企业认证流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveCompanyKycFaceResult(userId, id, certificate, isPassed, faceRemark, status);
        }
        return facePcResponse;
    }

    @Override
    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
    	TransactionFaceLog transLog = super.facePcPrivateResultHandler(transType, result);
    	if(transLog == null ) {
    		return null;
    	}
    	Long userId = transLog.getUserId();
        String faceRemark = transLog.getFaceRemark();
        boolean isPassed = result.isSuccess();
    	TransFaceLogStatus status = transLog.getStatus();
    	String transId = transLog.getTransId();
        if(transLog.isKycLockOne()) {
        	kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
    	}else {
    		Long id = Long.valueOf(transLog.getTransId());
    		CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
            if (certificate == null || CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
                log.info("FACE PC => 企业认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("FACE PC => 企业认证流程PC端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveCompanyKycFaceResult(userId, id, certificate, isPassed, faceRemark, status);
    	}
        return transLog;
    }


    @Override
    public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType, FaceSdkVerifyRequest request) {
        FaceSdkResponse faceSdkResponse = super.faceSdkResultHandler(transId, userId, faceTransType, request);
        String faceRemark = faceSdkResponse.getMessage();
        boolean isPassed = faceSdkResponse.isSuccess();
        TransFaceLogStatus status = faceSdkResponse.getStatus();
        if (faceSdkResponse.isKycLockOne()) {
            // 如果是新版本的kyc认证方式，需要按新的执行器走
            kycLockOneFaceExecutor(userId, faceRemark, status,isPassed);
        }else {
            Long id = Long.valueOf(transId);
            CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
            if (certificate == null || CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
                log.info("FACE SDK => 企业认证流程获取失败或者已经结束：userId:{} id:{}", userId, id);
                throw new BusinessException(GeneralCode.SYS_ERROR);
            }
            log.info("FACE SDK => 企业认证流程SDK端人脸识别结果:userId:{} transId:{} isPassed:{} status:{} faceRemark:{}", userId, transId, isPassed, status, faceRemark);
            saveCompanyKycFaceResult(userId, id, certificate, isPassed, faceRemark, status);
        }
        return faceSdkResponse;
    }

    private void kycLockOneFaceExecutor(Long userId, String faceTips, TransFaceLogStatus status,boolean isPassed) {
        FaceAuthRequest faceAuthRequest = new FaceAuthRequest();
        faceAuthRequest.setUserId(userId);
        faceAuthRequest.setKycType(KycCertificateKycType.USER);
        faceAuthRequest.setMessage(faceTips);
        faceAuthRequest.setStatus(status);
        faceAuthRequest.setFacePassed(isPassed);
        faceAuthRequest.setFaceStatus(null);
        kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_FACE_AUTH_RESULT).process(faceAuthRequest);
    }

    /**
     * 变更人脸识别通过或者失败后的状态信息
     * @param userId
     * @param id
     * @param certificate
     * @param facePassed
     * @param faceRemark
     */
    public void saveCompanyKycFaceResult(Long userId, Long id, CompanyCertificate certificate, boolean facePassed, String faceRemark, TransFaceLogStatus status) {
        // 变更人脸识别是否通过的结果
        if (facePassed) {
            FaceStatus faceStatus = status == TransFaceLogStatus.PASSED ? FaceStatus.FACE_PASS : FaceStatus.FACE_REVIEW;
            saveFaceStatus(certificate, faceStatus, faceRemark);
        } else {
            // 如果人脸识别的业务流程进入审核种的状态，也把人脸识别标识为失败的状态
            saveFaceStatus(certificate, FaceStatus.FACE_FAIL, faceRemark);
        }
        log.info("保存人脸识别结果信息: userId:{} transId:{} isPass:{} transFaceLogStatus:{}", userId, id, facePassed, status);
    }

    @Override
    public void faceImageErrorRedoUpload(String transId, Long userId, FaceTransType faceTransType, boolean isLockOne) {
        log.info("企业认证由于照片有问题需要重新上传照片的处理: userId:{} transId:{}, faceTransType:{} isLockOne:{}",
                userId, transId, faceTransType, isLockOne);
        if (isLockOne) {
            return;
        }
        Long id = Long.valueOf(transId);
        CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
        if (companyCertificate == null || CompanyCertificateStatus.pending != companyCertificate.getStatus()) {
            CompanyCertificateStatus status = companyCertificate == null ? null : companyCertificate.getStatus();
            log.info("企业认证流程获取失败或者已经不能重新上传. userId:{} transId:{} status:{}", userId, transId, status);
            return;
        }
        companyCertificate.setJumioStatus(null);
        companyCertificate.setJumioId(null);
        companyCertificate.setScanReference(null);
        companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
        companyCertificate.setInfo(JumioError.FACE_IMAGE_VERIFY_FAIL.name());
        companyCertificateMapper.saveJumioId(companyCertificate);
        log.info("企业认证由于上传照片无法做人脸识别进行设置到重传图片状态: userId:{} transId:{}", userId, transId);
    }

    @Override
    public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = super.transFaceAudit(auditRequest, faceTransType);
        if (faceLog.isKycLockOne()) {
            // 新版本的认证，直接进入后置处理器
            kycLockOneFaceExecutor(faceLog.getUserId(), faceLog.getFailReason(), faceLog.getStatus(),true);
            return faceLog;
        }
        Long id = Long.valueOf(faceLog.getTransId());
        Long userId = faceLog.getUserId();
        CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
        if (certificate == null) {
            log.warn("根据信息获取不到企业审核记录: userId:{} transId:{}", userId, id);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        // 如果已经是最终态，则不需要进行任何变动
        if (CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
            log.info("企业认证已经到了最终态，不需要做其他变动. userId:{} transId:{}", userId, id);
            return faceLog;
        }
        // 看是否审核通过或者拒绝，执行后续的流程
        if (faceLog.getStatus() == TransFaceLogStatus.FAIL) {
            log.info("人脸识别审核拒绝，直接把企业认证流程拒绝: userId:{} transId:{}", userId, id);
            saveFaceStatus(certificate, FaceStatus.FACE_FAIL, faceLog.getFailReason());
            certificate.setStatus(CompanyCertificateStatus.refused);
            certificate.setInfo(auditRequest.getFailReason());
            certificate.setUpdateTime(DateUtils.getNewUTCDate());
            companyCertificateMapper.updateByPrimaryKeySelective(certificate);
            log.info("企业认证人脸识别审核拒绝时，直接把整个认证流程拒绝并发送通知邮件: userId:{} transId:{}", userId, id);
            userCommonBusiness.sendJumioCheckEmail(userId, null, auditRequest.getFailReason(), Constant.JUMIO_COMPANY_CHECK_FAIL, "发送企业认证邮件");
            //同步业务状态到INSPECTOR 的JUMIO 数据
            jumioBusiness.syncJumioBizStatus(userId, certificate.getScanReference(), JumioBizStatus.REFUSED);
        }else if (faceLog.getStatus() == TransFaceLogStatus.PASSED) {
            log.info("人脸识别审核通过，把企业认证人脸识别的状态信息变更到通过状态. userId:{} transId:{}", userId, id);
            saveFaceStatus(certificate, FaceStatus.FACE_PASS, "人脸识别人工审核通过");
        }else {
            log.error("人工审核人脸识别状态信息错误. userId:{}, transId:{}", userId, id);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return faceLog;
    }
}
