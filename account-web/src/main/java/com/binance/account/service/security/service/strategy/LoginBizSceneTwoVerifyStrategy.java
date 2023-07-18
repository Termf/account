package com.binance.account.service.security.service.strategy;

import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:38 下午
 */
public class LoginBizSceneTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result=new MultiFactorSceneCheckResult();
        // 1 bindlist （登录不存在需要绑定的验证项）
        // 2 需要验证的项，yubikye优先级最高，有yubikey验证yubikey，反正就是google或者手机二选一
        Set<AccountVerificationTwoCheck> needCheckVerifyList= Sets.newHashSet();
        result.setNeedCheckVerifyList(needCheckVerifyList);
        if(verifyInfo.getIsBindYubikey()){
            AccountVerificationTwoCheck accountVerificationTwoCheck=new AccountVerificationTwoCheck();
            accountVerificationTwoCheck.setVerifyType(AccountVerificationTwoEnum.YUBIKEY);
            accountVerificationTwoCheck.setOption(1);
            needCheckVerifyList.add(accountVerificationTwoCheck);
            return result;
        }

        int bindNum = 0;
        if (verifyInfo.getIsBindMobile()) {
            bindNum++;
        }
        if (verifyInfo.getIsBindGoogle()) {
            bindNum++;
        }
        if (verifyInfo.getIsBindEmail()) {
            bindNum++;
        }

        // 绑定项小于2就无需校验了
        if (bindNum < 2) {
            return result;
        }

        //用户没有yubikey的话google 手机二选一
        // 手机谷歌二选一
        if (verifyInfo.getIsBindMobile() && verifyInfo.getIsBindGoogle()) {
            AccountVerificationTwoCheck mobile = new AccountVerificationTwoCheck();
            mobile.setVerifyType(AccountVerificationTwoEnum.SMS);
            mobile.setOption(0);
            needCheckVerifyList.add(mobile);

            AccountVerificationTwoCheck google = new AccountVerificationTwoCheck();
            google.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            google.setOption(0);
            needCheckVerifyList.add(google);
        } else {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setOption(1);
            if (verifyInfo.getIsBindMobile()) {
                check.setVerifyType(AccountVerificationTwoEnum.SMS);
                needCheckVerifyList.add(check);
            }
            if (verifyInfo.getIsBindGoogle()){
                check.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
                needCheckVerifyList.add(check);
            }
        }
        return result;
    }
}
