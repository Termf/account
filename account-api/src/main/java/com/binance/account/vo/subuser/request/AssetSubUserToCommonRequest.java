package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhao chenkai
 */
@ApiModel("资管子账号转成普通账号request")
@Getter
@Setter
public class AssetSubUserToCommonRequest {

    @ApiModelProperty(required = true, notes = "子账户userId")
    @NotNull
    private Long subUserId;

}
