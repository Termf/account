package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.MarginAccountSummaryInfoVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("QuerySubAccountMarginAccountSummaryResp")
@Data
public class QuerySubAccountMarginAccountSummaryResp {
    /**
     * 统计相关信息
     * */
    @ApiModelProperty("母账户净资产（单位：BTC")
    private String masterAccountNetAssetOfBtc;

    @ApiModelProperty("所有子账户总资产（单位：BTC")
    private String totalAssetOfBtc;

    @ApiModelProperty("所有子账户总负债（单位：BTC")
    private String totalLiabilityOfBtc;

    @ApiModelProperty("所有子账户净资产（单位：BTC")
    private String totalNetAssetOfBtc;
    @ApiModelProperty("所有子账户资产信息")
    private List<MarginAccountSummaryInfoVo> subAccountList;

    private Long totalSubAccountSize;
}
