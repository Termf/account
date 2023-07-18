package com.binance.account.service.kyc.config;

import java.util.HashMap;
import java.util.Map;

import com.binance.master.utils.StringUtils;

public class KycFlowConfigMap {

	private Map<String, KycFlowConfig> configs = new HashMap<String, KycFlowConfig>();

	public void putConfig(String k, KycFlowConfig v) {
		configs.put(k, v);
	}

	public Map<String, KycFlowConfig> getConfigs() {
		return configs;
	}

	@Override
	public String toString() {
		return StringUtils.objectToString(this);
	}

}
