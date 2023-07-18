package com.binance.account.service.kyc.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

@Service
public class GoogleFormAuditValidator extends AbstractKycCertificateValidator<KycAuditRequest> {

	@Override
	public void validateRequest(KycAuditRequest req) {
		if (req.getKycCertificateStatus() == null || req.getUserId() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		if (KycCertificateStatus.REFUSED.equals(req.getKycCertificateStatus()) && StringUtils.isBlank(req.getTips())) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		if (kycCertificate == null) {
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}
	}

	public void validateKycCertificateStatus(KycCertificate kycCertificate, KycCertificateStatus authStatus) {
		
		validateKycCertificateStatus(kycCertificate);
		
		if (authStatus.name().equals(kycCertificate.getGoogleFormStatus())) {
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
		if(KycCertificateStatus.REFUSED.equals(authStatus)) {
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
		if (!KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		// TODO Auto-generated method stub

	}

}
