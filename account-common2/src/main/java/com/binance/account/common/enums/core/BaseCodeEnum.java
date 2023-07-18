package com.binance.account.common.enums.core;

/**
 * Created by Shining.Cai on 2018/09/10.
 **/
public interface BaseCodeEnum {


    /**
     * 枚举值code
     */
    int getCode();

    /**
     * 通过code，匹配枚举值，若没有匹配的，则返回null
     */
    BaseCodeEnum getEnumByCode(int code);
}
