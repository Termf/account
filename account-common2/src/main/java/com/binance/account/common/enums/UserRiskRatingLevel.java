package com.binance.account.common.enums;

import com.binance.master.utils.StringUtils;

public enum UserRiskRatingLevel {

    EXTREME,
    HIGH,
    MEDIUM,
    LOW,
    LOWER;

    public static UserRiskRatingLevel of(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (UserRiskRatingLevel value : UserRiskRatingLevel.values()) {
            if (StringUtils.equalsIgnoreCase(value.name(), name)) {
                return value;
            }
        }
        return null;
    }
}
