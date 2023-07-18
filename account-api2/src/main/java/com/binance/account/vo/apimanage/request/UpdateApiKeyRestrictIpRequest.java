package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Men Huatao (alex.men@binance.com)
 * @date 2020/8/13
 */
@ApiModel
@Getter
@Setter
public class UpdateApiKeyRestrictIpRequest extends ToString {
    @ApiModelProperty("用户id")
    private String userId;
    @ApiModelProperty("生成的api key")
    private String apiKey;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("ip限制 1-不限制 2-限制指定ip")
    private int status;
}
