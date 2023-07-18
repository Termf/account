package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("查询单条用户行为日志的Request")
@Data
public class QueryDetailWithUuidRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("uuid")
    @NotNull
    private String uuid;

}
