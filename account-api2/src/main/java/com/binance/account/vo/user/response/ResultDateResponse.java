package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("修改返回Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResultDateResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3236938909921921042L;

	@ApiModelProperty(name = "描述")
    private String desc;
    
    @ApiModelProperty(name = "内容")
    private String data;
}
