package com.binance.account.service.security.service.strategy;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 2:54 下午
 */
@Slf4j
public class RegisterTwoVerifyStrategy {

    /**
     * 手机号注册用户只验证手机，邮箱注册用户只验证手机
     */
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result = new MultiFactorSceneCheckResult();

        Set<AccountVerificationTwoCheck> checkList = new HashSet<>();
        if (verifyInfo.getIsBindMobile()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.SMS);
            check.setOption(1);
            checkList.add(check);
        } else if (verifyInfo.getIsBindEmail()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.EMAIL);
            check.setOption(1);
            checkList.add(check);
        } else {
            log.error("RegisterTwoVerifyStrategy.get2FaVerifyList,用户既不是手机注册用户也不是邮箱注册用户,verifyInfo={}",
                    JSON.toJSONString(verifyInfo));
            throw new BusinessException(GeneralCode.SYS_VALID);
        }

        result.setNeedCheckVerifyList(checkList);
        return result;
    }
}
