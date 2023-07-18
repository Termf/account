package com.binance.account.common.enums;

public enum KycCertificateKycLevel {
	L0(0, "等级0"), L1(1, "等级1"), L2(2, "等级2");

	private Integer code;

	private String message;

	private KycCertificateKycLevel(int code, String message) {
		this.code = new Integer(code);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public Integer getCode() {
		return code;
	}

	public static KycCertificateKycLevel getByCode(int code) {
		switch (code) {
		case 0:
			return KycCertificateKycLevel.L0;
		case 1:
			return KycCertificateKycLevel.L1;
		case 2:
			return KycCertificateKycLevel.L2;
		default:
			return KycCertificateKycLevel.L0;
		}
	}

}
