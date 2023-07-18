package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel(description = "AddUserDeviceForQRCodeLoginRequest", value = "AddUserDeviceForQRCodeLoginRequest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddUserDeviceForQRCodeLoginRequest extends ToString {



    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "ip")
    @NotNull
    private String ip;

    @ApiModelProperty(required = true, notes = "设备指纹信息（json格式）")
    @NotNull
    private Map<String, String> deviceInfo;

}
