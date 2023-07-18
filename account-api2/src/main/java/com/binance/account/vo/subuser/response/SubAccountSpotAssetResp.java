package com.binance.account.vo.subuser.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Ben Nie on 2020/4/24.
 */
@Data
public class SubAccountSpotAssetResp implements Serializable {

    @ApiModelProperty("子账户ID")
    private String subAccountId;

    @ApiModelProperty("总资产（单位：BTC）")
    private String totalBalanceOfBtc;
}
