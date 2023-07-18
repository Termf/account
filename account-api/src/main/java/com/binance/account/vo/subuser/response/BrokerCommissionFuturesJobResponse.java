package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerCommissionFuturesJobResponse")
@Data
public class BrokerCommissionFuturesJobResponse {
    @ApiModelProperty(required = true, notes = "subUserId")
    private Long subUserId;

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