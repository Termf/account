package com.binance.account.vo.margin.response;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by pcx
 */
@Data
public class GetIsolatedMarginUserListResp {

    @ApiModelProperty("该主账号下逐仓margin账号数目")
    private Long total=0L;

    @ApiModelProperty("逐仓margin账号userIdList")
    private List<Long> isolatedMarginUserIdList= Lists.newArrayList();
}