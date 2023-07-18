package com.binance.account.vo.reset.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("2FA重置流程保护状态查询参数")
@Getter
@Setter
public class ResetProtectedModeArg {

	@ApiModelProperty("用户Id")
    @NotNull
	private Long userId;
}
