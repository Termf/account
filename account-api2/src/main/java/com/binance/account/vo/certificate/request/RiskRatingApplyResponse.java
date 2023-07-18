package com.binance.account.vo.certificate.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRatingApplyResponse extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3081165185152551794L;

	@ApiModelProperty("申报是否成功")
	private boolean success;
	
	@ApiModelProperty("申报描述")
	private String tips;
}
