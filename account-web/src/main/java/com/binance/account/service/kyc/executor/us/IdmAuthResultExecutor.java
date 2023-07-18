package com.binance.account.service.kyc.executor.us;

import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.vo.kyc.request.IdmAuthRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.error.BusinessException;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class IdmAuthResultExecutor extends AbstractKycFlowCommonExecutor {

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		IdmAuthRequest request = (IdmAuthRequest) kycFlowRequest;
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(request.getUserId());
		if(kycCertificate == null) {
			log.warn("idmAuthResult对应kycCertificate不存在 userId:{}", request.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}
		
		if(KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			log.info("idmAuthResult当前kycCertificate记录baseInfo状态不允许修改 userId:{},baseFillStatus:{}",request.getUserId(),kycCertificate.getBaseFillStatus());
			return null;
		}
		
		KycCertificate record = new KycCertificate();
		record.setUserId(kycCertificate.getUserId());
		if("A".equals(request.getState())) {
			record.setBaseFillStatus(KycCertificateStatus.PASS.name());
			record.setBaseFillTips("base info pass");
		}else {
			record.setBaseFillStatus(KycCertificateStatus.REVIEW.name());
			kycCertificate.setBaseFillTips("idm fail exception need manual review");
		}
		
		kycCertificateMapper.updateStatus(kycCertificate);
		return null;
	}

}
