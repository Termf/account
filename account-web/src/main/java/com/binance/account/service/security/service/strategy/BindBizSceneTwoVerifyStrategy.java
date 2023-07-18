package com.binance.account.service.security.service.strategy;

import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:38 下午
 */
public class BindBizSceneTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result=new MultiFactorSceneCheckResult();
        if(BizSceneEnum.BIND_EMAIL==verifyInfo.getBizScene() && verifyInfo.getIsBindEmail()){
            throw new BusinessException(AccountErrorCode.USER_EMAIL_ALREADY_BIND);// 用户已经绑定无需继续绑定
        }

        if(BizSceneEnum.BIND_MOBILE==verifyInfo.getBizScene() && verifyInfo.getIsBindMobile()){
            throw new BusinessException(GeneralCode.USER_MOBILE_BIND);// 用户已经绑定无需继续绑定
        }

        if(BizSceneEnum.BIND_GOOGLE==verifyInfo.getBizScene() && verifyInfo.getIsBindGoogle()){
            throw new BusinessException(AccountErrorCode.USER_GOOGLE_ALREADY_BIND);
        }

        // 2 需要验证的项 + 有几项验证几项，当然这个范围肯定是手机，邮箱，google 3个之内
        Set<AccountVerificationTwoCheck> needCheckVerifyList= Sets.newHashSet();
        if(verifyInfo.getIsBindEmail()){
            AccountVerificationTwoCheck accountVerificationTwoCheck=new AccountVerificationTwoCheck();
            accountVerificationTwoCheck.setVerifyType(AccountVerificationTwoEnum.EMAIL);
            accountVerificationTwoCheck.setOption(1);
            needCheckVerifyList.add(accountVerificationTwoCheck);
        }

        if(verifyInfo.getIsBindGoogle()){
            AccountVerificationTwoCheck accountVerificationTwoCheck=new AccountVerificationTwoCheck();
            accountVerificationTwoCheck.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            accountVerificationTwoCheck.setOption(1);
            needCheckVerifyList.add(accountVerificationTwoCheck);
        }

        if(verifyInfo.getIsBindMobile()){
            AccountVerificationTwoCheck accountVerificationTwoCheck=new AccountVerificationTwoCheck();
            accountVerificationTwoCheck.setVerifyType(AccountVerificationTwoEnum.SMS);
            accountVerificationTwoCheck.setOption(1);
            needCheckVerifyList.add(accountVerificationTwoCheck);
        }

        result.setNeedCheckVerifyList(needCheckVerifyList);
        return result;
    }
}
