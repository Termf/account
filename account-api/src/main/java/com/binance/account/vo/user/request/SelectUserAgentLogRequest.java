package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/8/22.
 */
@ApiModel("获取用户推荐记录")
@Getter
@Setter
public class SelectUserAgentLogRequest implements Serializable {

    @NotNull
    private Long userId;

    @NotBlank
    private String agentCode;

    private Long startTime;

    private Long endTime;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Max(100)
    private Integer rows;
}
