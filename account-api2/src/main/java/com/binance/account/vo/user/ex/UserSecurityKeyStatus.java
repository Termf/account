package com.binance.account.vo.user.ex;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.master.utils.BitUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@ApiModel(description = "用户状态", value = "用户状态")
@Getter
@Setter
@NoArgsConstructor
public class UserSecurityKeyStatus {

    @ApiModelProperty(name = "在哪些域名绑定了Security key, 如果为空说明没有绑定")
    private List<String> origins;

    @ApiModelProperty(name = "提现和API是否开启security key验证")
    private Boolean withdrawAndApi;

    @ApiModelProperty(name = "重置密码是否开启security key验证")
    private Boolean resetPassword;

    @ApiModelProperty(name = "Login是否开启security key验证")
    private Boolean login;

    public static UserSecurityKeyStatus build(Long scenarios, List<String> origins) {
        UserSecurityKeyStatus userSecurityKeyStatus = new UserSecurityKeyStatus();
        userSecurityKeyStatus.withdrawAndApi = BitUtils.isTrue(scenarios, SecurityKeyApplicationScenario.withdrawAndApi.bitVal());
        userSecurityKeyStatus.resetPassword = BitUtils.isTrue(scenarios, SecurityKeyApplicationScenario.resetPassword.bitVal());
        userSecurityKeyStatus.login = BitUtils.isTrue(scenarios, SecurityKeyApplicationScenario.login.bitVal());
        userSecurityKeyStatus.origins = origins;
        return userSecurityKeyStatus;
    }
}
