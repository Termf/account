package com.binance.account.vo.user;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("创建future用户信息Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CreateFutureUserResponse implements Serializable {

    private Long rootUserId; // 主账户的userid
    private Long rootTradingAccount; // 用户交易账户
    private Long futureUserId; // 期货账户的userid
    private Long futureTradingAccount; // 用户期货账户
    private Long futureDeliveryTradingAccount; // 用户期货交割合约账户

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
