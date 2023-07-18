package com.binance.account.service.face;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.service.security.IFace;
import com.binance.account.vo.kyc.request.JumioAuthRequest;
import com.binance.account.vo.user.request.CompanyCertificateAuditRequest;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.inspector.common.enums.FaceErrorCode;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.faceid.response.ImageCanDoFaceResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Log4j2
@Component
public class FaceHandlerHelper {

    @Resource
    private IFace iFace;
    @Resource
    private IUserCertificate iUserCertificate;
    @Resource
    private IUserKyc iUserKyc;
    @Resource
    private KycFlowProcessFactory kycFlowProcessFactory;

    /**
     * 判断是否能跳过kyc的upload逻辑
     * @param certificateResult
     * @return
     */
    public boolean canSkipKycUpload(KycCertificateResult certificateResult) {
        Integer certificateStatus = certificateResult.getCertificateStatus();
        Integer certificateType = certificateResult.getCertificateType();
        Long certificateId = certificateResult.getCertificateId();
        boolean isForbidPassed = certificateResult.isForbidPassed();
        if (certificateStatus == null || certificateType == null
                || certificateId == null) {
            return false;
        }
        if (Objects.equals(KycCertificateResult.STATUS_PASS, certificateStatus)) {
            return true;
        }else if (Objects.equals(KycCertificateResult.STATUS_REFUSED, certificateStatus) && isForbidPassed) {
            log.info("用户的KYC认证时由于不合规国籍验证通过的类型，允许直接做人脸识别, certificateId:{} certificateType:{}",
                    certificateId, certificateType);
            return true;
        }else {
            return false;
        }
    }

    /**
     * 检查照片是否能做人脸识别
     * @param userId
     * @param certificateResult
     * @param transId
     * @param retryTimes 如过检查人脸识别失败或者超时时，进行尝试的次数
     * @return
     */
    public boolean checkCurrentKycCanDoFace(Long userId, KycCertificateResult certificateResult, String transId, Integer retryTimes) {
        UserFaceReference faceReference = iFace.getUserFaceByMasterBD(userId);
        if (faceReference != null && StringUtils.isNotBlank(faceReference.getRefImage())) {
            return true;
        }
        // 如果没有对比照片，获取KYC的对比照片后检查是否能做人脸识别
        String checkImage = iFace.getCertificateImage(userId, certificateResult);
        if (StringUtils.isBlank(checkImage)) {
            log.info("获取KYC检查做人脸识别的图片地址失败. userId:{} transId:{}", userId, transId);
            return false;
        }
        //如果能获取到，检查图片是否能做人脸识别
        ImageCanDoFaceResponse imageValidate = loopValidImage(userId, transId, checkImage, retryTimes);
        if (imageValidate != null && imageValidate.isSuccess()) {
            log.info("检查到当前用户KYC照片允许做人脸识别: userId:{} transId:{} ", userId, transId);
            // 保存做人脸识别使用的对比照信息
            boolean saveFaceRef = iFace.saveKycPassUserFaceReference(userId, transId, imageValidate);
            if (saveFaceRef) {
                return true;
            }else {
                // 再次尝试保存
                saveFaceRef = iFace.saveKycPassUserFaceReference(userId, transId, imageValidate);
                return saveFaceRef;
            }
        }else {
            log.info("检查到当前用户KYC照片无法做人脸识别. userId:{} transId:{} ", userId, transId);
            return false;
        }
    }

    /**
     * 循环校验照片是否能做人脸识别
     * @param userId
     * @param transId
     * @param imageRef
     * @param count 尝试次数
     * @return
     */
    public ImageCanDoFaceResponse loopValidImage(Long userId, String transId, String imageRef, Integer count) {
        ImageCanDoFaceResponse imageValidate = null;
        int times = count <= 0 ? 1 : count;
        while (times > 0) {
            times--;
            imageValidate = iFace.validateFaceImageCanUsed(userId, transId, imageRef, true, true);
            if (imageValidate == null || FaceErrorCode.TOKEN_INIT_OTHER.equals(imageValidate.getFaceErrorCode())) {
                log.info("人脸图片验证是否能做人脸识别超时，等待再次验证. userId:{} transId:{}", userId, transId);
                try {
                    //等待5秒后再次尝试验证
                    Thread.sleep(3000);
                }catch (InterruptedException e) {
                    // do nothing
                }
            }else {
                log.info("人脸图片校验完成, 直接退出再试逻辑. userId:{} transId:{}", userId, transId);
                break;
            }
        }
        return imageValidate;
    }

    /**
     * 把用户的KYC认证拒绝掉，然后让用户重做KYC认证
     * @param userId
     * @param certificateResult
     * @param failReason 拒绝原因
     * @return 拒绝失败原因
     */
    public String refusedKycByWithdrawSecurityFace(Long userId, String transId, KycCertificateResult certificateResult, String failReason) {
        String refusedResult = null;
        if (certificateResult.isNewVersion()) {
            log.info("kyc 是新流程认证，拒绝新流程认证直接通过执行器拒绝. userId:{} transId:{}", userId, transId);
            try {
                // 新版本按用户处理的逻辑
                JumioAuthRequest jumioAuthRequest = new JumioAuthRequest();
                jumioAuthRequest.setJumioStatus(JumioStatus.REFUED.name());
                jumioAuthRequest.setMessage(failReason);
                jumioAuthRequest.setUserId(userId);
                kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_JUMIO_AUTH_RESULT).process(jumioAuthRequest);
                return null;
            } catch (Exception e) {
                log.warn("withdraw face => 拒绝认证处理逻辑处理异常. userId:{} transId:{}", userId, transId, e);
                return "kyc认证拒绝失败";
            }
        }
        log.info("kyc 老流程认证，拒绝老流程认证信息. userId:{} transId:{}", userId, transId);
        Long certificateId = certificateResult.getCertificateId();
        Integer certificateType = certificateResult.getCertificateType();
        if (certificateId == null || certificateType == null) {
            log.info("认证类型信息错误。userId:{} transId:{}", userId, transId);
            return "获取用户认证信息失败";
        }
        try {
            switch (certificateType) {
                case KycCertificateResult.TYPE_COMPANY:
                    log.info("拒绝用户的企业认证. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    // 先进行重置然后再进行拒绝
                    CompanyCertificateAuditRequest companyCertificateAuditRequest = new CompanyCertificateAuditRequest();
                    companyCertificateAuditRequest.setUserId(userId);
                    companyCertificateAuditRequest.setId(certificateId);
                    companyCertificateAuditRequest.setStatus(CompanyCertificateStatus.jumioPassed);
                    APIResponse companyReset = iUserCertificate.companyAuditCertificate(APIRequest.instance(companyCertificateAuditRequest));
                    if (companyReset.getStatus() != APIResponse.Status.OK) {
                        log.warn("重置企业认证失败, userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                        return "用户企业认证重置失败";
                    }
                    //等待1.0秒中后做拒绝操作（尽量防止下主从库数据同步的问题）
                    Thread.sleep(1000);
                    log.info("重置用户企业认证成功后再设置为认证失败. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    companyCertificateAuditRequest.setStatus(CompanyCertificateStatus.refused);
                    companyCertificateAuditRequest.setInfo(failReason);
                    APIResponse companyRefused = iUserCertificate.companyAuditCertificate(APIRequest.instance(companyCertificateAuditRequest));
                    if (companyRefused.getStatus() != APIResponse.Status.OK) {
                        log.warn("拒绝企业认证失败, userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                        return "用户企业认证重置成功后拒绝失败";
                    }

                    log.info("拒绝用户企业认证成功. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    break;
                case KycCertificateResult.TYPE_USER:
                    log.info("进行拒绝用户的个人认证. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    KycAuditRequest kycAuditRequest = new KycAuditRequest();
                    kycAuditRequest.setId(certificateId);
                    kycAuditRequest.setUserId(userId);
                    kycAuditRequest.setStatus(KycStatus.jumioPassed);
                    APIResponse userKycReset = iUserKyc.audit(APIRequest.instance(kycAuditRequest));
                    if (userKycReset.getStatus() != APIResponse.Status.OK) {
                        log.warn("重置个人认证失败. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                        return "用户个人认证重置失败";
                    }
                    //等待1.0秒中后做拒绝操作（尽量防止下主从库数据同步的问题）
                    Thread.sleep(1000);
                    log.info("重置用户个人验证成功后再设置为认证失败, userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    kycAuditRequest.setStatus(KycStatus.refused);
                    kycAuditRequest.setFailReason(failReason);
                    APIResponse userKycRefused = iUserKyc.audit(APIRequest.instance(kycAuditRequest));
                    if (userKycRefused.getStatus() != APIResponse.Status.OK) {
                        log.warn("拒绝个人认证失败. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                        return "用户个人认证重置成功后拒绝失败";
                    }
                    log.info("拒绝用户个人认证成功. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    break;
                default:
                    log.info("用户KYC认证类型错误, 不能进行拒绝. userId:{} certificateId:{} transId:{}", userId, certificateId, transId);
                    refusedResult = "用户KYC认证类型错误";
                    break;
            }
        }catch (Exception e) {
            log.error("拒绝用户KYC认证失败. userId:{} certificateId:{} transId:{}", userId, certificateId, transId, e);
            refusedResult = e.getMessage();
        }
        log.info("拒绝用户的KYC认证让用户重做KYC. userId:{} certificateId:{} transId:{} refuseResult:{} ", userId, certificateId, transId, refusedResult);
        return refusedResult;
    }
}
