package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("根据tradingAccount获取userId")
@Data
public class GetUserIdByTradingAccountRequest {

    @NotNull
    private Long tradingAccount;
}
