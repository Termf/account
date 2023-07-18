package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by yangyang on 2019/7/10.
 */
@ApiModel(description = "返佣查询返回", value = "返佣查询返回Response")
@Getter
@Setter
public class UserAgentRateResponse extends ToString {

    private static final long serialVersionUID = -131985148324570242L;

    private Long id;

    private Long userId;

    private Integer agentLevel;

    private BigDecimal referralRate;
}
