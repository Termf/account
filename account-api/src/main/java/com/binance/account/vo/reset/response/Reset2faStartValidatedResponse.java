package com.binance.account.vo.reset.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("初始化RESET-2FA前置检验结果")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Reset2faStartValidatedResponse implements Serializable {

    private static final long serialVersionUID = 4033890969533901180L;

    @ApiModelProperty("请求ID")
    private String requestId;

    @ApiModelProperty("保护模式下到答题剩余次数")
    private int protectCount;
}
