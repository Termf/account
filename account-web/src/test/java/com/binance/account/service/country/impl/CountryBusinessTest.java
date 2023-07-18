package com.binance.account.service.country.impl;

import com.binance.account.data.entity.country.Country;
import com.binance.account.data.mapper.country.CountryMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author liliang1
 * @date 2019-01-14 9:36
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CountryBusinessTest extends Mockito {

    @InjectMocks
    private CountryBusiness countryBusiness;
    @Mock
    private CountryMapper countryMapper;

    @Test
    public void testGetCountryByCode() {
        String code = "cn";
        Country country = new Country();
        country.setCode("CN");
        country.setCode2("CHN");
        country.setCn("中国");
        country.setEn("China");
        when(countryMapper.selectByPrimaryKey(Mockito.anyString())).thenReturn(country);
        Country result = countryBusiness.getCountryByCode(code);
        assert result != null && StringUtils.equals("CN", result.getCode());

        // 注意，经测试，在正常的容器中该方法传任null的参数值时是会报错误：java.lang.IllegalArgumentException: Null key returned for cache operation
        Country result2 = countryBusiness.getCountryByCode(null);
        assert result2 == null;
    }


}
