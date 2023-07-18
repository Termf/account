package com.binance.account.common.enums;

public enum SecurityKeyApplicationScenario {

    withdrawAndApi(1L, "提现和API"),
    resetPassword(1L << 1, "重置密码"),
    login(1L << 2, "登录"),
    ;

    private long bitVal;

    private String desc;

    SecurityKeyApplicationScenario(long bitVal, String desc) {
        this.bitVal = bitVal;
        this.desc = desc;
    }

    public long bitVal() {
        return bitVal;
    }

    public static long allOn() {
        long result = 0L;
        for (SecurityKeyApplicationScenario scenario : values()) {
            result |= scenario.bitVal;
        }
        return result;

    }
}
