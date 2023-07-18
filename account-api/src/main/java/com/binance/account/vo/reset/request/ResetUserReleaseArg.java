package com.binance.account.vo.reset.request;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.ProtectedStatus;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户2FA重置失败进入保护模式,解除保护模式请求")
@Getter
@Setter
public class ResetUserReleaseArg extends ToString {
	private static final long serialVersionUID = -606517462838208577L;
	@ApiModelProperty("userId")
	@NotNull
	private Long userId;
	@ApiModelProperty("管理的用户状态")
	@NotNull
	private ProtectedStatus status;
}
