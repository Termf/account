package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerCommissionFuturesResponse")
@Data
public class BrokerCommissionFuturesResponse {
    @ApiModelProperty(required = true, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "symbol")
    private String symbol;

    @ApiModelProperty(required = false, notes = "makerAdjustment")
    private Integer makerAdjustment;

    @ApiModelProperty(required = false, notes = "takerAdjustment")
    private Integer takerAdjustment;

    @ApiModelProperty(required = false, notes = "makerCommission")
    private Integer makerCommission;

    @ApiModelProperty(required = false, notes = "takerCommission")
    private Integer takerCommission;
}
