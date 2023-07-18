package com.binance.account.service.question.export.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.common.enums.ResetAnswerStatus;
import com.binance.account.common.query.QuestionQuery;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.QuestionConfig;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.data.entity.security.UserQuestionOptions;
import com.binance.account.data.mapper.security.QuestionConfigMapper;
import com.binance.account.data.mapper.security.QuestionRepositoryMapper;
import com.binance.account.data.mapper.security.UserQuestionAnswerMapper;
import com.binance.account.data.mapper.security.UserQuestionOptionsMapper;
import com.binance.account.service.question.Utils;
import com.binance.account.service.question.BO.OptionsWrapper;
import com.binance.account.service.question.export.IDecisionMgmtService;
import com.binance.account.service.question.options.OptionsGeneratorFactory;
import com.binance.account.service.question.options.UserQuestionEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 问题模块本地存储功能支持
 *
 */
@Slf4j
@Service
public class QuestionStoreService {
	private final String REDIS_PREFIX = "accout:question:store-service:";
	@Value("${question.cache.timeout.seconds:1800}")
	private int questionCacheTimeOutInSeconds;
	@Resource
	private QuestionRepositoryMapper questionRepositoryMapper;
	@Resource
	private UserQuestionAnswerMapper userQuestionAnswerMapper;
	@Resource
	private UserQuestionOptionsMapper userQuestionOptionsMapper;
	@Resource
	private QuestionConfigMapper questionConfig;
	@Resource
	private ApolloCommonConfig commonConfig;
	@Resource
	private IDecisionMgmtService mgmtService;
	
	
	private <T> List<T> getCache(String key,Class<T> type) {
		String json = RedisCacheUtils.get(key, String.class, REDIS_PREFIX);
		if (StringUtils.isNotBlank(json)) {
			log.info("question store -> 查询缓存。key:{},JSON:{}", key, json);
			return JSON.parseArray(json, type);
		}
		return new ArrayList<>(0);
	}
	

	private <T> void putCache(String key, List<T> questions) {
		if (CollectionUtils.isEmpty(questions)) {
			return;
		}
		RedisCacheUtils.set(key, JSON.toJSONString(questions), questionCacheTimeOutInSeconds, REDIS_PREFIX);
	}

	/**
	 * 返回指定场景的 <问题id,问题> map
	 * 
	 * @param userId
	 * 
	 * @return
	 */
	public List<QuestionRepository> getQuestions(Long userId, String flowType) {
		log.info("question store -> 获取用户应该回答的问题。userId:{},flowType:{}", userId, flowType);
		
		String key = StringUtils.join(userId,":",flowType);
		List<QuestionRepository> tmp = getCache(key, QuestionRepository.class);
		if (CollectionUtils.isNotEmpty(tmp)) {
			return tmp;
		}
		
		// 从数据库查询最新的配置
		QuestionSceneEnum scene = QuestionSceneEnum.ConvertUserSecurityResetTypeToScene(flowType);
		final List<QuestionConfig> configs = questionConfig.selectBy(scene.ordinal(), null);
		if (CollectionUtils.isEmpty(configs)) {
			log.error("question store -> 没有配置此场景的问题。 userId:{},scene:{}", userId, scene);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		
		/**
		 * 风控命中的规则与同一个场景的一套题一一对应
		 */
		List<String> rule = mgmtService.getRules(userId, scene);
		if(CollectionUtils.isEmpty(rule)) {
			log.warn("question store -> 没有命中规则，不需要答题。 userId:{},scene:{}", userId, scene);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// RM-406
		if (rule.size() > 1) {
			log.info("question store -> 命中多个规则，删除默认的取剩下的第一个。 userId:{},scene:{},rule:{}", userId, scene, rule);
			rule.remove(scene.getDefaultRule());
			rule = Arrays.asList(rule.get(0));
		}
		List<QuestionRepository> questions = findQuestionSet(configs, rule);
		if (CollectionUtils.isEmpty(questions)) {
			log.error("question store -> 没有查询到命中风控规则的问题。userId:{},scene:{}", userId, scene);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 缓存问题
		putCache(key, questions);
		log.info("question store -> 获取用户应该回答的问题,userId:{},flowType:{},questions:{}", userId, flowType,questions);
		return questions;
	}


	private List<QuestionRepository> findQuestionSet(final List<QuestionConfig> configs, final List<String> rules) {
		List<QuestionRepository> questions = new LinkedList<>();
		for (QuestionConfig c : configs) {
			for (String r : c.getRules()) {
				// 不区分大小写
				if (rules.contains(r)) { 
					// 选择命中风控规则的套题
					log.info("question store -> 规则:{},套题:{}", r, c.getGroup());
					List<QuestionRepository> selectBy = questionRepositoryMapper.selectBy(c.getGroup(), null);
					// 获取启用的问题
					questions.addAll(selectBy.stream().filter(q -> q.getEnable() == 0).collect(Collectors.toList()));
				}	
			}
		}
		// 去掉重复的题目
		return questions.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * 构建用户需要回答的问题及其选项
	 * 
	 * @param userId
	 * @param flowId
	 * @param flowType
	 * @return
	 */
	public List<UserQuestionAnswers> buildUserQuestionAnswers(Long userId, String flowId, String flowType) {
		log.info("question store -> 构造用户问题,userId:{},flowId:{},flowType:{} ", userId, flowId, flowType);
		
		List<QuestionRepository> questions = this.getQuestions(userId,flowType);
		
		// 找出没有缓存选项的问题
		List<String> questionNames = questions.stream().map(q -> q.getRiskType()).collect(Collectors.toList());
		Map<String, OptionsWrapper> riskOptionMap = mgmtService.getQuestionOptions(userId, questionNames);
		
		final List<String> newGetRiskList = fillOptionsIfExist(userId, questions);
		if (!newGetRiskList.isEmpty()) {
			// 拉取选项信息
			Map<String, OptionsWrapper> map = riskOptionMap.entrySet().stream()
			.filter(en->newGetRiskList.contains(en.getKey()))
			.collect(Collectors.toMap(en->en.getKey(), en->en.getValue()));
			
			// 缓存最新的问题选项
			saveQuestionOptions(userId, map);
		}
		// 创建并保存用户流程答题信息
		List<UserQuestionAnswers> answers = new ArrayList<>(questions.size());
		long anserId = DateUtils.getNewUTCTimeMillis().longValue();
		for (QuestionRepository question : questions) {
			UserQuestionAnswers answer = defaultAnswer();
			answer.setUserId(userId);
			answer.setAnswerId(anserId);// 同一个流程下，当前答题标记id，用于统计答题次数
			answer.setFlowId(flowId);
			answer.setFlowType(flowType);// type的name
			answer.setQuestionId(question.getId());
			answer.setQuestionType(question.getRiskType());
			// 问题选项
			List<String> options = question.getOptions();
			if (CollectionUtils.isEmpty(options)) {
				options = riskOptionMap.get(question.getRiskType()).getOptions();// 从新获取的
			}
			List<String> correctAnswers = riskOptionMap.get(question.getRiskType()).getAnswers();
			// 微调
			correctAnswers = trimming(options, correctAnswers, question.getRiskType());
			
			answer.setOptions(JSON.toJSONString(options));
			answer.setCorrectAnswer(JSON.toJSONString(correctAnswers));
			// 保存问题相关数据
			userQuestionAnswerMapper.insert(answer);
			answers.add(answer);
		}
		return answers;
	}

	private List<String> trimming(List<String> options, List<String> correctAnswers, String riskType) {
		UserQuestionEnum convert2Enum = UserQuestionEnum.Convert2Enum(riskType);
		return OptionsGeneratorFactory.getOptionsService(convert2Enum).trimming(options, correctAnswers);
	}


	private UserQuestionAnswers defaultAnswer() {
		UserQuestionAnswers answer = new UserQuestionAnswers();
		answer.setAnswers("");
		answer.setCorrectAnswer("");
		answer.setScore(0);
		answer.setPoint(0);
		answer.setStatus(ResetAnswerStatus.UnDone);// 答题后为Done
		answer.setCreateTime(DateUtils.getNewUTCDate());
		answer.setUpdateTime(DateUtils.getNewUTCDate());
		answer.setPass(Boolean.FALSE);
		return answer;
	}
	
	
	
	private List<UserQuestionOptions> saveQuestionOptions(final Long userId, Map<String, OptionsWrapper> optionMap) {
        final List<UserQuestionOptions> options = new ArrayList<>(optionMap.size());
        optionMap.entrySet().forEach(item -> {
            UserQuestionOptions opt = new UserQuestionOptions();
            String riskType = item.getKey();
            List<String> optionList = item.getValue().getOptions();
            opt.setUserId(userId);
            opt.setRiskType(riskType);
            opt.setOptions(JSON.toJSONString(optionList));// 保存字符串
            opt.setCreateTime(DateUtils.getNewDate());
            options.add(opt);
        });
        if (!CollectionUtils.isEmpty(options)) {
            userQuestionOptionsMapper.insertBatch(options);
        }
        return options;
    }

	/**
	 * 获取缓存的用户答题选项
	 *
	 */
	private UserQuestionOptions getEffectiveQuestionOptions(Long userId, String riskTyp) {
		UserQuestionOptions options = userQuestionOptionsMapper.selectByPrimaryKey(userId, riskTyp);
		if (options == null || options.getCreateTime() == null) {
			return null;
		}
		Date createTime = options.getCreateTime();
		Date addDays = DateUtils.addDays(createTime, commonConfig.getResetQuestionOptionsTimeOut());
		boolean expire = addDays.compareTo(DateUtils.getNewUTCDate()) < 0;
		if (expire) {
			// 删除过期的数据
			userQuestionOptionsMapper.deleteByPrimaryKey(options.getId());
			return null;
		} else {
			// 没有过期的话直接使用
			return options;
		}
	}

	/**
	 * 补充存在未过期选项缓存的问题，从原集合中删除并且返回需要重新生成选项的问题
	 */
	private List<String> fillOptionsIfExist(final long userId, final Collection<QuestionRepository> questions) {
		List<String> tmp = new LinkedList<>();
		questions.forEach(q -> {
			UserQuestionOptions options = this.getEffectiveQuestionOptions(userId, q.getRiskType());
			if (options != null) {
				q.setOptions(Utils.parseToListFromJson(options.getOptions()));
			} else {
				tmp.add(q.getRiskType());
			}
		});
		return tmp;
	}

	/**
     * 获取用户某一次流程所有的答题记录，并且按照anserId为key分别封装
     *
     * @param userId
     * @param flowId
     * @return
     */
	public List<UserQuestionAnswers> getUserFlowAnswerMapByAnswerId(Long userId, String flowId) {
		return userQuestionAnswerMapper.selectByKey(userId, flowId, null, null);
	}
	
	/**
	 * 对象字段非空就保存
	 * 
	 * @param answer
	 * @return 影响行数
	 */
	public int saveUserQuestionAnswer(final UserQuestionAnswers answer) {
		return userQuestionAnswerMapper.updateSelective(answer);
	}
	
	/**
	 * 查询用户当前流程，已经回答的次数
	 * 
	 * @param userId
	 * @param flowId
	 * @return
	 */
	public int getFlowCurrentAnswerTimes(Long userId, String flowId){
		return userQuestionAnswerMapper.getFlowCurrentAnswerTimes(userId, flowId);
	}
	
	/**
	 * 在回答问题流水表按照条件查询
	 * 
	 * @param query
	 * @return
	 */
	public List<UserQuestionAnswers> getUserQuestionAnswers(final QuestionQuery query) {
        return userQuestionAnswerMapper.getListByUser(query);
    }
	
	/**
	 * 保存问题
	 * 
	 * @param question
	 * @return
	 */
	public int saveQuestionRepository(final QuestionRepository question) {
		return questionRepositoryMapper.insertOrUpdate(question);
	}
	
	public List<QuestionRepository> queryBy(final String set,final String riskType){
	   return questionRepositoryMapper.selectBy(set,riskType);
	}
}
