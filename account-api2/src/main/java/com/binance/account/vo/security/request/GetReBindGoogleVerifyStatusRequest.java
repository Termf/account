package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Setter
@Getter
public class GetReBindGoogleVerifyStatusRequest implements Serializable {
    private static final long serialVersionUID = -1323133961531450984L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;
}
