package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("地址认证基础信息")
@Getter
@Setter
public class AddressInfoSubmitRequest extends KycFlowRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2306124888158906620L;

	@NotNull
	private String country;

	private String regionState;

	private String city;

	private String address;

	private String postalCode;

	//base64 地址账单图片字符串
	@ApiModelProperty(required = true, notes = "地址认证文件")
	@NotNull
	private String billFile;
	
	@ApiModelProperty(required = true, notes = "地址认证文件名")
	@NotNull
	private String billFileName;

}
