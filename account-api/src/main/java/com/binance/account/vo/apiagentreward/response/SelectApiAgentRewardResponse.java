package com.binance.account.vo.apiagentreward.response;

import java.math.BigDecimal;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "查询api返佣比例Response", value = "查询api返佣比例Response")
@Data
public class SelectApiAgentRewardResponse extends ToString {

    @ApiModelProperty("推荐人userId")
    private Long agentId;

    @ApiModelProperty("返佣比例")
    private BigDecimal agentRewardRatio;

}
