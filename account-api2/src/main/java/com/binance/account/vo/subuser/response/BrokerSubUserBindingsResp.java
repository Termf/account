package com.binance.account.vo.subuser.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by zhao chenkai
 */
@ApiModel("BrokerSubUserBindingsResp")
@Data
public class BrokerSubUserBindingsResp implements Serializable {

    private static final long serialVersionUID = 4244097767811133585L;
    
    @ApiModelProperty("母账号UserId")
    private Long parentUserId;

    @ApiModelProperty("子账号UserId")
    private Long subUserId;

    @ApiModelProperty("子账号subAccountId")
    private Long brokerSubAccountId;
	
}
