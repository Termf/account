package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 */
@ApiModel("SelectApiAgentCommissionDetailReq")
@Data
public class SelectApiAgentCommissionDetailReq {


	@ApiModelProperty(required = true, notes = "推荐人id")
    @NotNull
    private Long agentId;

    @ApiModelProperty(required = false, notes = "三方备注id")
    private String customerId;

    @ApiModelProperty(required = false, notes = "startTime")
    @NotNull
    private Date startTime;

    @ApiModelProperty(required = false, notes = "startTime")
    @NotNull
    private Date endTime;

    @NotNull
    @Range(max = 1000)
    private Integer limit = 500;
}