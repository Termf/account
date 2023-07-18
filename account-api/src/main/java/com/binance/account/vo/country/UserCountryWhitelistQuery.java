package com.binance.account.vo.country;


import com.binance.account.vo.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("查询白名单列表")
public class UserCountryWhitelistQuery extends Page {

    @ApiModelProperty("user.id")
    private Long userId;
}