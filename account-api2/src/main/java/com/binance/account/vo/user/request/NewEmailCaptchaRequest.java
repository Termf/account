package com.binance.account.vo.user.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class NewEmailCaptchaRequest {
    @NotBlank
    private String flowId;

    @NotNull
    private Long userId;

    @NotBlank
    private String emailVerifyCode; //邮箱验证码

    private String smsVerifyCode;// 手机验证码

    private String googleVerifyCode;//google 验证码
}
