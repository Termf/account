package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("获取做市商账号划转tranIdRequest")
@Getter
@Setter
public class GenMarketMakerTransferTranIdRequest implements Serializable{

    private static final long serialVersionUID = 1323667425063839970L;
    
    @ApiModelProperty(required = true, notes = "做市商userId")
    @NotNull
    private Long marketMakerUserId;

    @ApiModelProperty(required = true, notes = "0:从对公账号划转到做市商; 1:从做市商划转到对公账号")
    @NotNull
    @Max(1)
    @Min(0)
    private Integer transferFrom;

    @ApiModelProperty(required = true, notes = "对公账号userId")
    @NotNull
    private Long publicAccount;
    
    
    
}
