package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class WebAuthnListRequest implements Serializable {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("Email")
    private String email;

    @ApiModelProperty("Origin")
    private String origin;

}
