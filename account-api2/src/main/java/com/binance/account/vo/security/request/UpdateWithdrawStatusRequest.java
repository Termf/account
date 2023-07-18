package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.UpdateWithdrawStatusChannelEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("禁用状态Request")
@Getter
@Setter
public class UpdateWithdrawStatusRequest implements Serializable{


	@ApiModelProperty("用户Id")
    @NotNull
    private Long userId;
	
	@ApiModelProperty("手动禁用 0:正常;1:禁用")
	private Integer withdrawSecurityStatus;
	
	@ApiModelProperty("风控禁用 0:正常;1:禁用")
	private Integer withdrawSecurityAutoStatus;

	@ApiModelProperty("操作渠道")
	@NotNull
	private UpdateWithdrawStatusChannelEnum channel;

	@ApiModelProperty("操作原因")
	@NotBlank
	private String reason;
}


