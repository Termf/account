package com.binance.account.vo.security.request;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
@ApiModel("EnableFastWithdrawSwitchRequest Request")
@Data
public class EnableFastWithdrawSwitchRequest {
    @ApiModelProperty("用户userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = false, notes = "认证类型")
    private AuthTypeEnum authType;

    @ApiModelProperty(required = false, notes = "2次验证码")
    private String code;
}
