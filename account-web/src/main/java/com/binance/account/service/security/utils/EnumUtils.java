package com.binance.account.service.security.utils;

import com.binance.master.enums.BaseEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 4:12 下午
 */
public class EnumUtils {
    public static <T extends BaseEnum> T getByCode(Class<T> clazz, String code) {
        if(StringUtils.isBlank(code)) {
            return null;
        }

        for(T each : clazz.getEnumConstants()) {
            if (Objects.equals(each.getCode(), code)) {
                return each;
            }
        }

        return null;
    }
}
