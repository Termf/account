package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("修改future推荐人Request")
@Getter
@Setter
public class UserFutureAgentIdRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3140612079873781281L;
	
	@ApiModelProperty(required = false, notes = "用户futureUserId")
	@NotNull
    private Long futureUserId;
	
	@ApiModelProperty(required = false, notes ="推荐人Id")
	@NotNull
	private Long futureAgentId;

}
