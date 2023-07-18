package com.binance.account.constants.enums;

/**
 * 用来记录撮合用户的类型
 */
public enum MatchBoxAccountTypeEnum {
    SPOT("SPOT", "现货账户"),
    MARGIN("MARGIN", "贷款账户"),
    C2C("C2C", "C2C法币账户"),
    ISOLATED_MARGIN("ISOLATED_MARGIN", "逐仓margin账户"),


    ;
    // C2C("C2C", "主站法币账户");
    private String accountType;
    private String desc;


    MatchBoxAccountTypeEnum(String accountType, String desc) {
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
