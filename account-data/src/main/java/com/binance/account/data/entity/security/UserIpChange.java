package com.binance.account.data.entity.security;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserIpChange implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3166718413740714903L;

    private String id;

    private Long userId;

    private String ip;

    private Boolean status;

    private Date insertTime;

    private Date updateTime;
    
}