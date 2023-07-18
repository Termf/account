package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 */
@ApiModel("SelectBrokerCommissionDetailReq")
@Data
public class SelectBrokerCommissionDetailReq {


	@ApiModelProperty(required = true, notes = "brokersubAccountId")
    @NotNull
    private Long subaccountId;

    @ApiModelProperty(required = false, notes = "broker母账户userId")
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "startTime")
    @NotNull
    private Date startTime;

    @ApiModelProperty(required = false, notes = "startTime")
    @NotNull
    private Date endTime;

    @NotNull
    @Min(1)
    private Integer page;

    @NotNull
    @Range(max = 1000)
    private Integer limit = 500;
}