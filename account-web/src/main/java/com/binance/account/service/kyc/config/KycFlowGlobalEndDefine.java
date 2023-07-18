package com.binance.account.service.kyc.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class KycFlowGlobalEndDefine {

	public static final String DEFINE_RUN_GLOBAL_END = "run-global-end";

	public static final String ROOT_NAME = "global-end";

	public static final String DEFINE_EXECUTORS = "executors";

	public static final String DEFINE_EXECUTE_NAME = "name";
	
	public static final String DEFINE_IS_SYNC = "isSync";

	private List<KycFlowExecutorDefine> executors = new ArrayList<KycFlowExecutorDefine>();;
	
	public void addExecutor(KycFlowExecutorDefine executor) {
		if(executors == null) {
			executors = new ArrayList<KycFlowExecutorDefine>();
		}
		executors.add(executor);
	}
}
