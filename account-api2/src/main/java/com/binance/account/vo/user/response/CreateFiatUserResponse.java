package com.binance.account.vo.user.response;

import java.io.Serializable;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("创建fiat用户信息Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CreateFiatUserResponse implements Serializable {

    private static final long serialVersionUID = -2705422614123684928L;
    private Long rootUserId; // id
    private Long rootTradingAccount; // 用户交易账户
    private Long fiatUserId; // id
    private Long fiatTradingAccount; // 用户交易账户

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
