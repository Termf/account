package com.binance.account.common.enums;

import com.binance.account.common.enums.core.BaseCodeEnum;

/**
 * world check 审核状态： 1.初始化（已创建case） 2.待初审 3.待复核 4.待终审 9.拒绝 10.通过
 * Created by Shining.Cai on 2018/09/10.
 **/
public enum WckStatus implements BaseCodeEnum {

    INITIAL(1, "初始化"),
    AUDIT_FIRST(2, "待初审"),
    AUDIT_SECOND(3, "待二审"),
    AUDIT_THIRD(4, "待三审"),
    REJECTED(9, "拒绝"),
    PASSED(10, "通过"),
    ;

    private int code;
    private String description;


    WckStatus(int i, String s) {
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

    public static WckStatus of(int code){
        for (WckStatus status:WckStatus.values()){
            if (status.code == code){
                return status;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }
}
