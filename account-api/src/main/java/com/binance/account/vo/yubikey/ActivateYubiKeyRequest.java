package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("激活yubikey")
@Data
public class ActivateYubiKeyRequest implements Serializable {

    @ApiModelProperty("激活码")
    @NotNull
    private String activateCode;

}
