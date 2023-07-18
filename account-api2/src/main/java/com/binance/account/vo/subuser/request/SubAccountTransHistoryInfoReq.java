package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by zhao chenkai on 2019/10/24.
 */
@ApiModel("查询子母账户划转历史request")
@Data
public class SubAccountTransHistoryInfoReq extends ToString {

    private static final long serialVersionUID = -5768977367845906702L;

    @ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty("子账号id")
    private Long userId;

    @ApiModelProperty("划转方(to:划入方;from:划出方;默认查所有)")
    private String transfers;

    private Long startTime;

    private Long endTime;

    private Integer page;

    private Integer limit;
}
