package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("绑定谷歌验证Response")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BindGoogleVerifyResponse {

    @ApiModelProperty("用户Id")
    private Long userId;

    @ApiModelProperty("Google验证密钥")
    private String secretKey;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
