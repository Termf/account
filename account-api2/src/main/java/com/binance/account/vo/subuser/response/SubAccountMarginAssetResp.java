package com.binance.account.vo.subuser.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Ben Nie on 2020/4/24.
 */
@Data
public class SubAccountMarginAssetResp implements Serializable {

    @ApiModelProperty("子账户ID")
    private String subAccountId;

    @ApiModelProperty("总资产（单位：BTC）")
    private String totalAssetOfBtc;

    @ApiModelProperty("总负债（单位：BTC）")
    private String totalLiabilityOfBtc;

    @ApiModelProperty("净资产（单位：BTC）")
    private String totalNetAssetOfBtc;

    @ApiModelProperty("风险率")
    private String marginLevel;

    @ApiModelProperty("是否开通margin")
    private boolean marginEnable;
}
