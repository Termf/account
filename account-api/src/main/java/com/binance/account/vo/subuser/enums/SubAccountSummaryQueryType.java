package com.binance.account.vo.subuser.enums;

public enum SubAccountSummaryQueryType {

    ONLY_PARENT_ACCOUNT("ONLY_PARENT_ACCOUNT", "只查询母账号"),
    ONLY_SUB_ACCOUNT("ONLY_SUB_ACCOUNT", "只查询子账号"),
    QUERY_ALL("QUERY_ALL", "查询母账号加子账号");
    private String queryType;
    private String desc;

    SubAccountSummaryQueryType(String queryType, String desc) {
        this.queryType = queryType;
        this.desc = desc;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
