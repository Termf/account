package com.binance.account.service.kyc.validator;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.service.country.ICountry;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

@Log4j2
public abstract class AbstractKycCertificateValidator<T extends KycFlowRequest> {

	@Resource
	public ICountry iCountry;

	@Resource
	private UserSecurityMapper userSecurityMapper;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;

	public void validateApiRequest(T req) {
		KycFlowRequest request = (KycFlowRequest) req;
		if (request.getUserId() == null) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		validateRequest(req);
	}

	public void validateUserSecurityCountry(String countryCode, Long userId) {
		// 验证country输入是否正确，
		Country country = iCountry.getCountryByCode(countryCode);
		if (country == null) {
			log.warn("基本信息提交国家代码不正确. userId:{} countryCode:{}", userId, country);
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		// 没有做过Google 2FA或者SMS
		if (apolloCommonConfig.isKycNeed2faSwitch()) {
			UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
			if (userSecurity == null || StringUtils.isAllBlank(userSecurity.getAuthKey(), userSecurity.getMobile())) {
				log.warn("基本信息提交userSecurity信息不正确. userId:{} countryCode:{}", userId);
				throw new BusinessException(GeneralCode.USER_SERCURITY_NOT_BIND);
			}
		}
	}

//	public void validateUserSecurity(Long userId) {
//		// 没有做过Google 2FA或者SMS
//		UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
//		if (userSecurity == null || StringUtils.isAllBlank(userSecurity.getAuthKey(), userSecurity.getMobile())) {
//			log.warn("基本信息提交userSecurity信息不正确. userId:{} countryCode:{}", userId);
//			throw new BusinessException(GeneralCode.USER_SERCURITY_NOT_BIND);
//		}
//	}

	public abstract void validateRequest(T req);

	public abstract void validateKycCertificateStatus(KycCertificate kycCertificate);

	public abstract void validateRequestCount(KycCertificate kycCertificate);

}
