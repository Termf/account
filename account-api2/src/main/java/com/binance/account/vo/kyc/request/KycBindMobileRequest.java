package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("绑定手机信息")
@Getter
@Setter
public class KycBindMobileRequest extends KycFlowRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4203173067489704448L;
	
	private String mobile;
	
	private String mobileCode; 
	
	private String smsCode;
	
}
