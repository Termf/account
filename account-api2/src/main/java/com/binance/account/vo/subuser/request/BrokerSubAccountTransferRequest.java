package com.binance.account.vo.subuser.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("BrokerSubAccountTransferRequest")
@Data
public class BrokerSubAccountTransferRequest {
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;
	
	@ApiModelProperty(required = true, notes = "转出方经销商子账户id(不传代表是母账户)")
    private Long fromId;

    @ApiModelProperty(required = true, notes = "转入方经销商子账户id(不传代表是母账户)")
    private Long toId;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;

    @ApiModelProperty(required = true, notes = "三方的交易id")
    private String thirdTranId;
}
