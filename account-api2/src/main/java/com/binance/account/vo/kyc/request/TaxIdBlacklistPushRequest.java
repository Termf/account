package com.binance.account.vo.kyc.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 把TaxId列入黑名单请求参数
 * @author Zhang Xianhe
 */
@ApiModel("把TaxId列入黑名单请求参数")
@Data
public class TaxIdBlacklistPushRequest {

    @ApiModelProperty("taxId")
    private String taxId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("操作者")
    private String creator;
}
