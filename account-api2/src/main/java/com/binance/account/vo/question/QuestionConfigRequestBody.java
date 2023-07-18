package com.binance.account.vo.question;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("答题次数请求体")
@Setter
@Getter
public class QuestionConfigRequestBody extends ToString{
	private static final long serialVersionUID = -4368971588407501922L;

	@ApiModelProperty("业务流程号")
    @NotNull
    private String flowId;

    @ApiModelProperty("业务类型")
    @NotNull
    private String flowType;
}
