package com.binance.account.vo.subuser.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by pcx
 */
@Data
public class CreateMarginForBrokerSubUserResp {

    @ApiModelProperty("broke子账号Id")
    private String subaccountId;


    @ApiModelProperty("是否enable margin")
    private Boolean enableMargin;


    @ApiModelProperty("更新时间")
    private Long updateTime;
}