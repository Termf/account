package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel(description = "修改用户返佣比例Request", value = "修改用户返佣比例Request")
@Data
public class UpdateAgentRewardRatioRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6670690110205676200L;

    @NotNull
    private Long userId;

    @NotNull
    private BigDecimal agentRewardRatio;
}
