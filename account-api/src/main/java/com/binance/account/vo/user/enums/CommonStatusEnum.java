package com.binance.account.vo.user.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 用来记录accountid的类型
 */
public enum CommonStatusEnum {
    INIT("I", "初始化"),
    SUCCESS("S", "成功"),
    CANCEL("C", "撤销");
    private String key;
    private String desc;
    CommonStatusEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public static boolean isInit(String key){
        return StringUtils.equals(key,INIT.key);
    }
    public static boolean isSuccess(String key){
        return StringUtils.equals(key,SUCCESS.key);
    }
    public static boolean isCancel(String key){
        return StringUtils.equals(key,CANCEL.key);
    }
}
