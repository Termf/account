package com.binance.account.data.entity.subuser;

import lombok.Data;

import java.util.Date;

@Data
public class SubUserBinding {
    private Long subUserId;
    private Long parentUserId;
    private Long brokerSubAccountId;
    private String remark;
    private Date insertTime;
    private Date updateTime;
    private Long marginUserId; // margin账户的userid
    private Long fiatUserId; // 主站法币账户的userid
    private Long futureUserId;//future账户的userid
    private Long miningUserId;// 矿池账户的userid
}