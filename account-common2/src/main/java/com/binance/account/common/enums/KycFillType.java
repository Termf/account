package com.binance.account.common.enums;

public enum KycFillType {
	BASE("基础信息"),
	ADDRESS("地址认证信息");
	
	private String message;
	
	private KycFillType(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	
}
