package com.binance.account.vo.subuser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FuturePositionRiskVO {
    @ApiModelProperty("symbol name")
    private String symbol;

    @ApiModelProperty("position amount")
    private String positionAmount;

    @ApiModelProperty("avg cost of that position")
    private String entryPrice;

    @ApiModelProperty("mark price of that position")
    private String markPrice;

    @ApiModelProperty("unrealized pnl of that position")
    private String unrealizedProfit;

    @ApiModelProperty("liquidation price")
    private String liquidationPrice;

    @ApiModelProperty("max notional value")
    private String maxNotional;

    @ApiModelProperty("liquidation price")
    private String leverage;
}
