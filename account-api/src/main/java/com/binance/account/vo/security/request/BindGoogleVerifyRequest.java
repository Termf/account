package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel("绑定谷歌验证Request")
@Getter
@Setter
public class BindGoogleVerifyRequest {

    @ApiModelProperty(value="用户Id",required=true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value="Google验证密钥",required=false)
    private String secretKey;

    @ApiModelProperty(value="登录密码",required=false)
    private String password;

    @ApiModelProperty(value="短信验证码",required=false)
    private String smsCode;

    @ApiModelProperty(value="谷歌验证码",required=false)
    private Integer googleCode;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
