package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("禁用状态Request")
@Getter
@Setter
public class SecurityStatusRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1121354068789084748L;

	@ApiModelProperty("用户Id")
    @NotNull
    private Long userId;
	
	@ApiModelProperty("手动禁用 0:正常;1:禁用")
	private Integer withdrawSecurityStatus;
	
	@ApiModelProperty("风控禁用 0:正常;1:禁用")
	private Integer withdrawSecurityAutoStatus;
}
