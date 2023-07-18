package com.binance.account.vo.question;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 问题配置信息
 *
 */
@ApiModel("问题模块-问题配置请求体")
@Getter
@Setter
public class QuestionInfoRequestBody extends ToString {
	private static final long serialVersionUID = -1180790261743581206L;
	@ApiModelProperty("场景")
	@NotNull
	private QuestionSceneEnum scene;
	@ApiModelProperty("哪套题")
	@NotNull
	private String group;
	@ApiModelProperty("规则列表")
	private List<String> rules;//规则列表
	@ApiModelProperty("操作人")
	@NotNull
	private String operator;
	@ApiModelProperty("问题配置")
	private Question question;
}
