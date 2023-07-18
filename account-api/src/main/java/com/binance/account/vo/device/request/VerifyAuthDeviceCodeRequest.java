package com.binance.account.vo.device.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "VerifyAuthDeviceCodeRequest", value = "VerifyAuthDeviceCodeRequest")
@Getter
@Setter
public class VerifyAuthDeviceCodeRequest {
    @ApiModelProperty(notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "新设备授权的验证码")
    @NotNull
    private String code;

    @ApiModelProperty(required = false, notes = "半登陆态token")
    private String loginToken;

}
