package com.binance.account.common.enums;

public enum UserRiskRatingTierLevel {
	NoTier(null,null),
	Tier0("tier0","tier0Limit"),
	Tier1("tier1","tier1Limit"),
	Tier2("tier2","tier2Limit"),
	Tier3("tier3","tier3Limit");
	/**
	 * 风控限额请求key
	 */
	private String riskReqKey;
	/**
	 * 风控限额应答key
	 */
	private String riskRespKey;

	private UserRiskRatingTierLevel(String riskReqKey,String riskRespKey) {
		this.riskReqKey = riskReqKey;
		this.riskRespKey = riskRespKey;
	}

	public String getRiskReqKey() {
		return riskReqKey;
	}

	public String getRiskRespKey() {
		return riskRespKey;
	}


}
