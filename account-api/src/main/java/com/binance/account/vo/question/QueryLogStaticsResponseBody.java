package com.binance.account.vo.question;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel("问答模块-用户问答记录统计响应体")
@Setter
@Getter
@ToString
public class QueryLogStaticsResponseBody {
	@ApiModelProperty("响应体")
	private List<Body> body;
	
	@ApiModel("问答模块-用户问答记录统计数据")
	@Setter
	@Getter
	@ToString
	public static class Body{
		@ApiModelProperty("用户id")
		private Long userId;
		@ApiModelProperty("用户邮箱")
		private String email;
		@ApiModelProperty("流程id")
		private String flowId;
		@ApiModelProperty("流程类型")
		private String flowType;
		@ApiModelProperty("该流程问答次数")
		private int ansersTimes;
		@ApiModelProperty("该流程最终业务状态")
		private String totalStatus;
		@ApiModelProperty("流程创建时间")
		private Date createTime;
		@ApiModelProperty("流程更新时间")
		private Date updateTime;
	}
}
