package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("BindEmailRequest")
@Getter
@Setter
public class BindEmailRequest implements Serializable {
    private static final long serialVersionUID = -488747182645829428L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("邮箱")
    @NotNull
    private String email;

    @ApiModelProperty("邮箱验证码")
    @NotNull
    private String emailCode;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
