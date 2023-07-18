package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.master.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

@Log4j2
@Service
public class AddressInfoSubmitValidator extends AbstractKycCertificateValidator<AddressInfoSubmitRequest> {

	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private KycFillInfoHistoryMapper kycFillInfoHistoryMapper;

	@Override
	public void validateRequest(AddressInfoSubmitRequest request) {
		if (request.getUserId() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (request.getSource() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (StringUtils.isAnyBlank(request.getCountry(), request.getRegionState(), request.getCity(),
				request.getAddress(), request.getPostalCode(), request.getBillFile(), request.getBillFileName())) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		String billFillName = request.getBillFileName().toLowerCase().trim();

		if (!billFillName.endsWith(".pdf") && !billFillName.endsWith(".png") && !billFillName.endsWith(".jpg")
				&& !billFillName.endsWith(".jpeg")) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		validateUserSecurityCountry(request.getCountry(), request.getUserId());
		request.setCountry(request.getCountry().toUpperCase());

		CountryState countryState = CountryStateHelper.getCountryStateByPk(request.getCountry(),
				request.getRegionState());

		if (countryState == null || !countryState.getEnable()) {
			throw new BusinessException(AccountErrorCode.KYC_REGION_STATES_DISABLE);
		}
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			log.warn("提交地址信息 baseInfo信息未通过。不允许提交address信息 userId:{}", kycCertificate.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_BASE_INFO_NOT_PASS);
		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getAddressStatus())) {
			log.warn("提交地址信息kycCertificate信息已经Pass 不允许再次提交. userId:{}", kycCertificate.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		int configCount = apolloCommonConfig.getKycAddressSubmitCount();
		int configTime = apolloCommonConfig.getKycAddressSubmitTime();
		if (configCount <= 0 || configTime <= 0) {
			// 配置小于等于0的时候不做限制
			return;
		}
		Long userId = kycCertificate.getUserId();
		Date endTime = DateUtils.getNewUTCDate();
		Date startTime = DateUtils.add(endTime, Calendar.MINUTE, -configTime);
		int currentCount = kycFillInfoHistoryMapper.getHistoryCount(userId, KycFillType.ADDRESS.name(), startTime,
				endTime) + 1;
		if (currentCount > configCount) {
			log.warn("地址认证信息提交次数在{}分钟内达到{}次, userId:{}", configTime, currentCount, userId);
			throw new BusinessException(AccountErrorCode.KYC_ADDRESS_SUBMIT_OUT_COUNT);
		}
	}
}
