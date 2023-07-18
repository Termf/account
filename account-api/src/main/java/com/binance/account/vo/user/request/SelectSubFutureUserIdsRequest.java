package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("查询future子账户的UserIdRequest")
@Getter
@Setter
public class SelectSubFutureUserIdsRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3140612079873781281L;
	
	@ApiModelProperty(required = false, notes = "用户futureUserId")
	@NotNull
    private Long futureUserId;

}
