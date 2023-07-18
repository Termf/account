package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("子账户margin划转Response")
@Data
public class MainMarginAccountTransferResponse {

    @ApiModelProperty("事务操作id")
    private Long tranId;//事务操作id


}
