package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.FutureAssetRiskInfoVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("QuerySubAccountFutureAccountResp")
@Data
public class QuerySubAccountFutureAccountResp {
    /**
     * 基础信息
     * */

    private String email;

    private boolean canTrade;

    private boolean canDeposit;

    private boolean canWithdraw;

    private Integer feeTier;

    private Long updateTime;

    private String asset;


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

    private String maxWithdrawAmount;

    private List<FutureAssetRiskInfoVo> assets;
}
