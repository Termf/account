package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("SendFutureFundingRateMsgRequest")
@Data
public class SendFutureFundingRateMsgRequest {
    @NotNull
    private Long futureUserId;
    private String symbol="BTCUSDT";
    private String rate;
    private String amount;
    private String time;
}
