package com.binance.account.vo.tag.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户状态和用户ID")
public class StatusAndUserIdVo {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户邮箱")
    private Long status;

}
