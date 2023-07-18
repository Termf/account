package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("批量修改分佣费率Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BatchUpdateAgentResponse implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6891262500012279409L;

	@ApiModelProperty(name = "描述")
    private Integer total;
    
    @ApiModelProperty(name = "批次号")
    private String batchId;

}
