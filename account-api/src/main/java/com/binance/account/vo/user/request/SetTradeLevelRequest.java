package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "设置用户交易级别Request", value = "设置用户交易级别Request")
@Data
public class SetTradeLevelRequest implements Serializable {

    private static final long serialVersionUID = -925651337914453306L;

    @ApiModelProperty(required = true, notes = "用戶Id")
    @NotNull
    private Long userId;

    @ApiModelProperty(notes = "交易级别")
    private Integer tradeLevel;

}
