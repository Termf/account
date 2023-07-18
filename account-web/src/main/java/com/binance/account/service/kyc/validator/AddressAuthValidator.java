package com.binance.account.service.kyc.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

@Service
public class AddressAuthValidator extends AbstractKycCertificateValidator<AddresAuthResultRequest> {

	@Override
	public void validateRequest(AddresAuthResultRequest req) {
		if (req.getAddressStatus() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (StringUtils.isBlank(req.getAddressTips()) && KycCertificateStatus.REFUSED.equals(req.getAddressStatus())) {
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

		String curStatus = kycCertificate.getAddressStatus();


		switch (authStatus) {
		case PASS:
//			if (KycCertificateStatus.REFUSED.name().equals(curStatus)) {
//				throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
//			}
			break;
		case REFUSED:
			break;
		case REVIEW:
			if (KycCertificateStatus.PASS.name().equals(curStatus)) {
				throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		// Do nothing
	}
}
