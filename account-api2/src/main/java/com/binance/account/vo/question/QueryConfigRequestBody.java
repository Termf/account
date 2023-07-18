package com.binance.account.vo.question;

import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问答模块-问题配置查询请求体")
@Setter
@Getter
public class QueryConfigRequestBody extends ToString {
	private static final long serialVersionUID = 4303818324670656053L;
	@ApiModelProperty("场景")
	private QuestionSceneEnum scene;
	@ApiModelProperty("哪套题")
	private String group;
	@ApiModelProperty("风控类型：同一套题内问题唯一标志")
	private String riskType;
}
