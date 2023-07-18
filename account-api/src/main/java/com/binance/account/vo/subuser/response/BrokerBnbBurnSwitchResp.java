package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerBnbBurnSwitchResp")
@Data
public class BrokerBnbBurnSwitchResp {

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "spotBNBBurn")
    private Boolean spotBNBBurn;

}