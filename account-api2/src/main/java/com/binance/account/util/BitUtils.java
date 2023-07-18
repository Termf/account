package com.binance.account.util;

public class BitUtils {
    public BitUtils() {
    }

    public static boolean isTrue(Long status, Long code) {
        return (status & code) != 0L;
    }

    public static boolean isEnable(Long status, Long code) {
        return (status & code) == code;
    }

    public static boolean isFalse(Long status, Long code) {
        return !isTrue(status, code);
    }

    public static Long enable(Long status, Long code) {
        return status | code;
    }

    public static Long disable(Long status, Long code) {
        return status & ~code;
    }
}
