package com.binance.account.vo.user.enums;

/**
 * 注册方式
 */
public enum RegisterationMethodEnum {
    EMAIL("EMAIL", "邮箱注册"),
    MOBILE("MOBILE", "手机号注册");
    private String registerType;
    private String desc;

    RegisterationMethodEnum(String registerType, String desc) {
        this.registerType = registerType;
        this.desc = desc;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
