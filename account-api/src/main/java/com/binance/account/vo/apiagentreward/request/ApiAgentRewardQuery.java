package com.binance.account.vo.apiagentreward.request;

import com.binance.account.common.query.Pagination;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("api返佣比例配置分页查询")
@Data
public class ApiAgentRewardQuery extends Pagination {

    @ApiModelProperty(required = false, notes = "推荐人id")
    private Long agentId;

    @ApiModelProperty(required = false, notes = "api返佣码")
    private String agentRewardCode;

}
