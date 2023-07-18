package com.binance.account.vo.subuser.request;

import com.binance.account.vo.subuser.enums.SubAccountTransferTypeVersionForSapi;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("SubUserHistoryVersionForSapiRequest")
@Getter
@Setter
public class SubUserHistoryVersionForSapiRequest {

    @ApiModelProperty(required = true, notes = "userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    private String asset;

    @ApiModelProperty(required = true, notes = "划转类型")
    private SubAccountTransferTypeVersionForSapi subAccountTransferTypeVersionForSapi;

    @ApiModelProperty(required = true, notes = "fromId==txid")
    private Long fromId;

    @ApiModelProperty(required = true, notes = "开始时间")
    private Long startTime;

    @ApiModelProperty(required = true, notes = "结束时间")
    private Long endTime;

    @ApiModelProperty(required = true, notes = "行数量")
    private Integer limit=500;
}