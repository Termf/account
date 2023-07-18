package com.binance.account.vo.question;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问答模块-用户问答查询记录请求体")
@Setter
@Getter
public class QueryLogRequestBody {
	@ApiModelProperty("用户id")
	@NotNull
	private Long userId;
	@ApiModelProperty("流程id")
	@NotNull
	private String flowId;
	@ApiModelProperty("分页偏移量，默认0")
	private Integer offset = 0;
	@ApiModelProperty("分页大小，默认100")
	private Integer limit = 100;
}
