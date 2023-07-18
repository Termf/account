package com.binance.account.vo.question;

import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问答模块-问题")
@Getter
@Setter
public class Question extends ToString {
	private static final long serialVersionUID = 1631049963560605338L;
	@ApiModelProperty("问题类型")
	private String riskType;
	@ApiModelProperty("语言文案标识")
	private String docLangFlag;
	@ApiModelProperty("备注")
	private String remark;
	@ApiModelProperty("是否启用:0-启用 1-禁用")
	private byte enable;
	@ApiModelProperty("问题权重，百分之几")
	private Integer weight;
	@ApiModelProperty("问题id")
	private Long id;
	@ApiModelProperty("创建时间")
	private Date createTime;
	@ApiModelProperty("更新时间")
	private Date updateTime;
	@ApiModelProperty("属于哪套题")
	private String group;
}