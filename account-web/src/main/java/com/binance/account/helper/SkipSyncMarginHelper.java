package com.binance.account.helper;



public class SkipSyncMarginHelper {

    private static final ThreadLocal<Boolean> skipSyncMarginFlag = ThreadLocal.withInitial(() -> null);

    public static Boolean get(){
        return skipSyncMarginFlag.get();
    }

    public static void set(Boolean flag){
        skipSyncMarginFlag.set(flag);
    }

    public static void reset(){
        skipSyncMarginFlag.set(null);
    }
}
