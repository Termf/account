package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 */
@ApiModel("CreateApiAgentAliasRes")
@Data
public class CreateApiAgentAliasRes {

    @ApiModelProperty(required = false, notes = "customerId")
    private String customerId;

    @ApiModelProperty(required = false, notes = "email")
    private String email;
}