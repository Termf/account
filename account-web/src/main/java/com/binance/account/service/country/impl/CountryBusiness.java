package com.binance.account.service.country.impl;

import com.binance.account.constants.enums.CountryImageUrlEnum;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.service.country.ICountry;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.country.GetMobileRegisterSupportCountryListeRequest;
import com.binance.account.vo.country.GetMobileRegisterSupportCountryListeResp;
import com.binance.account.vo.country.RestrictedCountryVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@Service
public class CountryBusiness implements ICountry {

    private static final String COUNTRY_CODE2_CACHE_KEY = "country:code2:";
    private static final long COUNTRY_CODE_CACHE_TIME = 24 * 60 * 60L;

    @Resource
    private CountryMapper countryMapper;

    @Value("#{'${restricted.country.codes:}'.split(',')}")
    private List<String> restrictedCountryCodes;


    @Value("${mobile.web.register.country:CN}")
    private String mobileWebRegisterSupportCodeStr;

    @Value("${mobile.app.register.country:CN}")
    private String mobileAppRegisterSupportCodeStr;


    @Value("${mobile.app.register.default.country:CN}")
    private String mobileAppRegisterDefaultCodeStr;

    @Cacheable(value = CacheKeys.OLD_SYS_CONFIG, key = "#code")
    @Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    @Override
    public Country getCountryByCode(String code) {
        return this.countryMapper.selectByPrimaryKey(StringUtils.upperCase(code));
    }

    @CacheEvict(value = CacheKeys.OLD_SYS_CONFIG, key = "#code", allEntries = true)
    @Override
    public Boolean removeCache(String code) {
        log.info("清除缓存：{},key:{}", CacheKeys.OLD_SYS_CONFIG, code);
        return true;
    }

    @Override
    public CountryVo getCountryByAlpha3(String alpha3) {
        Country country = this.countryMapper.selectByCode2(alpha3);
        if (country == null) {
            return null;
        }
        CountryVo vo = new CountryVo();
        BeanUtils.copyProperties(country, vo);
        return vo;
    }

    @Override
    public CountryVo getCountryByAlpha3WithCache(String alpha3) {
        if (StringUtils.isBlank(alpha3)) {
            return null;
        }
        // 先从缓存获取
        String cacheKey = COUNTRY_CODE2_CACHE_KEY + alpha3;
        CountryVo vo = RedisCacheUtils.get(cacheKey, CountryVo.class);
        if (vo != null) {
            return vo;
        }else {
            CountryVo countryVo = getCountryByAlpha3(alpha3);
            if (countryVo != null) {
                RedisCacheUtils.set(cacheKey, countryVo, COUNTRY_CODE_CACHE_TIME);
            }
            return countryVo;
        }
    }

    @Override
    public List<Country> getCountryList() {
        return countryMapper.selectCountryList();
    }

    @Override
    public List<Country> getAllCountryList() {
        return countryMapper.selectAllCountryList();
    }

    @Override
    public RestrictedCountryVo isIpInRestrictedCountry(String ip) {
        String code = IP2LocationUtils.getCountryShort(ip);
        boolean restricted = CollectionUtils.isNotEmpty(restrictedCountryCodes) && restrictedCountryCodes.contains(code);
        return new RestrictedCountryVo(restricted, code);
    }

    @Override
    public Country getCountryByMobileCodeOrCountryCode(String code) {
        if(org.apache.commons.lang.StringUtils.isBlank(code)){
            return null;
        }

        if(org.apache.commons.lang.StringUtils.isNumeric(code)){
            return countryMapper.selectByMobileCode(code);
        }
        return countryMapper.selectByPrimaryKey(code);
    }

    @Override
    public GetMobileRegisterSupportCountryListeResp getMobileRegisterSupportCountryList(APIRequest<GetMobileRegisterSupportCountryListeRequest> apirequest) {
        GetMobileRegisterSupportCountryListeRequest request=apirequest.getBody();
        String ip = request.getIp();
        String clientType = apirequest.getTerminal().getCode();
        boolean isWebTerminal=TerminalEnum.WEB.getCode().equalsIgnoreCase(clientType);
        log.info("clientType={},isWebTerminal={}",clientType,isWebTerminal);
        GetMobileRegisterSupportCountryListeResp resp=new GetMobileRegisterSupportCountryListeResp();
        String code = IP2LocationUtils.getCountryShort(ip);
        if(!isWebTerminal){
            log.info("mobileAppRegisterDefaultCodeStr={}",mobileAppRegisterDefaultCodeStr);
            code= mobileAppRegisterDefaultCodeStr;
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(code)){
            Country country = getCountryByCode(code);
            if (null != country) {
                CountryVo vo = new CountryVo();
                BeanUtils.copyProperties(country, vo);
                if("CN".equalsIgnoreCase(vo.getCode())){
                    vo.setCountryImageUrl("https://bin.bnbstatic.com/image/20200527/chinaflag.png");
                }
                resp.setDefaultCountry(vo);
            }
        }
        log.info("mobileWebRegisterSupportCodeStr={},mobileAppRegisterSupportCodeStr={}",mobileWebRegisterSupportCodeStr,mobileAppRegisterSupportCodeStr);
        List<String> mobileRegisterSupportCode=Lists.newArrayList(mobileWebRegisterSupportCodeStr.split(","));
        log.info("mobileRegisterSupportCode={}",JsonUtils.toJsonNotNullKey(mobileRegisterSupportCode));


        if(!isWebTerminal){
            mobileRegisterSupportCode=Lists.newArrayList(mobileAppRegisterSupportCodeStr.split(","));
        }
        log.info("finalMobileRegisterSupportCode={}",JsonUtils.toJsonNotNullKey(mobileRegisterSupportCode));
        if(CollectionUtils.isNotEmpty(mobileRegisterSupportCode)){
            List<Country>  countryList=getAllCountryList();
            List<CountryVo> supportCountryList= Lists.newArrayList();
            for(Country co:countryList){
                if(mobileRegisterSupportCode.contains(co.getCode())){
                    CountryVo vo = new CountryVo();
                    BeanUtils.copyProperties(co, vo);
                    CountryImageUrlEnum countryImageUrlEnum=CountryImageUrlEnum.getCountryImageUrlEnummByCode(vo.getCode());
                    if(null!=countryImageUrlEnum){
                        vo.setCountryImageUrl(countryImageUrlEnum.getCountryImageUrl());
                    }
                    supportCountryList.add(vo);
                }
            }
            resp.setSupportCountryList(supportCountryList);
        }
        return resp;
    }

    @Override
    public boolean isSupportMobileRegisterCountry(String code) {
        Country country=getCountryByMobileCodeOrCountryCode(code);
        if(org.apache.commons.lang3.StringUtils.isNotBlank(mobileWebRegisterSupportCodeStr) && null!=country){
            List<String> mobileWebRegisterSupportCode=Lists.newArrayList(mobileWebRegisterSupportCodeStr.split(","));
           return mobileWebRegisterSupportCode.contains(country.getCode());
        }
        return false;
    }
}
