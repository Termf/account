package com.binance.account.vo.question;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问答模块-问题定义")
@Getter
@Setter
public class QuestionRequestBody extends ToString {

	private static final long serialVersionUID = -2114622398764326327L;

	@ApiModelProperty("流程id")
	@NotNull
	private String flowId;

	@ApiModelProperty("流程类型")
	@NotNull
	private String flowType;
}
