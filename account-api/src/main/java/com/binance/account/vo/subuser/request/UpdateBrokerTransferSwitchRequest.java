package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("UpdateBrokerTransferSwitchRequest")
@Data
public class UpdateBrokerTransferSwitchRequest {
    @ApiModelProperty(required = true, notes = "userid")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "是否禁止broker划转:true代表禁止，false代表不禁止")
    @NotNull
    private Boolean needForbiddenBrokerTransfer;

}