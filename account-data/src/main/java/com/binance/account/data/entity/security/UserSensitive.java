package com.binance.account.data.entity.security;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserSensitive implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 703596811085366241L;

    private Long userId;

    private Date insertTime;
    
}