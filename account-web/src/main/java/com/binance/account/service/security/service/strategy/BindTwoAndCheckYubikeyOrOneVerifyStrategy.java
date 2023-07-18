package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 绑定两项
 * 未绑定yubikey，手机谷歌二选一
 * 绑定yubikey，只验证yubikey
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:39 下午
 */
public class BindTwoAndCheckYubikeyOrOneVerifyStrategy {

    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult multiFactorSceneCheckResult = new MultiFactorSceneCheckResult();
        boolean isBindedMoreThanTwo = isBindedMoreThanTwo(verifyInfo);
        if (!isBindedMoreThanTwo) {
            Set<AccountVerificationTwoBind> needBindVerifyList = needBindVerifys(verifyInfo);
            multiFactorSceneCheckResult.setNeedBindVerifyList(needBindVerifyList);
            return multiFactorSceneCheckResult;
        }
        Set<AccountVerificationTwoCheck> needCheckVerifyList = needCheckVerifys(verifyInfo);
        multiFactorSceneCheckResult.setNeedCheckVerifyList(needCheckVerifyList);
        return multiFactorSceneCheckResult;
    }


    private static Set<AccountVerificationTwoBind> needBindVerifys(UserTwoVerifyInfo verifyInfo) {
        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindEmail = verifyInfo.getIsBindEmail() == null ? false : verifyInfo.getIsBindEmail();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        Set<AccountVerificationTwoBind> verificationTwoBinds = new HashSet<>();
        if (!isBindEmail) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.EMAIL, 0));
        }

        if (!isBindMobile) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.SMS, 0));
        }

        if (!isBindGoogle) {
            verificationTwoBinds.add(new AccountVerificationTwoBind(AccountVerificationTwoEnum.GOOGLE, 0));
        }

        return verificationTwoBinds;

    }


    private static Set<AccountVerificationTwoCheck> needCheckVerifys(UserTwoVerifyInfo verifyInfo) {
        Set<AccountVerificationTwoCheck> needCheckVerifyList = new HashSet<>();
        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();
        Boolean isBindYubikey = verifyInfo.getIsBindYubikey() == null ? false : verifyInfo.getIsBindYubikey();
        if (isBindYubikey) {
            needCheckVerifyList.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.YUBIKEY, 1));
            return needCheckVerifyList;
        }
        if (isBindMobile && isBindGoogle) {
            needCheckVerifyList.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.SMS, 0));
            needCheckVerifyList.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.GOOGLE, 0));
            return needCheckVerifyList;
        }
        if (isBindMobile) {
            needCheckVerifyList.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.SMS, 1));
        }
        if (isBindGoogle) {
            needCheckVerifyList.add(new AccountVerificationTwoCheck(AccountVerificationTwoEnum.GOOGLE, 1));
        }
        return needCheckVerifyList;
    }


    /**
     * 至少绑定两项 ，除 yubikey
     *
     * @param verifyInfo
     * @return
     */
    private static boolean isBindedMoreThanTwo(UserTwoVerifyInfo verifyInfo) {

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

        return count >= 2;
    }
}
