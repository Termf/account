package com.binance.account.vo.subuser;

import com.binance.account.vo.user.ex.UserStatusEx;
import lombok.Data;

@Data
public class FutureAccountSummaryInfoVo {
    private String email;

    private UserStatusEx userStatusEx;

    private String totalInitialMargin;

    private String totalMaintenanceMargin;

    private String totalWalletBalance;

    private String totalUnrealizedProfit;

    private String totalMarginBalance;

    private String totalPositionInitialMargin;

    private String totalOpenOrderInitialMargin;

}
