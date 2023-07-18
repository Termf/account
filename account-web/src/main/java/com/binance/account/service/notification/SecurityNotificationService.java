package com.binance.account.service.notification;

import com.binance.master.enums.LanguageEnum;
import com.binance.notification.api.vo.SecurityNotificationEnum;

public interface SecurityNotificationService {
	public void saveSecurityNotification(Long userId, SecurityNotificationEnum securityNotiEum, LanguageEnum langEnum);
}
