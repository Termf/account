package com.binance.account.common.enums;

/**
 * 是否预计恢复时间
 * Created by mengjuan on 2018/9/28.
 */
public enum RestoreEnum {
    N("n", "否"),
    Y("y", "是");

    private String code;
    private String desc;

    private RestoreEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}