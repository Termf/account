package com.binance.account.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 用户问答校验模块。
 * <p>1. 在不同场景(如reset2fa/设备绑定)分别预设几套题目，由决策系统根据用户历史行为确定回答那套题目或不用回答问题。</p>
 * <p>2. 问答模块提供问题选项，混淆后供web展示，用户回答后保存答案</p>
 * <p>3. 用户回答全部问题后，将问题和答案推送决策系统，按问题权重打分后判断是否通过</p>
 * <p>4. 按照答题结果，继续后续业务</p>
 *
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户问答模块")
@RequestMapping("/user-question-module")
public interface QuestionModuleApi {
	@ApiOperation("查询流程的配置信息，当前次数/总次数/流程超时时间")
	@PostMapping("/v2/getQuestionConfig")
	APIResponse<QuestionConfigResponseBody> getQuestionConfig(@Validated @RequestBody APIRequest<QuestionConfigRequestBody> request);

	 /**
     * 根据用户业务流程获取需要进行答题的题目选项等信息，暴露给前段的API
     *
     * @param flowId
     * @return
     */
	@ApiOperation("获取流程的问题及其选项")
	@PostMapping("/v2/getQuestions")
    APIResponse<QuestionResponseBody> getQuestions(@Validated @RequestBody APIRequest<QuestionRequestBody> request);

    /**
     * 答题，暴露给前段的API
     *
     * @param flowId
     * @param questionId
     * @param answers
     * @param isNewDevice
     * @return
     */
	@ApiOperation("回答流程下的问题，逐个问题上传答案")
	@PostMapping("/v2/answerQuestion")
    APIResponse<AnswerResponseBody> answerQuestion(@Validated @RequestBody APIRequest<AnswerRequestBody> request);
	
	/**
	 * 问答模块，查询指定用户的特定流程的答题详情
	 * 
	 * @param request
	 * @return
	 */
	@ApiOperation("获取用户问答记录，pnkadmin专用")
	@PostMapping("/v2/getUserQuestionLog")
    APIResponse<QueryLogResponseBody> getUserQuestionLog(@Validated @RequestBody APIRequest<QueryLogRequestBody> request);

	/**
	 * 查询指定用户的问答统计信息
	 * 
	 * @param request
	 * @return
	 */
	@ApiOperation("获取用户问答记录统计信息，pnkadmin专用")
	@PostMapping("/v2/getUserLogStatics")
    APIResponse<QueryLogStaticsResponseBody> getUserLogStatics(@Validated @RequestBody APIRequest<QueryLogStaticsRequestBody> request);
	
	
	@ApiOperation("问题管理接口，admin专用")
	@PostMapping("/v2/managerQuestions")
    APIResponse<QuestionInfoResponseBody> managerQuestions(@Validated @RequestBody APIRequest<QuestionInfoRequestBody> request);
	
	@ApiOperation("查询问题配置，admin专用")
	@PostMapping("/v2/getConfig")
    APIResponse<QueryConfigResponseBody> getConfig(@Validated @RequestBody APIRequest<QueryConfigRequestBody> request);
}
