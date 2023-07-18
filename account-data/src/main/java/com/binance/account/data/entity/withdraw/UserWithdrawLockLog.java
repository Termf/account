package com.binance.account.data.entity.withdraw;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawLockLog {
    private Long id;

    private Long userId;

    private Long tranId;

    private String type;
    
    private String isManual;

    private BigDecimal amount;

    private Date insertTime;

    private String operator;

}