package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 新设备授权验证策略
 */
public class NewDeviceAuthorizeBizSceneTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result = new MultiFactorSceneCheckResult();
        // 2 需要验证的项，yubikye优先级最高，有yubikey验证yubikey+手机/邮箱二选一
        Set<AccountVerificationTwoCheck> needCheckVerifyList = Sets.newHashSet();
        if (verifyInfo.getIsBindYubikey()) {
            //手机或者邮箱二选一
            if (verifyInfo.getIsBindEmail() && verifyInfo.getIsBindMobile()) {
                AccountVerificationTwoCheck emailCheck = new AccountVerificationTwoCheck();
                emailCheck.setVerifyType(AccountVerificationTwoEnum.EMAIL);
                emailCheck.setOption(0);
                needCheckVerifyList.add(emailCheck);
                AccountVerificationTwoCheck mobileCheck = new AccountVerificationTwoCheck();
                mobileCheck.setVerifyType(AccountVerificationTwoEnum.SMS);
                mobileCheck.setOption(0);
                needCheckVerifyList.add(mobileCheck);
            } else {
                if (verifyInfo.getIsBindEmail()) {
                    AccountVerificationTwoCheck emailCheck = new AccountVerificationTwoCheck();
                    emailCheck.setVerifyType(AccountVerificationTwoEnum.EMAIL);
                    emailCheck.setOption(1);
                    needCheckVerifyList.add(emailCheck);
                }

                if (verifyInfo.getIsBindMobile()) {
                    AccountVerificationTwoCheck mobileCheck = new AccountVerificationTwoCheck();
                    mobileCheck.setVerifyType(AccountVerificationTwoEnum.SMS);
                    mobileCheck.setOption(1);
                    needCheckVerifyList.add(mobileCheck);
                }
            }
            result.setNeedCheckVerifyList(needCheckVerifyList);
            return result;
        }

        //用户没有yubikey的话,手机，邮箱，google，3个里面有几个验证几个
        if (verifyInfo.getIsBindGoogle()) {
            AccountVerificationTwoCheck googleCheck = new AccountVerificationTwoCheck();
            googleCheck.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            googleCheck.setOption(1);
            needCheckVerifyList.add(googleCheck);
        }

        if (verifyInfo.getIsBindMobile()) {
            AccountVerificationTwoCheck mobileCheck = new AccountVerificationTwoCheck();
            mobileCheck.setVerifyType(AccountVerificationTwoEnum.SMS);
            mobileCheck.setOption(1);
            needCheckVerifyList.add(mobileCheck);
        }

        if (verifyInfo.getIsBindEmail()) {
            AccountVerificationTwoCheck emailCheck = new AccountVerificationTwoCheck();
            emailCheck.setVerifyType(AccountVerificationTwoEnum.EMAIL);
            emailCheck.setOption(1);
            needCheckVerifyList.add(emailCheck);
        }
        result.setNeedCheckVerifyList(needCheckVerifyList);
        return result;
    }
}
