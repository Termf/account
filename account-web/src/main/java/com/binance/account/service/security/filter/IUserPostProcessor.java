package com.binance.account.service.security.filter;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;

/**
 * 用户后置校验器，用户状态变更提交前再一次更改用户态变
 *
 */
public interface IUserPostProcessor {

	/**
	 * 用户状态变更提交前再一次更改用户态变
	 * 
	 * @param userId    用户id
	 * @param resetType 重置类型
	 * @param curStatus 用户当前reset状态
	 * @return
	 */
	PostResult postProcess(final Long userId, final UserSecurityResetType resetType, final UserSecurityResetStatus curStatus);
}
