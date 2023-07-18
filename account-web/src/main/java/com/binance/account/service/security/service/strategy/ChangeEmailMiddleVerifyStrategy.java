package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 2:54 下午
 */
@Slf4j
public class ChangeEmailMiddleVerifyStrategy {

    /**
     * 手机号注册用户只验证手机，邮箱注册用户只验证手机
     */
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result = new MultiFactorSceneCheckResult();
        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        Set<AccountVerificationTwoCheck> verificationTwoBinds = new HashSet<>();
        if (isBindMobile) {
            verificationTwoBinds.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.SMS, 1));
        }

        if (isBindGoogle) {
            verificationTwoBinds.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.GOOGLE, 1));
        }

        result.setNeedCheckVerifyList(verificationTwoBinds);
        return result;
    }
}
