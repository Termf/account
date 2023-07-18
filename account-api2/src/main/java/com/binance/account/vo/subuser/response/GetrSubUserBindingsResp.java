package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("GetrSubUserBindingsResp")
@Data
public class GetrSubUserBindingsResp {


    @ApiModelProperty("母账号UserId")
    private Long parentUserId;

    @ApiModelProperty("子账号UserId")
    private Long subUserId;

    @ApiModelProperty("子账号subAccountId")
    private String brokerSubAccountId;
	
}
