package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
public class CountLoginRequest implements Serializable {

    private static final long serialVersionUID = 38742843297740103L;


    @ApiModelProperty("查询范围开始的时间戳")
    @NotNull
    private Date startTime;

    @ApiModelProperty("查询范围结束的时间戳")
    @NotNull
    private Date endTime;
}
