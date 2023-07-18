package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by yangyang on 2019/8/22.
 */
@ApiModel("创建或更新")
@Getter
@Setter
public class UserAgentConfigReq implements Serializable{

    @ApiModelProperty(required = true, notes = "用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = false, notes = "推荐者的最大连接数")
    private Integer maxLink;

    //本期功能不做了
//    @ApiModelProperty(required = false, notes = "推荐者的最大返佣比例")
//    private BigDecimal maxAgentRate;

    @ApiModelProperty(required = true, notes = "更新用户")
    @NotBlank
    private String updateUser;
}
