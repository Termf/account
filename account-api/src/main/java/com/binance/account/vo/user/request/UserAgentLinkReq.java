package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/8/15.
 */
@ApiModel("获取创建的链接信息")
@Getter
@Setter
public class UserAgentLinkReq implements Serializable {

    @ApiModelProperty(required = true, notes = "推荐人UserId")
    private Long userId;

    @ApiModelProperty(required = true, notes = "页码")
    @NotNull
    @Min(1)
    private Integer page;

    @ApiModelProperty(required = true, notes = "行数")
    @NotNull
    @Max(100)
    private Integer rows;

    @ApiModelProperty(required = true, notes = "推荐码")
    private String agentCode;
}
