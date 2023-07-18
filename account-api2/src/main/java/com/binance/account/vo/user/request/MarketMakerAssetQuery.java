package com.binance.account.vo.user.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("做市商对公账号资产余额查询Request")
@Getter
@Setter
public class MarketMakerAssetQuery implements Serializable{

    private static final long serialVersionUID = -8762585248689068389L;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "对公账号userId")
    @NotNull
    private Long publicAccount;

}
