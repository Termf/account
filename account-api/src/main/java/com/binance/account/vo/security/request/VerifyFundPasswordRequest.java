package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("VerifyFundPasswordRequest")
@Data
public class VerifyFundPasswordRequest {
    @ApiModelProperty("用户userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "密码")
    @NotNull
    private String password;

}
