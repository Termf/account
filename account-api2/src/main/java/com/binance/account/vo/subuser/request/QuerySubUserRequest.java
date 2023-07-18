package com.binance.account.vo.subuser.request;

import com.binance.master.commons.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by mengjuan on 2018/10/26.
 */
@ApiModel("条件查询子账户列表Request")
@Getter
@Setter
public class QuerySubUserRequest extends Page implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -318259131682653145L;

	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "子账户开启状态,1:开启; 0:未开启")
    private Integer isSubUserEnabled;
}
