package com.binance.account.service.question.export.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.AnswerCompleteStatus;
import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.common.enums.ResetAnswerStatus;
import com.binance.account.common.query.QuestionQuery;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.QuestionConfig;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserRiskFeature;
import com.binance.account.data.mapper.security.QuestionConfigMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.question.Utils;
import com.binance.account.service.question.BO.QuestionMessage;
import com.binance.account.service.question.BO.QuestionScoreBody;
import com.binance.account.service.question.BO.QuestionScoreBody.QuestionScore;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.question.export.IDecisionMgmtService;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.question.export.IQuestionHnadler;
import com.binance.account.vo.question.AnswerRequestBody;
import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.account.vo.question.CreateQuestionVo;
import com.binance.account.vo.question.QueryConfigRequestBody;
import com.binance.account.vo.question.QueryConfigResponseBody;
import com.binance.account.vo.question.QueryLogRequestBody;
import com.binance.account.vo.question.QueryLogResponseBody;
import com.binance.account.vo.question.QueryLogResponseBody.Body;
import com.binance.account.vo.question.QueryLogStaticsRequestBody;
import com.binance.account.vo.question.QueryLogStaticsResponseBody;
import com.binance.account.vo.question.Question;
import com.binance.account.vo.question.QuestionConfigRequestBody;
import com.binance.account.vo.question.QuestionConfigResponseBody;
import com.binance.account.vo.question.QuestionInfoRequestBody;
import com.binance.account.vo.question.QuestionInfoResponseBody;
import com.binance.account.vo.question.QuestionRequestBody;
import com.binance.account.vo.question.QuestionResponseBody;
import com.binance.account.vo.reset.response.ResetQuestion;
import com.binance.account.vo.reset.response.ResetQuestionBody;
import com.binance.master.commons.ToString;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.shardingsphere.api.HintManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class QuestionService implements IQuestion {
	
	public static final String REDIS_PREFIX="question:module:";
	
    @Resource
    private ApolloCommonConfig commonConfig;
    @Resource
    private IDecisionMgmtService mgmtService;
    @Resource
	private QuestionModuleChecker questionChecker;
    @Resource 
    private QuestionConfigMapper questionConfig;
    @Resource 
    private QuestionStoreService questionStoreService;
    
    //必须同步发送消息，否则丢失HTTP上下文
	private void sendMsgWhenFlowComplete(final QuestionMessage msg) {
		List<IQuestionHnadler> beans = Utils.getBeans(IQuestionHnadler.class);
		if (!CollectionUtils.isEmpty(beans)) {
			beans.forEach(bean->{
				bean.invoke(msg);
			});
		}
	}
    
    // 答题完毕发送答题通知，并且删除缓存
	private void sendMsgWhenFlowComplete(String flowId,String flowType, Long userId, AnswerResponseBody answerQuestionResponse) {
		QuestionMessage msg = new QuestionMessage();
		msg.setUserId(userId);
		msg.setFlowId(flowId);
		msg.setFlowType(flowType);
		msg.setResult(answerQuestionResponse);
		safaExecute(() -> {
			sendMsgWhenFlowComplete(msg);
		});
	}

	//缓存保存
    private void doRedisSet(final String key,final QuestionCache questionCache) {
    	safaExecute(() -> {
    		 // 此处封装不是事务性的
    		RedisCacheUtils.set(key, 
    				questionCache, 
    				questionCache.getTimeout(), 
    				REDIS_PREFIX);
		});
    }
    
    private void safaExecute(Runnable run) {
		try {
			run.run();
		} catch (Exception e) {
			log.error("", e);
		}
    }
    private <T> T safaExecute(Callable<T> call) {
		try {
			return call.call();
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
    }

    //缓存查询
    private QuestionCache doResidGet(final String key) {
    	return safaExecute(()->{
    		return RedisCacheUtils.get(key, QuestionCache.class, REDIS_PREFIX);
    	});
    }
    
    //答题完毕，删除缓存
    private void doRedisDelete(final String key) {
    	safaExecute(() -> {
    		RedisCacheUtils.del(key, REDIS_PREFIX);
    		log.info("问题模块->答题完毕,删除缓存，flowId:{}", key);
		});
    }
    
    //更新缓存超时时间
    private void doRedisExpire(final String key,long minutes) {
		safaExecute(() -> {
			RedisCacheUtils.expire(key, minutes, TimeUnit.MINUTES, REDIS_PREFIX);
		});
    }

    private QuestionCache getValidateQuestionCache(String flowId) {
        QuestionCache cache = doResidGet(flowId);
        if (cache == null || cache.getUserId() == null) {
            log.warn("问题模块->获取缓存信息失败. flowId:{}", flowId);
            throw new BusinessException(AccountErrorCode.QUESTION_FLOW_CACHE_EMPTY);
        }
        if (!StringUtils.equals(flowId, cache.getFlowId())) {
            log.error("问题模块->答题流程缓存值错误. flowId:{} cache:{}", flowId, JSON.toJSONString(cache));
            throw new BusinessException(AccountErrorCode.QUESTION_FLOW_CACHE_EMPTY);
        }
		log.info("问题模块->缓存内容:{}", cache);
        return cache;
    }

    /**
     * 获取允许的最大答题次数
     * @return
     */
    private int getMaxAllowCount() {
        return commonConfig.getProtectedTimes();
    }

    /**
     * return true if not valided
     */
    private boolean validateCreateQuestion(CreateQuestionVo createQuestionVo) {
        if (createQuestionVo == null || createQuestionVo.getUserId() == null) {
            return true;
        }
        if (StringUtils.isAnyBlank(createQuestionVo.getFlowId(), createQuestionVo.getFlowType())) {
            return true;
        }
        if (StringUtils.isAnyBlank(createQuestionVo.getSuccessCallback(), createQuestionVo.getSuccessCallback())) {
            return true;
        }
        return false;
    }
    
    
	@Override
	public void createQuestionFlow(final CreateQuestionVo createQuestionVo) {
		if (validateCreateQuestion(createQuestionVo)) {
            log.error("Question CREATE => get question params miss. ");
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long userId = createQuestionVo.getUserId();
        String flowId = createQuestionVo.getFlowId();
        String flowType = createQuestionVo.getFlowType();
        // 1. 检查当前flowId是否存在有答题信息
        List<UserQuestionAnswers> undoList = getUndoAnswerList(userId, flowId);
        if (CollectionUtils.isNotEmpty(undoList)) {
            log.warn("QUESTION CREATE => 当前存在有有效答题信息，不能重建. userId:{} flowId:{}", userId, flowId);
            throw new BusinessException(AccountErrorCode.QUESTION_FLOW_UNDO_EXIST);
        }

        // 2. 检查当前flowId创建题目到次数是否到限制次数
        int count = getFlowCurrentAnswerTimes(userId, flowId);
        if (getMaxAllowCount() <= count) {
            log.warn("QUESTION CREATE => 答题次数已经用完，不能再次创建. userId:{} flowId:{}", userId, flowId);
            throw new BusinessException(AccountErrorCode.QUESTION_FLOW_ALLOW_TIMES);
        }

        // 3. 缓存
        QuestionCache cache = QuestionCache.builder()
                .userId(userId)
                .flowId(flowId)
                .flowType(flowType)
                .timeout(createQuestionVo.getTimeout())
                .successCallback(createQuestionVo.getSuccessCallback())
                .failCallback(createQuestionVo.getFailCallback())
                .build();
        doRedisSet(flowId,cache);
	}

	// 
    private List<UserQuestionAnswers> createQuestion(Long userId,String flowId,String flowType,long minutes) {
		// 创建题目
		List<UserQuestionAnswers> answers = questionStoreService.buildUserQuestionAnswers(userId, flowId, flowType);
		if (CollectionUtils.isEmpty(answers)) {
			log.error("问题模块->创建答题信息失败. userId:{} flowId:{}", userId, flowId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		//因为答题由用户发起，此处更新缓存超时时间以便用户能最大可能答完题目
		doRedisExpire(flowId, minutes);
		return answers;
    }

    @Override
    public QuestionConfigResponseBody getQuestionConfig(final QuestionConfigRequestBody body) {
		log.info("问题模块->getQuestionConfig,body:{}", body);
		QuestionCache cache = getValidateQuestionCache(body.getFlowId());
		Long userId = cache.getUserId();
		QuestionConfigResponseBody response = new QuestionConfigResponseBody();
		response.setCount(getFlowCurrentAnswerTimes(userId, body.getFlowId()));
		response.setMaxCount(getMaxAllowCount());
		response.setTimeout(cache.getTimeout());
		response.setSuccessPath(cache.getSuccessCallback());
		response.setFailPath(cache.getFailCallback());
		log.info("问题模块->getQuestionConfig,body:{},response:{}", body, response);
		return response;
    }

    @Override
    public QuestionResponseBody getQuestionsV2(final QuestionRequestBody body) {
		log.info("问题模块->获取问题，body:{}", body);
    	QuestionCache questionCache = getValidateQuestionCache(body.getFlowId());
        Long userId = questionCache.getUserId();
        // 先获取当前所有的题库信息
        // 查看当前用户的流程中是否存在有答题记录信息
        AnswerTuple answerTuple = getCurrentAnswerTuple(userId, body.getFlowId());
        QuestionResponseBody questionBody = new QuestionResponseBody();
        questionBody.setTimeRemaining(answerTuple.timeRemaining);
        List<ResetQuestion> resetQuestions = null;
        if (!answerTuple.getAnswers().isEmpty()) {
            // 有存在未答题完成的选项，直接把这些选项返回
			resetQuestions = buildResetQuestByAnswers(userId,
					body.getFlowId(),
					questionCache.getFlowType(),
					answerTuple.getAnswers());
        }else {
            // 如果当前流程中还没有答题信息，需要新创建出用户的答题记录信息
        	List<UserQuestionAnswers> answers = createQuestion(questionCache.getUserId(),
        			questionCache.getFlowId(),
        			questionCache.getFlowType(),
        			questionCache.getTimeout());
            resetQuestions = buildResetQuestByAnswers(userId,
            		body.getFlowId(),
            		questionCache.getFlowType(),
            		answers);
        }
        if (CollectionUtils.isEmpty(resetQuestions)) {
            log.error("问题模块->获取问题，生成答题选项信息失败. userId:{} flowId:{} flowType:{}", userId, body.getFlowId(), body.getFlowType());
            AnswerResponseBody result = new AnswerResponseBody();
			result.setCount(getMaxAllowCount());
			result.setMaxCount(getMaxAllowCount());
			result.setStatus(AnswerCompleteStatus.Fail);
			result.setGotoPath(questionCache.getFailCallback());
			sendMsgWhenFlowComplete(body.getFlowId(),body.getFlowType(), userId, result);
			//异常结束
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        questionBody.setMaxCount(getMaxAllowCount());
        questionBody.setCount(getAnswerCountByMaster(userId, body.getFlowId()));
        questionBody.setQuestions(resetQuestions);
        questionBody.sortQuestions();
        questionBody.setFailPath(questionCache.getFailCallback());
        return questionBody;
    }

    @Override
    @Deprecated
    public ResetQuestionBody getQuestions(Long userId, String flowId, String flowType, Boolean isNewDevice) {
        log.info("Question => userId:{} flowId:{} flowType:{}", userId, flowId, flowType);
        if (userId == null || StringUtils.isAnyBlank(flowId, flowType)) {
            log.warn("Question => get question params miss. ");
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        // 先获取当前所有的题库信息
        // 查看当前用户的流程中还没有回答的问题
        AnswerTuple answerTuple = getCurrentAnswerTuple(userId, flowId);
        ResetQuestionBody questionBody = new ResetQuestionBody();
        questionBody.setTimeRemaining(answerTuple.timeRemaining);
        List<ResetQuestion> resetQuestions;
        if (!answerTuple.getAnswers().isEmpty()) {
            // 有存在未答题完成的选项，直接把这些选项返回
            resetQuestions = buildResetQuestByAnswers(userId,flowId,flowType,answerTuple.getAnswers());
        } else {
            // 如果当前流程中还没有答题信息，需要新创建出用户的答题记录信息
            List<UserQuestionAnswers> answers = questionStoreService.buildUserQuestionAnswers(userId, flowId, flowType);
            resetQuestions = buildResetQuestByAnswers(userId,flowId,flowType, answers);
        }
        if (CollectionUtils.isEmpty(resetQuestions)) {
            log.error("Question => 生成答题选项信息失败. userId:{} flowId:{} flowType:{}", userId, flowId, flowType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        questionBody.setQuestions(resetQuestions);
        questionBody.sortQuestions();
        return questionBody;
    }

    /**
     *  构造用户还没有回答的问题
     */
	private List<ResetQuestion> buildResetQuestByAnswers(Long userId, String flowId, String flowType,
			Collection<UserQuestionAnswers> answers) {
    	if (CollectionUtils.isEmpty(answers)) {
            return new ArrayList<>(0);
        }
    	// 查询用户应该回答的问题
        List<QuestionRepository> questions = questionStoreService.getQuestions(userId,flowType);
        Map<Long, QuestionRepository> repositoryMap = Maps.newHashMapWithExpectedSize(questions.size());
		questions.forEach(q -> {
			repositoryMap.put(q.getId(), q);
		});
        List<ResetQuestion> tmp= new ArrayList<>(questions.size());
        for (UserQuestionAnswers answer : answers) {
            QuestionRepository repository = repositoryMap.get(answer.getQuestionId());
            if (repository == null) {
                continue;
            }
            ResetQuestion question = new ResetQuestion();
            question.setResetId(flowId);
            question.setLangFlag(repository.getDocLangFlag());//i18
            question.setOptions(Utils.parseToListFromJson(answer.getOptions()));//option
            question.setQuestionId(answer.getQuestionId());// 为题唯一id
            tmp.add(question);
        }
        return tmp;
	}
 

    /**
     * 获取用户当前正在答题的但是完成答题的信息且获取剩余的答题时间
     *
     * @param userId
     * @param flowId
     * @return
     */
    private AnswerTuple getCurrentAnswerTuple(Long userId, String flowId) {
        long allTime = commonConfig.getResetAnswerTimeOut() * 60;// 总答题时间,s
        List<UserQuestionAnswers> undoList = getUndoAnswerList(userId, flowId);
        Map<Long, UserQuestionAnswers> undoAnswerMap = Maps.newHashMap();
        long timeElipse = 0;// 已经耗时,s
        long now = DateUtils.getNewUTCTimeMillis();
        for (UserQuestionAnswers item : undoList) {
            // 这样取的目的是止保留createTime最大的
            undoAnswerMap.put(item.getQuestionId(), item);
            timeElipse = (now - item.getCreateTime().getTime()) / 1000;
        }
        Collection<UserQuestionAnswers> tempCollect = new ArrayList<>();
        if (!undoAnswerMap.isEmpty()) {
            // 如果存在有答题记录并且存在有未答题的数据，直接返回当前未答题的数据
            tempCollect = undoAnswerMap.values();
        }
        long timeRemaining = allTime - timeElipse;
        return AnswerTuple.builder()
                .timeRemaining(timeRemaining)
                .answers(tempCollect)
                .build();
    }

    /**
     * 获取流程中未答题当信息，会过滤掉未答题且超时答题的记录。
     *
     * @param userId
     * @param flowId
     * @return
     */
    private List<UserQuestionAnswers> getUndoAnswerList(Long userId, String flowId) {
        Map<Long, List<UserQuestionAnswers>> allAnswerMap = getUserFlowAnswerMapByAnswerId(userId, flowId);
        Long currentAnswerId = getCurrentAnswerId(allAnswerMap); // 当前正在答题到answerId
        List<UserQuestionAnswers> answers = allAnswerMap.get(currentAnswerId);
        if (answers == null) {
            return new ArrayList<>(0);
        }
        // 未完成的答题
        List<UserQuestionAnswers> undoList = answers.stream()
                .filter(item -> Objects.equals(ResetAnswerStatus.UnDone, item.getStatus()))
                .filter(item -> !this.isAnswerTimeout(item))
                .sorted(Comparator.comparing(UserQuestionAnswers::getCreateTime))
                .collect(Collectors.toList());
        return undoList;
    }

    /**
     * 获取用户某一次流程所有的答题记录，并且按照anserId为key分别封装
     *
     * @param userId
     * @param flowId
     * @return
     */
    private Map<Long, List<UserQuestionAnswers>> getUserFlowAnswerMapByAnswerId(Long userId, String flowId) {
        List<UserQuestionAnswers> answers = questionStoreService.getUserFlowAnswerMapByAnswerId(userId, flowId);
        Map<Long, List<UserQuestionAnswers>> result = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(answers)) {
            result = answers.stream().collect(Collectors.groupingBy(UserQuestionAnswers::getAnswerId));
        }
        return result;
    }

    /**
     * 判断是否答题超时，如果超时了，把超时的记录变革未超时状态
     *
     * @param answer
     * @return
     */
    private boolean isAnswerTimeout(@NonNull UserQuestionAnswers answer) {
        Date createTime = answer.getCreateTime();
        int timeOut = commonConfig.getResetAnswerTimeOut(); // 超时时间 单位为 minute
        if (timeOut <= 0) {
            // 当配置值小于等于0时认为没有超时当情况
            return false;
        }
        boolean result = createTime != null && DateUtils.addMinutes(createTime, timeOut).compareTo(DateUtils.getNewUTCDate()) < 0;
        if (result) {
            setAnswerTimeOut(answer);
        }
        return result;
    }

    /**
     * 把一笔未答题状态都记录设置到超时状态
     *
     * @param answer
     * @return
     */
    private int setAnswerTimeOut(UserQuestionAnswers answer) {
        int count = 0;
        if (Objects.equals(ResetAnswerStatus.UnDone, answer.getStatus())) {
            answer.setStatus(ResetAnswerStatus.Timeout);
            answer.setUpdateTime(DateUtils.getNewUTCDate());
            answer.setNewDevice(answer.getNewDevice());
            count = questionStoreService.saveUserQuestionAnswer(answer);
        }
        return count;
    }

    private Long getCurrentAnswerId(Map<Long, List<UserQuestionAnswers>> allAnswerMap) {
        Long currentAnswerId = null;
        for (Long answerId : allAnswerMap.keySet()) {
            if (currentAnswerId == null) {
                currentAnswerId = answerId;
            } else if (currentAnswerId < answerId) {
                currentAnswerId = answerId;
            }
        }
        return currentAnswerId;
    }

    @Override
	public AnswerResponseBody answerQuestionV2(final AnswerRequestBody body) {
		// 1. 先从缓存中获取
		log.info("问题模块->回答问题,body:{}", body);
		QuestionCache questionCache = getValidateQuestionCache(body.getFlowId());
		Long userId = questionCache.getUserId();
		String flowId = questionCache.getFlowId();
		String flowType =questionCache.getFlowType();
		// 2. 推送风控系统打分
		AnswerResponseBody answerQuestionResponse = answerQuestion(userId, 
				flowId, 
				body.getQuestionId(),
				body.getAnswers(),
				questionChecker.isNewDevice(userId, body.getDeviceInfo()));
		
		int count = answerQuestionResponse.getCount();
		int allowCount = getMaxAllowCount(); // 总答题次数
		answerQuestionResponse.setMaxCount(allowCount);
		
		switch (answerQuestionResponse.getStatus()) {
		case Success:
			answerQuestionResponse.setGotoPath(questionCache.getSuccessCallback());
			// 答题完毕且答题成功,发送消息通知业务方表示
			sendMsgWhenFlowComplete(flowId,flowType, userId, answerQuestionResponse);
			// 结束当前流
			doRedisDelete(flowId);
			break;
		case Fail:
		case TimeOut:
			// 答题结束，失败了
			answerQuestionResponse.setGotoPath(questionCache.getFailCallback());
			log.info("Answer => 答题失败,userId:{},flowId:{},已用次数:{},最大次数:{}", userId, flowId, count, allowCount);
			if (count >= allowCount) {
				log.info("Answer => 答题失败且已经用完可用次数,当作成功处理,风控后置处理tag. userId:{},flowId:{}", userId, flowId);
				answerQuestionResponse.setStatus(AnswerCompleteStatus.Success);
				answerQuestionResponse.setGotoPath(questionCache.getSuccessCallback());
				// 计数，最后一次失败打标签
				questionChecker.doWhenFails(userId, flowId, count);
				// 结束当前流
				doRedisDelete(flowId);
			}
			//  发消息
			sendMsgWhenFlowComplete(flowId, flowType, userId, answerQuestionResponse);
			break;
		case OK:
			//  继续回答下一题
			break;
		default:
			log.warn("Answer => 错误状态:{}", answerQuestionResponse);
		}
		return answerQuestionResponse;
	}
    
    @Override
    public AnswerResponseBody answerQuestion(Long userId, String flowId, Long questionId, List<String> answers, Boolean isNewDevice) {
        log.info("Answer => 用户提交答题信息：userId:{} flowId:{} questionId:{} isNewDevice:{} answers:{}",
                userId, flowId, questionId, isNewDevice, JSON.toJSONString(answers));
        Map<Long, List<UserQuestionAnswers>> allAnswerMap = getUserFlowAnswerMapByAnswerId(userId, flowId);
        if (allAnswerMap == null || allAnswerMap.isEmpty()) {
            log.warn("Answer => 根据flowId获取当前未答题记录失败. userId:{} flowId:{}", userId, flowId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        Long currentAnswerId = getCurrentAnswerId(allAnswerMap);
        // 获取当前正在答题的答题信息
        List<UserQuestionAnswers> currentQuestions = allAnswerMap.get(currentAnswerId);
        if (currentAnswerId == null || CollectionUtils.isEmpty(currentQuestions)) {
            log.warn("Answer => 获取当前答题记录信息失败。userId:{} flowId:{} answerId:{}", userId, flowId, currentAnswerId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        int currentCount = allAnswerMap.keySet().size(); // 当前是第几次答题
        // 判断当前题号是否存在并且处于为答题情况
        UserQuestionAnswers currentQuestion = null;
        Optional<UserQuestionAnswers> optional = currentQuestions.stream()
                .filter(item -> Objects.equals(ResetAnswerStatus.UnDone, item.getStatus()))
                .filter(item -> !isAnswerTimeout(item))
                .filter(item -> Objects.equals(item.getQuestionId(), questionId))
                .findFirst();
        if (optional.isPresent()) {
            currentQuestion = optional.get();
        } else {
            // 获取不到答题记录到时候，当作超时来计算
            return new AnswerResponseBody(AnswerCompleteStatus.TimeOut, currentCount, getMaxAllowCount());
        }
        // 设置当前答题信息
        currentQuestion.setNewDevice(isNewDevice);
        currentQuestion.setAnswers(JSON.toJSONString(answers));
        currentQuestion.setStatus(ResetAnswerStatus.Done);
        currentQuestion.setUpdateTime(DateUtils.getNewUTCDate());
        questionStoreService.saveUserQuestionAnswer(currentQuestion);
        // 检查当前是否所有题目都答题完成，如果是，请求风控获取打分信息
        long answerCount = currentQuestions.stream().filter(item -> Objects.equals(ResetAnswerStatus.Done, item.getStatus())).count();
        if (answerCount <= 0) {
            log.warn("Answer => 答题校验信息异常。userId:{} flowId:{} answerId:{}", userId, flowId, currentAnswerId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (answerCount == currentQuestions.size()) {
            log.info("Answer => 用户已经把整套题回答完成，开始计算得分信息, userId:{} flowId:{} answerId:{}", userId, flowId, currentAnswerId);
            AnswerCompleteStatus completeStatus = getAndSaveAnswerScore(userId, currentQuestions);
            return new AnswerResponseBody(completeStatus, currentCount, getMaxAllowCount());
        } else {
            log.info("Answer => 用户还未把整套题回答完成，返回保存成功状态, userId:{} flowId:{} answerId:{}", userId, flowId, currentAnswerId);
            return new AnswerResponseBody(AnswerCompleteStatus.OK, currentCount, getMaxAllowCount());
        }
    }


    /**
     * 统计用户答题得分
     *
     * @param userId
     * @param questionAnswers
     * @return
     */
    private AnswerCompleteStatus getAndSaveAnswerScore(Long userId, List<UserQuestionAnswers> questionAnswers) {
        if (userId == null || CollectionUtils.isEmpty(questionAnswers)) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        final QuestionScoreBody ruleResponse = mgmtService.getAnswerScore(userId, questionAnswers);
        List<QuestionScore> questionScoreResList = ruleResponse.getQuestionScoreList();
		final Map<String, QuestionScore> checkResultMap = new HashMap<>(questionScoreResList.size());
        for (QuestionScore checkResult : questionScoreResList) {
            checkResultMap.put(checkResult.getQuestion(), checkResult);
        }
        // 获取到分数后进行保存
        questionAnswers.forEach(item -> {
            String questionType = item.getQuestionType();
            QuestionScore checkResult = checkResultMap.get(questionType);
            if (checkResult != null) {
                item.setScore((int) (checkResult.getWeightedPoint() * 100));// 分数 * 100
                item.setPoint((int) (checkResult.getPoint() * 100));
                item.setPass(ruleResponse.getResult());
            }
            questionStoreService.saveUserQuestionAnswer(item);
        });
        boolean pass = ruleResponse.getResult() == null ? false : ruleResponse.getResult();
        UserQuestionAnswers answers = questionAnswers.get(0);
        String flowId = answers.getFlowId();
        Long answerId = answers.getAnswerId();
        log.info("Answer => 用户答题最终结果: userId:{} flowId:{} answerId:{} pass:{}", userId, flowId, answerId, pass);
        AnswerCompleteStatus response;
        if (pass) {
            // 清除答题失败次数
            response = AnswerCompleteStatus.Success;
        } else {
            // 答题失败次数
            response = AnswerCompleteStatus.Fail;
        }
        return response;
    }


    @Override
    public int getFlowCurrentAnswerTimes(Long userId, String flowId) {
    	return questionStoreService.getFlowCurrentAnswerTimes(userId, flowId);
    }

    /**
     * 强制从主库读取数据
     * @param userId
     * @param flowId
     * @return
     */
    private int getAnswerCountByMaster(Long userId, String flowId) {
        HintManager hintManager = null;
        try {
            // 出现过存入的时候如果直接查询结果还是查询不到的情况，睡眠以下是防止这种情况
            Thread.sleep(100);
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            Integer count = getFlowCurrentAnswerTimes(userId, flowId);
            return count == null ? 0 : count;
        }catch (InterruptedException e) {
            log.error("InterruptedException.");
            throw new BusinessException(GeneralCode.SYS_ERROR);
        } finally {
            if (null != hintManager) {
                hintManager.close();
            }
        }
    }

    @Override
    public List<UserQuestionAnswers> getUserQuestionAnswers(Long userId, String flowId, String flowType) {
        if (userId == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        QuestionQuery query = new QuestionQuery();
        query.setUserId(userId);
        query.setFlowId(flowId);
        query.setFlowType(flowType);
        return questionStoreService.getUserQuestionAnswers(query);
    }
	
	@Builder
	@Getter
	@Setter
	@AllArgsConstructor
	static class QuestionCache extends ToString {
		private static final long serialVersionUID = -7397915866106801347L;
		private long timeout;
		private Long userId;
		//private Long answerId;
		private String flowId;
		private String flowType;

		private String successCallback;
		private String failCallback;
		QuestionCache(){}
		
		// 转化到秒，且当小于等于0时配置默认值为24小时
		public long getTimeout() {
			if (timeout <= 0) {
				return 24 * 60 * 60;
			} else {
				return timeout * 60;
			}
		}
	}

	@Builder
	@Getter
	static class AnswerTuple extends ToString {
		private static final long serialVersionUID = -7067434829519574285L;
		private final long timeRemaining;
		private final Collection<UserQuestionAnswers> answers;
	}

	@Override
	public QueryLogResponseBody getUserQuestionLog(final QueryLogRequestBody requestBody) {
		if (requestBody == null) {
			log.error("request body is null.");
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		List<UserQuestionAnswers> logs = this.getUserQuestionAnswers(requestBody.getUserId(), requestBody.getFlowId(), null);
		QueryLogResponseBody responseBody = new QueryLogResponseBody();
		if (CollectionUtils.isEmpty(logs)) {
			log.warn("用户问答记录查询->userId:{},无记录.", requestBody.getUserId());
			return responseBody;
		}
		List<QueryLogResponseBody.Body> lst =new LinkedList<>();
		UserSecurity userSec = questionChecker.userSecurityExistValidate(requestBody.getUserId());
		BigDecimal divisor = new BigDecimal(100);
		logs.forEach(log -> {
			QueryLogResponseBody.Body body = new Body();
			BeanUtils.copyProperties(log, body, "score", "point");
			// 分数放大存储,需要还原
			body.setScore(new BigDecimal(log.getScore()).divide(divisor));
			body.setPoint(new BigDecimal(log.getPoint()).divide(divisor));
			boolean inProtected = userSec.getProtectedStatus().isInProtectedMode();
			body.setInProtectedMode(inProtected);
			List<UserRiskFeature> risklog = questionChecker.geRiskPostLog(log.getUserId(), log.getFlowId());
			if (!CollectionUtils.isEmpty(risklog)) {
				//  最近一次的状态
				UserRiskFeature userRiskFeature = risklog.stream().max(Comparator.comparingLong(UserRiskFeature::getId))
						.get();
				body.setRiskPostFeatures(userRiskFeature.getFeatures());
				body.setRiskPostResult(userRiskFeature.getRiskResult());
				body.setRiskPostTime(userRiskFeature.getUpdateTime());
			}
			lst.add(body);
		});
		responseBody.setBody(lst);
		return responseBody;
	}

	@Override
	public QueryLogStaticsResponseBody getUserLogStatics(QueryLogStaticsRequestBody requestBody) {
		log.info("用户问答记录统计查询->请求body:{}", requestBody);
		QuestionQuery query = new QuestionQuery();
        query.setUserId(requestBody.getUserId());
        query.setFlowType(requestBody.getFlowType());
        query.setStartTime(requestBody.getStartTime());
        query.setEndTime(requestBody.getEndTime());
        query.setOffset(requestBody.getOffset());
        query.setLimit(requestBody.getLimit());
        query.setGroupByFlowId(true);
        //按照流程分组的数据
		List<UserQuestionAnswers> logs = questionStoreService.getUserQuestionAnswers(query);
		QueryLogStaticsResponseBody responseBody =new QueryLogStaticsResponseBody();
		if (CollectionUtils.isEmpty(logs)) {
			log.warn("用户问答记录统计查询->body:{},无记录.", requestBody);
			return responseBody;
		}
		List<QueryLogStaticsResponseBody.Body> list =new LinkedList<>();
		for (UserQuestionAnswers log : logs) {
			QueryLogStaticsResponseBody.Body body =new QueryLogStaticsResponseBody.Body();
			//流程详情
			QuestionQuery queryTmp = new QuestionQuery();
			queryTmp.setUserId(log.getUserId());
			queryTmp.setFlowId(log.getFlowId());
			queryTmp.setFlowType(log.getFlowType());
	        List<UserQuestionAnswers> logTmp = questionStoreService.getUserQuestionAnswers(query);
	        
			UserQuestionAnswers first = logTmp.stream().findFirst().get();
			User user = questionChecker.userExistValidate(first.getUserId());
			body.setUserId(user.getUserId());
			body.setEmail(user.getEmail());
			//问答次数
			int times=(int)logTmp.stream().map(l->l.getAnswerId()).distinct().count();
			body.setAnsersTimes(times);
			//流程开始时间
			body.setCreateTime(first.getCreateTime());
			
			UserQuestionAnswers last = logTmp.stream().max(Comparator.comparingLong(UserQuestionAnswers::getId)).get();
			//流程结束时间
			body.setUpdateTime(last.getUpdateTime());
			body.setFlowType(log.getFlowType());
			body.setFlowId(log.getFlowId());
			//此处应该是流程的最终状态，暂时使用答题的最终结果填充
			boolean b = log.getPass() != null && log.getPass();
			body.setTotalStatus(b + "");
			list.add(body);	
		}
		responseBody.setBody(list);
		return responseBody;
	}

	@Override
	public boolean checkUserFlowComplete(Long userId, String flowId) {
		// 未达到最大次数
		int count = getFlowCurrentAnswerTimes(userId, flowId);
		if (count <= 0) {
			log.info("校验flow是否完毕->未创建流程，没有结束.userId:{},flowId:{}", userId, flowId);
			return false;
		}
		// 回答尚未完毕的问题
		List<UserQuestionAnswers> records = this.getUndoAnswerList(userId, flowId);
		if(!CollectionUtils.isEmpty(records)) {
			log.info("校验flow是否完毕->有未回答的问题,没有结束.userId:{},flowId:{}", userId, flowId);
			return false;
		}
		// 已经回答完毕的都失败了，但是未达到最大次数
		if(count< getMaxAllowCount()) {
			List<UserQuestionAnswers> history = this.getUserQuestionAnswers(userId, flowId, null);
			if(CollectionUtils.isEmpty(history)) {
				log.info("校验flow是否完毕->没有答题历史,没有结束.userId:{},flowId:{}", userId, flowId);
				return false;
			}
			for (UserQuestionAnswers his : history) {
				if(his.getPass()) {
					log.info("校验flow是否完毕->答题通过,流程结束.userId:{},flowId:{}", userId, flowId);
					return true;
				}
			}
			log.info("校验flow是否完毕->答题历史都失败了,没有结束.userId:{},flowId:{}", userId, flowId);
			return false;
		}
		log.info("校验flow是否完毕->流程已经结束.userId:{},flowId:{}", userId, flowId);
		return true;
	}

	@Override
	public QuestionInfoResponseBody managerQuestions(final QuestionInfoRequestBody body) {
		log.info("问答模块->问题配置，body:{}", body);
		QuestionConfig config =new QuestionConfig();
		Date updateTime = new Date();
		config.setCreateTime(updateTime);
		config.setUpdateTime(updateTime);
		config.setOperator(body.getOperator());
		config.setRules(body.getRules());
		config.setScene(body.getScene());
		config.setGroup(body.getGroup());
		// 保存或更新操作,保持 场景下 set与规则 一一对应
		List<QuestionConfig> old = questionConfig.selectBy(config.getScene().ordinal(), config.getGroup());
		if (CollectionUtils.isEmpty(old) || !checkRulesExists(config, old)) {
			questionConfig.insertOrUpdate(config);
		} else {
			log.warn("问答模块->问题配置存在，但是规则是空的，不更新。body:{}", body);
		}
		// 题目直接保存
		buildQuestion(body, config);
		QuestionInfoResponseBody questionInfoResponseBody = new QuestionInfoResponseBody();
		questionInfoResponseBody.setMsg("question configs have save successfully");
		return questionInfoResponseBody;
	}

	private boolean checkRulesExists(QuestionConfig config, List<QuestionConfig> old) {
		for (QuestionConfig r : old) {
			if(CollectionUtils.isEqualCollection(r.getRules(), config.getRules())) {
				return true;
			}
		}
		return false;
	}

	private void buildQuestion(final QuestionInfoRequestBody body, QuestionConfig config) {
		QuestionRepository question = new QuestionRepository();
		BeanUtils.copyProperties(body.getQuestion(), question);
		Date updateTime =new Date();
		question.setCreateTime(updateTime);
		question.setUpdateTime(updateTime);
		question.setGroup(config.getGroup());
		// 保存或更新问题
		questionStoreService.saveQuestionRepository(question);
	}

	@Override
	public QueryConfigResponseBody getConfig(final QueryConfigRequestBody body) {
		log.info("问答模块->查询问题配置，查询条件:{}", body);
		QueryConfigResponseBody response = new QueryConfigResponseBody();
		List<QuestionConfig> configs = new ArrayList<>(0);
		if (body.getScene() == null) {
			// 场景是空的 
			configs = questionConfig.selectBy(null, body.getGroup());
		} else {
			configs = questionConfig.selectBy(body.getScene().ordinal(), body.getGroup());
		}
		if(CollectionUtils.isEmpty(configs)) {
			log.warn("问答模块->没查询到问题配置,查询条件:{}", body);
		}
		for (QuestionConfig qConfig : configs) {
			List<QuestionRepository> qrs = questionStoreService.queryBy(qConfig.getGroup(),body.getRiskType());
			List<Question> tmp = Lists.<Question>newArrayListWithCapacity(qrs.size());
			for (QuestionRepository question : qrs) {
				Question q = new Question();
				BeanUtils.copyProperties(question, q);
				tmp.add(q);
			}
			// 返回值
			response.getBody().add(QueryConfigResponseBody.Body.builder()
					.scene(qConfig.getScene())
					.group(qConfig.getGroup())
					.rules(StringUtils.join(qConfig.getRules(),","))
					.questions(tmp)
					.build());
		}
		return response;
	}

	@Override
	public boolean needToAnswerQuestion(final Long userId,final String flowType, final Map<String, String> device) {
		QuestionSceneEnum scene = QuestionSceneEnum.ConvertUserSecurityResetTypeToScene(flowType);
		return mgmtService.needToAnswerQuestions(userId, scene, device);
	}
}
