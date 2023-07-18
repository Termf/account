package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("admin后台g管理员解绑Webauthn")
@Data
public class DeregisterForAdminRequest implements Serializable {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;
}
