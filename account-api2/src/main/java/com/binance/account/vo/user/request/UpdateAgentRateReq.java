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

/**
 * Created by yangyang on 2019/8/15.
 */
@ApiModel("更新返佣link的label")
@Getter
@Setter
public class UpdateAgentRateReq implements Serializable {

    @ApiModelProperty(required = true, notes = "推荐人agentCode")
    @NotBlank
    private String agentCode;

    @ApiModelProperty(required = true, notes = "label标记")
    @NotBlank
    @Length(max=20, message="label长度最大20")
    private String label;

    @NotNull
    private Long userId;
}
