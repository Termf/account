package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("绑定WebAuthn-验证密码")
@Data
public class ValidatePasswordRequest implements Serializable {

    @ApiModelProperty("用户Id")
    @NotNull
    private String userId;

    @ApiModelProperty("密码")
    @NotNull
    private String password;

    @ApiModelProperty("yubikey's nickname")
    @NotNull
    private String nickname;

}
