package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("broker子母账户划转Response")
@Data
public class BrokerSubAccountTransferResponse  {

    @ApiModelProperty("事务操作id")
    private Long txnId;//事务操作id


    @ApiModelProperty("三方交易流水号")
    private String clientTranId;

}
