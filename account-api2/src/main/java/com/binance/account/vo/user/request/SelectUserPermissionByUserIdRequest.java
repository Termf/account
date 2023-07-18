package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("SelectUserPermissionByUserIdRequest")
@Data
public class SelectUserPermissionByUserIdRequest {
    @ApiModelProperty(required = true, notes = "用戶Id")
    @NotNull
    private Long userId;

}
