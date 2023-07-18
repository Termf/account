package com.binance.account.service.question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("accout_question_utils_bean")
public class Utils implements ApplicationContextAware {
	
	private static ApplicationContext appContext;

	public static <T> String toJsonString(T t) {
		return JSON.toJSONString(t);
	}

	/**
	 * json 转成 List<String>
	 * 
	 * @param json
	 * @return
	 */
	public static List<String> parseToListFromJson(String json) {
		return parseToListFromJson(json,String.class);
	}
	
	public static <T> List<T> parseToListFromJson(String json, Class<T> type) {
		if (StringUtils.isBlank(json)) {
			return new ArrayList<>(0);
		}
		return JSON.parseArray(json, type);
	}

	/**
	 * 微服务返回值校验
	 * 
	 * @param <T>
	 * @param response
	 */
	public static <T> void CheckResponse(APIResponse<T> response) {
		if (response == null || !Objects.equals(response.getStatus(), Status.OK)) {
			log.error("Invalided response:{}", response);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}
	
	/**
	 * 按照分号分开
	 * 
	 * @param source
	 * @return
	 */
	public static String[] splieBySemicolon(String source) {
		return StringUtils.split(source, ";");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
	}
	
	public static <T> T getBean(Class<T> requiredType) {
		return appContext.getBean(requiredType);
	}
	
	public static <T> List<T> getBeans(Class<T> requiredType) {
		Map<String, T> names = appContext.getBeansOfType(requiredType);
		return new ArrayList<>(names.values());
	}
	
	public static <T> T getBean(String beanName,Class<T> requiredType) {
		return appContext.getBean(beanName,requiredType);
	}
}
