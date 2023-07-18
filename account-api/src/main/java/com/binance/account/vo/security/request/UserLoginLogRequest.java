package com.binance.account.vo.security.request;

import com.binance.master.commons.Page;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

public class UserLoginLogRequest extends Page implements Serializable {


    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("请求时间（查询范围开始）")
    private Date requestTimeFrom;

    @ApiModelProperty("请求时间（查询范围结束）")
    private Date requestTimeTo;


}
