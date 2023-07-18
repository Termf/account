package com.binance.account.vo.kyc.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class KycAccountChangeRequest extends ToString {

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    private Boolean isPass;

    private Integer securityLevel;
}
