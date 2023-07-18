package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("")
@Data
public class SaveJumioSdkScanRefRequest extends ToString {
    private static final long serialVersionUID = -3337038558859278015L;

    @ApiModelProperty("对应的是inspector的JumioHandlerType 的code")
    private String type;

    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("业务标识ID")
    private String bizId;

    @ApiModelProperty("自定义的jumio 初始化流水号")
    private String merchantRef;

    @ApiModelProperty("需要变更的JUMIO SCAN_REFERENCE")
    private String scanRef;
}
