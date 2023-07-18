package com.binance.account.vo.apiagentreward.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Edit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "查询api返佣比例Request", value = "查询api返佣比例Request")
@Data
public class SelectApiAgentRewardRequest extends ToString {

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "api返佣码")
    @NotNull
    private String agentRewardCode;



}
