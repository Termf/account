package com.binance.account.vo.security.request;

import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.utils.StringUtils;
import com.binance.master.validator.regexp.Regexp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@ApiModel("设置防钓鱼码Request")
@Getter
@Setter
public class BindPhishingCodeRequest implements Serializable {

    private static final long serialVersionUID = 1009075088793867912L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("防钓鱼码")
    //@NotNull
    //@Length(min = 4, max = 20)
    //@Pattern(regexp = Regexp.PHISHING_CODE_IGNORE,
            //message = "${com.binance.master.validator.constraints.phishingCodeIgnore.message}")
    private String antiPhishingCode;

    @ApiModelProperty("2fa验证码")
    private String code;

    @ApiModelProperty("认证标识")
    private AuthTypeEnum authType;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
