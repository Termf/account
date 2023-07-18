package com.binance.account.vo.subuser;

import com.binance.account.vo.user.ex.UserStatusEx;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class MarginAccountSummaryInfoVo {
    private String email;

    private UserStatusEx userStatusEx;

    @ApiModelProperty("风险率")
    private String marginLevel;

    @ApiModelProperty("子账户总资产（单位：BTC")
    private String totalAssetOfBtc;

    @ApiModelProperty("子账户总负债（单位：BTC")
    private String totalLiabilityOfBtc;

    @ApiModelProperty("子账户净资产（单位：BTC")
    private String totalNetAssetOfBtc;

}
