package com.binance.account.vo.user.enums;

/**
 * 用来记录accountid的类型
 */
public enum AccountTypeEnum {
    SPOT("SPOT", "现货账户"),
    MARGIN("MARGIN", "贷款账户"),
    FUTURE("FUTURE", "期货账户"),
    FUTURE_DELIVERY("FUTURE_DELIVERY", "期货交割合约账户"),
    ;
    private String accountType;
    private String desc;


    AccountTypeEnum(String accountType, String desc) {
        this.accountType = accountType;
        this.desc = desc;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
