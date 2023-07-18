package com.binance.account.vo.subuser;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class MarginProfitVo {
    @ApiModelProperty("盈亏起始时间")
    private Long beginTime;
    @ApiModelProperty("盈亏计算时间")
    private Long calcTime;
    @ApiModelProperty("资产名称")
    private String asset;
    @ApiModelProperty("盈亏")
    private String profit;
    @ApiModelProperty("盈亏率")
    private String profitRate;

}
