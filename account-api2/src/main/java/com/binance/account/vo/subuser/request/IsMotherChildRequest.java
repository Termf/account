package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

/**
 * Created by mengjuan on 2018/10/31.
 */
@ApiModel("验证是否母子关系Request")
@Getter
@Setter
public class IsMotherChildRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8915495513241122398L;

	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账户userId")
    @NotNull
    private Long subUserId;
}
