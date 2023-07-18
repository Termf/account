package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel("手机Request")
@Getter
@Setter
public class MobileRequest {

    @ApiModelProperty("手机号")
    @NotEmpty
    private String mobile;

    @ApiModelProperty("手机代码")
    @NotEmpty
    private String mobileCode;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
