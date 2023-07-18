package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RiskRatingChangeStatusRequest extends ToString {

    private static final long serialVersionUID = 143873964296412172L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("渠道编码")
    private String channelCode;

    @ApiModelProperty("状态 ENABLE/DISABLE")
    private String status;

    @ApiModelProperty("错误原因")
    private String failReason;
}
