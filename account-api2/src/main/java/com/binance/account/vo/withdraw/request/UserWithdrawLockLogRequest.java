package com.binance.account.vo.withdraw.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "提现锁定日志Request", value = "提现锁定日志Request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawLockLogRequest extends ToString {

    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "流水号")
    @NotNull
    private Long tranId;
    
    @ApiModelProperty(required = true, notes = "锁定类型(0锁定,1解锁)")
    @NotNull
    private String type;
    
    @ApiModelProperty(required = false, notes = "是否手动(0自动,1手动)")
    private String isManual;

}
