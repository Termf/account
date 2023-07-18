package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("BrokerSubMarginInterestBnbBurnSwitchRequest")
@Data
public class BrokerSubMarginInterestBnbBurnSwitchRequest {
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "interestBNBBurn")
    @NotNull
    private Boolean interestBNBBurn;

}