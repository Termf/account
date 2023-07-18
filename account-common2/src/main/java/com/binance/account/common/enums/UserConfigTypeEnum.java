package com.binance.account.common.enums;

import lombok.Getter;

/**
 * @author Men Huatao (alex.men@binance.com)
 * @date 2020/8/18
 */
public enum UserConfigTypeEnum {
    LVT_SAQ(1, "LVT调查问卷"),
    LVT_ADMIN_OPER(2, "admin修改LVT签署状态"),
    ;

    @Getter
    private final int code;
    @Getter
    private final String desc;

    UserConfigTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
