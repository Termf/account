package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("做市商账号划转Request")
@Getter
@Setter
public class MarketMakerTransferRequest implements Serializable{

    private static final long serialVersionUID = -3057267336468952382L;

    @ApiModelProperty(required = true, notes = "tranId")
    @NotNull
    private Long tranId;

    @ApiModelProperty(required = true, notes = "划转时间")
    @NotNull
    private Long tranTime;

    @ApiModelProperty(required = true, notes = "做市商userId")
    @NotNull
    private Long marketMakerUserId;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;

    @ApiModelProperty(required = true, notes = "对公账号userId")
    @NotNull
    private Long publicAccount;
}
