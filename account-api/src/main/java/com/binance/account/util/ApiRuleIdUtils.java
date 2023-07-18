package com.binance.account.util;


public class ApiRuleIdUtils {
    // 开放交易
    public static final long TRADE = 1L;//1L <<0
    // 开放提现
    public static final long WITHDRAW = 2L << 0;//1L <<1
    // 内部划转
    public static final long INTERNAL_TRANSFER = 2L << 1; //1L <<2
    // 内部划转 8
    public static final long MARGIN = 1L << 3; //1L <<3
    // 期货交易权限
    public static final long FUTURE_TRADE = 2L << 3; //1L <<4 == 2L << 3

    public static boolean isTradeEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, TRADE);
    }

    public static boolean isWithdrawEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, WITHDRAW);
    }

    public static boolean isInternalTransferEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, INTERNAL_TRANSFER);
    }

    public static boolean isMarginEnable(Long ruleId) {
        return BitUtils.isEnable(ruleId,MARGIN);
    }

    public static boolean isFutureTradeEnabled(Long ruleId) {
        return BitUtils.isEnable(ruleId, FUTURE_TRADE);
    }
}
