package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("解绑手机非安全方式Request")
@Getter
@Setter
public class UnbindMobileNonSafetyRequest implements Serializable {

    private static final long serialVersionUID = 7323668824987901848L;

    @ApiModelProperty(required = true, value = "用户Id")
    @NotNull
    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
