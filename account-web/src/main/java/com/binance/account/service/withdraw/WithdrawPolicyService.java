package com.binance.account.service.withdraw;

import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.UserInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Fei.Huang on 2019/1/14.
 */
public interface WithdrawPolicyService {

    List<BigDecimal> getDailyWithdrawLimits() throws Exception;

    BigDecimal getDailyWithdrawLimitBySecurityLevel(Long userId) throws Exception;

    BigDecimal getWithdrawReviewQuota(Long userId) throws Exception;


    List<BigDecimal> getDailyWithdrawLimitsByUserSecurityAndUserInfo(UserInfo userInfo, UserSecurity userSecurity) throws Exception;


}
