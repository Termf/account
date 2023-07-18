package com.binance.account.common.enums;

import com.binance.master.utils.StringUtils;

public enum UserRiskRatingStatus {

    ENABLE,
    DISABLE,
    FORBID,
    REVIEW;

    public static UserRiskRatingStatus of(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (UserRiskRatingStatus value : UserRiskRatingStatus.values()) {
            if (StringUtils.equalsIgnoreCase(value.name(), name)) {
                return value;
            }
        }
        return null;
    }
}
