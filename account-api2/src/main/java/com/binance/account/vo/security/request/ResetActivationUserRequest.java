package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ResetActivationUserRequest extends ToString {
    private static final long serialVersionUID = 2670581463849590203L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("申请ip")
    @NotNull
    private String applyIp;

    @ApiModelProperty("终端Code")
    private String terminalCode;


}
