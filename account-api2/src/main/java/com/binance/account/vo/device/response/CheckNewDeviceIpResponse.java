package com.binance.account.vo.device.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "CheckNewDeviceIpResponse", value = "CheckNewDeviceIpResponse")
@Data
public class CheckNewDeviceIpResponse {


    @ApiModelProperty("校验结果，true.是新设备并且是新ip  false.不是新设备或者不是新ip")
    private boolean isNewDeviceIp=false;



}
