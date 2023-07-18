package com.binance.account.vo.user.response;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("返佣比例Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserInfoRewardRatioResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8888889804695644794L;
	
	@ApiModelProperty(name = "返佣比例")
    private BigDecimal agentRewardRatio;
}
