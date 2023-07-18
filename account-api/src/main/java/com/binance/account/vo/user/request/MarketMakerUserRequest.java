package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhaochenkai
 */
@ApiModel(description = "做市商账号查询", value = "做市商账号查询")
@Getter
@Setter
public class MarketMakerUserRequest extends ToString {


    private static final long serialVersionUID = 775258338161606861L;

    @ApiModelProperty(required = false, notes = "账号id")
    private Long userId;

    @ApiModelProperty(required = false, notes = "账号邮箱")
    private String email;

}
