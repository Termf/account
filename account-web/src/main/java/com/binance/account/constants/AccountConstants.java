package com.binance.account.constants;

/**
 * Created by Fei.Huang on 2018/4/28.
 */
public final class AccountConstants {

    public static final String LOCAL_IP = "local-ip";

    // 用户初始化提交KYC的锁，防止多次提交
    public final static String USER_KYC_INIT_LOCK = "USER_KYC_INIT_LOCK_";
    public final static String USER_KYC_OCR_LOCK = "USER_KYC_OCR_LOCK_";
    public final static String COMPANY_KYC_INIT_LOCK = "COMPANY_KYC_INIT_LOCK_";

    /**
     * 用户地址认证成功邮件模板的key
     */
    public static final String USER_ADDRESS_VERIFIED_MAIL_TEMPLATE = "user_address_verified";
    /**
     * 用户地址认证拒绝邮件模板的key
     */
    public static final String USER_ADDRESS_REJECT_MAIL_TEMPLATE = "user_address_reject";

    /**
     * 提现人脸识别通知邮件
     */
    public static final String USER_WITHDRAW_FACE_EMAIL_TEMPLATE = "user_withdraw_face";

    public static final String USER_WITHDRAW_FACE_CHANGE_LOG = "withdraw_face_status";

    /**
     * 短信通知: 由于提币人脸人脸识别检测拒绝KYC的短信通知
     */
    public static final String SMS_WITHDRAW_FACE_KYC_REFUSED = "sms_withdraw_face_kyc_refused";
    /**
     * 短信通知：由于提币人脸识别检测中用户没有做过人脸KYC的短信通知
     */
    public static final String SMS_WITHDRAW_FACE_KYC_NOTIFY = "sms_withdraw_face_kyc_notify";
    /**
     * 邮件消息：由于提币人脸识别检测中用户没有做过人脸KYC的邮件通知
     */
    public static final String EMAIL_WITHDRAW_FACE_KYC_NOTIFY = "email_withdraw_Face_kyc_notify";

    /**
     * 当提币风控人脸识别人工审核拒绝时，发送通知邮件
     */
    public static final String EMAIL_WITHDRAW_FACE_REFUSED_NOTIFY = "emai_withdraw_face_refused";

    /**
     * 当提币风控人脸识别从开启状态变更到关闭状态时，发送通知邮件
     */
    public static final String EMAIL_WITHDRAW_FACE_PASS_NOTIFY = "email_withdraw_face_pass";

    /**
     * 邮件信息，KYC认证通知用户操作人脸识别
     */
    public static final String EMAIL_KYC_FACE_NOTIFY = "email_kyc_do_face_notify";

    /**
     * 短信通知：由于提币人脸触发的通知短信
     */
    public static final String SMS_WITHDRAW_FACE_NOTIFY = "sms_withdraw_face_notify";

    /**
     * 邮件模板：重置流程的重置2FA初始化邮件模板
     */
    public static final String RESET_INIT_2FA_EMAIL_TEMPLATE = "2fa_reset";

    /**
     * 邮件模板：重置流程的重置2F答题错误邮件模板
     */
    public static final String RESET_FAIL_ANSWER_2FA_EMAIL_TEMPLATE = "2fa_fail_answer_reset";

    /**
     * 邮件模板：重置流程的用户解禁初始化邮件模板
     */
    public static final String RESET_INIT_ENABLE_EMAIL_TEMPLATE = "user_enable";

    /**
     * 邮件模板：重置流程回答问题失败邮件模板
     */
    public static final String RESET_QUESTION_VERIFY_FAIL_2FA = "2fa_reset_verify_fail";

    /**
     * 邮件模板：用户解禁回答问题失败邮件模板
     */
    public static final String RESET_QUESTION_VERIFY_FAIL_ENABLE = "user_enable_verify_fail";

    /**
     * 邮件消息：当一个账号被激活母账号功能的时候发送的邮件提醒
     */
    public static final String SUB_ACCOUNT_ENABLE_NOTIFICATION = "sub_account_enable_notification";

    /**
     * 邮件消息：期货强平邮件
     */
    public static final String FUTURE_LIQUIDATION_CALL= "future_liquidation_call";

    /**
     * 邮件消息，交割强平邮件
     */
    public static final String DELIVERY_LIQUIDATION_CALL= "delivery_liquidation_call";

    /**
     * 邮件消息：期货强平短信
     */
    public static final String FUTURE_LIQUIDATION_SMS= "future_liquidation_sms";

    /**
     * 邮件消息：期货强平短信
     */
    public static final String DELIVERY_LIQUIDATION_SMS= "delivery_liquidation_sms";

    /**
     * 邮件消息：期货强平站内信
     */
    public static final String FUTURE_LIQUIDATION_INBOX= "future_liquidation_inbox";

    /**
     * 邮件消息：期货强平站内信
     */
    public static final String DELIVERY_LIQUIDATION_INBOX= "delivery_liquidation_inbox";

    /**
     * 邮件消息：自动减仓邮件
     */
    public static final String FUTURE_ADL_CALL= "futures_ADL_call";

    /**
     * 邮件消息：自动减仓邮件
     */
    public static final String DELIVERY_ADL_CALL= "delivery_ADL_call";

    /**
     * 邮件消息：自动减仓短信
     */
    public static final String FUTURE_ADL_SMS= "futures_ADL_sms";

    /**
     * 邮件消息：自动减仓短信
     */
    public static final String DELIVERY_ADL_SMS= "delivery_ADL_sms";

    /**
     * 邮件消息：自动减仓站内信
     */
    public static final String FUTURE_ADL_INBOX= "futures_ADL_inbox";

    /**
     * 邮件消息：自动减仓站内信
     */
    public static final String DELIVERY_ADL_INBOX= "delivery_ADL_inbox";

    /**
     * 邮件消息：保证金邮件
     */
    public static final String FUTURE_MARGIN_CALL= "future_margin_call_email";

    /**
     * 邮件消息：逐仓保证金邮件
     */
    public static final String FUTURE_ISOLATED_MARGIN_CALL= "futures_margin_call_isolated_email";

    /**
     * 邮件消息：保证金邮件
     */
    public static final String DELIVERY_MARGIN_CALL= "delivery_margin_call_email";

    /**
     * 邮件消息：逐仓保证金邮件
     */
    public static final String DELIVERY_ISOLATED_MARGIN_CALL= "delivery_margin_call_isolated_email";

    /**
     * 邮件消息：资金费率邮件
     */
    public static final String FUTURE_FUNDING_FEE_NOTIFICATION= "futures_funding_fee_notification";

    /**
     * 短信消息：保证金短信
     */
    public static final String FUTURE_MARGIN_CALL_SMS= "future_margin_call_sms";

    /**
     * 短信消息：逐仓保证金短信
     */
    public static final String FUTURE_ISOLATED_MARGIN_CALL_SMS= "futures_margin_call_isolated_sms";

    /**
     * 短信消息：保证金短信
     */
    public static final String DELIVERY_MARGIN_CALL_SMS= "delivery_margin_call_sms";

    /**
     * 短信消息：逐仓保证金短信
     */
    public static final String DELIVERY_ISOLATED_MARGIN_CALL_SMS= "delivery_margin_call_isolated_sms";

    /**
     * 短信消息：资金费率短信
     */
    public static final String FUTURE_FUNDING_FEE_SMS= "futures_funding_fee_notification_sms";

    /**
     * 站内信消息：保证金站内信
     */
    public static final String FUTURE_MARGIN_CALL_INBOX= "future_margin_call_inbox";

    /**
     * 站内信消息：逐仓保证金站内信
     */
    public static final String FUTURE_ISOLATED_MARGIN_CALL_INBOX= "future_margin_call_isolated_inbox";

    /**
     * 站内信消息：保证金站内信
     */
    public static final String DELIVERY_MARGIN_CALL_INBOX= "delivery_margin_call_inbox";

    /**
     * 站内信消息：逐仓保证金站内信
     */
    public static final String DELIVERY_ISOLATED_MARGIN_CALL_INBOX= "delivery_margin_call_isolated_inbox";

    /**
     * 站内信消息：资金费率站内信
     */
    public static final String FUTURE_FUNDING_FEE_INBOX= "futures_funding_fee";


    /**
     * 邮件消息：用户账号激活邮件V2
     */
    public static final String NODE_TYPE_EMAIL_AUTH2 = "email_auth2";


    /**
     * 邮件消息：新设备授权确认邮件V2
     */
    public static final String NODE_TYPE_DEVICE_AUTHORIZE2 = "email_new_device_authorize2";

    public static final long USER_IS_MARGIN_USER = 2L << 20;// 是否是margin_user 0 不是 1 是


    /**
     * API创建
     */
    public static final String NODE_TYPE_API_CREATE_ENABLE = "api_create_enable";

    public static final String NODE_TYPE_API_WITHDRAW_ENABLE = "api_withdraw_enable";

    /** 交易管理 **/
    public static final String MODEL_TRADE = "trade";

    /*--------------------------- 操作类型 ---------------------------*/
    /** 添加 **/
    public static final String TYPE_ADD = "add";

    /** 添加 **/
    public static final String TYPE_DELETE = "delete";

    /*--------------------------- 执行结果 ---------------------------*/
    /** 成功 **/
    public static final String RESULT_SUCCESS = "success";

    public static final String SYSTEM_MAINTENANCE = "system_maintenance";

    // 子母账户持仓划转
    public static final int SUBUSER_ASSET_TRANSFER = 29;

    // 子母账户future划转
    public static final int FUTURE_INNER_TRANSFER = 68;

    // 子母账户DELIVERY划转
    public static final int DELIVERY_INNER_TRANSFER = 69;

    /**
     * 提币人脸识别缓存锁
     */
    public static final String WITHDRAW_FACE_USER_LOCK = "WITHDRAW_FACE_USER_KEY_LOCK__%s";

    /**
     * 提币人脸识别拒绝正在进行的KYC审核流程的提示语KEY
     */
    public static final String WITHDRAW_FACE_KYC_APPLY_REFUSED = "withdraw.face.kyc.apply.refused";
    //母账户修改子账户邮箱操作类型
    public static final String SECURITY_OPERATE_TYPE_MODIFY_SUBUSER = "modify_subuser";

    /**
     * 用户KYC国籍缓存KEY前缀
     */
    public static final String KYC_COUNTRY_CACHE_PRE = "KYC_COUNTRY_%d";

    /**
     * 重置2FA 下一步校验缓存值
     */
    public static final String RESET_NEXT_STEP_CACHE = "RESET2FA.NEXTSTEP.";
    /**
     * 重置2FA 重发上传邮件
     */
    public static final String RESET_RESEND_EMAIL_CACHE = "ACCOUNT:RESEND:EMIAIL:";

    /**
     * 对于禁用国籍的kyc认证，在审核通过时，发送的通知邮件模版
     */
    public static final String USER_KYC_PASS_FORBID_COUNTRY_EMAIL_TEMPLATE = "user_kcy_pass_forbid_country_email";
    public static final String COMPANY_KYC_PASS_FORBID_COUNTRY_EMAIL_TEMPLATE = "company_kcy_pass_forbid_country_email";

    public static final String ONE_BUTTON_DISABLE_FRONTEND = "one_button_disable_frontend";

    public static final String ONE_BUTTON_DISABLE_BACKEND = "one_button_disable_backend";

    public static final String DISABLE_TRADE_AND_CANCEL_ORDER = "disable_trade_and_cancel_order";

    public static final String UPDATE_WITHDRAWSECURITYSTATUS= "update_withdrawSecurityStatus";

    /**
     * 修改、忘记密码-不必禁用提现
     */
    public static final String NODE_TYPE_RESET_PASSWORD_USABLE = "email_pwd_success_usable";

    /**
     * 消息节点类型: 谷歌验证解绑-不必禁用提现
     */
    public static final String NODE_TYPE_GOOGLE_VERIFY_UNBIND_USABLE = "google_verify_unbind_usable";
    /**
     * 消息节点类型: 手机解绑-不必禁用提现
     */
    public static final String NODE_TYPE_MOBILE_UNBIND_USABLE = "mobile_unbind_usable";
    /**
     * 新版更新用户提币状态
     */
    public static final String UPDATE_WITHDRAWSTATUS_NEW = "update_withdrawStatus_new";


    public static final long USER_IS_FUTURE_USER = 2L << 22;// 是否是future_user 0 不是 1 是
    public static final long USER_IS_EXIST_FUTURE_ACCOUNT = 2L << 23;// 该账户是否拥有future账户 0 不是 1 是


    public static final String KYC_FL_PREFIX ="FL";


    public static final String AUTH_DEVICE_EMAIL_USER_CACHE ="auth_device_email_user";

    public static final String AUTH_DEVICE_EMAIL_USER_LOCK ="auth_device_email_user_lock_";

    public static final String ACTIVE_EMAIL_USER_LOCK ="active_email_user_lock_";

    public static final String MINING_AGENT_CREATE_LOCK ="mining_agent_create_lock_";


    public static final String ENABLE_FAST_WITHDRAW_SWITCH = "enableFastWithdrawSwitch";


    public static final String DISABLE_FAST_WITHDRAW_SWITCH = "disableFastWithdrawSwitch";


    public static final String SEND_MOBILE_VERIFY_CODE_KEY = "account:verify:mobile";

    public static final String ACCOUNT_LOGIN_VERIFY_2FA_KEY = "account:login:verify:2fa";


    public static final String ACCOUNT_RESEND_ACTIVECODE_IP_KEY = "account:resend:activecode:ip";


    public static final String ACCOUNT_FUTURE_SYMBOL_ALL_KEY = "account:future:symbol:all";


    public static final String ACCOUNT_DELIVERY_SYMBOL_ALL_KEY = "account:delivery:symbol:all";



    public static final String FAST_CREATE_FUTURE_ACCOUNT = "fastCreateFutureAccount";


    public static final String FUTURES_REFERAL_CODE = "futuresReferalCode";


    public static final String UPDATE_BROKER_TRANSFER_STATUS= "update_brokerTransferStatus";

    public static final String ONEBUTTONUSER_RESETPSW_SMS_KEY = "oneButtonUser:resetPsw:sms";

    public static final String ONEBUTTONUSER_RESETPSW_SMS_CODE = "onebuttonuser_resetpsw_notify_sms";

    public static final String ONEBUTTONUSER_RESETPSW_EMAIL_CODE = "onebuttonuser_resetpsw_email";


    public static final String ACCOUNT_LOGIN_VERIFY_YUBIKEY_KEY = "account:login:verify:yubikey";







    public static final String USER_KYC_ADDRESS_AUDIT_SUCCESS = "user_kcy_address_audit_success";
    public static final String USER_KYC_ADDRESS_AUDIT_REFUSED = "user_kcy_address_audit_refused";
    public static final String RISK_RATING_WORLDCHECK_REFUSED = "risk_rating_worldcheck_refused";
    public static final String RISK_RATING_REFUSED = "risk_rating_refused";


    /**
     * 通过Broker API修改子账户费率
     */
    public static final String BROKER_API_CHANGE_COMMISSION = "broker_api_change_commission";




    public static final String REGISTER_IP_COUNT_USERID = "REGISTER_IP_COUNT_USERID";
    /**
     * 用户配置-偏好语言
     */
    public static final String USER_CONFIG_PREFER_LANG = "preferLang";
    /**
     * 用户配置-偏好汇率
     */
    public static final String USER_CONFIG_NATIVE_CURRENCY = "nativeCurrency";
    /**
     * 用户配置-上次2fa选验项
     */
    public static final String USER_CONFIG_LAST_2FA_TYPE = "last2FaType";

    /**
     * 用户配置-CME确认标识
     */
    public static final String USER_CONFIG_CME_FLAG = "userCmeConfirmFlag";

    public static final String GLOBAL_RISK_CHECK_WHITE_ADDRESSES = "GLOBAL_RISK_CHECK_WHITE_ADDRESSES";



    //缓存相关的key

    public static final String SEND_EMAIL_VERIFY_CODE_LIMIT_KEY = "account:email:verify:limit";

    public static final String SEND_EMAIL_VERIFY_CODE_KEY = "account:email:verify";


    public static final String SEND_BIND_INFO_KEY = "account:send:bindInfo";


    public static final String BIND_EMAIL_COUNT_USERID = "BIND_EMAIL_COUNT_USERID";


    public static final String ACCOUNT_BROKER_SUBTRANS_BLACK = "account:broker:subtrans:black";


    public static final String CRYPTO_WITHDRAW_ADDRESS_CODE_KEY = "account:capital:withdraw:address";

    public static final String ACCOUNT_FINANCE_FLAG_KEY = "account:financeFlag";

    public static final String ACCOUNT_FINANCE_FLAG_LOCK_KEY = "account_financeFlag_lock_";








    //security type

    public static final String SECURITY_OPERATE_TYPE_BIND_EMAIL = "bind_email";


    public static final String SECURITY_OPERATE_TYPE_UNBIND_EMAIL = "unbind_email";


    public static final String CONFIRMNEWEMAIL_EMAIL_USED_COUNT = "confirmNewEmailV3_email_used_count";



    public static final String SECURITY_OPERATE_TYPE_CHANGE_EMAIL = "change_email";


    public static final String SECURITY_OPERATE_TYPE_CHANGE_MOBILE = "change_mobile";


    /**
     * 邮件消息：创建future账户成功推送
     */
    public static final String CREATE_FUTURE_ACCOUNT_EMAIL = "Future_Operation_Lifecycle_Onboarding_D0";

    /**
     * 极光消息：创建future账户成功推送
     */
    public static final String CREATE_FUTURE_ACCOUNT_APP = "Future_Operation_Lifecycle_Onboarding_D0";


    /**
     * inbox消息：创建future账户成功推送
     */
    public static final String CREATE_FUTURE_ACCOUNT_INBOX = "Future_Operation_Lifecycle_Onboarding_D0";






}
