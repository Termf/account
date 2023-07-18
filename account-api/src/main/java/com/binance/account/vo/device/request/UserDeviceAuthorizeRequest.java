package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author: caixinning
 * @date: 2018/05/08 18:24
 **/

@ApiModel(description = "确认设备", value = "确认设备")
@Getter
@Setter
public class UserDeviceAuthorizeRequest extends ToString {

    private static final long serialVersionUID = 6177284945429888216L;

    @ApiModelProperty(required = true, notes = "验证码")
    @NotBlank
    private String code;

}
