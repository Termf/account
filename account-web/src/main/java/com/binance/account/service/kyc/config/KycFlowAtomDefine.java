package com.binance.account.service.kyc.config;

import com.binance.master.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;
/**
 * xml 配置类
 * @author liufeng
 *
 */
@Setter
@Getter
public class KycFlowAtomDefine {

	public static final String ROOT_NAME = "atom";

	public static final String DEFINE_NAME = "name";

	public static final String EXECUTOR_NAME = "executor";
	
	private String name;

	private KycFlowExecutorDefine executor;
	
	private boolean runGlobalEnd = true;
	
	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
