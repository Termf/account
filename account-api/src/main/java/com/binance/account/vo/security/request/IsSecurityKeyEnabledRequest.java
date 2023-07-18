package com.binance.account.vo.security.request;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@ApiModel("查询Security Key 在当前场景是否启用")
@Getter
@Setter
public class IsSecurityKeyEnabledRequest implements Serializable {

    private static final long serialVersionUID = 1370404444433775791L;


    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("Security Key 应用场景")
    @NotNull
    private SecurityKeyApplicationScenario scenario;
}
