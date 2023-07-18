package com.binance.account.service.question.export.impl;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.binance.account.Application;
import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.mapper.security.QuestionRepositoryMapper;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.vo.question.AnswerRequestBody;
import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.account.vo.question.CreateQuestionVo;
import com.binance.account.vo.question.QueryConfigRequestBody;
import com.binance.account.vo.question.QueryConfigResponseBody;
import com.binance.account.vo.question.QueryLogRequestBody;
import com.binance.account.vo.question.QueryLogResponseBody;
import com.binance.account.vo.question.QueryLogStaticsRequestBody;
import com.binance.account.vo.question.QueryLogStaticsResponseBody;
import com.binance.account.vo.question.Question;
import com.binance.account.vo.question.QuestionConfigRequestBody;
import com.binance.account.vo.question.QuestionConfigResponseBody;
import com.binance.account.vo.question.QuestionInfoRequestBody;
import com.binance.account.vo.question.QuestionRequestBody;
import com.binance.account.vo.question.QuestionResponseBody;
import com.binance.account.vo.reset.response.ResetQuestion;
import com.binance.messaging.common.utils.UUIDUtils;
import com.google.common.collect.Maps;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class QuestionServiceTest {

	@Resource
	private IQuestion iquestion;
	@Resource
	private QuestionRepositoryMapper mapper;

	@Test
	public void testdoQuestion() {
		String flowId = UUIDUtils.getId();
		String flowType = UserSecurityResetType.google.name();
		CreateQuestionVo createQuestionVo = CreateQuestionVo.builder().userId(350462043L).flowId(flowId)
				.flowType(flowType).failCallback("failPath").successCallback("successPath").build();
		iquestion.createQuestionFlow(createQuestionVo);

		QuestionConfigRequestBody body = new QuestionConfigRequestBody();
		body.setFlowId(flowId);
		body.setFlowType(flowType);
		QuestionConfigResponseBody configs = iquestion.getQuestionConfig(body);
		System.out.println("configs:" + configs);
		assertTrue(configs != null);

		QuestionRequestBody one = new QuestionRequestBody();
		one.setFlowId(flowId);
		one.setFlowType(flowType);
		QuestionResponseBody qs = iquestion.getQuestionsV2(one);
		System.out.println("qs:" + qs);
		assertTrue(qs != null);

		qs.getQuestions().forEach(q -> {
			AnswerRequestBody answers = new AnswerRequestBody();
			answers.setFlowId(flowId);
			answers.setQuestionId(q.getQuestionId());
			answers.setAnswers(Arrays.asList(q.getOptions().get(0)));
			answers.setDeviceInfo(Maps.newHashMap());
			AnswerResponseBody result = iquestion.answerQuestionV2(answers);
			System.out.println("result:" + result);
			assertTrue(result != null);
		});
	}

	@Test
	public void testgetUserQuestionLog() {
		QueryLogRequestBody body = new QueryLogRequestBody();
		body.setUserId(350461863L);
		body.setFlowId("e7af208506094ab9ad22c3c3210e9de6");
		QueryLogResponseBody logs = iquestion.getUserQuestionLog(body);
		System.out.println("question detail logs:" + logs);
		assertTrue(logs != null && logs.getBody() != null);
	}

	@Test
	public void testgetUserLogStatics() {
		QueryLogStaticsRequestBody body = new QueryLogStaticsRequestBody();
		//body.setFlowType("mobile");
		//body.setStartTime(DateUtils.getNewUTCDateAddDay(-60));
		//body.setEndTime(DateUtils.getNewUTCDate());
		body.setOffset(0);
		body.setLimit(10);
		QueryLogStaticsResponseBody logs = iquestion.getUserLogStatics(body);
		System.out.println("statics logs:" + logs);
		assertTrue(logs != null && logs.getBody() != null);
	}
	
	@Test
	public void testManagerQuestions() {
		createQuestions(QuestionSceneEnum.RESET_2FA,"Paper-A");// A 卷
		createQuestions(QuestionSceneEnum.RESET_2FA,"Paper-B");// B 卷
		createQuestions(QuestionSceneEnum.RESET_2FA,"Paper-C");// C 卷
		
		createQuestions(QuestionSceneEnum.AUTH_DEVICE,"Paper-A");// A 卷
		createQuestions(QuestionSceneEnum.AUTH_DEVICE,"Paper-B");// B 卷
		createQuestions(QuestionSceneEnum.AUTH_DEVICE,"Paper-C");// C 卷
	}

	private void createQuestions(QuestionSceneEnum scene,String group) {
		QuestionInfoRequestBody body = new QuestionInfoRequestBody();
		//body.setOperator("zwh");
		body.setRules(Arrays.asList(scene.getDefaultRule()));
		body.setScene(scene);
		body.setGroup(group);
		// 用现有的问题
		List<QuestionRepository> qs = mapper.selectEnableALL();
		Map<String, QuestionRepository> map = Maps.newHashMap();
		qs.forEach(q->{
			map.putIfAbsent(q.getRiskType(), q);
		});
		Collection<QuestionRepository> questions = map.values();
		int weight = 100/questions.size();
		System.out.println("weight -> "+weight);
		int count = 0;
		for (QuestionRepository q : questions) {
			Question question =new Question(); 
			BeanUtils.copyProperties(q, question);
			question.setDocLangFlag(null);
			int w = weight;
			if (count++ == qs.size() - 1) {
				// 权重相加 必须 == 100
				int cha = 100 - weight * qs.size();
				if (cha > 0) {
					w = weight + cha;
				}
			}
			question.setWeight(w);
			body.setQuestion(question);
			iquestion.managerQuestions(body);
		}
	}

	@Test
	public void testGetConfig() {
		QueryConfigRequestBody body =new QueryConfigRequestBody();
		body.setGroup("");
		QueryConfigResponseBody response = iquestion.getConfig(body);
		System.out.println("response ->  "+response);
	}

	@Test
	public void testNeedToAnswerQuestion() {
		 boolean result = iquestion.needToAnswerQuestion(350461863L, UserSecurityResetType.authDevice.name(),  Maps.newHashMap());
		 System.out.println("testNeedToAnswerQuestion ->  "+result);
	}
	
	@Test
	public void testgetQuestionsAndAnswerV2() {
		String flowId = UUID.randomUUID().toString().replace("-", "");
		String flowType = UserSecurityResetType.google.name();
		CreateQuestionVo createQuestionVo = CreateQuestionVo.builder()
				.userId(350461863L)
				.flowId(flowId)
				.flowType(flowType)
				.failCallback("/fail")
				.timeout(10L)
				.successCallback("/success")
				.build();
		iquestion.createQuestionFlow(createQuestionVo);
		
		QuestionRequestBody body =new QuestionRequestBody();
		body.setFlowId(flowId);
		body.setFlowType(flowType);
		QuestionResponseBody questions = iquestion.getQuestionsV2(body);
		System.out.println("questions->" + questions);
		
		for (ResetQuestion q : questions.getQuestions()) {
			AnswerRequestBody answers =new AnswerRequestBody();
			answers.setFlowId(flowId);
			answers.setDeviceInfo(Maps.newHashMap());
			answers.setQuestionId(q.getQuestionId());
			answers.setAnswers(Arrays.asList(q.getOptions().get(0)));
			AnswerResponseBody response = iquestion.answerQuestionV2(answers);
			System.out.println("response->" + response);
		}
		
		try {
			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
