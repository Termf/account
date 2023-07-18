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
@ApiModel("选取agentCode作为分享code")
@Getter
@Setter
public class UserAgentSelectShareReq implements Serializable{


    @ApiModelProperty(required = true, notes = "推荐人UserId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "用户创建defalt记录")
    @NotBlank
    private String agentCode;

}
