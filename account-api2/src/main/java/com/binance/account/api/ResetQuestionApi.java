package com.binance.account.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 这个接口会逐步过渡到 {@link QuestionModuleApi}
 *
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户重置2FA")
@RequestMapping("/userReset2fa")
public interface ResetQuestionApi {

	/**
	 * 2FA重置获取问题及其选项
	 * 
	 * @param request
	 * @return
	 */
	@Deprecated
	@ApiOperation("2FA重置获取问题及其选项")
	@PostMapping("/getQuestions")
	APIResponse<ResetQuestionBody> getResetQuestions(@Validated @RequestBody APIRequest<ResetQuestionArg> request);

	/**
	 * 2FA重置逐个回答问题
	 * 
	 * @param request
	 * @return
	 */
	@Deprecated
	@ApiOperation("2FA重置逐个回答问题")
	@PostMapping("/resetAnswerOneByOne")
	APIResponse<ResetAnswerBody> resetAnswerOneByOne(@Validated @RequestBody APIRequest<ResetAnswerRequestArg> request);

	/**
	 * binance-admin页面管理接口,管理问题配置
	 * 
	 * @param request
	 * @return
	 */
	@ApiOperation("管理重置问题配置,pnkadmin用")
	@PostMapping("/manageQuestionConfig")
	APIResponse<ResetQuestionConfigBody> manageQuestionConfig(@Validated @RequestBody APIRequest<ResetQuestionConfigArg> request);
	
	@ApiOperation("查询用户保护状态,pnkadmin用")
	@PostMapping("/getUserProtectedStatus")
	APIResponse<ResetProtectedModeBody> getUserProtectedStatus(@Validated @RequestBody APIRequest<ResetProtectedModeArg> request);
	
	
	@ApiOperation("查询用户重置问题和答案,pnkadmin用")
	@PostMapping("/getUserResetAnswers")
	APIResponse<ResetUserAnswerBody> getUserResetAnswers(@Validated @RequestBody APIRequest<ResetUserAnswerArg> request);
	
	@ApiOperation("解除用户的保护模式,pnkadmin用")
	@PostMapping("/releaseFromProtectedMode")
	APIResponse<ResetUserReleaseBody> releaseFromProtectedMode(@Validated @RequestBody APIRequest<ResetUserReleaseArg> request);

	@ApiOperation("跳过答题进入下一步,pnkadmin用")
	@PostMapping("/skipAnswerQuestionToNextStep")
	APIResponse<Void> skipAnswerQuestionToNextStep(@Validated @RequestBody APIRequest<ResetUserAnswerArg> request);

	@ApiOperation("查询大数据reset处理流水,pnkadmin用")
	@PostMapping("/getUserResetBigDataLog")
	APIResponse<UserResetBigDataLogResponseBody> getUserResetBigDataLog(@Validated @RequestBody APIRequest<UserResetBigDataLogRequestBody> request);
}
