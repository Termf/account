package com.binance.account.service.kyc.config;

import com.binance.master.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;
/**
 * xml executor 定义
 * @author liufeng
 *
 */
@Setter
@Getter
public class KycFlowExecutorDefine {

	private String executorName;
	
	
	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
