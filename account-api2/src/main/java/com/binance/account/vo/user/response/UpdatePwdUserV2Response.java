package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "修改密码Response", value = "修改密码Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UpdatePwdUserV2Response extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 167910116116805409L;

    @ApiModelProperty(required = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(required = true, notes = "账号")
    private String email;

    @ApiModelProperty(readOnly = true, notes = "密码加密")
    private String salt;

    @ApiModelProperty(readOnly = true, notes = "密码加密后的")
    private String password;

    @ApiModelProperty(readOnly = true, notes = "一键禁用码")
    private String disableToken;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

}
