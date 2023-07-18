package com.binance.account.error;

import com.binance.master.error.ErrorCode;

/**
 * error code Created by caixinning on 2018/11/27.
 */
public enum AccountErrorCode implements ErrorCode {

    COUNTRY_IP_NOT_SPPORT("001098", "Ip country not support"),
    COUNTRY_KYC_NOT_SPPORT("001099", "Kyc country not support"),
    DEVICE_WITHDRAW_NOT_AUTHORIZED("001100", "提现设备未授权"),
    FACE_EMAIL_EXPIRED("001101", "人脸识别邮件过期"),
    FACE_VERIFICATION_DAILY_TIMES("001102", "24小时内人脸识别超出限制次数"),
    FACE_VERIFICATION_MISS_REF_IMAGE("001103", "人脸识别缺失参照照片"),
    FACE_TRANS_STATUS_ERROR("001104", "业务状态已经不在人脸识别认证状态"),
    SUBMIT_ADDRESS_FILE_LARGE("001105", "上传文件超出尺寸限制"),
    SUB_USER_FEATURE_FORBIDDEN("001106", "功能不对子账户开放"),
    USER_NOT_ENABLE_GOOGLE("001107", "用户未开启谷歌验证"),
    USER_NOT_ENABLE_MOBILE("001108", "用户未开启手机验证"),
    USER_NOT_DISABLE("001109", "用户未被禁用"),
    USER_CAN_NOT_ENABLE_IN_2HOUR("001110", "冻结账户2小时之内无法提交解冻申请"),
    RESET_SUBMIT_DAILY_TIMES("001111", "24小时内只能发起3次"),
    RESET_SUBMIT_RECORD_EXIST("001112", "请勿重复申请重置流程"),
    RESET_SEND_EMAIL_MINUTE_TIMES("001113", "重置流程邮件发送频繁"),
    RESET_ANSWER_MANY_TRY_LOCK("001114", "账号已经被锁定"),
    RESET_ANSWER_QUESTION_SEQ_ERROR("001115", "答题顺序不匹配"),
    RESET_QUESTION_VERIFY_FAIL_TIMES("001116", "答题错误次数提示"),
    RESET_QUESTION_VERIFY_FAIL("001117", "安全问题验证失败，2个小时内禁止登录"),
    SUB_USER_TRANSFER_ACCOUNT_SHOULD_BE_DIFFERENT("001118", "转出和转入方不可同一账户"),
    SUB_USER_IS_NOT_EXIST("001119", "该账号不存在子账号"),
    SUB_USER_ILLEGAL_RELATION("001120", "子账户关系非法"),
    MISS_USER_BASE_DETAIL("001121", "用户基本信息缺失"),
    WITHDRAW_SECURITY_BAN("001122", "用户已经被禁止提币"),
    ASSET_IS_NOT_EXIST("001123", "该资产不存在"),
    USER_HAVE_NO_ASSET("001124", "用户没有该资产"),
    USER_HAVE_NO_AVALIABLE_AMOUNT("001125", "用户没有可用余额"),
    USER_AMOUNT_LESS_THAN_ZERO("001126", "用户有负资产"),

    KYC_STATUS_CANNOT_FACE("001127", "当前KYC状态不能做人脸识别"),
    CHECK_FLOW_END_OF_SUCCESS("001128", "业务流程已通过结束"),
    CHECK_FLOW_END_OF_FAIL("001129", "业务流程已失败结束"),
    FACE_TRANS_REVIEW_CANNOT_EMAIL("001130", "人脸识别流程正在审核中不能发送邮件"),
    WITHDRAW_FACE_SWITCH_CLOSED("001131", "提币人脸识别功能开关关闭"),
    WITHDRAW_FACE_OPEN_FAIL_OR_ALREADY_OPEN("001132", "提币人脸识别标识已经是开启状态"),
    WITHDRAW_FACE_FLOW_INIT_FAIL("001133", "提币人脸标识修改成功但人脸识别流程触发异常"),
    WITHDRAW_FACE_REVICE_CANNOT_GENERATE("001134", "提币人脸识别流程正在审核，不能发起新流程"),
    WITHDRAW_FACE_NEED_KYC("001135", "提币人脸识别需要先完成KYC认证"),
    WITHDRAW_FACE_PENDING("001136", "需要完成提币人脸识别认证"),
    WITHDRAW_FACE_WAIT_AUDIT("001137", "提币人脸识别需要等待审核"),
    WITHDRAW_FACE_REFUSED("001138", "提币人脸识别已被管理员拒绝, 需要联系客服"),




    MARGIN_USER_CANNOT_CREATE_MARGIN_ACCOUNT("001139", "不要用margin_userid去创建margin_account"),
    DONT_CREATE_MARGIN_ACCOUNT_AGAIN("001140", "不要重复创建margin_account"),
    ACTIVE_MARGIN_ACCOUNT_FAILED("001141", "激活marginaccount失败"),
    SUB_USER_CANNOT_CREATE_MARGIN_ACCOUNT("001142", "子账户不能创建margin账户"),
    PLEASE_PASS_KYC_BEFORE_CREATE_MARGIN_ACCOUNT("001143", "请您通过kyc认证再创建margin账号"),
    PLEASE_PASS_ZFA_BEFORE_CREATE_MARGIN_ACCOUNT("001144", "请您通过2fa认证再创建margin账号"),
    SYS_NOT_SUPPORT_FOR_MARGIN_USER("001145", "margin账户不能进行此操作"),

    KYC_STATUS_NOT_PASSED("001146", "用户KYC状态未通过"),
	FUTURE_USER_CANNOT_CREATE_FUTURE_ACCOUNT("001146", "不要用future_userid去创建future_account"),
    DONT_CREATE_FUTURE_ACCOUNT_AGAIN("001147", "不要重复创建future_account"),
    ACTIVE_FUTURE_ACCOUNT_FAILED("001148", "激活futureaccount失败"),
    SUB_USER_CANNOT_CREATE_FUTURE_ACCOUNT("001149", "子账户不能创建future账户"),

    YUBIKEY_ALREADY_EXIST_NICKNAME("001150", "当前域名下已经绑定过Yubikey"),
    YUBIKEY_NOT_REGISTER("001151", "用户未绑定Yubikey"),
    YUBIKEY_USER_CREDENTIAL_MISS("001152", "获取不到用户的凭证信息"),

    RESET_ANSWER_TIMES_OUT("001153", "重置流程超时"),
    RESET_PRE_VALIDA_TIMES_OUT("001154", "重置前置检查超时"),
    RESET_PROTECT_MODEL_REFUSED_OLD_DEVICE("001155", "保护模式下不能使用老设备操作"),
    RESET_PROTECT_MODEL_FROZEN("001156", "保护模式下被冻结不能操作"),
    RESET_FLOW_CAN_NOT_DO_QUESTION("001157", "重置流程当前不能获取答题信息"),

    KYC_FORBID_COUNTRY_PASS_STATUS_REFUSED("001158", "不合规国籍认证通过的不能再次做认证"),
    RESET_JUMIO_DAILY_COUNT("001159", "重置Jumio上传次数达限制次数"),

    YUBIKEY_VERIFY_TIMEOUT("001160", "验证超时，请重试"),
    YUBIKEY_NOT_SUPPORTED_IN_THE_ORIGIN("001162", "当前域名不支持yubikey"),
    AT_MOST_ONE_YUBIKEY_PER_USER_PER_ORIGIN("001163", "一个用户一个origin最多绑定一个yubikey"),
    SUBUSER_REGISTER_YUBIKEY_NOT_ENABLED("001165", "子账户不支持绑定yubikey"),
    USER_U2F_FAILED_EXCEED_LIMIT("001166", "U2F失败次数过多，请稍后重试"),

    //用户人工解禁，直接拒绝
    ACCIUNT_UNDER_POTENTIAL_RISK("001167", "由于您的账户存在风险，请提交工单咨询客服处理"),
    MUST_USE_YUBIKEY_TO_AUTHENTICATE("001168", "必须使用yubikey进行验证"),

    // 新KYC认证
    KYC_CERTIFICATE_IN_REVIEW("001170", "认证信息正在审核中"),
    KYC_CERTIFICATE_IN_PASS("001171", "已经验证完成"),

	KYC_CERTIFICATE_NOT_EXISTS("001172", "认证信息不存在"),
    KYC_CERTIFICATE_MOBILE_BIND("001173", "认证信息手机已绑定"),
    KYC_FILL_NOT_EXISTS("001174", "认证基础信息不存在"),
    KYC_CERTIFICATE_BASE_INFO_NOT_PASS("001175","基础信息认证未通过"),
    KYC_BASE_SUBMIT_OUT_COUNT("001176", "基础信息提交态频繁"),
    KYC_ADDRESS_SUBMIT_OUT_COUNT("001177", "地址认证信息提交频繁"),
    KYC_JUMIO_OUT_DAILY_COUNT("001178", "JUMIO申请超单日限制次数"),
    KYC_AUTH_CANT_PROCESS("001179", "当前状态不允许审核"),
    KYC_REGION_STATES_DISABLE("001180","地区洲信息无效"),
    KYC_TAXID_IS_USED("001181","taxId已被占用"),
    KYC_TAX_ID_IN_BLACKLIST("001196", "taxId已被列入黑名单"),
    KYC_ENTER_MOBILE("001182", "请输入你的手机号"),
    KYC_AGE_VARIFY("001183", "年龄过小"),
    KYC_FACE_OCR_SUBMIT_OUT_COUNT("001184", "证件OCR识别提交频繁"),
    KYC_CANNOT_SUBMIT_CURRENT_STATUS("001185", "当前状态下不能再提交"),
    AUTH_FACE_REFERENCE_PROCESSING("001186","当前存在正在进行中的认证流程"),
    KYC_ADDRESS_FORBID_COUNTRY("001187", "地址认证风险国家"),
    KYC_ADDRESS_SUBMIT_KYC_NOT_PASS("001188", "kyc未通过不允许提交地址信息"),
    KYC_NOT_NEED_SUBMIT_KYC_ADDRESS("001189", "用户不需要提交地址认证信息"),

    REMARK_FIELD_EXCEEDS_LIMIT("001190", "remark备注字段超过限制"),
    OAUTH_EXPIRED("001191", "oauth授权已过期"),


	QUESTION_FLOW_UNDO_EXIST("001170", "答题流程存在有效的未答题信息"),
	QUESTION_FLOW_ALLOW_TIMES("001171", "答题流程已经达到最大的答题次数"),
	QUESTION_FLOW_CACHE_EMPTY("001172", "答题流程缓存已经失效"),

    CREATE_BROKER_SUB_ACCOUNT_API_ERROR("001120", "创建broker-subaccount报错"),
    QUERY_BROKER_SUB_ACCOUNT_CONFIG_ERROR("001121", "获取broker母账户基础配置错误"),


    USER_MOBILE_CODE_NOT_EXIST("001173","不支持的手机代码"),
    RESET_USER_RESEND_EMAIL_BUSY("001174", "重发邮件过于频繁，请稍后再试"),
    US_IP_FORBIDDEN_DEFAULT_PROMPT("910001","Based on your current IP address, Binance is unable to service your state."),



    //新返佣
    USER_AGENT_CODE_NOT_EXIST("001180", "推广码不存在"),
    USER_AGENT_RATE_NOT_EXIST("001181", "返佣费率不存在"),
    USER_INSERT_AGENT_RATE_FAIL("001182", "插入返佣失败"),
    USER_AGENT_WITHOUT_PERMISSION("001183", "用户无权限创建返佣"),
    USER_AGENT_RATE_WITHOUT_500BNB("001184", "持仓不足500BNB，无法申请该比例返佣链接"),
    USER_AGENT_RATE_WITH_ERROR_RATE("001185", "无法申请该比例返佣链接"),
    USER_AGENT_LINK_BEYOND_LIMIT("001186", "用户推广链接超出限制"),
    USER_UPDATE_AGENT_AGENTCODE_ERROR("001187", "返佣code不存在"),
    USER_AGENT_LABEL_HAVE_SPECIAL_CHAR("001188", "推广链接的label中有特殊字符"),
    USER_AGENT_RATE_NOT_WITHIN_LIMIT("001189", "返佣比例不能小于20%且不能大于100%"),


    FUTURE_INVITATION_CODE_EMPTY("001190", "推荐码不能为空"),
    FUTURE_INVITATION_CODE_INVALID("001191", "推荐码不存在或者被使用"),


    MARGIN_IP_COUNTRY_NOT_SPPORT("001192", "您的IP国家无法使用杠杆交易"),

    USER_QUERY_AGENT_RATE_FAIL("001193", "查询返佣失败"),

    PLEASE_PASS_ZFA_BEFORE_CREATE_FUTURE_ACCOUNT("001194", "请您通过2fa认证再创建future账号"),

    FUTURE_IP_COUNTRY_NOT_SPPORT("001195", "您的IP国家无法使用期货交易"),

    BIND_CHINA_MOBILE("001200", "请先绑定中国区手机号码"),
    VERIFY_KYC_COUNTRY("001201", "法币交易暂且仅支持中国,越南,俄罗斯用户"),
    DONT_CREATE_FIAT_ACCOUNT_AGAIN("001202", "不要重复创建法币账户"),
    ACTIVE_FIAT_ACCOUNT_FAILED("001203", "激活法币账户失败"),
    SUB_USER_CANNOT_CREATE_FIAT_ACCOUNT("001204", "子账户不能创建法币账户"),
    FIAT_USER_CANNOT_CREATE_FIAT_ACCOUNT("001205", "不要用法币账户去创建"),
    SUB_USER_MARGIN_IS_NOT_EXIST("001206", "子账户的margin账户不存在，不能禁用"),
    SUB_USER_MARGIN_TRADE_STATUS_EXIST("001207", "子账户的margin账户状态已存在"),
    SUB_USER_FUTURE_IS_NOT_EXIST("001208", "子账户的future账户不存在，不能禁用"),
    SUB_USER_FUTURE_TRADE_STATUS_EXIST("001209", "子账户的future账户状态已存在"),
    FUTURE_AGENT_CODE_IS_NOT_EXIST("001210", "future推荐码不存在"),

    MARGIN_ACCOUNT_IS_NOT_EXIST("001211", "margin账号不存在"),
    FUTURE_AGENT_CODE_ERROR("001207", "推荐码须为3-16位数字或字母"),
    FUTURE_AGENT_CODE_EXIST("001208", "此邀请码已被使用"),
    ACCOUNT_TYPE_AS_FUTURE_AGENT_CODE_ERROR("001209", "此操作不被允许"),
    PLEASE_OPEN_MARGIN_ACCOUNT_FIRST("001206", "请先开通margin账户"),
    PLEASE_OPEN_FUTURES_ACCOUNT_FIRST("001207", "请先开通futures账户"),
    AC_RESET_FACE_SDK_QR_TIMEOUT("001029", "二维码过期"),
    CREATE_SUBUSER_MARGIN_OVERLIMIT("001208", "创建子账户margin超过上限"),
    PLEASE_FINISH_KYC_FIRST_BEFORE_OPENNING_MARGIN_ACCOUNT("001230", "请您完成kyc"),

    CANNOT_TRASFER_TO_PARENT_ACCOUNT("001210", "不能向母账号划转"),
    NORMAL_PARENT_TO_ASSET_PARENT_IS_VALID("001211", "资管母账户不能转为broker母账户"),
    CANNOT_GET_REDIS_LOCK("001300", "获取redis锁失败"),
    PLEASE_INPUT_TRUST_IP("001301", "请输入信任 ip"),
    UNCONFIRMED_RESTRICTED_TRANSFER("001301", "您有价值 {0}btc的币因为确认数未到不允许划转 "),
    RISK_RATING_HAS_APPLY("001400","已有申报记录"),
    RISK_RATING_NOT_SUPPORT_COMPANY("001401","不支持企业认证"),
    RISK_RATING_WCK_HAS_REFUSED("001402","WorldCheck已经拒绝"),
    RISK_RATING_CANT_PUSH_RISK("001403","无法提交风控评分"),
    RISK_RATING_CANT_PROCESS("001404","RiskRating无法处理等级变更"),


    FORBIDDEN_BROKER_TRANSFER("001302", "broker账户被禁用划转"),

    USER_NAME_LENGTH_EXCEED("001303", "User name length cannot exceed 20"),


    BROKER_COMMISSION_CONFIG_OUT_OF_RANGE("001303", "{0} 允许配置区间为 {1} ~ {2}"),

    BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_GREATER_THAN("001304", "{0} 大于允许配置的最大值 {1}"),


    BROKER_COMMISSION_CONFIG_OUT_OF_RANGE_LESS_THAN("001305", "{0} 小于允许配置的最小值为 {1}"),






    APIAGENT_CUSTOMERID_IS_EXIST("001231", "customerId已存在"),
    APIAGENT_CUSTOMERID_IS_NOT_EXIST("001232", "customerId不存在"),
    APIAGENT_CODE_IS_NOT_EXIST("001233", "agentCode不存在"),
    API_AGENT_RAWARD_IS_NOT_EXIST("001234", "仅支持返佣推荐码用户"),
    BROKER_SUBACCOUNTID_IS_NOT_EXIST("001235", "SubaccountId不存在"),
    BROKER_AGENTID_IS_NOT_EXIST("001236", "broker推荐人id不存在"),


    ACCOUNT_HAS_BEEN_REGISTERED("001400", "该账户已经被注册"),


    COUNTRY_RESTRICTED("001033", "您尝试访问的服务目前在您所在的地区无法使用。"),
    BROKER_TAKER_COMMISSION_ERROR("001034", "broker现货margin的TakerCommisssion不能小于0.1%或者大于0.2%"),
    BROKER_MAKER_COMMISSION_ERROR("001035", "broker现货margin的MakerCommisssion不能小于0.1%或者大于0.2%"),
    BROKER_FUTURES_TAKER_COMMISSION_ERROR("001037", "broker合约Maker手续费调整值不能小于0或者大于400"),
    BROKER_FUTURES_MAKER_COMMISSION_ERROR("001036", "broker合约Maker手续费调整值不能小于0或者大于200"),
    BROKER_MARGIN_TAKER_COMMISSION_ERROR("001034", "broker-margin的TakerCommisssion不能小于0.1%或者大于0.2%"),
    BROKER_MARGIN_MAKER_COMMISSION_ERROR("001035", "broker-margin的MakerCommisssion不能小于0.1%或者大于0.2%"),
    BROKER_COMMISSION_ERROR_SET_ERROR("001035", "broker费率配置最小值不能大于最大值"),

    PLEASE_OPEN_MARGIN_ACCOUNT_BEFORE_CREATE_ISOLATED_MARGIN("001401", "请先开通全仓margin账户才能创建逐仓margin"),


    DONT_CREATE_ISOLATED_MARGIN_ACCOUNT_AGAIN("0011402", "不要重复创建isolated_margin_account, symbol={0}"),
    CARD_ACOUNT_CANNOT_CREATE_CARD_ACCOUNT("0011403", "card账户不能创建card账户"),
    SUB_ACOUNT_CANNOT_CREATE_CARD_ACCOUNT("0011404", "子账户不能创建card账户"),


    USER_EMAIL_VERIFY_CODE_EXPIRED("001410", "用户邮箱验证码已经过期"),
    USER_EMAIL_VERIFY_TIME_LIMIT("001411", "用户邮箱验证超过最大限制次数，请稍后重新获取"),
    USER_EMAIL_VERIFY_CODE_ERROR("001412", "用户邮箱验证码错误"),
    USER_EMAIL_VERIFY_CODE_LOSS("001413", "用户邮箱验证码缺失"),
    USER_EMAIL_NOT_BIND("001414", "用户未绑定邮箱"),
    USER_GOOGLE_NOT_BIND("001415", "用户未绑定谷歌"),
    USER_MOBILE_NOT_BIND("001416", "用户未绑定手机"),
    USER_GOOGLE_VERIFY_CODE_LOSS("001417", "用户谷歌验证码缺失"),

    PLEASE_AUTHORIZE_MOBILE_LOGIN("001418", "请授权手机号登录"),
    USER_EMAIL_ALREADY_BIND("001419", "已经绑定了邮箱"),
    USER_GOOGLE_ALREADY_BIND("001420", "已经绑定了谷歌"),

    USER_MOBILE_NOT_CORRECT("024072", "手机号输入不正确，请重新输入"),

    USER_PWD_ERROR("200001004", "password error, you have {0} tries left."),

    CLIENT_VERSION_IS_TOO_LOW("001421", "客户端版本过低，请升级到最新版本,或者使用网页版登录"),


    MOBILE_USER_IS_NOT_SUPPORT_FOR_LOGIN("001422", "APP端暂不支持手机号用户登录"),


    PLEASE_GO_TO_WEBSIDE_FOR_OPERATION("001423", "功能升级，请前往web端操作,我们会在下个版本支持"),


    ACCOUNT_OR_PASSWORD_ERROR("001424", "账号或密码错误"),


    USER_YUBIKEY_VERIFY_CODE_LOSS("001425", "用户yubikey验证码缺失"),


    BROKER_TRANSFER_HINT_DECISION_RULE("001426", "broker划转命中风控"),

    FORBIDDEN_BROKER_FUTURE_TRANSFER("001430", "broker的future账户被禁用划转"),

    BROKER_FUTURE_ACCOUNT_NOT_EXIST("001431", "broker的future账户不存在"),
    PLEASE_VERFIY_YUBIKEY_FIRST("001427", "请先验证yubikey"),


    BROKER_DAY_WITHDRAW_LIMIT_ERROR("001050", "broker日提现额度设置错误"),


    SUB_ACCOUNT_ACCOUNT_NOT_EXIST("001431", "账户的future账户不存在"),

    YUBIKEY_USER_IS_SUPPORT_FOR_LOGIN_ON_BROWSER("001427", "yubikey用户只能在浏览器登录"),
    YUBIKEY_EXCEED_REGISTER_LIMIT_PER_USER("001428", "该用户已经达到绑定个数上限"),
    YUBIKEY_DUPLCATE_NICKNAME("001429", "Yubikey Nickname 重复"),

    BROKER_DELIVERY_TAKER_COMMISSION_ERROR("001428", "broker交割合约Taker手续费调整值不能小于0或者大于400"),
    BROKER_DELIVERY_MAKER_COMMISSION_ERROR("001429", "broker交割合约Maker手续费调整值不能小于0或者大于150"),

    USER_HAS_ETF_ASSET_ERROR("001429", "用户有杠杆代币资产，不能禁止其LVT协议"),

    NOT_ALLOW_SIGNLVT_ERROR("001430", "用户被admin限制lvt"),





    ;
	private String code;
	private String message;

	AccountErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
