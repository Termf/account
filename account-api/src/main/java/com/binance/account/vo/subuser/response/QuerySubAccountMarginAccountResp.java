package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.MarginProfitVo;
import com.binance.account.vo.subuser.MarginTradeCoeffVo;
import com.binance.account.vo.subuser.MarginUserAssetVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("QuerySubAccountMarginAccountResp")
@Data
public class QuerySubAccountMarginAccountResp {
    /**
     * 基础信息
     * */

    private String email;

    /**
     * 统计相关信息
     * */
    @ApiModelProperty("风险率")
    private String marginLevel;
    @ApiModelProperty("风险状态")
    private String marginLevelStatus;
    @ApiModelProperty("总资产（单位：BTC）")
    private String totalAssetOfBtc;
    @ApiModelProperty("总负债（单位：BTC）")
    private String totalLiabilityOfBtc;
    @ApiModelProperty("净资产（单位：BTC）")
    private String totalNetAssetOfBtc;

    private List<MarginProfitVo> marginProfitVoList;

    @ApiModelProperty("资产信息")
    private List<MarginUserAssetVo> marginUserAssetVoList;

    private MarginTradeCoeffVo marginTradeCoeffVo;

}
