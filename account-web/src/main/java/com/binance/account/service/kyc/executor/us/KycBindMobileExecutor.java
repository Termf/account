package com.binance.account.service.kyc.executor.us;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurityCache;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.request.KycBindMobileRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.RedisVerify;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.WebUtils;
import com.binance.messaging.api.msg.request.SendMsgRequest;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.binance.master.utils.StringUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class KycBindMobileExecutor extends AbstractKycFlowCommonExecutor {

	private static final String SEND_BIND_MOBILE_VERIFY_CODE_KEY = "kyc:account:bind:mobile";

	@Resource
	private ICountry iCountry;

	@Resource
	private UserCommonBusiness userCommonBusiness;

	@Resource
	private ApolloCommonConfig commonConfig;

	private static final String KYC_SMS_VERIFY_CACHE_KEY = "KYC_SMS_VERIFY";

	private static final int ERROR_COUNT = 3;

	private static final String KYC_MOBILE_TIME_CACHE_KEY = "KYC_MOBILE_TIME";
	
	@Value("${kyc.validate.sms.code:true}")
    private boolean kycValidSmsCode;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		try {
			KycBindMobileRequest request = (KycBindMobileRequest) kycFlowRequest;

			validateBindMobile(request);

			Long userId = request.getUserId();
			String mobile = request.getMobile();
			String mobileCode = request.getMobileCode();
			String smsCode = request.getSmsCode();

			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

			if (kycCertificate == null) {
				throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS, "用户kyc信息不存在");
			}
			if (StringUtils.isNotBlank(kycCertificate.getBindMobile())) {
				log.info("用户已绑定手机不允许再次绑定. userId:{}", userId);
				throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_MOBILE_BIND, "用户已绑定手机不允许再次绑定");
			}
			
			if(!KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
				log.warn("提交地址信息 baseInfo信息未通过。不允许提交address信息 userId:{}", request.getUserId());
				throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_BASE_INFO_NOT_PASS);
			}

			UserSecurityCache userSecurityCache = RedisCacheUtils.get(userId.toString(), UserSecurityCache.class,
					KYC_SMS_VERIFY_CACHE_KEY);
			// error time + 3秒 判断3秒内多次提交
			if (userSecurityCache != null) {
				if (userSecurityCache.getSmsErrorTime() != null
						&& (userSecurityCache.getSmsErrorTime() + TimeUnit.SECONDS.toMillis(3)) > DateUtils
								.getNewUTCTimeMillis()) {
					throw new BusinessException(GeneralCode.USER_FAILED_TIME_LIMT);
				}
			}

			String mobileKey = StringUtils.getMobileKey(mobile, mobileCode);
			RedisVerify redisVerify = RedisCacheUtils.get(mobileKey, RedisVerify.class, KYC_MOBILE_TIME_CACHE_KEY);

			if (null == redisVerify || StringUtils.isBlank(redisVerify.getCode())) {
				throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_EXPIRED);
			}

			if (null != redisVerify.getErrorCount() && redisVerify.getErrorCount() >= ERROR_COUNT) {
				throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_TIME_LIMITE);
			}

			Long expireTime = 30L;// 有效期30分钟

			if (userSecurityCache == null) {
				userSecurityCache = new UserSecurityCache();
			}

			//校验短信开关 打开-校验短信，关闭 直接通过
			boolean validateCode = kycValidSmsCode ? StringUtils.equals(redisVerify.getCode(), commonConfig.convertSecretCode(mobile, smsCode)): true;
			
			if (!validateCode) {
				redisVerify.setErrorCount(redisVerify.getErrorCount() + 1);

				if (redisVerify.getErrorCount() != null && redisVerify.getErrorCount() >= ERROR_COUNT) {
					// redis 删除短信验证码
					// RedisCacheUtils.del(mobileKey, CacheKeys.MOBILE_AUTH_TIME);
					RedisCacheUtils.set(mobileKey, redisVerify, -1, KYC_MOBILE_TIME_CACHE_KEY);

					userSecurityCache.setSmsErrorTime(DateUtils.getNewUTCTimeMillis());
					RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60,
							KYC_SMS_VERIFY_CACHE_KEY);

					throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_TIME_LIMITE);
				} else {
					// count +1 更新
					RedisCacheUtils.set(mobileKey, redisVerify, -1, KYC_MOBILE_TIME_CACHE_KEY);
				}

				userSecurityCache.setSmsErrorTime(DateUtils.getNewUTCTimeMillis());
				RedisCacheUtils.set(userId.toString(), userSecurityCache, expireTime * 60, KYC_SMS_VERIFY_CACHE_KEY);
				throw new BusinessException(GeneralCode.USER_MOBILE_AUTH_CODE_ERROR);
			}
			KycCertificate record = new KycCertificate();
			record.setBindMobile(mobile);
			record.setUserId(kycCertificate.getUserId());
			record.setMobileCode(mobileCode);
			kycCertificateMapper.updateByPrimaryKeySelective(record);

			return new KycFlowResponse();
		} catch (BusinessException e) {
			log.error(String.format("绑定手机执行异常 userId:%s", kycFlowRequest.getUserId()),e);
			throw e;
		} catch (Exception e) {
			log.error(String.format("绑定手机执行异常 userId:%s", kycFlowRequest.getUserId()),e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public KycFlowResponse sendBindMobileVerifyCode(KycBindMobileRequest request) {
		try {
			validateSendCode(request);

			Long userId = request.getUserId();

			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

			if (kycCertificate == null) {
				throw new BusinessException(GeneralCode.USER_NOT_EXIST, "用户kyc信息不存在");
			}
			if (StringUtils.isNotBlank(kycCertificate.getBindMobile())) {
				log.warn("用户已绑定手机不允许再次绑定. userId:{}", userId);
				throw new BusinessException(GeneralCode.USER_MOBILE_BIND, "用户已绑定手机不允许再次绑定");
			}
			
			// 一分钟频率控制
			String frequencyLimits = RedisCacheUtils.get(String.valueOf(userId), String.class,
					SEND_BIND_MOBILE_VERIFY_CODE_KEY);
			if (StringUtils.isNotBlank(frequencyLimits)) {
				throw new BusinessException(GeneralCode.COMMON_TRY_AGAIN_LATER, new Object[] { 1 });
			}
			RedisCacheUtils.set(String.valueOf(userId), String.valueOf(userId), 60L, SEND_BIND_MOBILE_VERIFY_CODE_KEY);

			String code = StringUtils.getNumberRandomString(6);

			SendMsgRequest requestSms = new SendMsgRequest();
			requestSms.setIp(WebUtils.getRequestIp());
			Country country = this.iCountry.getCountryByCode(request.getMobileCode());
			requestSms.setMobileCode(country.getMobileCode());
			requestSms.setRecipient(request.getMobile());
			requestSms.setUserId(request.getUserId().toString());
			requestSms.setTplCode(Constant.NODE_TYPE_MOBILE_VERIFY);

			// 绑定手机加上 verifyCodeId
//	        requestSms.setVerifyCodeId(requestBody.getVerifyCodeId());

			Map<String, Object> params = new HashMap<>();
			params.put(Constant.MESSAGE_TEMPLATE_PROP_VERIFYCODE, code);

			requestSms.setData(params);

			// 发送短信
			userCommonBusiness.sendMsg(requestSms, WebUtils.getAPIRequestHeader().getLanguage(),
					WebUtils.getAPIRequestHeader().getTerminal());

			// 有效期30分钟
			Long expireTime = 30L;
			String mobileKey = StringUtils.getMobileKey(request.getMobile(), request.getMobileCode());

			RedisVerify redisVerify = new RedisVerify();
			redisVerify.setTime(DateUtils.getNewUTCDate());
			redisVerify.setCode(commonConfig.convertSecretCode(request.getMobile(), code));
			redisVerify.setErrorCount(0);

			// 验证码
			RedisCacheUtils.set(mobileKey, redisVerify, expireTime * 60L, KYC_MOBILE_TIME_CACHE_KEY);

			return new KycFlowResponse();
		} catch (BusinessException e) {
			log.warn(String.format("绑定手机发送验证码执行异常 userId:%s", request.getUserId()) , e);
			throw e;
		} catch (Exception e) {
			log.error(String.format("绑定手机发送验证码执行异常 userId:%s", request.getUserId()) , e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	private void validateBindMobile(KycBindMobileRequest request) {
		validateSendCode(request);
		if (StringUtils.isBlank(request.getSmsCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
	}

	private void validateSendCode(KycBindMobileRequest request) {
		if (request.getUserId() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (StringUtils.isAnyBlank(request.getMobileCode(), request.getMobile())) {
			throw new BusinessException(AccountErrorCode.KYC_ENTER_MOBILE);
		}
		Country country = this.iCountry.getCountryByCode(request.getMobileCode());
		if (country == null) {
			log.warn("发送kyc验证码国家代码不正确. userId:{} countryCode:{}", request.getUserId(), request.getMobileCode());
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
	}
}
