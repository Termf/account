package com.binance.account.vo.user.request;

import com.binance.account.common.enums.AgentRewardEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ApiModel("根据批次号返佣审核状态修改Request")
@Getter
@Setter
public class AuditAgentStatusByBatchIdRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -191527964777510861L;

	@ApiModelProperty(required = true, notes = "批次号Ids")
	@NotNull
	private List<String> batchIds;
	
	@ApiModelProperty(required = true, notes = "审核状态")
	@NotNull
	private AgentRewardEnum status;
	
	@ApiModelProperty(required = false, notes = "审核人ID")
	private String operatorId;
}
