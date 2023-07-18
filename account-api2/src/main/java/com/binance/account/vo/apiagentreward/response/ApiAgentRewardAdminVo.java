package com.binance.account.vo.apiagentreward.response;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("api返佣比例配置")
@Data
public class ApiAgentRewardAdminVo implements Serializable {

    private static final long serialVersionUID = -5693752910479958445L;

    private Long id;

    private Long agentId;

    private String agentRewardCode;

    private Long startTime;

    private BigDecimal newUserRatio;

    private BigDecimal oldUserRatio;

    private Integer rewardTo;

    private String updateBy;

    private Long createTime;

    private Long updateTime;
}
