package com.binance.account.vo.user.request;

import com.binance.account.vo.futures.enums.FutureEmailTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("SendFutureClosePositionMsgRequest")
@Data
public class SendFutureClosePositionMsgRequest {
    @NotNull
    private Long futureUserId;
    private String symbol="BTCUSDT";
    private BigDecimal makePrice = BigDecimal.ZERO;
    private FutureEmailTypeEnum futureEmailTypeEnum=FutureEmailTypeEnum.LIQUIDATION;
    private BigDecimal totalMarginBalance = BigDecimal.ZERO;

}
