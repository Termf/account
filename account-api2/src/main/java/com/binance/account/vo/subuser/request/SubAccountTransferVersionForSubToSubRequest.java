package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel("SubAccountTransferVersionForSubToSubRequest")
@Getter
@Setter
public class SubAccountTransferVersionForSubToSubRequest {

    @ApiModelProperty(required = true, notes = "发起方的userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "转入方的email")
    @NotNull
    private String toEmail;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;
}