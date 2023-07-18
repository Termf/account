package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by zhao chenkai
 */
@ApiModel("QueryBrokerSubAccountIdResponse")
@Data
public class QueryBrokerSubAccountIdResponse implements Serializable {

    private static final long serialVersionUID = 5278933260530021808L;
    
    @ApiModelProperty("母账号UserId")
    private Long parentUserId;

    @ApiModelProperty("子账号UserId")
    private Long subUserId;

    @ApiModelProperty("子账号subAccountId")
    private Long subAccountId;
	
}
