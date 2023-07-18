package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("VerificationTwoV3Request")
@Getter
@Setter
public class VerificationTwoV3Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = 2564016785508142914L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("业务场景")
    @NotNull
    private BizSceneEnum bizScene;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
