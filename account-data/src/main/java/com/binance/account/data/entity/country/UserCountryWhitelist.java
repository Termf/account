package com.binance.account.data.entity.country;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class UserCountryWhitelist {
    private Long userId;

    private String memo;

    private Date createTime;

    private Date expireTime;

    public UserCountryWhitelist(Long userId, String memo) {
        this.userId = userId;
        this.memo = memo;
    }
}