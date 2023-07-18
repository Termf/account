package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author mikiya.chen
 * @date 2020/3/3 3:25 下午
 */
@Data
public class RiskRatingChangeMonthlyLimitRequest extends ToString {

    private static final long serialVersionUID = 143873964296412177L;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("渠道编码")
    private String channelCode;

    @ApiModelProperty("月限额")
    private String monthlyLimit;
}
