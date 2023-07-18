package com.binance.account.vo.reset.response;

import java.util.List;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("重置2FA查询问题返回值-问题定义")
@Getter
@Setter
public class ResetQuestion extends ToString implements Comparable<ResetQuestion>{
	private static final long serialVersionUID = 4400556564089371064L;

	@ApiModelProperty("当前重置请求id")
	private String resetId;

	@ApiModelProperty("问题配置表id")
	private Long questionId;

	@ApiModelProperty("问题可选项")
	private List<String> options;

	@ApiModelProperty("语言文案标识")
	private String langFlag;

	@Override
	public int compareTo(ResetQuestion o) {
		return this.questionId.compareTo(o.questionId);
	}	
}