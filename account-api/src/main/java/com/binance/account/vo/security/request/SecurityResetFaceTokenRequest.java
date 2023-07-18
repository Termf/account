package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-08-27 13:51
 */
@ApiModel("重置2FA 获取Face++ Token")
@Setter
@Getter
public class SecurityResetFaceTokenRequest implements Serializable {

    private static final long serialVersionUID = -8624268282748882474L;

    @ApiModelProperty("id")
    @NotNull
    private String id;

    @ApiModelProperty("userId")
    @NotNull
    private String userId;
}
