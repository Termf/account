package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
@ApiModel("IDM确认信息")
@Getter
@Setter
public class IdmAuthRequest extends KycFlowRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5654017473186877316L;
	
	// 欺诈评估结果
	private String frp;

	// 政策评估结果。结合欺诈和自动审查评估的结果。可能的值是：
	private String res;

	// 当前kyc值
	private String state;

}
