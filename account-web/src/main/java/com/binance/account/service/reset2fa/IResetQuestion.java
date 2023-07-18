package com.binance.account.service.reset2fa;

import com.binance.account.service.question.export.IQuestion;
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

/**
 * 用户重置流程，回答问题接口，用于用户流程状态的的维护
 * 
 * reset问答的逻辑逐步迁移到问答模块 {@link IQuestion}
 * 
 * 
 * @author zwh-binance
 *
 */
public interface IResetQuestion {
	/**
	 * 查询问题以及可选项
	 * 
	 * @param resetQuestionArg
	 * @return
	 */
	@Deprecated
	ResetQuestionBody getResetQuestions(ResetQuestionArg resetQuestionArg);

	/**
	 * 2FA重置逐个回答问题
	 * 
	 * @param body
	 * @return
	 */
	@Deprecated
	ResetAnswerBody resetAnswerOneByOne(ResetAnswerRequestArg body);

	/**
	 * 2fa重置问题管理配置
	 * 
	 * @param body
	 * @return
	 */
	ResetQuestionConfigBody manageQuestionConfig(ResetQuestionConfigArg body);

	/**
	 * pnkadmin查询用户重置答案数据
	 * 
	 * @param body
	 * @return
	 */
	ResetUserAnswerBody getUserResetAnswers(ResetUserAnswerArg body);

	/**
	 * pnkadmin解除用户保护模式
	 * 
	 * @param body
	 * @return
	 */
	ResetUserReleaseBody releaseFromProtectedMode(ResetUserReleaseArg body);

	/**
	 * 
	 * 
	 * @param resetProtectedModeArg
	 * @return
	 */
	ResetProtectedModeBody getUserProtectedStatus(final ResetProtectedModeArg resetProtectedModeArg);

	/**
	 * 跳过答题记录
	 * @param body
	 * @return
	 */
	void skipAnswerQuestionToNextStep(ResetUserAnswerArg body);

	/**
	 * 查询大数据处理reset流水
	 * 
	 * @param body
	 * @return
	 */
	UserResetBigDataLogResponseBody getUserResetBigDataLog(UserResetBigDataLogRequestBody body);
}
