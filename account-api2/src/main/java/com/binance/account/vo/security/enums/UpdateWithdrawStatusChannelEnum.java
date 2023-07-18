package com.binance.account.vo.security.enums;


/**
 * Created by yangyang on 2019/11/7.
 */
public enum UpdateWithdrawStatusChannelEnum {

    MARGIN(1, "margin"),
    FUTURE(2, "future"),
    CAPITAL(3, "capital"),
    RISK(4, "risk-manager"),
    PNK_ADMIN(5,"pnk-admin"),
    ACCOUNT(6,"account"),
    OTHER(7,"other")
    ;
    private Integer num;
    private String desc;


    UpdateWithdrawStatusChannelEnum(Integer num, String desc) {
        this.num = num;
        this.desc = desc;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
