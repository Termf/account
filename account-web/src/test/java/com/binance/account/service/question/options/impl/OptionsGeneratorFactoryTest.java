package com.binance.account.service.question.options.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.binance.account.Application;
import com.binance.account.service.question.options.IOptionsService;
import com.binance.account.service.question.options.OptionsGeneratorFactory;
import com.binance.account.service.question.options.UserQuestionEnum;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class OptionsGeneratorFactoryTest {
	
	@Test
	public void generatorTest() {
//		for (UserQuestionEnum qe : UserQuestionEnum.values()) {
//			test(qe);
//		}
		test(UserQuestionEnum.USER_TRADED_ASSET);
	}

	private void test(UserQuestionEnum q) {
		IOptionsService curger = OptionsGeneratorFactory.getOptionsService(q);
		assertTrue(curger != null);
		long userId = 350569249L;
		List<String> currencies = curger.genaerateOptions(userId);
		System.out.println(q+"->options:" + currencies);
		assertTrue(CollectionUtils.isNotEmpty(currencies));
		
		List<String> answers = curger.getCorrectAnswers(userId, q);
		System.out.println(q+"->answers:" + answers);
		assertTrue(CollectionUtils.isNotEmpty(answers));
		List<String> correctAnswers = curger.trimming(currencies, answers);
		System.out.println(q+"->correctAnswers:" + correctAnswers);
		assertTrue(CollectionUtils.isNotEmpty(correctAnswers));
	}

}
