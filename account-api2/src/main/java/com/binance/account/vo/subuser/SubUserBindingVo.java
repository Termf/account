package com.binance.account.vo.subuser;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class SubUserBindingVo implements Serializable {
    private static final long serialVersionUID = -7855177520716403376L;
    
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