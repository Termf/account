package com.binance.account.common.enums;

public enum KycFillInfoGender {

	MALE(1,"男"),
	FEMALE(2,"女"),
	UNSPECIFIED(0,"未知");
	
	private Byte gender;
	
	private String message;
	
	private KycFillInfoGender(int gender,String message) {
		this.gender = new Byte((byte)gender);
		this.message = message;
	}

	public Byte getGender() {
		return gender;
	}

	public void setGender(Byte gender) {
		this.gender = gender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public static KycFillInfoGender getGender(Byte code) {
		if(code == null) {
			return KycFillInfoGender.UNSPECIFIED;
		}
		switch (code.intValue()) {
		case 1:
			return KycFillInfoGender.MALE;
		case 2:
			return KycFillInfoGender.FEMALE;
		default:
			return KycFillInfoGender.UNSPECIFIED;
		}
		
	}
}
