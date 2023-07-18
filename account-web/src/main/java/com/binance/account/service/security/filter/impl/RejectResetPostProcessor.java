package com.binance.account.service.security.filter.impl;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.security.IUserSecurityLog;
import com.binance.account.service.security.filter.IUserPostProcessor;
import com.binance.account.service.security.filter.PostResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
public class RejectResetPostProcessor implements IUserPostProcessor {

	@Resource
	private IUserSecurityLog iUserSecurityLog;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;

	@Override
	public PostResult postProcess(final Long userId, final UserSecurityResetType resetType, final UserSecurityResetStatus curStatus) {
		PostResult r = new PostResult();
		r.setStatus(curStatus);
		if (!apolloCommonConfig.isResetEnableAutoRefused()) {
			// 如果开关未开，直接返回
			return r;
		}
		if (Objects.equals(UserSecurityResetType.enable, resetType) && Objects.equals(UserSecurityResetStatus.passed, curStatus) && hadDisabled(userId)) {
			// https://jira.toolsfdg.net/browse/FIAT-58 用户由人工解禁，自动拒绝他的重置申请
			log.info("userId:{},人工解禁,自动拒绝", userId);
			r.setStatus(UserSecurityResetStatus.refused);
			r.setMsgKey(AccountErrorCode.class.getName() + "." + AccountErrorCode.ACCIUNT_UNDER_POTENTIAL_RISK.name());
		}
		return r;
	}

	private Boolean hadDisabled(Long userId) {
		try {
			return iUserSecurityLog.isBackendDisadbled(userId);
		} catch (Exception e) {
			log.warn("查询用户是否人工解禁userId:" + userId, e);
		}
		return Boolean.FALSE;
	}
}
