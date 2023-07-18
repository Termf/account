package com.binance.account.constants.enums;

/**
 * 用来记录撮合用户的限制模式的类型
 */
public enum MatchBoxAccountRestrictionModeEnum {
    BLACK_LIST("BLACK_LIST", "All symbols are trade-able except the ones in the restriction list"),
    WHITE_LIST("WHITE_LIST", "Only symbols in the restriction list are trade-able"),
    SINGLE("SINGLE", "Only the single symbol in the restriction list is trade-able")

    ;
    private String restrictionMode;
    private String desc;


    MatchBoxAccountRestrictionModeEnum(String restrictionMode, String desc) {
        this.restrictionMode = restrictionMode;
        this.desc = desc;
    }

    public String getRestrictionMode() {
        return restrictionMode;
    }

    public void setRestrictionMode(String restrictionMode) {
        this.restrictionMode = restrictionMode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
