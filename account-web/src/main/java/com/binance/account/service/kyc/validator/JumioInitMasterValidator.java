package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.inspector.common.enums.JumioHandlerType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.master.error.BusinessException;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Resource;

@Service
@Log4j2
public class JumioInitMasterValidator extends AbstractKycCertificateValidator<KycFlowRequest> {

	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;

	@Override
	public void validateRequest(KycFlowRequest req) {
//		super.validateUserSecurity(req.getUserId());
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();
		if (StringUtils.equalsIgnoreCase(KycCertificateStatus.PASS.name(), kycCertificate.getJumioStatus())) {
			// jumio 已经通过的情况下不能再做jumio.
			log.info("KYC认证JUMIO => jumio认证已经通过，不能再做. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}
		if (StringUtils.equalsIgnoreCase(KycCertificateStatus.REVIEW.name(), kycCertificate.getJumioStatus())) {
			// jumio 正在审核中, 不能在做jumio认证
			log.info("KYC认证JUMIO => jumio认证正在审核中. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		JumioHandlerType handlerType = KycCertificateKycType.getByCode(kycCertificate.getKycType()) == KycCertificateKycType.USER ?
				JumioHandlerType.USER_KYC : JumioHandlerType.COMPANY_KYC;
		Long userId = kycCertificate.getUserId();
		int configCount = apolloCommonConfig.getKycJumioDailyCount();
		if (configCount <= 0) {
			// 当配置值小于等于0时不做限制
			return;
		}
		long currentCount = jumioBusiness.getDailyJumioTimes(userId, handlerType);
		if (currentCount > configCount) {
			log.warn("KYC认证JUMIO => jumio认证申请达到单日限制次数. userId:{} currentCount:{}", userId, currentCount);
			throw new BusinessException(AccountErrorCode.KYC_JUMIO_OUT_DAILY_COUNT);
		}
	}
}
