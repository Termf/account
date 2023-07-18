package com.binance.account.controller.country;

import com.binance.account.api.CountryApi;
import com.binance.account.common.query.SearchResult;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.country.CountryBlacklist;
import com.binance.account.data.entity.country.UserCountryWhitelist;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.country.impl.CountryBlacklistBusiness;
import com.binance.account.vo.country.*;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BeanConverter;
import com.binance.master.web.handlers.MessageHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class CountryController implements CountryApi {

    @Autowired
    private ICountry iCountry;
    @Autowired
    private CountryBlacklistBusiness countryBlacklistBusiness;
    @Autowired
    private MessageHelper messageHelper;
    @Autowired
    private ApolloCommonConfig apolloCommonConfig;


    @Override
    public APIResponse<Boolean> removeCache(@Validated() @RequestBody() APIRequest<String> request) {
        return APIResponse.getOKJsonResult(this.iCountry.removeCache(request.getBody()));
    }

    @Override
    public APIResponse<Boolean> isInBlacklist(@RequestParam("countryCode") String countryCode) {
        if (StringUtils.length(countryCode)==2){
            boolean isBlack = countryBlacklistBusiness.isBlack(countryCode);
            String message = messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT);
            return APIResponse.getOKJsonResult(isBlack, message);
        }else {
            return APIResponse.getErrorJsonResult("请输入二位国家字母代码（ISO 3166-1 alpha-2）");
        }
    }

    @Override
    public APIResponse<Boolean> isUserInBlacklist(@RequestParam("userId") Long userId) {
        Pair<Boolean, Boolean> rt = countryBlacklistBusiness.isBlack(userId);
        Boolean isBlack = rt.getLeft();
        String message = null;
        if (isBlack){
            if (rt.getRight()){
                message = messageHelper.getMessage(AccountErrorCode.COUNTRY_KYC_NOT_SPPORT);
            }else {
                message = messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT);
            }
        }
        return APIResponse.getOKJsonResult(isBlack, message);
    }

    @Override
    public APIResponse<Boolean> isIpInBlacklist(@RequestParam("ip") String ip) {
        boolean isBlack = countryBlacklistBusiness.isBlackIp(ip);
        String message = messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT);
        return APIResponse.getOKJsonResult(isBlack, message);
    }

    @Override
    public APIResponse<IpForbiddenCheckResponse> isIpInForbiddenRegion(@RequestParam("ip") String ip) {
        IpForbiddenCheckResponse ipForbiddenCheckResponse = new IpForbiddenCheckResponse();
        boolean isBlack = countryBlacklistBusiness.isBlackIp(ip);
        ipForbiddenCheckResponse.setIsCountryForbidden(isBlack);
        if(isBlack){
            ipForbiddenCheckResponse.setMessage(messageHelper.getMessage(AccountErrorCode.COUNTRY_IP_NOT_SPPORT));
            return APIResponse.getOKJsonResult(ipForbiddenCheckResponse);
        }
        boolean isForbidden = countryBlacklistBusiness.checkIsUsNotSupportedStatesOrCity(ip);
        ipForbiddenCheckResponse.setIsRegionForbidden(isForbidden);
        if(isForbidden) {
            ipForbiddenCheckResponse.setMessage(messageHelper.getMessage(AccountErrorCode.US_IP_FORBIDDEN_DEFAULT_PROMPT));
        }
        return APIResponse.getOKJsonResult(ipForbiddenCheckResponse);
    }

    @Override
    public APIResponse<List<CountryBlacklistVo>> queryBlacklist() {
        return APIResponse.getOKJsonResult(countryBlacklistBusiness.listAll());
    }

    @Override
    public APIResponse<Void> addBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request) {
        countryBlacklistBusiness.add(BeanConverter.createBean(request.getBody(), CountryBlacklist.class));
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> updateBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request) {
        countryBlacklistBusiness.update(BeanConverter.createBean(request.getBody(), CountryBlacklist.class));
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> deleteBlacklist(@RequestBody @Validated APIRequest<CountryBlacklistRequest> request) {
        countryBlacklistBusiness.delete(request.getBody().getCountryCode());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<CountryVo> getCountryByAlpha3(@RequestParam("alpha3") String alpha3) {
        return APIResponse.getOKJsonResult(iCountry.getCountryByAlpha3(alpha3));
    }

    @Override
    public APIResponse<SearchResult> queryWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistQuery> request) {
        SearchResult<UserCountryWhitelist> result = countryBlacklistBusiness.queryWhiteList(request.getBody());
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<Void> addWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistRequest> request) {
        countryBlacklistBusiness.addWhiteList(BeanConverter.createBean(request.getBody(), UserCountryWhitelist.class));
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> deleteWhiteList(@RequestBody @Valid APIRequest<UserCountryWhitelistRequest> request) {
        countryBlacklistBusiness.deleteWhiteList(request.getBody().getUserId());
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<List<CountryVo>> getCountryList() {
        List<CountryVo> voList = new ArrayList<>();
        List<Country> countryList = iCountry.getCountryList();
        Set<String> kycForbids = apolloCommonConfig.kycForbidCountry();
        for (Country country : countryList) {
            CountryVo countryVo = new CountryVo();
            BeanUtils.copyProperties(country, countryVo);
            countryVo.setKycForbid(kycForbids.contains(countryVo.getCode().toUpperCase()));
            voList.add(countryVo);
        }
        return APIResponse.getOKJsonResult(voList);
    }

    @Override
    public APIResponse<List<CountryVo>> getAllCountryList() {
        List<Country> countries = iCountry.getAllCountryList();
        List<CountryVo> voList = new ArrayList<>();
        Set<String> kycForbids = apolloCommonConfig.kycForbidCountry();
        for (Country country : countries) {
            CountryVo countryVo = new CountryVo();
            BeanUtils.copyProperties(country, countryVo);
            countryVo.setKycForbid(kycForbids.contains(countryVo.getCode().toUpperCase()));
            voList.add(countryVo);
        }
        return APIResponse.getOKJsonResult(voList);
    }

    @Override
    public APIResponse<CountryVo> getCountryByCode(@RequestBody @Valid APIRequest<GetCountryByCodeRequest> request) {
        GetCountryByCodeRequest getCountryByCodeRequest=request.getBody();
        Country country= iCountry.getCountryByCode(getCountryByCodeRequest.getCode());
        CountryVo countryVo = new CountryVo();
        BeanUtils.copyProperties(country, countryVo);
        return APIResponse.getOKJsonResult(countryVo);
    }

    @Override
    public APIResponse<CountryVo> getCountryByMobileCode(@RequestBody @Valid APIRequest<String> request) {
        Country country= iCountry.getCountryByMobileCodeOrCountryCode(request.getBody());
        CountryVo countryVo = new CountryVo();
        BeanUtils.copyProperties(country, countryVo);
        return APIResponse.getOKJsonResult(countryVo);
    }


    @Override
    public APIResponse<RestrictedCountryVo> isIpInRestrictedCountry(String ip) {
        if (StringUtils.isBlank(ip)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        return APIResponse.getOKJsonResult(iCountry.isIpInRestrictedCountry(ip));
    }

    @Override
    public APIResponse<GetMobileRegisterSupportCountryListeResp> getMobileRegisterSupportCountryList(@RequestBody @Validated APIRequest<GetMobileRegisterSupportCountryListeRequest> request) {
        return APIResponse.getOKJsonResult(iCountry.getMobileRegisterSupportCountryList(request));
    }

}
