package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("解绑手机Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UnbindMobileResponse {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("一键禁用码")
    private String disableToken;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
