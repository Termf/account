package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WithdrawFaceStatusChangeRequest extends ToString {

    private static final long serialVersionUID = -7956814482497803613L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("fromStatus")
    @NotNull
    private Integer fromStatus;

    @ApiModelProperty("toStatus")
    @NotNull
    private Integer toStatus;
}
