package com.binance.account.vo.user.request;

import com.binance.account.vo.futures.enums.FutureEmailTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("SendFutureMarginCallRequest")
@Data
public class SendFutureMarginCallRequest {
    @NotNull
    private Long futureUserId;

    private String symbol;

    private boolean isDelivery = false;

}
