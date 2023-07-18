package com.binance.account.vo.kyc.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("地址信息提交")
@Getter
@Setter
public class AddressInfoSubmitResponse extends KycFlowResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2306124888158906620L;

	private String country;

	private String regionState;
	
	private String regionStateCode;

	private String city;

	private String address;

	private String postalCode;

}
