package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VerificationsDemandRequest extends BaseMultiCodeVerifyRequest {

    private static final long serialVersionUID = 5944162888803804853L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("业务场景")
    private BizSceneEnum bizScene;

    @ApiModelProperty("如果是输入的是新手机号的时候，需要带上")
    private String mobile;
    @ApiModelProperty("如果是输入的是新手机号的时候，需要带上")
    private String mobileCode;

}
