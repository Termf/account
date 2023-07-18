package com.binance.account.vo.country;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("GetMobileRegisterSupportCountryListeResp")
@Data
public class GetMobileRegisterSupportCountryListeResp {

    private CountryVo defaultCountry;

    private List<CountryVo> supportCountryList;
}