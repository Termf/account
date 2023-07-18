package com.binance.account.controller.question;

import com.binance.account.api.QuestionModuleApi;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.vo.question.AnswerRequestBody;
import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.account.vo.question.QueryConfigRequestBody;
import com.binance.account.vo.question.QueryConfigResponseBody;
import com.binance.account.vo.question.QueryLogRequestBody;
import com.binance.account.vo.question.QueryLogResponseBody;
import com.binance.account.vo.question.QueryLogStaticsRequestBody;
import com.binance.account.vo.question.QueryLogStaticsResponseBody;
import com.binance.account.vo.question.QuestionConfigRequestBody;
import com.binance.account.vo.question.QuestionConfigResponseBody;
import com.binance.account.vo.question.QuestionInfoRequestBody;
import com.binance.account.vo.question.QuestionInfoResponseBody;
import com.binance.account.vo.question.QuestionRequestBody;
import com.binance.account.vo.question.QuestionResponseBody;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Monitor
@RestController
public class QuestionController implements QuestionModuleApi {

	@Resource
	private IQuestion questionService;

	@Override
	public APIResponse<QuestionConfigResponseBody> getQuestionConfig(@Validated @RequestBody APIRequest<QuestionConfigRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.getQuestionConfig(request.getBody()));
	}

	@Override
	public APIResponse<QuestionResponseBody> getQuestions(@Validated @RequestBody APIRequest<QuestionRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.getQuestionsV2(request.getBody()));
	}

	@Override
	public APIResponse<AnswerResponseBody> answerQuestion(@Validated @RequestBody APIRequest<AnswerRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.answerQuestionV2(request.getBody()));
	}

	@Override
	public APIResponse<QueryLogResponseBody> getUserQuestionLog(@Validated @RequestBody APIRequest<QueryLogRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.getUserQuestionLog(request.getBody()));
	}

	@Override
	public APIResponse<QueryLogStaticsResponseBody> getUserLogStatics(@Validated @RequestBody APIRequest<QueryLogStaticsRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.getUserLogStatics(request.getBody()));
	}

	@Override
	public APIResponse<QuestionInfoResponseBody> managerQuestions(@Validated @RequestBody APIRequest<QuestionInfoRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.managerQuestions(request.getBody()));
	}

	@Override
	public APIResponse<QueryConfigResponseBody> getConfig(@Validated @RequestBody APIRequest<QueryConfigRequestBody> request) {
		return APIResponse.getOKJsonResult(questionService.getConfig(request.getBody()));
	}

}
