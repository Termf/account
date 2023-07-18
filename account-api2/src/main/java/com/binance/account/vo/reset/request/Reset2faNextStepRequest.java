package com.binance.account.vo.reset.request;

import com.binance.account.common.enums.UserSecurityResetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@ApiModel("Reset2FA 初始化时的请求参数")
@Setter
@Getter
public class Reset2faNextStepRequest implements Serializable {

    private static final long serialVersionUID = 6701112781809650370L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("重置类型")
    @NotNull
    private UserSecurityResetType type;
    
	@ApiModelProperty("设备信息,中台封装")
	@NotNull
	Map<String, String> deviceInfo;
}
