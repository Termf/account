package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "账号激活Response", value = "账号激活Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountActiveUserV2Response extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -1115526903748373071L;

    @ApiModelProperty(required = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(required = true, notes = "账号")
    private String email;

    @ApiModelProperty(required = true, notes = "交易账号")
    private Long tradingAccount;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }
}
