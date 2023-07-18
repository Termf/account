package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("SelectBrokerSubBnbBurnStatusResp")
@Data
public class SelectBrokerSubBnbBurnStatusResp {

    @ApiModelProperty(required = true, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "spotBNBBurn")
    private Boolean spotBNBBurn;

    @ApiModelProperty(required = true, notes = "spotinterestBNBBurnBNBBurn")
    private Boolean interestBNBBurn;

}