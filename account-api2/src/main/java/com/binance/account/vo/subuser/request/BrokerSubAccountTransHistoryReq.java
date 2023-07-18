package com.binance.account.vo.subuser.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerSubAccountTransHistoryReq")
@Data
public class BrokerSubAccountTransHistoryReq {

    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;
	
	@ApiModelProperty(required = true, notes = "经销商子账户id")
    @NotNull
    private Long subAccountId;

    @ApiModelProperty(required = true, notes = "开始时间")
    private Long startTime;

    @ApiModelProperty(required = true, notes = "结束时间")
    private Long endTime;

    @ApiModelProperty(required = true, notes = "页码")
    private Integer page;

    @ApiModelProperty(required = true, notes = "行数量")
    private Integer limit;

    @ApiModelProperty(required = true, notes = "三方的交易id")
    private String thirdTranId;
}
