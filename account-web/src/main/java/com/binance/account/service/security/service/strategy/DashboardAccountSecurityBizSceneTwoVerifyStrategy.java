package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * dashboard页面账户安全需要绑定场景，3个（google/email/mobile）中需要绑定2个
 * 只返回绑定项就行了
 * @Author: mingming.sheng
 * @Date: 2020/5/26 4:17 下午
 */
public class DashboardAccountSecurityBizSceneTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult multiFactorSceneCheckResult = new MultiFactorSceneCheckResult();
        //如果不符合满足一项,提示绑定项
        if (!isMoreBindOne(verifyInfo)){
            Set<AccountVerificationTwoBind> needBindVerifyList = needBindVerifys(verifyInfo);
            multiFactorSceneCheckResult.setNeedBindVerifyList(needBindVerifyList);
        }
        return multiFactorSceneCheckResult;
    }

    private static Set<AccountVerificationTwoBind> needBindVerifys(UserTwoVerifyInfo verifyInfo) {
        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindEmail = verifyInfo.getIsBindEmail() == null ? false : verifyInfo.getIsBindEmail();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        Set<AccountVerificationTwoBind> verificationTwoBinds = new HashSet<>();
        if (!isBindGoogle) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.GOOGLE, 0));
        }

        if (!isBindEmail) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.EMAIL, 0));
        }

        if (!isBindMobile) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.SMS, 0));
        }

        return verificationTwoBinds;
    }

    /**
     * 判断是否绑定多项目 ，除 yubikey
     *
     * @param verifyInfo
     * @return
     */
    private static boolean isMoreBindOne(UserTwoVerifyInfo verifyInfo) {

        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindEmail = verifyInfo.getIsBindEmail() == null ? false : verifyInfo.getIsBindEmail();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        List<Boolean> binds = new ArrayList<>();
        binds.add(isBindMobile);
        binds.add(isBindEmail);
        binds.add(isBindGoogle);

        int count = 0;
        for (Boolean bind : binds) {
            if (bind) {
                count++;
            }
        }

        return count > 1;
    }
}
