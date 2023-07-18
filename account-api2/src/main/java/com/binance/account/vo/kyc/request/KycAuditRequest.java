package com.binance.account.vo.kyc.request;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.KycCertificateStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("审核请求")
@Getter
@Setter
public class KycAuditRequest extends KycFlowRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888032909536788816L;
	
	@ApiModelProperty("kycCertificateStatus")
    @NotNull
	private KycCertificateStatus kycCertificateStatus;

	@ApiModelProperty("tips")
	private String tips;
	
	@ApiModelProperty("操作人")
	private String operator;
}
