package com.binance.account.service.kyc.executor.us;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFLowExecutorHelper;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.kyc.request.JumioAuthRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Log4j2
@Service
public class JumioAuthResultUsExecutor extends AbstractKycFlowCommonExecutor {

    @Resource
    private KycCertificateMapper kycCertificateMapper;
    @Resource
    private IFace iFace;
    @Resource
    private IUserFace iUserFace;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;
    
    @Resource
    private KycFLowExecutorHelper kycFLowExecutorHelper;

    @Override
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
    	KycFlowResponse response = new KycFlowResponse();
    	if (kycFlowRequest == null) {
    		return response;
    	}
        Long userId = kycFlowRequest.getUserId();
        response.setKycType(kycFlowRequest.getKycType());
        response.setUserId(userId);
        JumioAuthRequest authRequest = (JumioAuthRequest) kycFlowRequest;
        // 获取kyc认证信息
        KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
        if (kycCertificate == null) {
            throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
        }
        KycCertificateStatus oldJumoStatus = KycCertificateStatus.getByName(kycCertificate.getJumioStatus());
        // jumio 状态转化
        JumioStatus jumioStatus = JumioStatus.getByName(authRequest.getJumioStatus());
        if (jumioStatus == null) {
            log.warn("KYC JUMIO AUTH => 状态转换错误. userId:{} authStatus:{}", userId, authRequest.getJumioStatus());
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        // 只处理 review/pass/refused等状态
        kycCertificate.setJumioTips(authRequest.getMessage());
        switch (jumioStatus) {
            case PASSED:
                kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
                break;
            case REVIEW:
            case UPLOADED:
                kycCertificate.setJumioStatus(KycCertificateStatus.REVIEW.name());
                break;
            case REFUED:
            case ERROR:
            case EXPIRED:
                kycCertificate.setJumioStatus(KycCertificateStatus.REFUSED.name());
                break;
            default:
                // do nothing
                return response;
        }
        log.info("KYC JUMIO AUTH => JUMIO状态变更: userId:{}, jumioStatus: message:{}", userId, jumioStatus, authRequest.getMessage());
        kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
        kycCertificateMapper.updateJumioStatus(kycCertificate);
        // 设置上下文信息
        KycFlowContext.getContext().setKycCertificate(kycCertificate);
        KycFlowContext.getContext().setKycFlowResponse(response);

        // Jumio 审核变化的话，需要处理一些后续处理逻辑(这些后续处理是由于可能不会触发End处理器的补充)
        kycFLowExecutorHelper.jumioChangeAfterHandler(kycCertificate, oldJumoStatus);

        return response;
    }

}
