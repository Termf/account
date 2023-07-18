package com.binance.account.vo.user.request;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("修改推荐人Request")
@Getter
@Setter
public class UserAgentIdRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3140612079873781281L;
	
	@ApiModelProperty(required = false, notes = "用户Id")
    private Long userId;
	
	@ApiModelProperty(required = false, notes ="推荐人Id")
	private Long agentId; 

}
