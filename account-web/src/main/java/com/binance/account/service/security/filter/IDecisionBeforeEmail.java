package com.binance.account.service.security.filter;

/**
 * reset成功发送邮件之前，由决策系统决定发哪一封邮件
 *
 */
public interface IDecisionBeforeEmail {

	String EMAIL_TEMPLATE_NAME_RESET2FA = "2fa_reset_success_usable";
	String EMAIL_TEMPLATE_NAME_ENABLE = "user_enable_success_usable";
	String RULE = "rule_risk_operation_reset_or_undo_forbid";
	
	/**
	 * 是否命中禁止体现规则，若命中则发成功邮件，否则发指定邮件
	 * 
	 * @param userId
	 * @param resetId
	 * @return 命中规则返回true，否则false
	 */
	boolean beforeSuccessEmail(Long userId,String resetId);
}
