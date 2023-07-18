package com.binance.account.vo.security.request;

import com.binance.account.vo.user.request.BaseMultiCodeVerifyRequest;
import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("设置防钓鱼码Request")
@Getter
@Setter
public class BindPhishingCodeV2Request extends BaseMultiCodeVerifyRequest {
    private static final long serialVersionUID = 3658958064343071104L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("防钓鱼码")
    //@NotNull
    //@Length(min = 4, max = 20)
    //@Pattern(regexp = Regexp.PHISHING_CODE_IGNORE,
            //message = "${com.binance.master.validator.constraints.phishingCodeIgnore.message}")
    private String antiPhishingCode;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
