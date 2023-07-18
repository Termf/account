package com.binance.account.common.enums;

/**
 * 返佣比例审核状态
 * @author mengjuan
 *
 */
public enum AgentRewardEnum {
	AUDIT(0, "待审核"), 
	PASS(1, "审核通过"),
	REFUSED(2, "审核拒绝");

	private Integer code;
	private String desc;

	private AgentRewardEnum(Integer code, String desc) {
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
