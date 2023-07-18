package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel("SubAccountFuturesEnableResp")
@Data
public class SubAccountFuturesEnableResp {

    @ApiModelProperty(required = false, notes = "子账户邮箱")
    private String email;

    @ApiModelProperty("是否启用futures")
    private Boolean isFuturesEnabled;

}
