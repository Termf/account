package com.binance.account.vo.country;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
@ApiModel("根据国家代号获取国家列表")
@Data
public class GetCountryByCodeRequest {

    @ApiModelProperty("国家code")
    @NotNull
    private String code;
}