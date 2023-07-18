package com.binance.account.vo.yubikey;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("结束绑定请求")
@Data
public class FinishRegisterRequestV2 extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -2112372962935533229L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("yubikey nickname")
    private String nickname;

    @ApiModelProperty("验证详情信息, 需要符合JSON格式, 能序列化为RegistrationFinishRequest对象," +
            "内容主要有：requestId(开始请求中返回的requestId), credential(公钥签名验证信息)")
    @NotNull
    private String finishDetail;
}
