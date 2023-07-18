package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.AgentRewardEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("返佣审核状态修改Request")
@Getter
@Setter
public class AgentStatusRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 43411597743530906L;

	@ApiModelProperty(required = true, notes = "主键Ids")
	@NotNull
	private List<Long> ids;
	
	@ApiModelProperty(required = true, notes = "审核状态")
	@NotNull
	private AgentRewardEnum status;
	
	@ApiModelProperty(required = false, notes = "审核人ID")
	private String operatorId;
}
