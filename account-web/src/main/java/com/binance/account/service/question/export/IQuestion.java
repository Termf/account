package com.binance.account.service.question.export;

import java.util.List;
import java.util.Map;

import com.binance.account.data.entity.security.UserQuestionAnswers;
import com.binance.account.vo.question.AnswerRequestBody;
import com.binance.account.vo.question.AnswerResponseBody;
import com.binance.account.vo.question.CreateQuestionVo;
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
import com.binance.account.vo.reset.response.ResetQuestionBody;

import lombok.NonNull;

/**
 * 与流程无关的问题模块，维护答题相关记录，如问题选项，答案，答题时间，答题状态等
 * <p>0 流程业务需要内部调用needToAnswerQuestion以判断是否需要回答问题</p>
 * <p>1 如需要回答问题，业务流程调用createQuestionFlow以创建业务流缓存，flowId为key，后续交由前后端传递</p>
 * <p>2 web按照业务流程返回的flowId重定向到问答页面，getQuestionsV2拉问题</p>
 * <p>3 获取问题后，用户在web上answerQuestionV2逐个回答问题，直至全部回答完毕。
 *      问答模块向{@link IQuestionHnadler}实现bean发送问答打分结果，返回业务方创建流程时的落地页</p>
 * <p>4 业务流程实现{@link IQuestionHnadler}处理后续流程 </p>
 */
public interface IQuestion {

	String EXCHANGE_KEY="question_module_exchange_default";

    /**
     * 创建答题记录，系统内部调用
     * @param createQuestionVo
     * @param
     */
    void createQuestionFlow(CreateQuestionVo createQuestionVo);

    /**
     * 判断用户指定流程是否已经回答完毕问题
     * <p>1含有未回答的问题，return false</p>
     * <p>2没有任何答题记录，return false</p>
     * <p>3其他，return true</p>
     * @param userId
     * @param flowId
     * @return
     */
    boolean checkUserFlowComplete(final Long userId,final String flowId);
    
    /**
     * 获取答题次数信息，暴露给前段的API
     * @param flowId
     * @param flowType
     * @return
     */
    QuestionConfigResponseBody getQuestionConfig(@NonNull QuestionConfigRequestBody body);

    /**
     * 根据用户业务流程获取需要进行答题的题目选项等信息，暴露给前段的API
     *
     * @param flowId
     * @return
     */
    QuestionResponseBody getQuestionsV2(@NonNull QuestionRequestBody body);

    /**
     * 答题，暴露给前段的API
     *
     * @param flowId
     * @param questionId
     * @param answers
     * @param isNewDevice
     * @return
     */
    AnswerResponseBody answerQuestionV2(@NonNull AnswerRequestBody body);


    /**
     * 查询用户的问答记录
     * 
     * @param body
     * @return
     */
    QueryLogResponseBody getUserQuestionLog(@NonNull QueryLogRequestBody body);


    /**
     * 根据用户业务流程获取需要进行答题的题目选项等信息
     * @deprecated 已经直接
     * @param userId
     * @param flowId
     * @return
     */
    @Deprecated
    ResetQuestionBody getQuestions(@NonNull Long userId, @NonNull String flowId, @NonNull String flowType, Boolean isNewDevice);


    /**
     * 答题
     *
     * @param userId
     * @param flowId
     * @param questionId
     * @param answers
     * @param isNewDevice
     * @return
     */
    @Deprecated
    AnswerResponseBody answerQuestion(@NonNull Long userId, @NonNull String flowId, @NonNull Long questionId, List<String> answers, Boolean isNewDevice);


    /**
     * 查询流程当前到答题次数
     *
     * @param userId
     * @param flowId
     * @return
     */
    int getFlowCurrentAnswerTimes(@NonNull Long userId, @NonNull String flowId);

    /**
     * 查询用户流程中的所有答题信息
     *
     * @param userId   不能为空
     * @param flowId   允许为空
     * @param flowType 允许空
     * @return
     */
    List<UserQuestionAnswers> getUserQuestionAnswers(@NonNull Long userId, String flowId, String flowType);

	/**
	 * 消息mq的exchange
	 * 
	 * @return
	 */
	default String getExchangeName() {
		return EXCHANGE_KEY;
	}

	/**
	 * 查询用户的问答统计信息
	 * 
	 * @param body
	 * @return
	 */
	QueryLogStaticsResponseBody getUserLogStatics(@NonNull QueryLogStaticsRequestBody body);

	/**
	 * admin上，提供的问题的添加/更新/启用/禁用功能
	 * 
	 * @param body
	 * @return
	 */
	QuestionInfoResponseBody managerQuestions(QuestionInfoRequestBody body);

	/**
	 * admin上，查询问题配置
	 * 
	 * @param body
	 * @return
	 */
	QueryConfigResponseBody getConfig(QueryConfigRequestBody body);
	
	
	
	/**
	 * 有大数据和风控决定的是否需要回答问题
	 * 
	 * @param userId 用户id
	 * @param device 用户当前设备信息
	 * @return
	 */
	boolean needToAnswerQuestion(final Long userId,final String flowType, final Map<String, String> device);
}
