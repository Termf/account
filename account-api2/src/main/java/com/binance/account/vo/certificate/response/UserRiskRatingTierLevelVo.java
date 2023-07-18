package com.binance.account.vo.certificate.response;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRiskRatingTierLevelVo extends ToString {

    private static final long serialVersionUID = 7255816519311791770L;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;

    private BigDecimal totalLimit;

    private BigDecimal yearlyLimit;

    private BigDecimal withdrawDailyLimit;

    private BigDecimal withdrawMonthlyLimit;

    private BigDecimal withdrawYearlyLimit;

    private BigDecimal withdrawTotalLimit;
}
