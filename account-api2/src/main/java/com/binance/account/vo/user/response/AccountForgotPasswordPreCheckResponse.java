package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("AccountForgotPasswordPreCheckResponse")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountForgotPasswordPreCheckResponse {


    @ApiModelProperty(required = true, notes = "token")
    private String token;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
