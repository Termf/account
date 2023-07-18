package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("绑定谷歌验证Request")
@Getter
@Setter
public class BindGoogleVerifyV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -8925616161058113256L;

    @ApiModelProperty(value="用户Id",required=true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value="Google验证密钥",required=false)
    private String secretKey;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
