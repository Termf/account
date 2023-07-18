package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetDisableTradingRequest extends ToString {
    private static final long serialVersionUID = -8432668293892595318L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

}
