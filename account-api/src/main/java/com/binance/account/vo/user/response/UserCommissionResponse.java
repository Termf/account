package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lufei
 * @date 2018/11/29
 */
@ApiModel("用户手续费")
@Data
public class UserCommissionResponse extends ToString {

    private static final long serialVersionUID = -4597264522597644528L;

    @ApiModelProperty(name = "用户ID")
    private Long userId;

    @ApiModelProperty(name = "被动方手续费")
    private BigDecimal makerCommission;

    @ApiModelProperty(name = "主动方手续费")
    private BigDecimal takerCommission;

    @ApiModelProperty(name = "买方交易手续费")
    private BigDecimal buyerCommission;

    @ApiModelProperty(name = "卖方交易手续费")
    private BigDecimal sellerCommission;
}
