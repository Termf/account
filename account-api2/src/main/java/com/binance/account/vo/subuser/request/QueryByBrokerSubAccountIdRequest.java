package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 */
@ApiModel("QueryByBrokerSubAccountIdRequest")
@Data
public class QueryByBrokerSubAccountIdRequest {


    @ApiModelProperty(required = true, notes = "broker三方Id")
    @NotNull
    private Long brokerSubAccountId;


	
}
