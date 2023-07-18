package com.binance.account.vo.question;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel("问答模块-用户问答记录统计查询请求体")
@Setter
@Getter
@ToString
public class QueryLogStaticsRequestBody {
	@ApiModelProperty("用户邮箱")
	private Long userId;
	@ApiModelProperty("流程类型")
	private String flowType;
	@ApiModelProperty("创建时间起始点")
	private Date startTime;
	@ApiModelProperty("创建时间结束点")
	private Date endTime;
	@ApiModelProperty("分页偏移量")
	private int offset = 0;
	@ApiModelProperty("分页大小")
	private int limit = 20;
}
