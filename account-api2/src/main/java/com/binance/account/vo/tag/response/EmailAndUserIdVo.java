package com.binance.account.vo.tag.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("用户邮箱和标签")
public class EmailAndUserIdVo {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("标签名字")
    private List<String> tagNames;

}
