package com.binance.account.vo.apiagentreward.enums;

/**
 * api现货返佣，返佣对象
 * Created by zhao chenkai on 2020/02/28.
 */
public enum ApiAgentRewardToEnum {

    BROKER(1, "推荐人"),
    TRADER(0, "交易人"),
    ;
    private Integer code;
    private String desc;

    ApiAgentRewardToEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
