package com.binance.account.vo.mining.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("CreateMiningUserResponse")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CreateMiningUserResponse {

    private Long rootUserId; // 主账户的userid
    private Long rootTradingAccount; // 用户交易账户
    private Long miningUserId; // 矿池账户的userid
    private Long miningTradingAccount; // 用户矿池账户

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
