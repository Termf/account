package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.country.ICountry;
import com.binance.account.utils.RegexUtils;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

@Service
@Log4j2
public class BaseInfoSubmitValidator extends AbstractKycCertificateValidator<BaseInfoRequest> {

	@Resource
	private ICountry iCountry;
	
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	
	@Resource
	private KycFillInfoHistoryMapper kycFillInfoHistoryMapper;
	
	@Override
	public void validateRequest(BaseInfoRequest req) {
		
		KycCertificateKycType kycType = req.getKycType();
		
		switch (kycType) {
		case USER:
			if (StringUtils.isAnyBlank(req.getFirstName(), req.getLastName(), req.getBirthday(), req.getAddress(),
					req.getCity(), req.getCountry())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			break;

		default:
			if (StringUtils.isAnyBlank(req.getCompanyName(), req.getContactNumber(), req.getRegisterName())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			break;
		}
		
		
		// 验证country输入是否正确，和是否已经绑定过 google auth/mobile
		super.validateUserSecurityCountry(req.getCountry(), req.getUserId());
		// tin格式校验
		if (StringUtils.isNotBlank(req.getTin())) {
			if (!RegexUtils.matchTin(req.getTin())) {
				log.warn("tin格式错误, userId:{}, tin:{}", req.getUserId(), req.getTin());
				throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
			}
		}
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();

		if (StringUtils.isBlank(kycCertificate.getBaseFillStatus())) {
			return;
		}
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus()) || 
				KycCertificateStatus.FORBID_PASS.name().equals(kycCertificate.getStatus())) {
			log.warn("基本信息提交kycCertificate当前状态{}不允许再次提交 userId:{}", kycCertificate.getBaseFillStatus(), userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}
			
//		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
//			log.warn("基本信息提交kycCertificate当前状态{}不允许再次提交 userId:{}", kycCertificate.getBaseFillStatus(), userId);
//			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
//		}
		if (KycCertificateStatus.REVIEW.name().equals(kycCertificate.getBaseFillStatus())) {
			log.warn("基本信息提交kycCertificate当前状态{}不允许再次提交 userId:{}", kycCertificate.getBaseFillStatus(), userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
		}
	}

	@Override
	public void validateRequestCount(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();
		int configCount = apolloCommonConfig.getKycBaseSubmitCount();
		int configTime = apolloCommonConfig.getKycBaseSubmitTime();
		if (configCount <= 0 || configTime <= 0) {
			// 配置小于等于0时不限制
			return;
		}
		Date endTime = DateUtils.getNewUTCDate();
		Date startTime = DateUtils.add(endTime, Calendar.MINUTE, -configTime);
		int currentCount = kycFillInfoHistoryMapper.getHistoryCount(userId, KycFillType.BASE.name(), startTime, endTime)
				+ 1;
		if (currentCount > configCount) {
			log.warn("基本信息提交次数在{}分钟内达到{}次, userId:{}", configTime, currentCount, userId);
			throw new BusinessException(AccountErrorCode.KYC_BASE_SUBMIT_OUT_COUNT);
		}
	}


}
