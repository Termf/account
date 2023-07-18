package com.binance.account.vo.security.request;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("DisableFastWithdrawSwitchRequest Request")
@Data
public class DisableFastWithdrawSwitchRequest {
    @ApiModelProperty("用户userid")
    @NotNull
    private Long userId;

}
