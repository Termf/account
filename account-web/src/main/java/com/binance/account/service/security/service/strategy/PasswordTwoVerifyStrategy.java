package com.binance.account.service.security.service.strategy;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.vo.security.AccountVerificationTwoBind;
import com.binance.account.vo.security.AccountVerificationTwoCheck;
import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:37 下午
 */
@Slf4j
public class PasswordTwoVerifyStrategy {
    public static MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        if (verifyInfo.getBizScene() == BizSceneEnum.FORGET_PASSWORD) {
            return forgetPassword(verifyInfo);
        } else if (verifyInfo.getBizScene() == BizSceneEnum.MODIFY_PASSWORD) {
            return modifyPassword(verifyInfo);
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
        result.setNeedCheckVerifyList(checkList);
        if (verifyInfo.getIsBindMobile()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.SMS);
            check.setOption(1);
            checkList.add(check);
        }
        if (verifyInfo.getIsBindEmail()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.EMAIL);
            check.setOption(1);
            checkList.add(check);
        }
        if (verifyInfo.getIsBindGoogle()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            check.setOption(1);
            checkList.add(check);
        }

        return result;
    }

    /**
     * 修改密码。
     * 绑定：需要绑定两项（本场景yubikey也算作一项）；
     * 校验：1、未开启yubikey：手机谷歌二选一  2、开启yubikey：只验证youbikey
     */
    private static MultiFactorSceneCheckResult modifyPassword(UserTwoVerifyInfo verifyInfo) {
        MultiFactorSceneCheckResult result = new MultiFactorSceneCheckResult();

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
        if (verifyInfo.getIsBindYubikey()) {
            bindNum++;
        }

        // 绑定场景
        if (bindNum < 2) {
            Set<AccountVerificationTwoBind> needBindVerifyList = new HashSet<>();
            result.setNeedBindVerifyList(needBindVerifyList);
            if (!verifyInfo.getIsBindEmail()) {
                AccountVerificationTwoBind bind = new AccountVerificationTwoBind();
                bind.setVerifyType(AccountVerificationTwoEnum.EMAIL);
                bind.setOption(0);
                needBindVerifyList.add(bind);
            }
            if (!verifyInfo.getIsBindMobile()) {
                AccountVerificationTwoBind bind = new AccountVerificationTwoBind();
                bind.setVerifyType(AccountVerificationTwoEnum.SMS);
                bind.setOption(0);
                needBindVerifyList.add(bind);
            }
            if (!verifyInfo.getIsBindGoogle()) {
                AccountVerificationTwoBind bind = new AccountVerificationTwoBind();
                bind.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
                bind.setOption(0);
                needBindVerifyList.add(bind);
            }
            return result;
        }

        // 验证场景
        Set<AccountVerificationTwoCheck> checkList = new HashSet<>();
        result.setNeedCheckVerifyList(checkList);
        result.setNeedCheckVerifyList(checkList);
        if (verifyInfo.getIsBindYubikey()) {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setVerifyType(AccountVerificationTwoEnum.YUBIKEY);
            check.setOption(1);
            checkList.add(check);
            return result;
        }

        // 手机谷歌二选一
        if (verifyInfo.getIsBindMobile() && verifyInfo.getIsBindGoogle()) {
            AccountVerificationTwoCheck mobile = new AccountVerificationTwoCheck();
            mobile.setVerifyType(AccountVerificationTwoEnum.SMS);
            mobile.setOption(0);
            checkList.add(mobile);

            AccountVerificationTwoCheck google = new AccountVerificationTwoCheck();
            google.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            google.setOption(0);
            checkList.add(google);
            return result;
        } else {
            AccountVerificationTwoCheck check = new AccountVerificationTwoCheck();
            check.setOption(1);
            checkList.add(check);
            if (verifyInfo.getIsBindMobile()) {
                check.setVerifyType(AccountVerificationTwoEnum.SMS);
            }
            if (verifyInfo.getIsBindGoogle()){
                check.setVerifyType(AccountVerificationTwoEnum.GOOGLE);
            }
            return result;
        }
    }
}
