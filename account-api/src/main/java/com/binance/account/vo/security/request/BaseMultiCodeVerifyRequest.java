package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: mingming.sheng
 * @Date: 2020/4/29 5:21 下午
 */
@Data
public class BaseMultiCodeVerifyRequest extends ToString {
    private static final long serialVersionUID = -48222599550961256L;

    @ApiModelProperty("手机验证码")
    private String mobileVerifyCode;
    @ApiModelProperty("google验证码")
    private String googleVerifyCode;
    @ApiModelProperty("邮件验证码")
    private String emailVerifyCode;
    @ApiModelProperty("yubikey验证码")
    private String yubikeyVerifyCode;
}
