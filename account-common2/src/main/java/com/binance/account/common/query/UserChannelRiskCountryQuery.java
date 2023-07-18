package com.binance.account.common.query;

import com.binance.master.commons.Pagination;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserChannelRiskCountryQuery extends Pagination {

    @ApiModelProperty("country code")
    private String countryCode;

    @ApiModelProperty("channel code")
    private String channelCode;
}
