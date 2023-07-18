package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("AccountResetPasswordResponseV2")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountResetPasswordResponseV2 {


    @ApiModelProperty(name = "用户ID")
    private Long userId;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
