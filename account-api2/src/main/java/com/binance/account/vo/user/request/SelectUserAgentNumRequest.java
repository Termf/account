package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/8/22.
 */
@ApiModel("获取用户推荐数量")
@Getter
@Setter
public class SelectUserAgentNumRequest implements Serializable {

    @NotBlank
    private String agentCode;

    private Long startTime;

    private Long endTime;
}
