package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author yangyang
 * @date 2019/7/10
 */
@ApiModel("创建推荐人返佣费率信息")
@Getter
@Setter
public class UserAgentRateReq implements Serializable{


    @ApiModelProperty(required = true, notes = "推荐人UserId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "推荐人返佣比例")
    @NotNull
    private Integer agentLevel;

    @ApiModelProperty(required = true, notes = "被推荐人返佣比例")
    @NotNull
    private BigDecimal referralRate;

    @ApiModelProperty(required = true, notes = "label标记")
    @Length(max=20, message="label长度最大20")
    @NotBlank
    private String label;

    @ApiModelProperty(required = true, notes = "用户创建defalt记录")
    private String agentCode;

    private Integer agentChannel;

}
