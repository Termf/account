package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
public class AddressInfoSubmitMasterValidator extends AbstractKycCertificateValidator<AddressInfoSubmitRequest> {

	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private KycFillInfoHistoryMapper kycFillInfoHistoryMapper;
	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Override
	public void validateRequest(AddressInfoSubmitRequest request) {
		if (request.getUserId() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (request.getSource() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (StringUtils.isAnyBlank(request.getCountry(), request.getCity(),
				request.getAddress(),  request.getBillFile(), request.getBillFileName())) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		String billFillName = request.getBillFileName().toLowerCase().trim();

		if (!billFillName.endsWith(".pdf") && !billFillName.endsWith(".png") && !billFillName.endsWith(".jpg")
				&& !billFillName.endsWith(".jpeg")) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		validateUserSecurityCountry(request.getCountry(), request.getUserId());
		request.setCountry(request.getCountry().toUpperCase());

	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getAddressStatus())) {
			log.warn("提交地址信息kycCertificate信息已经Pass 不允许再次提交. userId:{}", kycCertificate.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}

		if(!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			log.warn("提交地址信息kyc状态不为Pass 不允许提交. userId:{} status:{}", kycCertificate.getUserId(),kycCertificate.getStatus());
			throw new BusinessException(AccountErrorCode.KYC_ADDRESS_SUBMIT_KYC_NOT_PASS);
		}

		// 如果用户的risk_rating没记录，不能提交
		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(kycCertificate.getUserId());
		if (ratings == null || ratings.isEmpty()) {
			log.warn("提交地址认证信息需要riskRating中有值. userId:{}", kycCertificate.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_NOT_NEED_SUBMIT_KYC_ADDRESS);
		}
		boolean enable = false;
		for (UserChannelRiskRating userChannelRiskRating : ratings) {
			if(UserRiskRatingStatus.ENABLE.name().equals(userChannelRiskRating.getStatus())) {
				enable = true;
				break;
			}
		}
		if(!enable) {
			log.warn("提交地址认证信息需要riskRating中无enable记录. userId:{}", kycCertificate.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_NOT_NEED_SUBMIT_KYC_ADDRESS);
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
