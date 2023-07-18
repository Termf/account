package com.binance.account.common.enums;

public enum UserKycEmailNotifyStatus {
	
	INIT("初始"),
	FAIL("失败"),
	SUCCESS("成功");

	private String desc;

	UserKycEmailNotifyStatus(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

}
