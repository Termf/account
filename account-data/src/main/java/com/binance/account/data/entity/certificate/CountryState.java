package com.binance.account.data.entity.certificate;

public class CountryState {

	private String code;

	private String stateCode;

	private String en;

	private String cn;

	private String nationality;

	private boolean enable;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en == null ? null : en.trim();
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn == null ? null : cn.trim();
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality == null ? null : nationality.trim();
	}

	public boolean getEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}