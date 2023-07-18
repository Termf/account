package com.binance.account.common.enums;

/**
 * 财务标识enum
 * @author zhao chenkai
 *
 */
public enum FinanceFlagEnum {
	HAS_FINANCE_RECORD(0, "是否有财务记录"), 
	KYC_PASS(1, "是否完成kyc认证"),
	HAS_ASSET(2, "是否有资产"),
	HAS_SPOT_ORDER(3, "是否有现货订单");

	private Integer code;
	private String desc;

	private FinanceFlagEnum(Integer code, String desc) {
	        this.code = code;
	        this.desc = desc;
	    }

	public Integer getCode() {
		return this.code;
	}

	public String getDesc() {
		return this.desc;
	}
}
