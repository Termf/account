package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("请求开始绑定WebAuthn")
@Data
public class StartRegisterRequest implements Serializable {

    private static final long serialVersionUID = 7321223772928605331L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("绑定域名")
    @NotNull
    private String origin;

    @ApiModelProperty("别名")
    private String nickname;
}
