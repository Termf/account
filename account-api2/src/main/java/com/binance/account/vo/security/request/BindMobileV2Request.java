package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("绑定手机Request")
@Getter
@Setter
public class BindMobileV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = -1551369767789992284L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("手机号")
    @NotEmpty
    private String mobile;

    @ApiModelProperty("手机代码")
    //@NotEmpty
    private String mobileCode;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
