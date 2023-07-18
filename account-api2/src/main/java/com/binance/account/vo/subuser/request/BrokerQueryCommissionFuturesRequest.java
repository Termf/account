package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("BrokerQueryCommissionFuturesRequest")
@Data
public class BrokerQueryCommissionFuturesRequest {
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "symbol")
    private String symbol;

}