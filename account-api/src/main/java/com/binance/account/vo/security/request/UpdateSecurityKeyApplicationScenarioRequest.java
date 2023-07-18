package com.binance.account.vo.security.request;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("更新用户Security key使用场景")
@Getter
@Setter
public class UpdateSecurityKeyApplicationScenarioRequest {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("应用场景")
    @NotNull
    private Map<SecurityKeyApplicationScenario, Boolean> scenarios;

    //开->关 需要验证yubikey
    @ApiModelProperty(notes = "2次验证码")
    private String code;

}