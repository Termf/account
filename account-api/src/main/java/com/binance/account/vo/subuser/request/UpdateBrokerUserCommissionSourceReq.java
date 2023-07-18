package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 */
@ApiModel("UpdateBrokerUserCommissionSourceReq")
@Data
public class UpdateBrokerUserCommissionSourceReq {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @NotNull
	@ApiModelProperty(required = true, notes = "source来源字段")
    private Integer source;


}