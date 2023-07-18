package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mengjuan on 2018/11/21.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReCaptchaReq implements Serializable{
    @ApiModelProperty("用户Id")
    private Long userId;

    @ApiModelProperty("邮箱")
    private String  email;

    @ApiModelProperty("分数")
    private String score;

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("action")
    private String action;

    @ApiModelProperty("请求时间")
    private Date challengeTs;

    @ApiModelProperty("错误码")
    private String errorCodes;
}