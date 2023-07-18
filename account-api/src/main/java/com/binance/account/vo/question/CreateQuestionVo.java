package com.binance.account.vo.question;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@ToString
public class CreateQuestionVo implements Serializable {
    private static final long serialVersionUID = -7844922741815744407L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("业务流程号")
    @NotNull
    private String flowId;

    @ApiModelProperty("业务类型")
    @NotNull
    private String flowType;

    @ApiModelProperty("业务超时时间，单位为分钟, 小与等于0的时候默认为24小时")
    private long timeout;

    @ApiModelProperty("答题成功后的回调地址")
    @NotNull
    private String successCallback;

    @ApiModelProperty("答题失败后的回调地址")
    @NotNull
    private String failCallback;
}
