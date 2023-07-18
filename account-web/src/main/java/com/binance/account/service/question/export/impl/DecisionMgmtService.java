package com.binance.account.service.question.export.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.service.question.Utils;
import com.binance.account.service.question.BO.OptionsWrapper;
import com.binance.account.service.question.BO.QuestionScoreBody;
import com.binance.account.service.question.BO.QuestionScoreBody.QuestionScore;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.question.export.IDecisionMgmtService;
import com.binance.account.service.question.options.IOptionsService;
import com.binance.account.service.question.options.OptionsGeneratorFactory;
import com.binance.account.service.question.options.UserQuestionEnum;
import com.binance.decision.vo.user.question.request.QuestionRequest;
import com.binance.decision.vo.user.question.request.QuestionScoreRequest;
import com.binance.decision.vo.user.question.request.QuestionScoreRequest.QuestionAnswer;
import com.binance.decision.vo.user.question.response.request.QuestionResponse;
import com.binance.decision.vo.user.question.response.request.QuestionScoreResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.IPUtils;
import com.binance.master.utils.WebUtils;
import com.binance.rule.api.CommonRiskApi;
import com.binance.rule.api.UserQuestionDecisionApi;
import com.binance.rule.request.DecisionCommonRequest;
import com.binance.rule.response.DecisionCommonResponse;
import com.google.common.collect.Maps;
import com.ip2location.IP2Location;

import lombok.extern.slf4j.Slf4j;

/**
 * 问答模块用到的风控接口统一封装
 * <p>
 * 是否需要回答问题
 * </p>
 * <p>
 * 特定场景，用户应该回答那套问题
 * </p>
 * <p>
 * 用户答题打分
 * </p>
 * 
 * @author zwh-binance
 *
 */
@Slf4j
@Service
public class DecisionMgmtService implements IDecisionMgmtService {
	@Resource
	private QuestionModuleChecker checker;
	@Resource
	private UserQuestionDecisionApi decisionApi;
	@Resource
	private CommonRiskApi commonRiskApi;
	@Resource
	private QuestionStoreService questionStoreService;
	
	@Value("${account.question.risk.event.question:reset2fa_need_question}")
	private String questionEvent;// 是否回答问题
	@Value("${account.question.risk.event.rule:common_question}")
	private String ruleEvent;// 命中规则
	
	/**
	 * 获取问题及其选项
	 * 
	 * @param userId
	 * @param riskTypes 问题名称
	 * @return
	 */
	@Override
	public Map<String, OptionsWrapper> getQuestionOptions(final Long userId, final List<String> riskTypes) {
		log.info("risk->获取答题选项. userId:{} riskType:{}", userId, riskTypes);
		Map<String, OptionsWrapper> optionMap =Maps.newHashMapWithExpectedSize(riskTypes.size());
		for (String question : riskTypes) {
			/* 
			 *1、 手动配置的数据库问题名称risktype 应该与问题枚举 一一对应
			 *2、 每个问题理论上都有自己的选项和答案实现逻辑
			 */
			UserQuestionEnum questionName = UserQuestionEnum.Convert2Enum(question);
			IOptionsService service = OptionsGeneratorFactory.getOptionsService(questionName);
			List<String> options = service.genaerateOptions(userId);// 选项
			List<String> answers = service.getCorrectAnswers(userId, questionName); // 答案
			OptionsWrapper wrapper = OptionsWrapper.builder()
					.answers(answers)
					.options(options)
					.build();
			optionMap.put(question, wrapper);
		}
		if (optionMap.isEmpty()) {
			log.error("risk->未获取到答题选项. userId:{},riskType:{}", userId, riskTypes);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		log.info("risk->获取答题选项. userId:{},选项和答案集合:{}", userId, JSON.toJSONString(optionMap));
		return optionMap;
	}

	/**
	 * 推送用户答案，获取打分结果
	 * 
	 * @param userId
	 * @param questionAnswers
	 * @return
	 */
	@Override
	public QuestionScoreBody getAnswerScore(final Long userId,final List<UserQuestionAnswers> questionAnswers) {
		List<QuestionRepository> questions = questionStoreService.getQuestions(userId, questionAnswers.get(0).getFlowType());
		Map<Long,Float> tmp =new HashMap<>(questionAnswers.size());
		questions.forEach(q->{
			// 百分比整数转小数
			float weight = new BigDecimal(q.getWeight()*1.0f/100).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			tmp.put(q.getId(), weight);
		});
		List<QuestionAnswer> answersLst = new ArrayList<>(questionAnswers.size());
		questionAnswers.forEach(question -> {
			QuestionAnswer answer = new QuestionAnswer();
			String questionType = question.getQuestionType();
			answer.setQuestion(questionType);// 问题类型
			answer.setOption(Utils.parseToListFromJson(question.getOptions()));// 问题选项原样返回
			answer.setAnswer(Utils.parseToListFromJson(question.getAnswers()));// 问题答案
			answer.setTimes(question.getUpdateTime().getTime());
			// 正确答案
			answer.setRightAnswer(Utils.parseToListFromJson(question.getCorrectAnswer()));
			// 问题权重
			answer.setWeights(tmp.get(question.getQuestionId()));
			answersLst.add(answer);
		});

		QuestionScoreRequest request = new QuestionScoreRequest();
		request.setUserId(userId);
		request.setQuestionAnswerList(answersLst);
		APIResponse<QuestionScoreResponse> response = null;
		try {
			response = decisionApi.scoreForQuestion(APIRequest.instance(request));
			log.info("risk->上传答案结果,req:{}, resp:{}", JSON.toJSONString(request), JSON.toJSONString(response));
		} catch (Exception e) {
			log.error("risk->上传答案异常. request:" + JSON.toJSONString(request), e);
		}
		// 返回结果异常
		Utils.CheckResponse(response);
		QuestionScoreResponse data = response.getData();
		Assert.notNull(data, "");
		List<QuestionScore> scores = new ArrayList<>(data.getQuestionScoreResList().size());
		data.getQuestionScoreResList().forEach(score -> {
			QuestionScore sc = new QuestionScore();
			BeanUtils.copyProperties(score, sc);
			scores.add(sc);
		});
		return QuestionScoreBody.builder()
				.questionScoreList(scores)
				.weightedPointTotal(data.getWeightedPointTotal())
				.passThresholdValue(data.getPassThresholdValue())
				.result(data.getResult())
				.build();
	}
	
	/**
	 * 是否需要回答问题
	 * 
	 * @param userId
	 * @return
	 */
	@Override
	public boolean needToAnswerQuestions(final Long userId,final QuestionSceneEnum scene, final Map<String, String> device) {
		log.info("risk->是否需要回答问题,userId:{},scene:{}", userId, scene);
		// 新设备并且不是白ip的才需要回答问题
		boolean newDevice = checker.isNewDevice(userId, device);
		if (newDevice) {
			return needQuestionFromRisk(userId);
		}
		// 老设备不回答问题
		return false;
	}
	

	public boolean needQuestionFromRisk(Long userId) {
		log.info("risk->决策引擎,判断是否回答问题开始,userId:{}", userId);
		DecisionCommonRequest request = new DecisionCommonRequest();
		request.setEventCode(questionEvent);
		Map<String, Object> context = new HashMap<>(2);
		context.put("ip", requestIp());
		context.put("userId",userId);
		request.setContext(context);
		DecisionCommonResponse response = executeRequest(request);
		if (response == null) {
			return true;
		}
		return response.getIsHit();
	}

	private DecisionCommonResponse executeRequest(DecisionCommonRequest request) {
		String jsonString = JSON.toJSONString(request);
		APIResponse<DecisionCommonResponse> response = null;
		try {
			log.info("risk->决策引擎,req:{}", jsonString);
			response = commonRiskApi.commonRule(APIRequest.instance(request));
			log.info("risk->决策引擎,req:{}, resp:{}", jsonString, JSON.toJSONString(response));
			// 返回结果异常,默认需要答题
			Utils.CheckResponse(response);
		} catch (Exception e) {
			log.error("risk->决策引擎异常,返回默认规则,request:" + jsonString, e);
			return null;
		}
		return response.getData();
	}

	private String requestIp() {
		try {
			return WebUtils.getRequestIp();
		} catch (Exception e) {
			log.warn("risk->获取用户请求ip异常", e);
		}
		return "";
	}

	@Override
	public List<String> getRules(Long userId, QuestionSceneEnum scene) {
		log.info("risk->决策引擎,回去回答问题规则开始,userId:{}", userId);
		DecisionCommonRequest request = new DecisionCommonRequest();
		request.setEventCode(ruleEvent);
		Map<String, Object> context = new HashMap<>(2);
		context.put("scene", scene.name());
		context.put("userId", userId);
		request.setContext(context);
		DecisionCommonResponse response = executeRequest(request);
		if (response == null || response.getExtend() == null) {
			return Arrays.asList(scene.getDefaultRule());
		}
		String rule = response.getExtend().get("rule") + "";
		if ("default".equalsIgnoreCase(rule)) {
			return Arrays.asList(scene.getDefaultRule());
		}
		return Arrays.asList(rule);
	}
}
