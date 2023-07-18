package com.binance.account.vo.futures.enums;

/**
 * 用来记录期货发送的邮件类型
 */
public enum FutureEmailTypeEnum {
    LIQUIDATION("LIQUIDATION", "强制平仓邮件"),
    ADL("ADL", "自动减仓邮件"),
    DELIVERY_LIQUIDATION("DELIVERY_LIQUIDATION", "交割强制平仓邮件"),
    DELIVERY_ADL("DELIVERY_ADL", "交割自动减仓邮件");
    private String futureEmailType;
    private String desc;

    FutureEmailTypeEnum(String futureEmailType, String desc) {
        this.futureEmailType = futureEmailType;
        this.desc = desc;
    }

    public String getFutureEmailType() {
        return futureEmailType;
    }

    public void setFutureEmailType(String futureEmailType) {
        this.futureEmailType = futureEmailType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
