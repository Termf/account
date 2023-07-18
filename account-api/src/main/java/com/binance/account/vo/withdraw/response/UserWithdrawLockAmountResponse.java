package com.binance.account.vo.withdraw.response;

import java.math.BigDecimal;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithdrawLockAmountResponse extends ToString {

	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty("用户Id")
    private Long userId;

    @ApiModelProperty("锁定总数量")
    private BigDecimal totalAmount;
    
    @ApiModelProperty("自动锁定数量")
    private BigDecimal autoAmount;
    
    @ApiModelProperty("手动锁定数量")
    private BigDecimal manualAmount;
	

}
