package com.binance.account.vo.user.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lufei
 * @date 2018/7/10
 */
@ApiModel(description = "设置用户交易级别是否自动更新Request", value = "设置用户交易级别是否自动更新Request")
@Data
public class SetTradeAutoStatus implements Serializable {

    private static final long serialVersionUID = -6326350590929254734L;

    @ApiModelProperty(required = true, notes = "用戶Id")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "是否自动更新交易级别")
    @NotNull
    private Boolean isAutoUpdate;

}
