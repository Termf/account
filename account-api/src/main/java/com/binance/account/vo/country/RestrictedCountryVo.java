package com.binance.account.vo.country;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RestrictedCountryVo extends ToString {

    @ApiModelProperty("是否受限")
    private boolean restricted;

    @ApiModelProperty("country code")
    private String countryCode;

}