package com.binance.account.vo.country;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("GetMobileRegisterSupportCountryListeRequest")
@Data
public class GetMobileRegisterSupportCountryListeRequest {

    @ApiModelProperty("ip")
    @NotNull
    private String ip;
}