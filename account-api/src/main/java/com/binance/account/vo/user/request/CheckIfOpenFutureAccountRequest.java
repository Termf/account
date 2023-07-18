package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("CheckIfOpenFutureAccountRequest")
@Data
public class CheckIfOpenFutureAccountRequest {
    @NotNull
    private Long rootUserId;
}
