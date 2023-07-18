
package com.binance.account.service.country.impl;

import com.binance.account.data.entity.country.CountryBlacklist;
import com.binance.account.data.mapper.country.CountryBlacklistMapper;
import com.binance.account.service.file.IFileStorage;
import com.binance.master.error.BusinessException;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After; 
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** 
* Created by Shining.Cai on 10/29/2018.
*/ 
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CountryBlacklistBusinessTest extends Mockito{ 

    @InjectMocks
    private CountryBlacklistBusiness targetService;
    @Mock
    private CountryBlacklistMapper countryBlacklistMapper;
    @Mock
	private IFileStorage fileStorage;
    @Before
    public void before() throws Exception {
        CountryBlacklist cn = new CountryBlacklist();
        cn.setCountryCode("CN");
        cn.setIsActive(true);
        when(countryBlacklistMapper.selectByPrimaryKey("CN")).thenReturn(cn);
    } 

    @After
    public void after() throws Exception { 
    } 

    /** 
    * 
    * Method: isBlack(String countryCode) 
    * 
    */ 
    @Test
    public void testIsBlack() throws Exception { 
        assert targetService.isBlack("cn");
    } 

    /** 
    * 
    * Method: listAll() 
    * 
    */ 
    @Test
    public void testListAll() throws Exception { 

    } 

    /** 
    * 
    * Method: add(CountryBlacklist blacklist) 
    * 
    */ 
    @Test(expected = BusinessException.class)
    public void testAdd() throws Exception {
        targetService.add(new CountryBlacklist("SG", "new"));
        targetService.add(new CountryBlacklist("CN", "already exist"));
    }

    /** 
    * 
    * Method: update(CountryBlacklist blacklist) 
    * 
    */ 
    @Test(expected = BusinessException.class)
    public void testUpdate() throws Exception {
        targetService.update(new CountryBlacklist("CN", "already exist"));
        targetService.update(new CountryBlacklist("SG", "new"));

    }

    /** 
    * 
    * Method: delete(String countryCode) 
    * 
    */ 
    @Test(expected = BusinessException.class)
    public void testDelete() throws Exception {
        targetService.update(new CountryBlacklist("SG", "new"));
        targetService.update(new CountryBlacklist("CN", "already exist"));
    } 


} 
