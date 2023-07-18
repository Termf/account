package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author yangyang
 * @date 2019/7/10
 */
@ApiModel("创建future推荐人返佣code信息")
@Getter
@Setter
public class FutureUserAgentReq implements Serializable{


    @ApiModelProperty(required = true, notes = "用户UserId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "用户自定义推荐码agentCode")
    @NotNull
//    @Length(max = 16, min = 3)
    private String futureAgentCode;

}
