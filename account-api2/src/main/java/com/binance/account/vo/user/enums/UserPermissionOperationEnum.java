package com.binance.account.vo.user.enums;

public enum UserPermissionOperationEnum {

    ENABLE_DEPOSIT("ENABLE_DEPOSIT", "是否可以充值(0:禁止;1:允许)"),
    ENABLE_WITHDRAW("ENABLE_WITHDRAW", "是否可以提币(0:禁止;1:允许)"),
    ENABLE_TRADE("ENABLE_TRADE", "是否可以交易(0:禁止;1:允许)"),
    ENABLE_TRANSFER("ENABLE_TRANSFER", "是否可以划转(0:禁止;1:允许)"),
    ENABLE_SUB_TRANSFER("ENABLE_SUB_TRANSFER", "是否可以子母账户划(0:禁止;1:允许)"),
    ENABLE_CREATE_APIKEY("ENABLE_CREATE_APIKEY", "是否可以创建apikey(0:禁止;1:允许)"),
    ENABLE_LOGIN("ENABLE_LOGIN", "是否可以登录(0:禁止;1:允许)"),
    ENABLE_CREATE_MARGIN("ENABLE_CREATE_MARGIN", "是否可以创建margin账号(0:禁止;1:允许)"),
    ENABLE_CREATE_FUTURES("ENABLE_CREATE_FUTURES", "是否可以创建futures账号(0:禁止;1:允许)"),
    ENABLE_CREATE_FIAT("ENABLE_CREATE_FIAT", "是否可以创建法币账号(0:禁止;1:允许)"),
    ENABLE_CREATE_ISOLATED_MARGIN("ENABLE_CREATE_ISOLATED_MARGIN", "否可以创建逐仓margin账号(0:禁止;1:允许)"),
    ENABLE_CREATE_SUB_ACCOUNT("ENABLE_CREATE_SUB_ACCOUNT", "是否可以创建子账号(0:禁止;1:允许)"),
    ENABLE_PARENT_ACCOUNT("ENABLE_PARENT_ACCOUNT", "是否可以成为母账号(0:禁止;1:允许)"),
    ENABLE_BROKER_PARENT_ACCOUNT("ENABLE_BROKER_PARENT_ACCOUNT", "是否可以成为broker母账号(0:禁止;1:允许)"),
    ENABLE_CREATE_BROKER_SUB_ACCOUNT("ENABLE_CREATE_BROKER_SUB_ACCOUNT", "是否可以创建broker子账号(0:禁止;1:允许)");


    private String operation;
    private String desc;

    UserPermissionOperationEnum(String operation, String desc) {
        this.operation = operation;
        this.desc = desc;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
