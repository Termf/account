package com.binance.account.data.entity.country;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CountryBlacklist {
    private String countryCode;

    private Boolean isActive;

    private String memo;

    private Date createTime;

    private Date updateTime;

    public CountryBlacklist(String countryCode, String memo) {
        this.countryCode = countryCode;
        this.memo = memo;
    }
}