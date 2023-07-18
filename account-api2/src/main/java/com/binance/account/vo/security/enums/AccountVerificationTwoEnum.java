package com.binance.account.vo.security.enums;

import com.binance.master.enums.BaseEnum;

public enum AccountVerificationTwoEnum implements BaseEnum {

    GOOGLE("google", "谷歌"),
    SMS("sms", "短信"),
    YUBIKEY("yubikey", "yubikey"),
    EMAIL("email", "邮件");

    private String code;
    private String desc;

    AccountVerificationTwoEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

}
