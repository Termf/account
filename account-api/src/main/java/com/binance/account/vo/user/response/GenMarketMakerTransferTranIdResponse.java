package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("获取做市商账号划转tranId Response")
@Getter
@Setter
public class GenMarketMakerTransferTranIdResponse implements Serializable{

    private static final long serialVersionUID = 5057478876472087301L;
    
    @ApiModelProperty(required = true, notes = "tranId")
    private Long tranId;

    @ApiModelProperty(required = true, notes = "划转时间")
    private Long transferTime;
    
}
