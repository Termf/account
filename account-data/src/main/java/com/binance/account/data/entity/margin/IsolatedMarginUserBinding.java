package com.binance.account.data.entity.margin;

import lombok.Data;

import java.util.Date;

@Data
public class IsolatedMarginUserBinding {
    private Long isolatedMarginUserId;
    private Long rootUserId;
    private String remark;
    private Date insertTime;
    private Date updateTime;
}