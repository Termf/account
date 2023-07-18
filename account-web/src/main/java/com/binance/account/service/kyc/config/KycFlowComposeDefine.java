package com.binance.account.service.kyc.config;

import java.util.ArrayList;
import java.util.List;

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
public class KycFlowComposeDefine {
	
	public static final String ROOT_NAME = "compose";

	public static final String DEFINE_NAME = "name";
	
	public static final String DEFINE_EXECUTORS = "executors";
	
	public static final String DEFINE_EXECUTE_NAME = "name";
	
	private String name;
	
	private List<KycFlowExecutorDefine> executors;
	
	private boolean runGlobalEnd = true;
	
	public void addExecutor(KycFlowExecutorDefine executor) {
		if(executors == null) {
			executors = new ArrayList<KycFlowExecutorDefine>();
		}
		executors.add(executor);
	}

	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
