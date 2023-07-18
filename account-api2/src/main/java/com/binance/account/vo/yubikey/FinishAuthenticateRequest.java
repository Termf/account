package com.binance.account.vo.yubikey;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("结束验证详情")
@Data
public class FinishAuthenticateRequest implements Serializable {

    private static final long serialVersionUID = 8676054725153837055L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("验证详情信息, 需要符合JSON格式, 能序列化为：AssertionFinishRequest对象," +
            " 内容主要有：requestId(开始请求中返回的requestId), deregister(是否解绑: true/false), credential(公钥签名验证信息)")
    @NotNull
    private String finishDetail;
}
