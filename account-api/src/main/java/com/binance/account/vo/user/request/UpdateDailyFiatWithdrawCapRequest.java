package com.binance.account.vo.user.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by zhangxi on 2019/8/1.
 */
@Data
public class UpdateDailyFiatWithdrawCapRequest {

    @NotNull
    private Long userId;
    private BigDecimal dailyFiatWithdrawCap;
    private BigDecimal singleFiatWithdrawCap;
}