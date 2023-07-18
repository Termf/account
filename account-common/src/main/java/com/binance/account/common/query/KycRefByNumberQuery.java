package com.binance.account.common.query;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class KycRefByNumberQuery {

    private String number;

    public String getNumber() {
        return StringUtils.isEmpty(this.number) ? null : this.number.trim().toLowerCase();
    }

}
