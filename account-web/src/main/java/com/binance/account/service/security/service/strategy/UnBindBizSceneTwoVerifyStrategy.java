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
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:38 下午
 */
public class UnBindBizSceneTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {

        MultiFactorSceneCheckResult multiFactorSceneCheckResult = new MultiFactorSceneCheckResult();
        boolean isMoreBindOne = isMoreBindOne(verifyInfo);

        //如果没有满足至少绑定一项，提示用户绑定
        if (!isMoreBindOne) {
            Set<AccountVerificationTwoBind> needBindVerifyList = needBindVerifys(verifyInfo);
            multiFactorSceneCheckResult.setNeedBindVerifyList(needBindVerifyList);
            return multiFactorSceneCheckResult;
        }

        Set<AccountVerificationTwoCheck> needCheckVerifyList = needCheckVerifys(verifyInfo);
        multiFactorSceneCheckResult.setNeedCheckVerifyList(needCheckVerifyList);
        return multiFactorSceneCheckResult;
    }

    private static Set<AccountVerificationTwoCheck> needCheckVerifys(UserTwoVerifyInfo verifyInfo) {

        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindEmail = verifyInfo.getIsBindEmail() == null ? false : verifyInfo.getIsBindEmail();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        Set<AccountVerificationTwoCheck> needCheckVerifyList = new HashSet<>();
        if (isBindEmail) {
            needCheckVerifyList.add(getAccountVerificationTwoCheck(AccountVerificationTwoEnum.EMAIL, 1));
        }

        if (isBindMobile) {
            needCheckVerifyList.add(getAccountVerificationTwoCheck(AccountVerificationTwoEnum.SMS, 1));
        }

        if (isBindGoogle) {
            needCheckVerifyList.add(getAccountVerificationTwoCheck(AccountVerificationTwoEnum.GOOGLE, 1));
        }

        return needCheckVerifyList;
    }


    private static Set<AccountVerificationTwoBind> needBindVerifys(UserTwoVerifyInfo verifyInfo) {
        Boolean isBindMobile = verifyInfo.getIsBindMobile() == null ? false : verifyInfo.getIsBindMobile();
        Boolean isBindEmail = verifyInfo.getIsBindEmail() == null ? false : verifyInfo.getIsBindEmail();
        Boolean isBindGoogle = verifyInfo.getIsBindGoogle() == null ? false : verifyInfo.getIsBindGoogle();

        Set<AccountVerificationTwoBind> verificationTwoBinds = new HashSet<>();
        if (!isBindEmail) {
            verificationTwoBinds.add(getAccountVerificationTwoBind(AccountVerificationTwoEnum.EMAIL, 0));
        }

        if (!isBindMobile) {
            verificationTwoBinds.add(getAccountVerificationTwoBind(AccountVerificationTwoEnum.SMS, 0));
        }

        if (!isBindGoogle) {
            verificationTwoBinds.add(getAccountVerificationTwoBind(AccountVerificationTwoEnum.GOOGLE, 0));
        }

        return verificationTwoBinds;

    }

    private static AccountVerificationTwoCheck getAccountVerificationTwoCheck(AccountVerificationTwoEnum verificationTwoEnum, Integer option) {
        AccountVerificationTwoCheck verificationTwoCheck = new AccountVerificationTwoCheck();
        verificationTwoCheck.setOption(option);
        verificationTwoCheck.setVerifyType(verificationTwoEnum);

        return verificationTwoCheck;
    }

    private static AccountVerificationTwoBind getAccountVerificationTwoBind(AccountVerificationTwoEnum verificationTwoEnum, Integer option) {
        AccountVerificationTwoBind verificationTwoBind = new AccountVerificationTwoBind();
        verificationTwoBind.setVerifyType(verificationTwoEnum);
        verificationTwoBind.setOption(option);
        return verificationTwoBind;
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
