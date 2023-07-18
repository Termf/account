package com.binance.account.common.enums;

public enum UserKycEmailNotifyType {
	
	BASIC("24h通知用户完成BASIC"),
	BASIC72H("72h通知用户完成BASIC"),
	BASIC30D("30d通知用户完成BASIC"),
	TRADE("72h通知用户去做交易"),
	TRADE24H("72h通知用户去做交易"),
	TRADE7D("7d通知用户去做交易"),
	TRADE30D("30d通知用户去做交易"),
	DEPOSIT("充值5000USD后30天内未交易"),
	TRADECOMPLETE1("30天交易满1000USD"),
	TRADECOMPLETE2("30天交易满10000USD"),
	VIP1("用户等级1"),
	;

	private String desc;

	UserKycEmailNotifyType(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
