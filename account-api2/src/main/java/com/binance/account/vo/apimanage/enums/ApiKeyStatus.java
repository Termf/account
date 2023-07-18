package com.binance.account.vo.apimanage.enums;

import lombok.Getter;

/**
 * @author Men Huatao (alex.men@binance.com)
 * @date 2020/8/13
 */
public enum ApiKeyStatus {

    ALL(1, "无限制"),
    LIMITED(2, "限定IP访问");

    @Getter
    private final int code;
    @Getter
    private final String description;

    ApiKeyStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
