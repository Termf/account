package com.binance.account.common.enums;

public enum OrderConfirmType {
    limitOrder(1L, "限价单，默认关闭"),
    marketOrder(1L << 1, "市价单，默认关闭"),
    stopLossOrder(1L << 2, "止损单，默认开启"),
    marginAutoBorrow(1L << 3, "margin借入，默认开启"),
    marginAutoRepay(1L << 4, "margin偿付，默认开启"),
    oco(1L << 5, "oco，默认关闭"),
    ;

    private long bitVal;

    private String desc;

    OrderConfirmType(long bitVal, String desc) {
        this.bitVal = bitVal;
        this.desc = desc;
    }

    public long getBitVal() {
        return bitVal;
    }

    public String getDesc() {
        return desc;
    }
}
