package com.binance.account.vo.withdraw.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "获取锁定数量Request", value = "获取锁定数量Request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawLockAmountRequest extends ToString {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private Long userId;

}
