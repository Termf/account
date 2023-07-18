package com.binance.account.vo.subuser.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Ben Nie on 2020/4/24.
 */
@Data
public class SubAccountFuturesAssetResp implements Serializable {

    @ApiModelProperty("经销商子账户id")
    private String subAccountId;

    @ApiModelProperty("起始保证金（单位：USDT）")
    private String totalInitialMarginOfUsdt;

    @ApiModelProperty("维持保证金（单位：USDT）")
    private String totalMaintenanceMarginOfUsdt;

    @ApiModelProperty("钱包余额（单位：USDT）")
    private String totalWalletBalanceOfUsdt;

    @ApiModelProperty("持仓未实现盈亏（单位：USDT）")
    private String totalUnrealizedProfitOfUsdt;

    @ApiModelProperty("保证金余额（单位：USDT）")
    private String totalMarginBalanceOfUsdt;

    @ApiModelProperty("持仓起始保证金（单位：USDT）")
    private String totalPositionInitialMarginOfUsdt;

    @ApiModelProperty("挂单起始保证金（单位：USDT）")
    private String totalOpenOrderInitialMarginOfUsdt;

    @ApiModelProperty("是否开通futures")
    private boolean futuresEnable;
}
