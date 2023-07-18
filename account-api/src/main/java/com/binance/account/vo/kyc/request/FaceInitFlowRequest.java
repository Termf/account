package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("face_init请求")
@Getter
@Setter
public class FaceInitFlowRequest extends KycFlowRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6677995809416709633L;
	
	private String transId;

}
