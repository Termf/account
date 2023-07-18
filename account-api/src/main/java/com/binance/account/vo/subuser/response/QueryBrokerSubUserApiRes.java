package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by yangyang on 2019/8/19.
 */
@Data
public class QueryBrokerSubUserApiRes {

    @ApiModelProperty
    private String apiKey;

    @ApiModelProperty
    private String subaccountId;

    @ApiModelProperty
    private Boolean canTrade;

    @ApiModelProperty
    private Boolean marginTrade;

    @ApiModelProperty
    private Boolean futuresTrade;
}
