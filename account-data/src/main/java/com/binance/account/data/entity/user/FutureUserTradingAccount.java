package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FutureUserTradingAccount  {

    private Long tradingAccount; // 用户交易账户
    private Long userId; // 用户id

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
