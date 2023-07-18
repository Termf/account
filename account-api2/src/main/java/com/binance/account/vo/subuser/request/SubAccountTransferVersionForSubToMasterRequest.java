package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("SubAccountTransferVersionForSubToMasterRequest")
@Getter
@Setter
public class SubAccountTransferVersionForSubToMasterRequest {

    @ApiModelProperty(required = true, notes = "发起方的userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;
}