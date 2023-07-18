package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by zhao chenkai
 */
@ApiModel("QueryBrokerSubAccountIdRequest")
@Data
public class QueryBrokerSubAccountIdRequest {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = true, notes = "经销商子账户userId")
    @NotNull
    private List<Long> subUserIds;
	
}
