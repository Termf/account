package com.binance.account.service.question.export.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.binance.account.Application;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.entity.security.UserQuestionAnswers;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class QuestionStoreServiceTest {
	
	@Resource
	private QuestionStoreService service;

	private long userId = 350458800L;
	
	@Test
	public void testGetQuestions() {
		List<QuestionRepository> response = service.getQuestions(userId, UserSecurityResetType.authDevice.name());
		System.out.println("response:" + response);
	}

	@Test
	public void testBuildUserQuestionAnswers() {
		String flowId = UUID.randomUUID().toString().replace("-", "");
		List<UserQuestionAnswers> response = service.buildUserQuestionAnswers(userId, flowId, UserSecurityResetType.google.name());
		System.out.println("response:" + response);
	}
}
