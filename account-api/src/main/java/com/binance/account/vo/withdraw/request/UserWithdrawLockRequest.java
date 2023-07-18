package com.binance.account.vo.withdraw.request;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.binance.account.common.validator.ValidateResult;
import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "提现锁定Request", value = "提现锁定Request")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawLockRequest extends ToString {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "流水号")
    @NotNull
    private Long tranId;
    
    @ApiModelProperty(required = true, notes = "锁定类型(0锁定,1解锁)")
    @NotNull
    private String type;
    
    @ApiModelProperty(required = true, notes = "是否手动(0自动,1手动)")
    @NotNull
    private String isManual;
    
    @ApiModelProperty(required = true, notes = "锁定数量")
    @NotNull
    private BigDecimal amount;
    
    @ApiModelProperty(required = true, notes = "锁定人")
    @NotNull
    private String operator;

    /**
     * 校验公共信息
     * @return
     */
    public ValidateResult validate(){
        if (!StringUtils.equalsAny(type, "0", "1")){
            return ValidateResult.reject("type can not be null");
        }
        if (!StringUtils.equalsAny(isManual, "0", "1")){
            return ValidateResult.reject("isManual can not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 1){
            return ValidateResult.reject("amount can not be less than zero");
        }

        return ValidateResult.pass();
    }
}
