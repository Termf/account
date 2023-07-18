package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by yangyang on 2019/5/6.
 */
@ApiModel("查询子母账户划转历史request")
@Data
public class SubAccountTransHisReq {

    @NotNull
    private Long parentUserId;
    private String email;
    private Long startTime;
    private Long endTime;
    private Integer page;
    private Integer limit;
}
