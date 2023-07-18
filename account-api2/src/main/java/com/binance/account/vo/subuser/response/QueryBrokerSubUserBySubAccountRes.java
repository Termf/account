package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by yangyang on 2019/8/19.
 */
@Data
public class QueryBrokerSubUserBySubAccountRes {



    @ApiModelProperty
    private Long subaccountId;

    @ApiModelProperty
    private Long parentUserId;

    @ApiModelProperty
    private Long subUserId;

    @ApiModelProperty
    private Long brokerAgentId;

    /**
     * 0非1币安云broker
     */
    @ApiModelProperty
    private Integer source;
}
