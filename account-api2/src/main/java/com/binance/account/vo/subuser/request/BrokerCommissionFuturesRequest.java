package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("BrokerCommissionFuturesRequest")
@Data
public class BrokerCommissionFuturesRequest {
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "symbol")
    @NotNull
    private String symbol;

    @ApiModelProperty(required = false, notes = "makerAdjustment")
    @NotNull
    private Integer makerAdjustment;

    @ApiModelProperty(required = false, notes = "takerAdjustment")
    @NotNull
    private Integer takerAdjustment;
}