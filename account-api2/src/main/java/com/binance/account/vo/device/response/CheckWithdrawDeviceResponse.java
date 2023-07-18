package com.binance.account.vo.device.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("校验用户提现的设备Response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckWithdrawDeviceResponse implements Serializable {
    private static final long serialVersionUID = -2144286131317071262L;

    public static final CheckWithdrawDeviceResponse INVALID = new CheckWithdrawDeviceResponse(false,null, null);

    @ApiModelProperty("校验结果，true.校验不通过")
    private boolean valid;

    @ApiModelProperty("UserDevice主键id")
    private Long id;
    @ApiModelProperty("设备id")
    private String deviceId;
}
