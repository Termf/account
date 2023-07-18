package com.binance.account.vo.yubikey;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("结束绑定请求")
@Data
public class FinishRegisterRequest implements Serializable {
    private static final long serialVersionUID = 6518347373316444021L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;


    @ApiModelProperty("yubikey nickname")
    private String nickname;

    @ApiModelProperty("验证详情信息, 需要符合JSON格式, 能序列化为RegistrationFinishRequest对象," +
            "内容主要有：requestId(开始请求中返回的requestId), credential(公钥签名验证信息)")
    @NotNull
    private String finishDetail;


    @ApiModelProperty("验证类型")
    private AuthTypeEnum authType;

    @ApiModelProperty("验证码")
    private String code;
}
