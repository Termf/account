package com.binance.account.vo.apiagentreward.response;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "查询用户是否满足返佣条件Response", value = "查询用户是否满足返佣条件Response")
@Data
public class IfNewUserResponse extends ToString {

    @ApiModelProperty("api返佣码")
    private String apiAgentCode;

    @ApiModelProperty("对该推荐码是否满足API反佣条件")
    private Boolean rebateWorking;

    @ApiModelProperty("对该推荐码是否为新客 true:新客  false:老客")
    private Boolean ifNewUser;

}
