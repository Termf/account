package com.binance.account.vo.user.response;

import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.commons.ToString;
import com.binance.master.enums.AuthStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "LoginUserResponseV2", value = "登录Response")
@Getter
@Setter
@NoArgsConstructor
public class LoginUserResponseV2 extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 167910116116805409L;

    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "账号")
    private String email;

    @ApiModelProperty(readOnly = true, notes = "用户状态：使用long类型的2进制占位可以有64个状态,1:激活状态(1),2:禁用状态(2),3:锁定状态(4)")
    private Long status;

    @ApiModelProperty(readOnly = true, notes = "2次验证状态")
    private AuthStatusEnum authStatus;

    @ApiModelProperty(readOnly = true, notes = "认证令牌（有效期2小时）")
    private String token;

    @ApiModelProperty(readOnly = true, notes = "用户状态态")
    private UserStatusEx userStatus;

    @ApiModelProperty(readOnly = true, notes = "登录验证Security Key")
    private boolean useSecurityKey;

    @ApiModelProperty(readOnly = true, notes = "设备指纹id")
    private String currentDeviceId;

    private String ipLocation;
    private String disableToken;
    private Long logId;

    public LoginUserResponseV2(Long userId, String email, Long status, AuthStatusEnum authStatus) {
        super();
        this.userId = userId;
        this.email = email == null ? null : email.trim().toLowerCase();
        this.status = status;
        this.authStatus = authStatus;
        userStatus = new UserStatusEx(status);
    }

}
