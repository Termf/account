package com.binance.account.vo.user.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by Fei.Huang on 2019/1/18.
 */
@Data
public class UpdateDailyWithdrawCapRequest {

    @NotNull
    private Long userId;
    @NotNull
    private BigDecimal dailyWithdrawCap;
}