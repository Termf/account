package com.binance.account.common.enums;

import com.binance.account.common.enums.core.BaseCodeEnum;

/**
 * @author mikiya.chen
 * @date 2020/3/3 4:19 下午
 */
public enum WckChannelStatus implements BaseCodeEnum {
    ERROR(0, "调用WCK错误"),
    INITIAL(1, "初始化"),
    AUDIT_FIRST(2, "待初审"),
    AUDIT_SECOND(3, "待二审"),
    REJECTED(9, "拒绝"),
    PASSED(10, "通过"),
    AUTO_PASS(11, "自动通过")
    ;
    private int code;
    private String description;


    WckChannelStatus(int i, String s) {
        this.code = i;
        this.description = s;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public BaseCodeEnum getEnumByCode(int code) {
        return of(code);
    }

    public static WckChannelStatus of(int code){
        for (WckChannelStatus status:WckChannelStatus.values()){
            if (status.code == code){
                return status;
            }
        }
        return null;
    }

    public static Boolean isEndStatus(WckChannelStatus status){
        if(status == WckChannelStatus.REJECTED || status == WckChannelStatus.PASSED || status == WckChannelStatus.AUTO_PASS){
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }
}
