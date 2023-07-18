package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "新增做市商账号request", value = "新增做市商账号request")
@Getter
@Setter
public class AddMarketMakerUserRequest extends ToString {

    @ApiModelProperty(required = false, notes = "账号id")
    private Long userId;

    @ApiModelProperty(required = false, notes = "邮箱")
    private String email;

    @ApiModelProperty(required = false, notes = "备注")
    private String remark;

}
