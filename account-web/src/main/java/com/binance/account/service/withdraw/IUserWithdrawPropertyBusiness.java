package com.binance.account.service.withdraw;

import com.binance.account.vo.withdraw.request.UserWithdrawLockLogRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawLockAmountResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockLogResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockResponse;
import com.binance.master.models.APIResponse;

public interface IUserWithdrawPropertyBusiness {


    /**
     * 锁定提现额度
     * @param request
     * @return
     * @throws Exception 
     */
	UserWithdrawLockResponse lock(UserWithdrawLockRequest request) throws Exception;

    /**
     * 解锁提现额度
     * @param request
     * @return
     * @throws Exception 
     */
	UserWithdrawLockResponse unlock(UserWithdrawLockRequest request) throws Exception;
	
	/**
	 * 获取锁定数量
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	UserWithdrawLockAmountResponse getLockAmount(Long userId) throws Exception;

	UserWithdrawLockLogResponse queryLockLog(UserWithdrawLockLogRequest request);
}
