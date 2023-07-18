package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("获取某一次验证的结果，该结果只保留两分钟")
@Data
public class AuthenticateResultRequest implements Serializable {
    private static final long serialVersionUID = 6029731596805161481L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("请求ID")
    @NotNull
    private String requestId;
}
