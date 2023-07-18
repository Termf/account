package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

@Data
public class WebAuthnOriginSupportedRequest implements Serializable {

    @ApiModelProperty("Origin")
    @NotEmpty
    private String origin;

}
