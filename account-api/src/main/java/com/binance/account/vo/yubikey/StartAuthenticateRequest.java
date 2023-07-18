package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("开始请求验证WebAuthn")
@Data
public class StartAuthenticateRequest implements Serializable {

    private static final long serialVersionUID = -1867304765068851212L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("绑定域名")
    @NotNull
    private String origin;
}
