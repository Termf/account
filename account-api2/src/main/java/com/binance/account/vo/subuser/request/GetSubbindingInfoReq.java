package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("GetSubbindingInfoReq")
@Data
public class GetSubbindingInfoReq {


    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

	@ApiModelProperty(required = false, notes = "经销商子账户id")
    private Long subAccountId;

    @ApiModelProperty(required = false, notes = "经销商子账户id")
    private Long subUserId;


}
