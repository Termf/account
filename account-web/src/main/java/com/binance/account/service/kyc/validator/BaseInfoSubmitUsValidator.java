package com.binance.account.service.kyc.validator;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycTaxidIndex;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.account.data.mapper.certificate.KycTaxidIndexMapper;
import com.binance.account.data.mapper.certificate.TaxIdBlacklistMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

@Log4j2
@Service
public class BaseInfoSubmitUsValidator extends AbstractKycCertificateValidator<BaseInfoRequest> {

	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private KycFillInfoHistoryMapper kycFillInfoHistoryMapper;
	@Resource
	private KycTaxidIndexMapper kycTaxidIndexMapper;
	@Resource
	private TaxIdBlacklistMapper taxIdBlacklistMapper;

	@Override
	public void validateRequest(BaseInfoRequest req) {
		if (req.getKycType() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		if (StringUtils.isEmpty(req.getCountry())) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		req.setCountry(req.getCountry().toUpperCase());
		KycCertificateKycType kycType = req.getKycType();

		switch (kycType) {
		// 用户校验
		case USER:
			if (req.getGender() == null) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}

			if (StringUtils.isAnyBlank(req.getFirstName(), req.getLastName(), req.getRegionState(), req.getCity(),
					req.getAddress(), req.getPostalCode(), req.getTaxId())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}

			if (req.getPostalCode().length() != 5 || req.getTaxId().length() != 9) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}

			// 验证生日是否符合要求
			if (StringUtils.isBlank(req.getBirthday())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			try {
				Date birthday = DateUtils.parseDate(req.getBirthday(), DateUtils.SIMPLE_PATTERN);
				Date current = DateUtils.getNewUTCDate();
				int ageLimit = apolloCommonConfig.getKycBasicMinAge();
				if (ageLimit > 0) {
					Date minYear = DateUtils.add(birthday, Calendar.YEAR, ageLimit);
					if (current.compareTo(minYear) < 0) {
						log.warn("kyc basic 年龄小于最小限制. userId:{}", req.getUserId(), req.getBirthday());
						throw new BusinessException(AccountErrorCode.KYC_AGE_VARIFY);
					}
				}
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				log.warn("kyc birthday parse error. userId:{} birthday:{}", req.getUserId(), req.getBirthday());
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			CountryState countryState = CountryStateHelper.getCountryStateByPk(req.getCountry(), req.getRegionState());

			if (countryState == null || !countryState.getEnable()) {
				throw new BusinessException(AccountErrorCode.KYC_REGION_STATES_DISABLE);
			}
			KycTaxidIndex taxiIndex = kycTaxidIndexMapper.selectByPrimaryKey(req.getTaxId());
			if (taxiIndex != null) {
				log.warn("tax id 已经被注册，注册用户:%s,提交用户:%s", taxiIndex.getUserId(), req.getUserId());
				throw new BusinessException(AccountErrorCode.KYC_TAXID_IS_USED);
			}
			// taxId在黑名单里
			if (taxIdBlacklistMapper.getBlacklistByTaxId(req.getTaxId()) != null) {
				log.warn("taxId:{}已被列入黑名单", req.getTaxId());
				throw new BusinessException(AccountErrorCode.KYC_TAX_ID_IN_BLACKLIST);
			}

			break;
		// 企业校验
		case COMPANY:
			if (StringUtils.isAnyBlank(req.getCompanyName(), req.getContactNumber(), req.getRegisterName())) {
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			break;
		default:
			break;
		}

		// 校验国家
		validateUserSecurityCountry(req.getCountry(), req.getUserId());
	}

	@Override
	public void validateKycCertificateStatus(KycCertificate kycCertificate) {
		Long userId = kycCertificate.getUserId();

		if (StringUtils.isBlank(kycCertificate.getBaseFillStatus())) {
			return;
		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			log.warn("基本信息提交kycCertificate当前状态{}不允许再次提交 userId:{}", kycCertificate.getBaseFillStatus(), userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
		}
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
