package com.binance.account.constants.enums;

import com.binance.account.constant.AccountCommonConstant;
import com.binance.master.constant.Constant;
import com.binance.master.utils.BitUtils;

/**用户维度区分账号
 * Created by yangyang on 2019/11/7.
 */
public enum UserTypeEnum {

    MARGIN(1, "margin账户"),
    FUTURE(2, "future账户"),
    FIAT(3, "法币账户"),
    NORMAL(4, "普通账户"),
    MINING(5, "矿池账户"),
    CARD(6,"card账户"),
    ISOLATED_MARGIN(7,"逐仓margin账户")
    ;
    private Integer accountType;
    private String desc;


    UserTypeEnum(Integer accountType, String desc) {
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
            return 0;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_MARGIN_USER)) {
            return 1;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FUTURE_USER)) {
            return 2;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FIAT_USER)) {
            return 3;
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_MINING_USER)) {
            return 5;
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_CARD_USER)) {
            return 6;
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER)) {
            return 7;
        }
        return 4;
    }

    /**
     * broker与parent、sub可能重合
     * broker账号又是母账户
     * @param status
     * @return
     */
    public static String getAccountTypeName(Long status) {
        if (status == null) {
            return null;
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_MARGIN_USER)) {
            return MARGIN.name();
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FUTURE_USER)) {
            return FUTURE.name();
        }
        if (BitUtils.isEnable(status, Constant.USER_IS_FIAT_USER)) {
            return FIAT.name();
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_MINING_USER)) {
            return MINING.name();
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_CARD_USER)) {
            return CARD.name();
        }
        if (BitUtils.isEnable(status, AccountCommonConstant.USER_IS_ISOLATED_MARGIN_USER)) {
            return ISOLATED_MARGIN.name();
        }
        return NORMAL.name();
    }
}
