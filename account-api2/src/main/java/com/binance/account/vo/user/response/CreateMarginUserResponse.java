package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("创建margin用户信息Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CreateMarginUserResponse implements Serializable {

    private static final long serialVersionUID = 8709805917749346913L;
    private Long rootUserId; // id
    private Long rootTradingAccount; // 用户交易账户
    private Long marginUserId; // id
    private Long marginTradingAccount; // 用户交易账户
    private  Boolean isSubUser;
    private Boolean isBrokerSubUser;//是否是broker子账号

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
