package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 */
@ApiModel("SelectApiAgentCommissionDetailRes")
@Data
public class SelectApiAgentCommissionDetailRes {

    @ApiModelProperty(required = false, notes = "三方备注id")
    private String customerId;

    @ApiModelProperty(required = false, notes = "email")
    private String email;

    @ApiModelProperty(required = false, notes = "income")
    private String income;

    @ApiModelProperty(required = false, notes = "asset")
    private String asset;

    @ApiModelProperty(required = false, notes = "symbol")
    private String symbol;

    @ApiModelProperty(required = false, notes = "time")
    private Long time;
}