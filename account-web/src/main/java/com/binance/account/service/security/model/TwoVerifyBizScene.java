package com.binance.account.service.security.model;

import com.binance.account.service.security.service.strategy.*;
import com.binance.account.service.security.service.strategy.ApiKeyManageBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.BindBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.BindTwoAndCheckAllExistVerifyStrategy;
import com.binance.account.service.security.service.strategy.BindTwoAndCheckOneVerifyStrategy;
import com.binance.account.service.security.service.strategy.BindTwoAndCheckYubikeyOrOneVerifyStrategy;
import com.binance.account.service.security.service.strategy.BindTwoAndCheckYubikeyWithOneVerifyStrategy;
import com.binance.account.service.security.service.strategy.ChangeEmailMiddleVerifyStrategy;
import com.binance.account.service.security.service.strategy.CryptoWithdrawBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.DashboardAccountSecurityBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.ForgetPasswordTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.LoginBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.NewDeviceAuthorizeBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.OldEmailBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.PasswordTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.RegisterTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.UnBindBizSceneTwoVerifyStrategy;
import com.binance.account.service.security.service.strategy.*;
import com.binance.master.enums.BaseEnum;
import lombok.Getter;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:34 下午
 */
@Getter
public enum TwoVerifyBizScene implements BaseEnum {
    ACCOUNT_ACTIVATE("account_activate", "账户激活") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return RegisterTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    LOGIN("login", "登录") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return LoginBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    API_KEY_MANAGE("api_key_manage", "api key创建更新") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return ApiKeyManageBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    API_WITHDRAW_SWITCH("api_withdraw_switch", "api编辑（包括开启提现）") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckYubikeyWithOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    API_EDIT_SWITCH("api_edit_switch", "api编辑（不包括开启提现）") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckYubikeyOrOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    WITHDRAW_WHITE_SWITCH("switch_withdraw_white", "关闭提币白名单") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    WITHDRAW_WHITE_ENABLE("withdraw_white_enable", "开启提币白名单") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    BIND_EMAIL("bind_email", "绑定邮箱") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    BIND_MOBILE("bind_mobile", "绑定手机") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    UNBIND_MOBILE("unbind_mobile", "解绑手机") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return UnBindBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    BIND_GOOGLE("bind_google", "绑定谷歌") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    UNBIND_GOOGLE("unbind_google", "解绑谷歌") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return UnBindBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    AUTHORIZE_NEW_DEVICE("authorize_new_device", "新设备授权") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return NewDeviceAuthorizeBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    CRYPTO_WITHDRAW("crypto_withdraw", "crypto提现") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return CryptoWithdrawBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    WHITE_ADDRESS_MANAGE("white_address_manage", "白名单地址管理") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    WITHDRAW_ADDRESS_MANAGE("withdraw_address_manage", "提现地址管理") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    FORGET_PASSWORD("forget_password", "忘记密码") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return ForgetPasswordTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    MODIFY_PASSWORD("modify_password", "修改密码") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return PasswordTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    YUBIKEY_SAFE_VERIFY("yubikey_safe_verify", "启用YubiKey安全验证") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    OLD_EMAIL_VERIFY("old_email_verify", "老邮箱验证") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return OldEmailBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    NEW_EMAIL_VERIFY("new_email_verify", "新邮箱验证") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    CHANGE_EMAIL_MIDDLE_VERIFY("change_email_middle_verify", "更换邮箱中间场景验证") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return ChangeEmailMiddleVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    FIAT_ASSET_WITHDRAW_CONFIRM("fiat_asset_withdraw_confirm", "第三方划转确认") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    FIAT_CASH_BALANCE_PURCHASE("fiat_cash_balance_purchase", "现金余额购买") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    FIAT_CREDIT_CARD_PURCHASE("fiat_credit_card_purchase", "信用卡购买") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    ASSET_TRANSFER_APPLY_CONFIRM("asset_transfer_apply_confirm", "资产划转申请确认") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckAllExistVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    SET_ANTI_PHISHING_CODE("set_anti_phishing_code", "设置防钓鱼码") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    DEREGISTER_YUBIKEY("deregister_yubikey", "注销yubikey") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    C2C_BIND_PAYMENT("c2c_bind_payment", "C2C绑定/编辑收款方式") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    C2C_POST_EDIT_AD("c2c_post_edit_ad", "C2C发布编辑广告") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    C2C_RELEASE_CURRENCY("c2c_release_currency", "C2C放币") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    OPEN_NOTIFICATION_MGS("open_notification_mgs", "开启通知消息") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return BindTwoAndCheckOneVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    DASHBOARD_ACCOUNT_SECURITY("dashboard_account_security", "dashboard账户安全场景") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return DashboardAccountSecurityBizSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    CARD_VIEW_PIN("card_view_pin", "查看银行卡pin码") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return CardViewPinSceneTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },

    CARD_UNLOCK("card_unlock", "解绑银行卡") {
        @Override
        public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
            return CardAccountUnlockCardVerifyStrategy.get2FaVerifyList(verifyInfo);
        }
    },


    ;

    private String code;
    private String desc;

    TwoVerifyBizScene(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据用户绑定信息和场景获取2fa列表
     */
    public abstract MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo);
}
