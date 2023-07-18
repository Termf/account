package com.binance.account.vo.subuser;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubUserEmailVo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -893158548039281420L;

	@ApiModelProperty("子账户UserId")
    private Long userId;

    @ApiModelProperty("子账户邮箱")
    private String email;
}
