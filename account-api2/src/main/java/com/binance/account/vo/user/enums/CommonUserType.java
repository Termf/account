package com.binance.account.vo.user.enums;
/**
 * 账户类型，一共有4种：普通账户，母账户，子账户，借贷账户
 * */
public enum CommonUserType {
    NORMAL,//普通账户
    PARENT,//母账户
    SUB,//子账户
    MARGIN//借贷账户
}
