package com.binance.account.vo.user.response;

import java.io.Serializable;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("账号重置密码Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountResetPasswordResponse implements Serializable {

    private static final long serialVersionUID = -3511014638727110141L;

    @ApiModelProperty(name = "用户ID")
    private Long userId;

    @ApiModelProperty(name = "账号")
    private String email;

    @ApiModelProperty(name = "状态")
    private Long status;

    @ApiModelProperty(readOnly = true, notes = "密码加密")
    private String salt;

    @ApiModelProperty(readOnly = true, notes = "密码加密后的")
    private String password;

    @ApiModelProperty(readOnly = true, notes = "禁用token")
    private String disableToken;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
