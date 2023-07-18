package com.binance.account.service.kyc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.mapper.certificate.CountryStateMapper;

import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CountryStateHelper implements ApplicationContextAware {

	private static CountryStateMapper countryStateMapper;

	private static ConcurrentHashMap<String, List<CountryState>> LOCAL_CACHE;

	public static synchronized void init() {
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			log.info("初始化COUNTRY_STATE信息开始");
			ConcurrentHashMap<String, List<CountryState>> temp = new ConcurrentHashMap<>();
			List<CountryState> results = countryStateMapper.selectAll();
			for (CountryState countryState : results) {
				if (temp.get(countryState.getCode()) == null) {
					temp.put(countryState.getCode(), new ArrayList<CountryState>());
					log.info("初始化COUNTRY_STATE信息结束,国家:{}", countryState.getCode());
				}
				temp.get(countryState.getCode()).add(countryState);
			}
			LOCAL_CACHE = temp;
			log.info("初始化COUNTRY_STATE信息结束");
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
	}

	public static List<CountryState> getCountryStateByCode(String code, Boolean enable) {
		if (StringUtils.isBlank(code)) {
			return getAllCountryState(enable);
		}
		List<CountryState> tmp = LOCAL_CACHE.get(code);
		if (tmp == null) {
			return null;
		}
		if (enable == null) {
			return tmp;
		}
		return tmp.stream().filter(item -> (item.getEnable() == enable.booleanValue())).collect(Collectors.toList());
	}

	public static CountryState getCountryStateByPk(String code, String stateCode) {
		List<CountryState> tmp = LOCAL_CACHE.get(code);
		if (tmp == null) {
			return null;
		}
		for (CountryState countryState : tmp) {
			if (countryState.getStateCode().equals(stateCode)) {
				return countryState;
			}
		}
		return null;
	}

	public static List<CountryState> getAllCountryState(Boolean enable) {
		List<CountryState> result = new ArrayList<>();
		for (Map.Entry<String, List<CountryState>> entry : LOCAL_CACHE.entrySet()) {
			List<CountryState> value = entry.getValue();
			if (enable == null) {
				result.addAll(value);
				continue;
			}
			result.addAll(value.stream().filter(item -> (item.getEnable() == enable.booleanValue()))
					.collect(Collectors.toList()));
		}
		return result;
	}

	public static void updateCountryState(CountryState countryState) {
		countryStateMapper.updateByPrimaryKeySelective(countryState);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CountryStateHelper.countryStateMapper = applicationContext.getBean(CountryStateMapper.class);
		CountryStateHelper.init();
	}
}
