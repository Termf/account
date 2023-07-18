package com.binance.account.service.kyc.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class FaceOcrAuditValidator extends AbstractKycCertificateValidator<FaceOcrAuthRequest> {

	@Override
	public void validateRequest(FaceOcrAuthRequest req) {
		if (req.getStatus() == null || req.getUserId() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		// 审核通过流程 face faceCheck 不能为空
		if(IdCardOcrStatus.PASS.name().equals(req.getStatus())
				&& StringUtils.isAnyBlank(req.getFace(),req.getFaceCheck()) ) {
			log.warn("face ocr 审核通过，缺少face，faceCheck 参数. userId:{}",req.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
		}
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		if (kycCertificate == null) {
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}

//		if (StringUtils.isBlank(kycCertificate.getFaceOcrStatus())) {
//			log.warn("Face Ocr审核，当前kyc的face ocr状态为空，不允许审核 userId:{}", kycCertificate.getUserId());
//			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
//		}

//		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
//			throw new BusinessException(AccountErrorCode.KYC_AUTH_CANT_PROCESS);
//		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		// TODO Auto-generated method stub

	}
}