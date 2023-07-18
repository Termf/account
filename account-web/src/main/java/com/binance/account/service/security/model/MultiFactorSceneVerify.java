package com.binance.account.service.security.model;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 多因子校验请求
 *
 * @Author: mingming.sheng
 * @Date: 2020/4/24 10:46 上午
 */
@Data
@Builder
public class MultiFactorSceneVerify {
    private static final long serialVersionUID = 7296161318062308419L;

    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("业务场景")
    private BizSceneEnum bizScene;

    @ApiModelProperty("手机验证码")
    private String mobileVerifyCode;

    @ApiModelProperty("google验证码")
    private String googleVerifyCode;

    @ApiModelProperty("邮件验证码")
    private String emailVerifyCode;

    @ApiModelProperty("yubikey验证码")
    private String yubikeyVerifyCode;

    @Override
    public String toString() {
        return "MultiFactorSceneVerify{" +
                "userId=" + userId +
                ", bizScene=" + bizScene +
                ", mobileVerifyCode='" + (StringUtils.isBlank(mobileVerifyCode) ? "null" : "***") + '\'' +
                ", googleVerifyCode='" + (StringUtils.isBlank(googleVerifyCode) ? "null" : "***") + '\'' +
                ", emailVerifyCode='" + (StringUtils.isBlank(emailVerifyCode) ? "null" : "***") + '\'' +
                ", yubikeyVerifyCode='" + (StringUtils.isBlank(yubikeyVerifyCode) ? "null" : "***") + '\'' +
                '}';
    }
}
