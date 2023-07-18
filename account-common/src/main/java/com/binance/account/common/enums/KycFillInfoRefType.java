package com.binance.account.common.enums;

public enum KycFillInfoRefType {
	WITHDRAW_FACE("提币人脸");
	
	private String message;
	
	private KycFillInfoRefType(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
	
}
