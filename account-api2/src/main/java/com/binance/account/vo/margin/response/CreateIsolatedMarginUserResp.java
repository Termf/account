package com.binance.account.vo.margin.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by pcx
 */
@Data
public class CreateIsolatedMarginUserResp  {

    @ApiModelProperty("主账号UserId")
    private Long rootUserId;

    @ApiModelProperty("主账号accountid")
    private Long rootTradingAccount;

    @ApiModelProperty("逐仓margin账号UserId")
    private Long isolatedMarginUserId;

    @ApiModelProperty("逐仓margin账号accountid")
    private Long isolatedMarginTradingAccount;

    @ApiModelProperty("是否是子账号")
    private Boolean isSubUser=false;

    @ApiModelProperty("是否是broker子账号")
    private Boolean isBrokerSubUser;

}