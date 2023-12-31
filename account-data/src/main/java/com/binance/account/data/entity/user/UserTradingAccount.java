// Generated by the devefx compiler. DO NOT EDIT!
package com.binance.account.data.entity.user;

import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * UserTradingAccount 撮合返回
 *
 * @date 2018-01-09 11:49:50
 */
@Getter
@Setter
public class UserTradingAccount implements Serializable {

    private static final long serialVersionUID = -909996404196888576L;
    private Long tradingAccount; // 用户交易账户
    private Long userId; // 用户id

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
