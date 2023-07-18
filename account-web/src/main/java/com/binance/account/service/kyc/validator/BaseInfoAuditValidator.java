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
public class BaseInfoAuditValidator extends AbstractKycCertificateValidator<KycAuditRequest> {

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
		if (KycCertificateKycLevel.L2.getCode().equals(kycCertificate.getKycLevel())) {
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
	}

	public void validateKycCertificateStatus(KycCertificate kycCertificate, KycCertificateStatus authStatus) {

		validateKycCertificateStatus(kycCertificate);

		String curStatus = kycCertificate.getBaseFillStatus();

		if (authStatus.name().equals(curStatus)) {
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}

		switch (authStatus) {
		case REFUSED:
			if (KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
				// 个人
				// PASS || REVIEW 状态可以拒绝。PASS 必须是L1 REVIEW 必须是L2
				if (KycCertificateStatus.PASS.name().equals(curStatus)
						&& !KycCertificateKycLevel.L1.getCode().equals(kycCertificate.getKycLevel())) {
					throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
				}

				if (KycCertificateStatus.REVIEW.name().equals(curStatus)
						&& !KycCertificateKycLevel.L0.getCode().equals(kycCertificate.getKycLevel())) {
					throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
				}

			} else if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
				// 企业
				// PASS || REVIEW 状态可以拒绝。
				if (!KycCertificateStatus.PASS.name().equals(curStatus)
						&& !KycCertificateStatus.REVIEW.name().equals(curStatus)) {
					throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
				}
			}
			break;
		case PASS:
			if (KycCertificateStatus.REFUSED.name().equals(curStatus)) {
				throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
			}
			break;
		case REVIEW:
//			if (KycCertificateStatus.REFUSED.name().equals(curStatus)) {
//				throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
//			}
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
		// TODO Auto-generated method stub

	}

}
