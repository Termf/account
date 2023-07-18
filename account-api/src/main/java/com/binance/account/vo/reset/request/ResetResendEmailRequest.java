package com.binance.account.vo.reset.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("重置流程重发上传邮件")
@Setter
@Getter
public class ResetResendEmailRequest implements Serializable {
    private static final long serialVersionUID = -424353409963755734L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("类型")
    private String type;
}
