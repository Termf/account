package com.binance.account.vo.subuser.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by pcx
 */
@Data
public class CreateFuturesForBrokerSubUserResp {

    @ApiModelProperty("broke子账号Id")
    private String subaccountId;


    @ApiModelProperty("是否enable futures")
    private Boolean enableFutures;


    @ApiModelProperty("更新时间")
    private Long updateTime;
}