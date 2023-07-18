package com.binance.account.vo.device.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.HashMap;

@ApiModel(description = "ResendAuthorizeDeviceEmailRequest", value = "ResendAuthorizeDeviceEmailRequest")
@Getter
@Setter
public class ResendAuthorizeDeviceEmailRequest {
    @ApiModelProperty(notes = "账号")
    @NotNull
    private String email;

    @ApiModelProperty(notes = "设备信息")
    @NotNull
    private HashMap<String, String> deviceInfo;

    @ApiModelProperty(required = false, notes = "半登陆态token")
    private String loginToken;

}
