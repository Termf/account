package com.binance.account.vo.security.enums;

import com.binance.master.enums.BaseEnum;
import lombok.Getter;

/**
 * @Author: mingming.sheng
 * @Date: 2020/4/16 1:47 下午
 */
@Getter
public enum BizSceneEnum implements BaseEnum {
    ACCOUNT_ACTIVATE("account_activate", "账户激活","email_auth2","unified_verification_code_template"),

    LOGIN("login", "登录","email_login","unified_verification_code_template"),

    API_KEY_MANAGE("api_key_manage", "api key创建更新","api_create_enable_v3","unified_verification_code_template"),

    API_WITHDRAW_SWITCH("api_withdraw_switch", "api提现开关","api_withdraw_enable_v3","unified_verification_code_template"),

    API_EDIT_SWITCH("api_edit_switch", "api编辑（不包括开启提现）","api_trusted_ip","unified_verification_code_template"),

    WITHDRAW_WHITE_SWITCH("switch_withdraw_white", "关闭提币白名单","withdraw_close_white_list_v3","unified_verification_code_template"),

    WITHDRAW_WHITE_ENABLE("withdraw_white_enable", "开启提币白名单","withdraw_white_enable","unified_verification_code_template"),

    BIND_EMAIL("bind_email", "绑定邮箱","bind_email","unified_verification_code_template"),

    BIND_MOBILE("bind_mobile", "绑定手机","bind_mobile","unified_verification_code_template"),

    UNBIND_MOBILE("unbind_mobile", "解绑手机","unbind_mobile","unified_verification_code_template"),

    BIND_GOOGLE("bind_google", "绑定谷歌","bind_google","unified_verification_code_template"),

    UNBIND_GOOGLE("unbind_google", "解绑谷歌","unbind_google","unified_verification_code_template"),

    AUTHORIZE_NEW_DEVICE("authorize_new_device", "新设备授权","email_new_device_authorize2","unified_verification_code_template"),//ok

    CRYPTO_WITHDRAW("crypto_withdraw", "crypto提现","asset_withdraw_apply_V3","unified_verification_code_template"),//ok

    WHITE_ADDRESS_MANAGE("white_address_manage", "白名单地址管理","withdraw_save_white_list_v3","unified_verification_code_template"),

    WITHDRAW_ADDRESS_MANAGE("withdraw_address_manage", "提现地址管理","withdraw_address_manage","unified_verification_code_template"),

    FORGET_PASSWORD("forget_password", "忘记密码","email_tml_pwd_v3","unified_verification_code_template"),

    MODIFY_PASSWORD("modify_password", "修改密码","modify_password","unified_verification_code_template"),

    YUBIKEY_SAFE_VERIFY("yubikey_safe_verify", "YubiKey安全验证","turn_on_yubiKey_v3","unified_verification_code_template"),

    OLD_EMAIL_VERIFY("old_email_verify", "老邮箱验证","user_email_change_old_link_V3","unified_verification_code_template"),

    NEW_EMAIL_VERIFY("new_email_verify", "新邮箱验证","user_email_change_new_link_v3","unified_verification_code_template"),

    CHANGE_EMAIL_MIDDLE_VERIFY("change_email_middle_verify", "更换邮箱中间场景验证","change_email_middle_verify","unified_verification_code_template"),

    FIAT_ASSET_WITHDRAW_CONFIRM("fiat_asset_withdraw_confirm", "第三方划转确认","asset_fiatwithdraw_apply_v3","unified_verification_code_template"),

    FIAT_CASH_BALANCE_PURCHASE("fiat_cash_balance_purchase", "现金余额购买","fiat_cash_balance_purchase","unified_verification_code_template"),

    FIAT_CREDIT_CARD_PURCHASE("fiat_credit_card_purchase", "信用卡购买","fiat_credit_card_purchase","unified_verification_code_template"),

    ASSET_TRANSFER_APPLY_CONFIRM("asset_transfer_apply_confirm", "资产划转申请确认","email_open_transfer_apply_v3","unified_verification_code_template"),

    SET_ANTI_PHISHING_CODE("set_anti_phishing_code", "设置防钓鱼码","set_anti_phishing_code","unified_verification_code_template"),

    DEREGISTER_YUBIKEY("deregister_yubikey", "注销yubikey","deregister_yubikey","unified_verification_code_template"),

    C2C_BIND_PAYMENT("c2c_bind_payment", "C2C绑定/编辑收款方式","c2c_bind_payment","unified_verification_code_template"),

    C2C_POST_EDIT_AD("c2c_post_edit_ad", "C2C发布编辑广告","c2c_post_edit_ad","unified_verification_code_template"),

    C2C_RELEASE_CURRENCY("c2c_release_currency", "C2C放币","c2c_release_currency","unified_verification_code_template"),

    OPEN_NOTIFICATION_MGS("open_notification_mgs", "开启通知消息","open_notification_mgs","unified_verification_code_template"),

    DASHBOARD_ACCOUNT_SECURITY("dashboard_account_security", "dashboard账户安全场景","dashboard_account_security","unified_verification_code_template"),

    RESET_APPLY_2FA("reset_apply_2fa", "申请重置2fa", "reset_apply_2fa_template_email", "reset_apply_2fa_code_template"),

    CARD_VIEW_PIN("card_view_pin", "币安借记卡解锁","card_2fa_email","card_2fa_for_sms"),

    CARD_UNLOCK("card_unlock", "币安借记卡查看密码","card_2fa_email","card_2fa_for_sms"),

    CARD_OTP_FILL_MOBILE("card_otp_fill_mobile", "币安借记卡补充手机号OTP","unified_verification_code_template_email","card_otp_filll_mobile_for_sms"),

    CARD_OTP_FILL_EMAIL("card_otp_filll_email", "币安借记卡补充邮箱OTP","card_otp_fill_email","unified_verification_code_template"),



    RESET_APPLY_UNLOCK("reset_apply_unlock", "申请解禁账户", "reset_apply_unlock_template_email", "reset_apply_unlock_code_template"),

    RESET_APPLY_MOBILE("reset_apply_mobile", "申请重置新手机", "reset_apply_mobile_template_email", "reset_apply_mobile_code_template"),

    ;
    private String code;
    private String desc;
    private String emailTplCode;
    private String smsTplCode;

    BizSceneEnum(String code, String desc, String emailTplCode, String smsTplCode) {
        this.code = code;
        this.desc = desc;
        this.emailTplCode = emailTplCode;
        this.smsTplCode = smsTplCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getEmailTplCode() {
        return emailTplCode;
    }

    public void setEmailTplCode(String emailTplCode) {
        this.emailTplCode = emailTplCode;
    }

    public String getSmsTplCode() {
        return smsTplCode;
    }

    public void setSmsTplCode(String smsTplCode) {
        this.smsTplCode = smsTplCode;
    }
}
