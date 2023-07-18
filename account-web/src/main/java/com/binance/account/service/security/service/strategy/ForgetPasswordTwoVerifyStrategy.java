package com.binance.account.service.security.service.strategy;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;


@Slf4j
public class ForgetPasswordTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        if (verifyInfo.getBizScene() == BizSceneEnum.FORGET_PASSWORD) {
            return forgetPassword(verifyInfo);
        } else {
            log.error("PasswordTwoVerifyStrategy.get2FaVerifyList,当前场景未配置对应2fa策略,verifyInfo={}", JSON.toJSONString(verifyInfo));
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
    }

    /**
     * 忘记密码。绑定：需要绑定一项；校验：用户绑定几项安全项验证几项安全项
     */
    private static MultiFactorSceneCheckResult forgetPassword(UserTwoVerifyInfo verifyInfo) {
        // 用户注册后肯定默认有一项绑定项，所以直接透出验证项，无需额外绑定
        MultiFactorSceneCheckResult result = new MultiFactorSceneCheckResult();
        Set<AccountVerificationTwoCheck> checkList = new HashSet<>();
        // 手机邮箱二选一
        if (verifyInfo.getIsBindMobile() && verifyInfo.getIsBindEmail()) {
            AccountVerificationTwoCheck mobile = new AccountVerificationTwoCheck();
            mobile.setVerifyType(AccountVerificationTwoEnum.SMS);
            mobile.setOption(0);
            checkList.add(mobile);

            AccountVerificationTwoCheck email = new AccountVerificationTwoCheck();
            email.setVerifyType(AccountVerificationTwoEnum.EMAIL);
            email.setOption(0);
            checkList.add(email);
        } else {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setOption(1);
            if (verifyInfo.getIsBindMobile()) {
                check.setVerifyType(AccountVerificationTwoEnum.SMS);
                checkList.add(check);
            }
            if (verifyInfo.getIsBindEmail()){
                check.setVerifyType(AccountVerificationTwoEnum.EMAIL);
                checkList.add(check);
            }
        }
        result.setNeedCheckVerifyList(checkList);
        return result;
    }


}
