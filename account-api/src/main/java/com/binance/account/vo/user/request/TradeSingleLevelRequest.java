package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lufei
 * @date 2018/11/20
 */
@Data
@ApiModel("交易等级请求查询")
public class TradeSingleLevelRequest extends ToString {

    private static final long serialVersionUID = -2274354373795259523L;

    @ApiModelProperty("交易等级")
    @NotNull
    private Integer level;

}
