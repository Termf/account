package com.binance.account.constants.enums;

import com.binance.account.data.entity.user.User;
import com.binance.master.constant.Constant;
import com.binance.master.utils.BitUtils;

/**
 * Created by yangyang on 2019/11/7.
 */
public enum AccountTypeEnum {

    PARENT(1, "母账户"),
    SUB(2, "子账户"),
    MARGIN(3, "margin账户"),
    FUTURE(4, "future账户"),
    FIAT(5, "法币账户"),
    BROKER_PAR(6, "broker母账户"),
    BROKER_SUB(7, "broker子账户"),
    NORMAL(8, "普通账户"),
    ;
    private Integer accountType;
    private String desc;


    AccountTypeEnum(Integer accountType, String desc) {
        this.accountType = accountType;
        this.desc = desc;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * broker与parent、sub可能重合
     * broker账号又是母账户
     * @param status
     * @return
     */
    public static Integer getAccountType(Long status) {
        if (status == null) {
            return -1;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED)) {
            return 1;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_SUBUSER)) {
            return 2;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_MARGIN_USER)) {
            return 3;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FUTURE_USER)) {
            return 4;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FIAT_USER)) {
            return 5;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER_FUNCTION_ENABLED)) {
            return 6;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_BROKER_SUBUSER)) {
            return 7;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUBUSER_FUNCTION_ENABLED)) {
            return 9;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_ASSET_SUBUSER)) {
            return 10;
        }
        return 8;
    }


    /**
     * broker与parent、sub可能重合
     * broker账号又是母账户
     * @param accountType
     * @return
     */
    public static AccountTypeEnum getAccountTypeEnumByAccountType(Integer accountType) {
        if (accountType == null){
            return null;
        }
        AccountTypeEnum[] values = AccountTypeEnum.values();
        for (AccountTypeEnum accountTypeEnum:values){
            if (accountTypeEnum.getAccountType().equals(accountType)){
                return accountTypeEnum;
            }
        }
        return null;
    }
}
