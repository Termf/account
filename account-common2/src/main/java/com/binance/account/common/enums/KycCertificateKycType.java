package com.binance.account.common.enums;

import java.util.Objects;

public enum KycCertificateKycType {
	USER(1, "个人认证"),
	COMPANY(2, "企业认证");

	private Integer code;

	private String message;


	private KycCertificateKycType(int code, String message) {
		this.code = new Integer(code);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public Integer getCode() {
		return code;
	}

	public static KycCertificateKycType getByCode(Integer code) {
		if (code == null) {
			return null;
		}
		for (KycCertificateKycType type : KycCertificateKycType.values()) {
			if (Objects.equals(type.getCode(), code)) {
				return type;
			}
		}
		return null;
	}

}
