package com.binance.account.data.entity.withdraw;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class UserWithdrawProperty implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6154227236812255715L;

    private Long userId;

    private BigDecimal withdrawLock;
    
    private BigDecimal withdrawLockManual;

    private BigDecimal withdrawMaxAssetDay;

    private Date insertTime;

    private Date updateTime;
}