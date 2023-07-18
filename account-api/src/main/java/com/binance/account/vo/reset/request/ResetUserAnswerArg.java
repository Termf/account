package com.binance.account.vo.reset.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户2FA重置,问题与答案查询请求")
@Getter
@Setter
public class ResetUserAnswerArg extends ToString {
	private static final long serialVersionUID = -4699028555708345114L;

	@ApiModelProperty("userId")
	@NotNull
	private Long userId;

	@ApiModelProperty("resetId")
	@NotNull
	private String resetId;
}