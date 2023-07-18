package com.binance.account.vo.subuser.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Created by yangyang on 2019/5/6.
 */
@ApiModel("母账户查询子账户列表Request")
@Data
public class SubUserSearchReq {
    @NotNull
    private Long parentUserId;
    @NotNull
    private String email;
    private String status;
    private Integer page;
    private Integer limit;

}
