package com.binance.account.vo.user.request;

import javax.validation.constraints.NotNull;

import com.binance.account.vo.user.TradeLevelVo;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lufei
 * @date 2018/11/16
 */
@Data
@ApiModel("交易等级请求")
public class TradeLevelRequest extends ToString {

    private static final long serialVersionUID = -7406073027181465596L;

    @ApiModelProperty("交易等级详情")
    @NotNull
    private TradeLevelVo vo;
}
