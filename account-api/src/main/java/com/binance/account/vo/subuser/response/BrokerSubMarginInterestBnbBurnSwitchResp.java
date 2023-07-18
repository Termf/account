package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerSubMarginInterestBnbBurnSwitchResp")
@Data
public class BrokerSubMarginInterestBnbBurnSwitchResp {

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "interestBNBBurn")
    private Boolean interestBNBBurn;

}