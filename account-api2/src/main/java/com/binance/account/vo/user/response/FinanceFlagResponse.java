package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "用户财务标识Response", value = "用户财务标识Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FinanceFlagResponse extends ToString {

    @ApiModelProperty(notes = "是否有财务记录")
    private Boolean hasFinanceRecord;

    @ApiModelProperty(notes = "是否完成kyc认证")
    private Boolean isKycPass;

    @ApiModelProperty(notes = "是否有资产")
    private Boolean hasAsset;

    @ApiModelProperty(notes = "是否下过现货订单")
    private Boolean hasSpotOrder;

    @ApiModelProperty(notes = "是否有提币记录")
    private Boolean hasWithdrawRecord;

}
