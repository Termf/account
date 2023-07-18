package com.binance.account.service.country;

import com.binance.account.data.entity.country.Country;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.country.GetMobileRegisterSupportCountryListeRequest;
import com.binance.account.vo.country.GetMobileRegisterSupportCountryListeResp;
import com.binance.account.vo.country.RestrictedCountryVo;
import com.binance.master.models.APIRequest;

import java.util.List;

public interface ICountry {

    Country getCountryByCode(String code);

    Boolean removeCache(String code);

    CountryVo getCountryByAlpha3(String alpha3);

    CountryVo getCountryByAlpha3WithCache(String alpha3);

    List<Country> getCountryList();

    List<Country> getAllCountryList();

    RestrictedCountryVo isIpInRestrictedCountry(String ip);


    Country getCountryByMobileCodeOrCountryCode(String code);


    GetMobileRegisterSupportCountryListeResp getMobileRegisterSupportCountryList(APIRequest<GetMobileRegisterSupportCountryListeRequest> request);



    boolean isSupportMobileRegisterCountry(String code);



}
