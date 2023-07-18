package com.binance.account.service.face.handler;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.face.FaceHandlerHelper;
import com.binance.account.service.face.FaceHandlerType;
import com.binance.account.service.user.UserEmailChangeHandler;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.EncryptUtil;
import com.binance.account.vo.face.ResetEmailFaceFlowInitResult;
import com.binance.account.vo.face.request.FacePcPrivateResult;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.response.FacePcResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 变更邮件人脸识别流程
 */
@Log4j2
@Component
@FaceHandlerType(values = {FaceTransType.RESET_EMAIL})
public class ResetEmailFaceHandler extends AbstractFaceHandler {

    @Resource
    private UserCommonBusiness userCommonBusiness;
    @Resource
    private FaceHandlerHelper faceHandlerHelper;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;

    @Resource
    private UserEmailChangeHandler userEmailChangeHandler;



    @Override
    public ResetEmailFaceFlowInitResult initTransFace(String transId, Long userId, FaceTransType faceTransType, boolean needEmail, boolean isKycLockOne) {
        // step1: 预先检查当前流程和用户是否能做人脸识别
        if (StringUtils.isBlank(transId) || !Objects.equals(FaceTransType.RESET_EMAIL, faceTransType) || userId == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        ResetEmailFaceFlowInitResult result = new ResetEmailFaceFlowInitResult();
        result.setTransId(transId);
        result.setType(faceTransType.getCode());
        try {
            // step2: 检查下当前是否已经存在这个流程，如果存在了，并且处于等待验证到情况到话，直接返回对应到信息
            TransactionFaceLog faceLog = transactionFaceLogMapper.findByUserIdTransId(userId, transId, faceTransType.name());
            if (faceLog != null) {
                if (!TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
                    log.info("当前用户的人脸识别正在进行, userId:{} transId:{}", userId, transId);
                    result.setNextStep(ResetEmailFaceFlowInitResult.NextStep.FACE);
                    return result;
                }else {
                    log.info("当前用户的重置Email的人脸识别流程已经结束. userId:{} transId:{} status:{}", userId, transId, faceLog.getStatus());
                    throw new BusinessException(GeneralCode.SYS_ERROR);
                }
            }
            // step3: check
            ResetEmailFaceFlowInitResult.NextStep nextStep = initTransPreCheck(transId, userId);
            log.info("检查用户是否可以直接做人脸识别结果, userId:{} transId:{} nextStep:{}", userId, transId, nextStep);
            if (ResetEmailFaceFlowInitResult.NextStep.KYC  == nextStep) {
                result.setNextStep(ResetEmailFaceFlowInitResult.NextStep.KYC);
            }else {
                faceLog = super.generateTransFaceLog(faceTransType, userId, transId, isKycLockOne);
                if (faceLog == null) {
                    log.warn("创建重置Email的人脸识别流程失败. userId:{} transId:{}", userId, transId);
                    throw new BusinessException(GeneralCode.SYS_ERROR);
                }else {
                    log.info("创建重置Email的人脸识别流程成功. userId:{} transId:{}", userId, transId);
                    result.setNextStep(ResetEmailFaceFlowInitResult.NextStep.FACE);
                }
            }
            return result;
        }catch (BusinessException e) {
            log.warn("初始化修改邮箱人脸识别失败. userId:{} transId:{} message: ", userId, transId, e);
            throw e;
        }catch (Exception e) {
            log.error("初始化修改邮箱人脸识别异常. userId:{}, transId:{}", userId, transId, e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    private ResetEmailFaceFlowInitResult.NextStep initTransPreCheck(String transId, Long userId) {
        // 1. 检查用户是否存在有kyc， 如果没有，需要用户先做kyc
        KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
        if (!faceHandlerHelper.canSkipKycUpload(certificateResult)) {
            log.info("用户KYC不能合要求，需要用户做KYC. userId:{} transId:{}", userId, transId);
            return ResetEmailFaceFlowInitResult.NextStep.KYC;
        }
        // 如果用户存在有kyc信息，则需要检查用户的kyc数据是否能直接做人脸识别
        Long certificateId = certificateResult.getCertificateId();
        Integer certificateType = certificateResult.getCertificateType();
        if (!faceHandlerHelper.checkCurrentKycCanDoFace(userId, certificateResult, transId, 2)) {
            log.warn("检查当前用户KYC是否能做人脸识别失败, userId:{} transId:{} certificateId:{}", userId, transId, certificateId);
            // 拒绝用户的KYC, 然后让用户去做KYC
            faceHandlerHelper.refusedKycByWithdrawSecurityFace(userId, transId, certificateResult, UserConst.RESET_EMAIL_FACE_REFUSED_KYC);
            return ResetEmailFaceFlowInitResult.NextStep.KYC;
        }
        return ResetEmailFaceFlowInitResult.NextStep.FACE;
    }

    @Override
    protected String emailNotifyTemplate() {
        return null;
    }


    /**
     * 重写父类中获取对比照的方法，只取正式通过的人脸对比照信息
     *
     * @param userId
     * @return
     */
    @Override
    protected String getFaceCheckImage(Long userId) {
        UserFaceReference faceReference = userFaceReferenceMapper.selectByPrimaryKey(userId);
        if (faceReference == null || StringUtils.isBlank(faceReference.getRefImage())) {
            log.info("获取对比照片信息失败. userId:{}", userId);
            throw new BusinessException(AccountErrorCode.FACE_VERIFICATION_MISS_REF_IMAGE);
        }
        return faceReference.getRefImage();
    }

    @Override
    public FacePcResponse facePcResultHandler(String transId, FaceTransType transType, FaceWebResultResponse faceWebResult) {
        FacePcResponse response = super.facePcResultHandler(transId, transType, faceWebResult);
        log.info("用户重置Email PC 端人脸识别结果: userId:{} transId:{}", response.getUserId(), transId, JSON.toJSONString(response));
        if (response != null && response.isSuccess()) {
            callbackResetEmailFlow(response.getUserId(), transId, true);
        }
        return response;
    }
    
    @Override
    public TransactionFaceLog facePcPrivateResultHandler(FaceTransType transType, FacePcPrivateResult result) {
    	TransactionFaceLog transLog = super.facePcPrivateResultHandler(transType, result);
    	if(transLog == null) {
    		return null;
    	}
        log.info("用户重置Email PC 端私有云人脸识别结果: userId:{} transId:{}", transLog.getUserId(), transLog.getTransId(),result);
        if (transLog != null && result.isSuccess()) {
            callbackResetEmailFlow(transLog.getUserId(), result.getTransId(), true);
        }
        return transLog;
    }

    @Override
    public FaceSdkResponse faceSdkResultHandler(String transId, Long userId, FaceTransType faceTransType, FaceSdkVerifyRequest request) {
        FaceSdkResponse response = super.faceSdkResultHandler(transId, userId, faceTransType, request);
        log.info("用户重置Email SDK 端人脸识别结果: userId:{} transId:{}", response.getUserId(), transId, JSON.toJSONString(response));
        if (response != null && response.isSuccess()) {
            callbackResetEmailFlow(response.getUserId(), transId, true);
        }
        return response;
    }

    @Override
    public TransactionFaceLog transFaceAudit(TransFaceAuditRequest auditRequest, FaceTransType faceTransType) {
        TransactionFaceLog faceLog = super.transFaceAudit(auditRequest, faceTransType);
        log.info("用户重置Email 审核人脸识别结果: userId:{} transId:{} status:{}", faceLog.getUserId(), faceLog.getTransId(), faceLog.getStatus());
        if (faceLog.getStatus() == TransFaceLogStatus.PASSED) {
            callbackResetEmailFlow(faceLog.getUserId(), faceLog.getTransId(), true);
        }else if (faceLog.getStatus() == TransFaceLogStatus.FAIL){
            callbackResetEmailFlow(faceLog.getUserId(), faceLog.getTransId(), false);
        }else {
            log.error("人工审核人脸识别状态信息错误. userId:{}, transId:{}", faceLog.getUserId(), faceLog.getTransId());
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return faceLog;
    }

    /**
     * 人脸识别通过，回调上级流程
     * @param userId
     * @param transId
     * @param isPass true-通过，false-拒绝
     */
    private void callbackResetEmailFlow(Long userId, String transId, boolean isPass) {
        // todo 通知上一级业务，人脸识别通过，进入到下一步
        try {
            if (isPass) {
                if (EncryptUtil.isHexNumber(transId)){
                    String decry = EncryptUtil.decryptHex(transId,"sharingordlw");
                    userEmailChangeHandler.updateStatus(decry);
                }else {
                    userEmailChangeHandler.updateStatus(transId);
                }
            }
        } catch (Exception e) {
           log.error("callbackResetEmailFlow error is {},transId is {}",e,transId);
        }
    }
}
