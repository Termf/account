package com.binance.account.service.reset2fa.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.sound.midi.SysexMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import com.binance.account.common.enums.AnswerCompleteStatus;
import com.binance.account.common.enums.ProtectedStatus;
import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.QuestionRepository;
import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.data.entity.security.UserResetBigDataLog;
import com.binance.account.data.entity.security.UserResetBigDataLogQuery;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.QuestionRepositoryMapper;
import com.binance.account.data.mapper.security.UserResetBigDataLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.question.BO.QuestionMessage;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.question.export.IQuestionHnadler;
import com.binance.account.service.reset2fa.IResetQuestion;
import com.binance.account.service.reset2fa.strategy.IstrategyTrigger;
import com.binance.account.service.reset2fa.strategy.StrategyExecutor;
import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.account.vo.reset.request.ResetAnswerRequestArg;
import com.binance.account.vo.reset.request.ResetProtectedModeArg;
import com.binance.account.vo.reset.request.ResetQuestionArg;
import com.binance.account.vo.reset.request.ResetQuestionConfigArg;
import com.binance.account.vo.reset.request.ResetUserAnswerArg;
import com.binance.account.vo.reset.request.ResetUserReleaseArg;
import com.binance.account.vo.reset.request.UserResetBigDataLogRequestBody;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.account.vo.reset.response.ResetAnswerBody;
import com.binance.account.vo.reset.response.ResetProtectedModeBody;
import com.binance.account.vo.reset.response.ResetQuestionBody;
import com.binance.account.vo.reset.response.ResetQuestionConfigBody;
import com.binance.account.vo.reset.response.ResetUserAnswerBody;
import com.binance.account.vo.reset.response.ResetUserAnswerBody.ResetAnswersInfo;
import com.binance.account.vo.reset.response.ResetUserReleaseBody;
import com.binance.account.vo.reset.response.UserResetBigDataLogResponseBody;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ResetQuestionService implements IResetQuestion,IQuestionHnadler {
	@Resource
	private ApolloCommonConfig commonConfig;
	@Resource
	private QuestionRepositoryMapper questionRepositoryMapper;
	@Resource
	private QuestionModuleChecker resetChecker;
	@Resource
	private IQuestion iQuestion;
	@Resource
	private UserResetBigDataLogMapper mapper;

	@Deprecated
	@Override
	public ResetQuestionBody getResetQuestions(ResetQuestionArg arg) {
		// 上游业务校验
//		Reset2faNextStepResponse lastRequest = resetChecker.questionArgsValidate(arg);
//		// 上游重置记录校验
//		UserSecurityReset reset = resetChecker.userAnsweringValidate(lastRequest.getTransId());
//		// 用户存在性校验
//		User user = resetChecker.userExistValidate(reset.getUserId());
//		// 用户达到最大失败次数校验
//		if (resetChecker.remainingTimes(user.getUserId(), reset.getId()) == 0) {
//			resetChecker.cancelReset(reset);
//			log.error("重置流程->用户达到最大失败次数,取消本次重置流程.userId:{},resetId:{}", reset.getUserId(), reset.getId());
//			throw new BusinessException(AccountErrorCode.RESET_ANSWER_TIMES_OUT);
//		}
//
//		final Long userId = user.getUserId();
//		UserSecurityResetType type = reset.getType();
//		final boolean isNewDevice = resetChecker.isNewDevice(userId, arg.getDeviceInfo());
//		log.info("重置流程->查询问题. userId:{},type:{},isNewDevice:{}", userId, type, isNewDevice);
//		ResetQuestionBody response = iQuestion.getQuestions(userId, reset.getId(), type.name(), isNewDevice);
//		log.info("重置流程->问题选项. userId:{},type:{},response:{}", userId, type, response);
//		return response;
		log.error("the old getResetQuestions is not supported now");
		throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
	}

	@Deprecated
	@Override
	public ResetAnswerBody resetAnswerOneByOne(ResetAnswerRequestArg answerArg) {
		// 流程校验
//		String resetId = answerArg.getResetId();
//		UserSecurityReset reset = resetChecker.userAnsweringValidate(resetId);
//		// 是否新设备
//		Long userId = reset.getUserId();
//		boolean isNewDevice = resetChecker.isNewDevice(userId, answerArg.getDeviceInfo());
//		// 已经回答的问题和答案
//		Long questionId = answerArg.getQuestionId();
//
//		AnswerResponseBody response = iQuestion.answerQuestion(userId, resetId, questionId, answerArg.getAnswers(),
//				isNewDevice);
//
//		log.info("重置流程->问题答案,保存完毕. userId:{},resetId:{},questionId:{},response:{}", userId, resetId, questionId,
//				response);
//		// 处理答题结果
//		trigger(userId, resetId, response);
//
//		// 前端答题结果返回
//		ResetAnswerBody body = new ResetAnswerBody();
//		body.setAnswerComplete(response.getStatus());
//		if (successWhenLastTime(response)) {
//			log.info("重置流程->最后一次答题都是成功,醉了,userId:{},resetId:{}", userId, resetId);
//			body.setAnswerComplete(AnswerCompleteStatus.Success);
//		}
//		return body;
		log.error("the old resetAnswerOneByOne is not supported now");
		throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
	}

	private void trigger(Long userId, String resetId, AnswerResponseBody response) {
		List<IstrategyTrigger> triggers = new ArrayList<>(3);
		log.info("重置流程->执行后置规则开始. userId:{},resetId:{},", userId, resetId);
		triggers.add(whenFirstFail(userId, response));// 第一次失败发邮件
		triggers.add(whenFail(userId, resetId, response));// 失败计数，最后一次打标签
		triggers.add(whenSuccess(userId, resetId, response));// 成功的发邮件,最后后一次失败发成功的邮件
		StrategyExecutor.builder().triggers(triggers).build().execute();
		log.info("重置流程->执行后置规则完毕. userId:{},resetId:{},", userId, resetId);
	}

	private IstrategyTrigger whenFail(final Long userId,final String resetId, final AnswerResponseBody response) {
		return new IstrategyTrigger() {

			@Override
			public void postProcess() {
				// 计数，最后一次失败打标签
				log.info("重置流程->最后一次失败打标签,userId:{},resetId:{},", userId, resetId);
				resetChecker.doWhenFails(userId, resetId, response.getCount());
			}

			@Override
			public boolean isTriggerRules() {
				return whetherFailOrTimeOut(response);
			}
		};
	}
	
	private IstrategyTrigger whenSuccess(Long userId, String resetId, AnswerResponseBody response) {
		return new IstrategyTrigger() {

			@Override
			public void postProcess() {
				log.info("重置流程->成功发邮件,userId:{},resetId:{},", userId, resetId);
				resetChecker.sendEmailWhenSuccess(userId, resetId);
			}

			@Override
			public boolean isTriggerRules() {
				return successWhenLastTime(response);
			}
		};
	}

	private boolean successWhenLastTime(AnswerResponseBody response) {
		if (response.getStatus() == AnswerCompleteStatus.Success) {
			return true;
		}
		//最后一次失败和超时，当作成功处理
		return whetherFailOrTimeOut(response) && response.getCount() >= commonConfig.getProtectedTimes();
	}

	private IstrategyTrigger whenFirstFail(final Long userId, final AnswerResponseBody response) {
		return new IstrategyTrigger() {

			@Override
			public void postProcess() {
				log.info("重置流程->首次失败发邮件,userId:{}", userId);
				resetChecker.sendEmailWhenFail(userId);
			}

			@Override
			public boolean isTriggerRules() {
				return whetherFirstFail(response) && whetherFailOrTimeOut(response);
			}
		};
	}

	private boolean whetherFirstFail(final AnswerResponseBody response) {
		return response.getCount() == 1 && response.getCount() <= commonConfig.getProtectedTimes();
	}

	private boolean whetherFailOrTimeOut(final AnswerResponseBody response) {
		return response.getStatus() == AnswerCompleteStatus.Fail
				|| response.getStatus() == AnswerCompleteStatus.TimeOut;
	}

	@Override
	public ResetQuestionConfigBody manageQuestionConfig(ResetQuestionConfigArg body) {
		log.info("manageQuestionConfig,问题配置接口,request:{}", body);
		boolean result = false;
		switch (body.getOpType().toLowerCase()) {
		case "enable":
			if (body.getId() != null) {
				result = questionRepositoryMapper.enable(body.getId()) > 0;
			}
			return ResetQuestionConfigBody.builder().success(result).build();
		case "disable":
			if (body.getId() != null) {
				result = questionRepositoryMapper.deleteByPrimaryKey(body.getId()) > 0;
			}
			return ResetQuestionConfigBody.builder().success(result).build();
		default:
			throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
		}
	}

	@Override
	public ResetUserAnswerBody getUserResetAnswers(@Validated @RequestBody ResetUserAnswerArg body) {
		Long userId = body.getUserId();
		String resetId = body.getResetId();
		ResetUserAnswerBody response = new ResetUserAnswerBody();
		// 用户当前失败次数
		response.setProtectedCounts(resetChecker.userCurrentAnswerTimes(userId, resetId));
		// 配置的最大失败次数
		response.setConfigCounts(commonConfig.getProtectedTimes());

		List<UserQuestionAnswers> answers = iQuestion.getUserQuestionAnswers(userId, resetId, null);
		if (CollectionUtils.isEmpty(answers)) {
			log.error("查询重置答案->问题和答案不存在.arg:{}", body);
			response.setAnswers(new ArrayList<>());
			return response;
		}
		List<QuestionRepository> questions = questionRepositoryMapper.selectALL();
		Map<Long, QuestionRepository> questionMap = questions.stream()
				.collect(Collectors.toMap(QuestionRepository::getId, item -> item));
		List<ResetUserAnswerBody.ResetAnswersInfo> infos = new LinkedList<>();
		BigDecimal divisor = new BigDecimal(100);
		answers.forEach(answer -> {
			ResetUserAnswerBody.ResetAnswersInfo info = new ResetAnswersInfo();
			BeanUtils.copyProperties(answer, info);
			// 分数放大存储,需要还原
			info.setScore(new BigDecimal(answer.getScore()).divide(divisor));
			info.setPoint(new BigDecimal(answer.getPoint()).divide(divisor));
			QuestionRepository question = questionMap.get(answer.getQuestionId()) == null ? null
					: questionMap.get(answer.getQuestionId());
			if (question != null) {
				info.setQuestionType(question.getRiskType());
				info.setRemark(question.getRemark());
			}
			infos.add(info);
		});
		response.setAnswers(infos);
		return response;
	}

	@Override
	public ResetUserReleaseBody releaseFromProtectedMode(@Validated @RequestBody ResetUserReleaseArg body) {
		Long userId = body.getUserId();
		boolean result = false;
		switch (body.getStatus()) {
		case NORMAL_MODE:
			result = resetChecker.clearProtectedMode(userId);
			break;
		case PROTECTED_MODE:
			result = resetChecker.make2ProtectedMode(userId);
			break;
		case FORBID_MODE:
			result = resetChecker.make2ForbitMode(userId);
			break;
		default:
			log.error("解除保护模式->status不存在.arg:{}", body);
			throw new BusinessException(GeneralCode.SYS_VALID);
		}
		ResetUserReleaseBody res = new ResetUserReleaseBody();
		res.setUserId(userId);
		res.setSuccess(result);
		return res;
	}

	@Override
	public ResetProtectedModeBody getUserProtectedStatus(@Validated @RequestBody ResetProtectedModeArg arg) {
		ProtectedStatus s = resetChecker.userSecurityExistValidate(arg.getUserId()).getProtectedStatus();
		String status = s == null ? ProtectedStatus.NORMAL_MODE.name() : s.name();
		ResetProtectedModeBody body = new ResetProtectedModeBody();
		body.setStatus(status);
		return body;
	}

	@Override
	public void skipAnswerQuestionToNextStep(@Validated @RequestBody ResetUserAnswerArg body) {
		Long userId = body.getUserId();
		String resetId = body.getResetId();
		UserSecurityReset reset = resetChecker.userResetExistValidate(resetId);
		if (!Objects.equals(UserSecurityResetStatus.unverified, reset.getStatus())) {
			log.info("用户当前状态不是答题状态，不能跳过答题环节. userId:{} resetId:{}", userId, resetId, reset.getStatus());
			throw new BusinessException(GeneralCode.SYS_ERROR, "当前状态不能跳过答题环节");
		}
		log.info("直接跳过答题环节进入下一步流程. userId:{} resetId:{}", userId, resetId);
		resetChecker.sendEmailWhenSuccess(userId, resetId);
	}
	
	@Override
	public void invoke(QuestionMessage msg){
		try {
			log.info("reset流程,v2答题模块消息=>msg:{}", msg);
			QuestionSceneEnum scence = QuestionSceneEnum.ConvertUserSecurityResetTypeToScene(msg.getFlowType());
			if (scence != QuestionSceneEnum.RESET_2FA) {
				log.info("reset流程,v2答题模块,不是reset流程场景,scence:{}", scence);
				return;
			}
			trigger(msg.getUserId(), msg.getFlowId(), msg.getResult());
			log.info("reset流程,v2答题模块消息=>trigger后续事件结束,userId:{},resetId:{}", msg.getUserId(), msg.getFlowId());
		} catch (Exception e) {
			log.error("reset流程,v2答题模块消息异常.msg:" + msg, e);
		}
	}

	@Override
	public UserResetBigDataLogResponseBody getUserResetBigDataLog(UserResetBigDataLogRequestBody body) {
		log.info("查询大数据reset处理流水,body:{}",body);
		UserResetBigDataLogQuery query =new UserResetBigDataLogQuery();
		query.setUserId(body.getUserId());
		query.setTransId(body.getTransId());
		query.setStartTime(body.getStartTime());
		query.setEndTime(body.getEndTime());
		List<UserResetBigDataLog> lst = mapper.select(query);
		UserResetBigDataLogResponseBody response =new UserResetBigDataLogResponseBody();
		if(!CollectionUtils.isEmpty(lst)) {
			BigDecimal _100 = new BigDecimal(100);
			List<UserResetBigDataLogResponseBody.Body> result =new ArrayList<UserResetBigDataLogResponseBody.Body>(lst.size());
			for (UserResetBigDataLog resetLog : lst) {
				// RESET
				try {
					UserSecurityReset reset = resetChecker.userResetExistValidate(resetLog.getTransId());
					if (StringUtils.isNotBlank(body.getResetType())) {
						if (!reset.getType().name().equalsIgnoreCase(body.getResetType())) {
							log.info("查询大数据reset处理流水,resetId:{},不是指定的类型:{}",reset.getId(),body.getResetType());
							continue;
						}
					}
					
					Long userId = resetLog.getUserId();
					// USER
					User user = resetChecker.userExistValidate(userId);
					// PROTECTED MODE
					UserSecurity se = resetChecker.userSecurityExistValidate(userId);
					UserResetBigDataLogResponseBody.Body b = new UserResetBigDataLogResponseBody.Body();
					b.setId(resetLog.getId());
					b.setUserId(userId);
					b.setTransId(resetLog.getTransId());
					BigDecimal score = new BigDecimal(resetLog.getScore());
					b.setScore(score.divide(_100).setScale(4, RoundingMode.HALF_UP).doubleValue());
					b.setBatchTime(resetLog.getBatchTime());
					b.setCreateTime(resetLog.getCreateTime());
					b.setEmail(user.getEmail());
					b.setProtectedMode(se.getProtectedStatus().name());
					b.setResetType(reset.getType().name());
					result.add(b);
				} catch (Exception e) {
					log.warn("查询大数据reset处理流水,transId:{},userId:{},数据异常未通过校验", resetLog.getTransId(),
							resetLog.getUserId());
				}
			}
			response.setBody(result);
		}
		return response;
	}
}
