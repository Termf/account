package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("BindEmailRequest")
@Getter
@Setter
public class BindEmailRequest extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -488747182645829428L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("邮箱")
    @NotNull
    private String email;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
