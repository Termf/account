package com.binance.account.controller.reset2fa;

import com.binance.account.api.ResetQuestionApi;
import com.binance.account.service.reset2fa.IResetQuestion;
import com.binance.account.vo.reset.request.ResetAnswerRequestArg;
import com.binance.account.vo.reset.request.ResetProtectedModeArg;
import com.binance.account.vo.reset.request.ResetQuestionArg;
import com.binance.account.vo.reset.request.ResetQuestionConfigArg;
import com.binance.account.vo.reset.request.ResetUserAnswerArg;
import com.binance.account.vo.reset.request.ResetUserReleaseArg;
import com.binance.account.vo.reset.request.UserResetBigDataLogRequestBody;
import com.binance.account.vo.reset.response.ResetAnswerBody;
import com.binance.account.vo.reset.response.ResetProtectedModeBody;
import com.binance.account.vo.reset.response.ResetQuestionBody;
import com.binance.account.vo.reset.response.ResetQuestionConfigBody;
import com.binance.account.vo.reset.response.ResetUserAnswerBody;
import com.binance.account.vo.reset.response.ResetUserReleaseBody;
import com.binance.account.vo.reset.response.UserResetBigDataLogResponseBody;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.platform.monitor.logging.aop.Monitor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Monitor
@RestController
public class ResetQuestionController implements ResetQuestionApi {

	@Resource
	private IResetQuestion iResetQuestion;

	@Override
	public APIResponse<ResetQuestionBody> getResetQuestions(@Validated @RequestBody APIRequest<ResetQuestionArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.getResetQuestions(request.getBody()));
	}

	@Override
	public APIResponse<ResetAnswerBody> resetAnswerOneByOne(@Validated @RequestBody APIRequest<ResetAnswerRequestArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.resetAnswerOneByOne(request.getBody()));
	}

	@Override
	public APIResponse<ResetQuestionConfigBody> manageQuestionConfig(@Validated @RequestBody APIRequest<ResetQuestionConfigArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.manageQuestionConfig(request.getBody()));
	}

	@Override
	public APIResponse<ResetUserAnswerBody> getUserResetAnswers(@Validated @RequestBody APIRequest<ResetUserAnswerArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.getUserResetAnswers(request.getBody()));
	}

	@Override
	public APIResponse<ResetUserReleaseBody> releaseFromProtectedMode(@Validated @RequestBody APIRequest<ResetUserReleaseArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.releaseFromProtectedMode(request.getBody()));
	}

	@Override
	public APIResponse<ResetProtectedModeBody> getUserProtectedStatus(@Validated @RequestBody APIRequest<ResetProtectedModeArg> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.getUserProtectedStatus(request.getBody()));
	}

	@Override
	public APIResponse<Void> skipAnswerQuestionToNextStep(@Validated @RequestBody APIRequest<ResetUserAnswerArg> request) {
		iResetQuestion.skipAnswerQuestionToNextStep(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<UserResetBigDataLogResponseBody> getUserResetBigDataLog(@Validated @RequestBody APIRequest<UserResetBigDataLogRequestBody> request) {
		return APIResponse.getOKJsonResult(iResetQuestion.getUserResetBigDataLog(request.getBody()));
	}
}
