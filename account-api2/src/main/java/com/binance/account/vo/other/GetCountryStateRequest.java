package com.binance.account.vo.other;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("获取消息映射的Key")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetCountryStateRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6012760239354647621L;
	
	@ApiModelProperty("code")
    private String code;
	
	@ApiModelProperty("stateCode")
    private String stateCode;
	
	@ApiModelProperty("enable")
	private Boolean enable;
	
	@ApiModelProperty("en")
	private String en;

	@ApiModelProperty("cn")
	private String cn;

	@ApiModelProperty("nationality")
	private String nationality;
}
