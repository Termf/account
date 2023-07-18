package com.binance.account.vo.apiagentreward.request;

import java.math.BigDecimal;
import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("api返佣比例配置请求")
@Data
public class ApiAgentRewardRequest extends ToString {

    private Long id;

    @ApiModelProperty(required = true, notes = "推荐人id")
    @NotNull
    private Long agentId;

    @ApiModelProperty("返佣码")
    private String agentRewardCode;

    @ApiModelProperty(required = true, notes = "开始时间")
    @NotNull
    private Long startTime;

    @ApiModelProperty(required = true, notes = "新用户返佣比例")
    @NotNull
    private BigDecimal newUserRatio;

    @ApiModelProperty(required = true, notes = "老用户返佣比例")
    @NotNull
    private BigDecimal oldUserRatio;

    @ApiModelProperty(required = true, notes = "返佣给broker或者交易人")
    @NotNull
    private Integer rewardTo;

    @ApiModelProperty(required = false, notes = "操作人")
    private String updateBy;

}
