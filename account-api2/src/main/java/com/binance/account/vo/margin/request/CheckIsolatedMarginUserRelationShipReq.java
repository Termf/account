package com.binance.account.vo.margin.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by pcx
 */
@ApiModel("CheckIsolatedMarginUserRelationShipReq")
@Getter
@Setter
public class CheckIsolatedMarginUserRelationShipReq {


	@ApiModelProperty(required = true, notes = "主账号UserId")
    @NotNull
    private Long rootUserId;


    @ApiModelProperty(required = true, notes = "isolatedMarginUser")
    @NotNull
    private Long isolatedMarginUser;
}