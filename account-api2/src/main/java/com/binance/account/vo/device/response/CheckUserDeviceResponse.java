package com.binance.account.vo.device.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@ApiModel(description = "校验用户设备Response", value = "校验用户设备Response")
@Getter
@Setter
@AllArgsConstructor
public class CheckUserDeviceResponse implements Serializable {
    private static final long serialVersionUID = -2144286131317071262L;

    public static final CheckUserDeviceResponse INVALID = new CheckUserDeviceResponse(false,null, null);

    @ApiModelProperty("校验结果，true.合法的老设备  false.新设备")
    private boolean valid;
    @ApiModelProperty("匹配的UserDevice主键id，无匹配则为空")
    private Long id;
    @ApiModelProperty("设备id")
    private String deviceId;


}
