package com.binance.account.service.kyc;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.binance.account.common.enums.CacheRefreshType;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.vo.kyc.CacheRefreshVo;
import com.binance.master.constant.MQConstant;

public class CountryStateHelperTest extends BaseTest{
	
	@Resource
    private RabbitTemplate rabbitTemplate;
	
	@Test
	public void test() {
//		List<CountryState> list = CountryStateHelper.getCountryState("US");
//		for (CountryState countryState : list) {
//			System.out.println(countryState);
//		}
		List<CountryState> list = CountryStateHelper.getCountryStateByCode("US", null);
		System.out.println(list.size());
		list =CountryStateHelper.getCountryStateByCode("US", true);
		System.out.println(list.size());
		list =CountryStateHelper.getCountryStateByCode("US", false);
		System.out.println(list.size());
		
		CountryState s =CountryStateHelper.getCountryStateByPk("US", "AK");
		
		s.setCode("US");
		s.setStateCode("AK");
		s.setEn("Alaska");
		s.setCn("阿拉斯加");
		s.setNationality("UNITED");
		s.setEnable(false);
		
		CountryStateHelper.updateCountryState(s);
	}
	
	@Test
	public void testRefresh() {
		CacheRefreshVo vo = new CacheRefreshVo();
		vo.setType(CacheRefreshType.COUNTRY_STATE);
		rabbitTemplate.convertAndSend("account.cache.refresh", null, vo);
	}

}
