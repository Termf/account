package com.binance.account.service.reset2fa;


import java.util.List;

import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.vo.reset.request.Reset2faNextStepRequest;
import com.binance.account.vo.reset.request.Reset2faStartValidatedRequest;
import com.binance.account.vo.reset.request.ResetResendEmailRequest;
import com.binance.account.vo.reset.request.ResetUploadInitRequest;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.account.vo.reset.response.Reset2faStartValidatedResponse;

public interface IReset2Fa {


    /**
     * 初步校验是否能做Reset2fa
     *
     * @param body
     * @return
     */
    Reset2faStartValidatedResponse reset2faStartValidated(Reset2faStartValidatedRequest body);

    /**
     * 获取reset2fa next step
     *
     * @param request
     * @return
     */
    Reset2faNextStepResponse reset2faNextStepFlow(Reset2faNextStepRequest request);

    /**
     * 从邮件点击打开后的获取reset2fa next step
     *
     * @param body
     * @return
     */
    Reset2faNextStepResponse reset2faUploadEmailOpen(ResetUploadInitRequest body);

	/**
	 * <p>问题回答完毕后用户点击上传jumio邮件超时的补偿措施，此时next接口返回异常，web上‘重发邮件’按钮触发此接口</p>
	 * 
	 * @param body
	 * @return
	 */
	Reset2faNextStepResponse sendEmailAgain(ResetResendEmailRequest body);
	
	/**
	 * 查询认证中间态的reset记录，用于jumio/ocr 结果通知时同步更新
	 * @param userId
	 * @return
	 */
	List<UserSecurityReset> findJumioPendingResets(Long userId);
}
