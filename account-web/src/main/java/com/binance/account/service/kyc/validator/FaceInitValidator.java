package com.binance.account.service.kyc.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.FaceInitFlowRequest;
import com.binance.master.error.BusinessException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FaceInitValidator extends AbstractKycCertificateValidator<FaceInitFlowRequest> {

	@Override
	public void validateRequest(FaceInitFlowRequest req) {

	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();
		if (StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getFaceStatus())) {
			log.info("KYC认证FACE => 人脸识别已经通过, 不能再次操作. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}
		if (StringUtils.equalsIgnoreCase(KycCertificateStatus.REVIEW.name(), kycCertificate.getFaceStatus())) {
			log.info("KYC认证FACE => 人脸识别正在审核中. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		// do nothing
	}
}
