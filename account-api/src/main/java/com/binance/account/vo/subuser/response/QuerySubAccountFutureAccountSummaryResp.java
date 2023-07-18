package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.FutureAccountSummaryInfoVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("QuerySubAccountFutureAccountSummaryResp")
@Data
public class QuerySubAccountFutureAccountSummaryResp {
    /**
     * 统计相关信息
     * */
    private String totalInitialMargin;

    private String totalMaintenanceMargin;

    private String totalWalletBalance;

    private String totalUnrealizedProfit;

    private String totalMarginBalance;

    private String totalPositionInitialMargin;

    private String totalOpenOrderInitialMargin;

    private FutureAccountSummaryInfoVo parentAccount;

    private List<FutureAccountSummaryInfoVo> subAccountList;

    private Long totalSubAccountSize;
}
