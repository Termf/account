package com.binance.account.vo.yubikey;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("解绑Webauthn")
@Data
public class DeregisterRequest implements Serializable {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("验证详情信息, 需要符合JSON格式, 能序列化为：AssertionFinishRequest对象," +
            " 内容主要有：requestId(开始请求中返回的requestId), credential(公钥签名验证信息)")
    @NotNull
    private String finishDetail;

    @ApiModelProperty("验证类型")
    private AuthTypeEnum authType;

    @ApiModelProperty("验证码")
    private String code;

}
