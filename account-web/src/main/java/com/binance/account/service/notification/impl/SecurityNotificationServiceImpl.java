package com.binance.account.service.notification.impl;

import javax.annotation.Resource;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import com.binance.account.service.notification.SecurityNotificationService;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.models.APIRequest;
import com.binance.notification.api.SecurityNotificationApi;
import com.binance.notification.api.request.SecurityNotificationRequest;
import com.binance.notification.api.vo.SecurityNotificationEnum;

/**
 * @author freeman
 */
@Log4j2
@Service
public class SecurityNotificationServiceImpl implements SecurityNotificationService {

	@Resource
	private SecurityNotificationApi securityNotificationApi;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private PushCenterService pushCenterService;


	@Override
	public void saveSecurityNotification(Long userId, SecurityNotificationEnum securityNotiEum, LanguageEnum langEnum) {
		// TODO Auto-generated method stub
		if (!apolloCommonConfig.getNofificationSwitch()) {
			return;
		}
		// 新增开关用来使用新的push-center
		if (pushCenterService.isUseNewApi(userId)) {
			pushCenterService.saveSecurityNotification(userId, securityNotiEum, langEnum);
		} else {
			sendOld(userId, securityNotiEum, langEnum);
		}
	}

	private void sendOld(Long userId, SecurityNotificationEnum securityNotiEum, LanguageEnum langEnum) {
		try {
			log.info("saveSecurityNotification userId = {},func={}", userId, securityNotiEum.getDesc());
			SecurityNotificationRequest syRequest = new SecurityNotificationRequest();
			syRequest.setSecurityNotificationEnum(securityNotiEum);
			syRequest.setUserId(userId);
			APIRequest<SecurityNotificationRequest> apiRequest = APIRequest.instance(syRequest);
			apiRequest.setLanguage(langEnum);
			APIResponse<Boolean> result = securityNotificationApi.saveSecurityNotification(apiRequest);
			log.info("saveSecurityNotification userId = {},result = {},func={}", userId, result, securityNotiEum.getDesc());
		} catch (Exception e) {
			log.error("saveSecurityNotification userId={},exception : {}", userId, e.getMessage());
		}
	}

}
