package com.binance.account.vo.subuser.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by pcx
 */
@Data
public class CreateBrokerSubUserResp{

    @ApiModelProperty("broke子账号Id")
    private String subaccountId;
}