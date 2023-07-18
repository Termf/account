package com.binance.account.common.enums;

public enum JumioType {
    /**
     * 个人认证
     */
    user,

    /**
     * 企业认证
     */
    company,

    /**
     * 重置GOOGLE
     */
    google,

    /**
     * 重置手机
     */
    mobile,

    /**
     * 解禁账户
     */
    enable,

    /**
     * 老照片处理
     */
    remediation;

    public static JumioType getByName(String name) {
        if (name == null) {
            return null;
        }
        JumioType[] types = JumioType.values();
        for (JumioType type : types) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
