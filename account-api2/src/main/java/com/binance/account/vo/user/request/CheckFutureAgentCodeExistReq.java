package com.binance.account.vo.user.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author yangyang
 * @date 2019/7/10
 */
@ApiModel("创建future推荐人返佣code信息")
@Getter
@Setter
public class CheckFutureAgentCodeExistReq implements Serializable{

    @ApiModelProperty(required = true, notes = "用户UserId")
    private Long userId;

    @ApiModelProperty(required = true, notes = "用户FutureUserId")
    private Long futureUserId;

    @ApiModelProperty(required = true, notes = "用户自定义推荐码agentCode")
    private String futureAgentCode;

}
