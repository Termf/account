package com.binance.account.service.question.options.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.binance.account.service.question.options.IOptionsService;
import com.binance.account.service.question.options.IUserTraceService;
import com.binance.account.service.question.options.QustionOptionsTypeEnum;
import com.binance.account.service.question.options.UserQuestionEnum;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于持BTC数量的随机数区间的问题选项
 *
 */
@Slf4j
@Service
public class RandomInternalGenerator implements IOptionsService {

	@Value("${application.userQuestion.optionNumber:8}")
	private Integer optionNumber;// 选项数。
	@Value("${application.userQuestion.userBTCAmount.stepLength:0.01;0.05;0.1;0.5;}")
	private String stepLength;// btc步长。
	
	@Resource
	private IUserTraceService userTraceService;

	@Override
	public List<String> genaerateOptions(Long userId) {
		log.info("随机数区间生成选项，userId:{}",userId);
		
		String[] split = stepLength.split(";");
		String length = split[ThreadLocalRandom.current().nextInt(split.length)];

		BigDecimal len = new BigDecimal(length);
		List<String> thisOption = new ArrayList<>(optionNumber);
		String op;
		for (int i = 0; i < optionNumber; i++) {
			if (i == 0) {
				op = "0-" + len;
			} else if (i == optionNumber - 1) {
				op = ">" + (new BigDecimal(i + 1).multiply(len));
			} else {
				op = (new BigDecimal(i).multiply(len)) + "-" + (new BigDecimal(i + 1).multiply(len));
			}
			thisOption.add(op);
		}
		log.info("随机数区间生成选项，userId:{}，result:{}", userId, thisOption);
		return thisOption;
	}

	@Override
	public List<String> getCorrectAnswers(Long userId, UserQuestionEnum questionName) {
		Assert.notNull(questionName, "invalid param");
		Assert.isTrue(questionName.getOptionType() == QustionOptionsTypeEnum.RANDOM_NUMBER_INTERNAL,
				"wrong QustionOptionsTypeEnum");
		switch (questionName) {
		case USER_BTC_AMOUNT:
			BigDecimal amount = userTraceService.getUserAssetAmount(userId, "BTC");
			log.info("用户持有btc数量,userId:{},amount:{}", userId, amount);
			return Arrays.asList(amount.toString());// 用户交易量
		default:
			throw new RuntimeException("Invalid UserQuestionEnum:" + questionName);
		}
	}

	@Override
	public List<String> trimming(List<String> options, List<String> correctAnswers) {
		BigDecimal amout = new BigDecimal(correctAnswers.get(0));
		for (String o : options) {// [0-1,1-2,...,>9]
			String[] values = StringUtils.split(o, "-");
			if (values.length == 2) {
				BigDecimal left = new BigDecimal(values[0]);
				BigDecimal right = new BigDecimal(values[1]);
				if (left.compareTo(amout) <= 0 && right.compareTo(amout) >= 0) {
					return Arrays.asList(o);
				}
			}
			else {
				String[] last = StringUtils.split(o, ">");
				BigDecimal num = new BigDecimal(last[0]);
				if (num.compareTo(amout) <= 0) {
					return Arrays.asList(o);
				}
			}
		}
		throw new RuntimeException("Invalid options:" + options);
	}
}
