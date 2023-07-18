package com.binance.account.vo.tag.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("查询用户是否标记Yubikey Marketing标签")
public class YubikeyMarketingRequest {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("标签名称")
    @NotBlank
    private String tagName;

}
