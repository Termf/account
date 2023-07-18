package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by yangyang on 2019/8/19.
 */
@Data
public class QueryBrokerSubAccountRes {



    @ApiModelProperty
    private String subaccountId;

    @ApiModelProperty
    private String makerCommission;

    @ApiModelProperty
    private String takerCommission;

    @ApiModelProperty
    private String marginMakerCommission;

    @ApiModelProperty
    private String marginTakerCommission;

    @ApiModelProperty
    private Long createTime;
}
