package com.binance.account.common.enums;

public enum CacheRefreshType {

	COUNTRY_STATE("城市国家信息"),
	MESSAGE_MAP("代码映射信息");
	
	
	private String name;
	
	private CacheRefreshType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
