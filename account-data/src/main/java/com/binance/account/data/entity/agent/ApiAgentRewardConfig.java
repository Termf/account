package com.binance.account.data.entity.agent;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ApiAgentRewardConfig {
    private Long id;

    private Long agentId;

    private String agentRewardCode;

    private Date startTime;

    private BigDecimal newUserRatio;

    private BigDecimal oldUserRatio;

    private Integer rewardTo;

    private Integer del;

    private String updateBy;

    private Date createTime;

    private Date updateTime;

}
