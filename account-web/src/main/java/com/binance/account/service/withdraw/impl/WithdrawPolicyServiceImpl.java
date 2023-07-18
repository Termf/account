package com.binance.account.service.withdraw.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.binance.account.data.entity.user.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.service.withdraw.WithdrawPolicyService;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Fei.Huang on 2019/1/14.
 */
@Slf4j
@Service
public class WithdrawPolicyServiceImpl implements WithdrawPolicyService {

    @Resource
    private UserCommonBusiness userCommonBusiness;
    @Resource
    private UserSecurityMapper userSecurityMapper;

    @Value("${withdraw.security.policy}")
    private String withdrawSecurityPolicies;

    @Value("${withdraw.security.review.quota}")
    private String withdrawSecurityReviewQuotas;

    @Override
    public List<BigDecimal> getDailyWithdrawLimits() {
        List<BigDecimal> limits = new ArrayList<>();

        String[] policies = withdrawSecurityPolicies.split(",");
        for (String policy : policies) {
            limits.add(new BigDecimal(policy));
        }

        return limits;
    }

    @Override
    public BigDecimal getDailyWithdrawLimitBySecurityLevel(Long userId) {

        User user = userCommonBusiness.checkAndGetUserById(userId);

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (null == userSecurity) {
            log.error("userSecurity not exist, userId:{}", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Integer securityLevel = userSecurity.getSecurityLevel();
        if (null == securityLevel || 0 == securityLevel) {
            securityLevel = 1;
        }

        String[] policies = withdrawSecurityPolicies.split(",");
        if (securityLevel > policies.length) {
            securityLevel = policies.length;
        }

        BigDecimal dailyLimit = new BigDecimal(policies[securityLevel - 1]);

        log.info("getDailyWithdrawLimit securityLevel:{}, userId:{}, dailyLimit:{}", securityLevel, userId,
                dailyLimit.toString());

        return dailyLimit;
    }

    @Override
    public BigDecimal getWithdrawReviewQuota(Long userId) {

        User user = userCommonBusiness.checkAndGetUserById(userId);

        UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(user.getUserId());
        if (null == userSecurity) {
            log.error("userSecurity not exist, userId:{}", userId);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }

        Integer securityLevel = userSecurity.getSecurityLevel();
        if (null == securityLevel || 0 == securityLevel) {
            securityLevel = 1;
        }

        String[] quotas = withdrawSecurityReviewQuotas.split(",");
        if (securityLevel > quotas.length) {
            securityLevel = quotas.length;
        }

        BigDecimal quota = new BigDecimal(quotas[securityLevel - 1]);

        log.info("getWithdrawReviewQuota securityLevel:{}, userId:{}, quota:{}", securityLevel, userId,
                quota.toString());

        return quota;
    }

    @Override
    public List<BigDecimal> getDailyWithdrawLimitsByUserSecurityAndUserInfo(UserInfo userInfo, UserSecurity userSecurity) throws Exception {
        List<BigDecimal> limits = new ArrayList<>();
        String[] policies = withdrawSecurityPolicies.split(",");
        for (String policy : policies) {
            if(limits.size()==2 && null!=userInfo && null!=userInfo.getDailyWithdrawCap() && userInfo.getDailyWithdrawCap().compareTo(BigDecimal.ZERO) > 0){
                limits.add(userInfo.getDailyWithdrawCap());
            }else{
                limits.add(new BigDecimal(policy));
            }
        }
        return limits;
    }
}
