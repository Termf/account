package com.binance.account.vo.subuser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MarginUserAssetVo {
    @ApiModelProperty("总资产")
    private String total;
    @ApiModelProperty("资产名称")
    private String asset;
    @ApiModelProperty("借款")
    private String borrowed;
    @ApiModelProperty("可用余额")
    private String free;
    @ApiModelProperty("利息")
    private String interest;
    @ApiModelProperty("锁定余额")
    private String locked;
    @ApiModelProperty("净资产")
    private String netAsset;
    @ApiModelProperty("净资产（单位：BTC）")
    private String netAssetOfBtc;
}
