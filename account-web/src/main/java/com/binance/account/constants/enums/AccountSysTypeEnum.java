package com.binance.account.constants.enums;

public enum AccountSysTypeEnum {

    PNK_WEB("pnk_web", "前台"),
    PNK_ADMIN("pnk_admin", "后台"),
    EXCHANGE_ADMIN("exchange_admin", "exchange-admin"),
    RISK("risk_exception_action_exchange", "risk"),
    FRONT_GROUP("front_group", "前台组"),
    ;
    private String code;

    private String desc;

    AccountSysTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
