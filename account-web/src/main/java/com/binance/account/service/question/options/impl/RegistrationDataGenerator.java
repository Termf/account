package com.binance.account.service.question.options.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.user.User;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.question.options.IOptionsService;
import com.binance.account.service.question.options.QustionOptionsTypeEnum;
import com.binance.account.service.question.options.UserQuestionEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;

/**
 * 注册区间 https://jira.toolsfdg.net/browse/RM-361
 *
 */
@Slf4j
@Service
public class RegistrationDataGenerator implements IOptionsService {
	
	private static final List<String> OPTIONS = new ArrayList<String>(8);
	static {
		OPTIONS.add("Before 2018.01");
		OPTIONS.add("2018.01-2018.03");
		OPTIONS.add("2018.04-2018.06");
		OPTIONS.add("2018.07-2018.09");
		OPTIONS.add("2018.10-2018.12");
		OPTIONS.add("2019.01-2019.03");
		OPTIONS.add("2019.04-2019.06");
		OPTIONS.add("After 2019.06");
	}
	
	@Resource
	private QuestionModuleChecker checker;
	
	@Override
	public List<String> genaerateOptions(Long userId) {
		return OPTIONS;
	}

	@Override
	public List<String> getCorrectAnswers(Long userId, UserQuestionEnum questionName) {
		Assert.isTrue(questionName.getOptionType() == QustionOptionsTypeEnum.USER_REGISTRATION_DATE);
		User user = checker.userExistValidate(userId);
		// 用户注册时间 user
		Date date = user.getInsertTime();
		Assert.notNull(date, "用户没有注册时间,userId:" + userId);
		String registrationData = DateUtils.formatterUTC(date,"yyyy.MM");
		Assert.hasText(registrationData,"用户注册时间格式化错误,userId:" + userId);
		for (String d : OPTIONS) {
			String[] pair = StringUtils.split(d, "-");
			if (pair.length > 1) {
				if (registrationData.compareTo(pair[0]) >= 0 && registrationData.compareTo(pair[1]) <= 0) {
					return Arrays.asList(d);
				}
			}
			if(registrationData.compareTo("2018.01")<0) {
				return Arrays.asList(OPTIONS.get(0));
			}
			if(registrationData.compareTo("2019.06")>0) {
				return Arrays.asList(OPTIONS.get(OPTIONS.size() - 1));
			}
		}
		log.error("用户注册时间,没找到正确选项,userId:{}", userId);
		throw new BusinessException(GeneralCode.SYS_ERROR);
	}

	@Override
	public List<String> trimming(List<String> options, List<String> correctAnswers) {
		correctAnswers.retainAll(options);
		return correctAnswers;
	}
}
