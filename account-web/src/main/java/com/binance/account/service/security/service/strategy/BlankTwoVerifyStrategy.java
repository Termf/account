package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 4:08 下午
 */
public class BlankTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        return new MultiFactorSceneCheckResult();
    }
}
