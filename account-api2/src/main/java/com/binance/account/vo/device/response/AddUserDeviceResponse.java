package com.binance.account.vo.device.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("新增设备指纹Response")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserDeviceResponse {

    @ApiModelProperty("UserDevice主键id")
    private Long id;

    @ApiModelProperty("用户Id")
    private Long userId;

    @ApiModelProperty("设备id")
    private String deviceId;

}
