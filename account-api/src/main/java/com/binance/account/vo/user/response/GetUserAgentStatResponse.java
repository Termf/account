package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangyang on 2019/7/11.
 */
@ApiModel("获取用户返佣所有信息")
@Getter
@Setter
public class GetUserAgentStatResponse implements Serializable {

    private Long id;

    private String agentCode;

    private String promoteUrl;

    private Integer status;

    private Integer peopleNums;

    private String referralRate;

    private String agentRate;

    private String label;

    private Long userId;

    private Integer selectShare;

}
