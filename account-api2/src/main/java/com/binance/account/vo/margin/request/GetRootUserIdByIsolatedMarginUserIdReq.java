package com.binance.account.vo.margin.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Created by pcx
 */
@ApiModel("GetRootUserIdByIsolatedMarginUserIdReq")
@Getter
@Setter
public class GetRootUserIdByIsolatedMarginUserIdReq {


	@ApiModelProperty(required = true, notes = "逐仓margin账号UserId")
    @NotNull
    private Long isolatedMarginUserId;
}