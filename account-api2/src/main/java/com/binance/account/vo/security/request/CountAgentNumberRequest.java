package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author pengchenxue
 */
@Getter
@Setter
@NoArgsConstructor
public class CountAgentNumberRequest implements Serializable{
    private static final long serialVersionUID = -5958375107679833518L;

    @ApiModelProperty(required = true, notes = "推荐人")
    @NotNull
    private Long agentId;
}