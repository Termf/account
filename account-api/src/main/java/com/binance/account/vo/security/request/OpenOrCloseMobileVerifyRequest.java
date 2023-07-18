package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Api("打开手机验证Request")
@Getter
@Setter
public class OpenOrCloseMobileVerifyRequest extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = -4929998503828511433L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

}
