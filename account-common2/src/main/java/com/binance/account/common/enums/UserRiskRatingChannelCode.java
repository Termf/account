package com.binance.account.common.enums;

import java.math.BigDecimal;

public enum UserRiskRatingChannelCode {
	CHECKOUT("EUR", "CHECKOUT", new BigDecimal(100000),false),
	StandardBank("ZAR", "StandardBank", new BigDecimal(-1),false),
	ClearJunction("EUR", "ClearJunction", new BigDecimal(-1),false),
	FourBill("UAH", "4Bill", new BigDecimal(-1),false),
	BCB("EUR", "BCB", new BigDecimal(-1),false),
	Qiwi("RUB", "Qiwi", new BigDecimal(-1),false),
	FlutterwaveNGN("NGN", "FlutterwaveNGN", new BigDecimal(-1),false),
	FlutterwaveUGX("UGX", "FlutterwaveUGX", new BigDecimal(-1),false),
	worldpay("EUR", "worldpay", new BigDecimal(-1), false),
	BauPoli("AUD", "BauPoli", new BigDecimal(-1),true),
	BauPayId("AUD", "BauPayId", new BigDecimal(-1),true);

	private String currency;

	private String code;

	private BigDecimal dailyLimit;
	
	//基础信息是否需要上送三方验证
	private boolean baseApplyThirdPart;

	UserRiskRatingChannelCode(String currency, String code, BigDecimal dailyLimit,boolean baseApplyThirdPart) {
		this.currency = currency;
		this.dailyLimit = dailyLimit;
		this.code = code;
		this.baseApplyThirdPart = baseApplyThirdPart;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getDailyLimit() {
		return dailyLimit;
	}

	public String getCode() {
		return code;
	}

	public boolean isBaseApplyThirdPart() {
		return baseApplyThirdPart;
	}
	
	public static UserRiskRatingChannelCode getByCode(String code) {
		for (UserRiskRatingChannelCode channel: UserRiskRatingChannelCode.values()) {
			if (channel.getCode().equals(code)) {
				return channel;
			}
		}
		return null;
	}

}
