package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel("MobileRateLimitRequest")
@Getter
@Setter
public class MobileRateLimitRequest {

    @ApiModelProperty("手机号后四位")
    @NotEmpty
    private String mobile;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("当前发送请求的用户Id")
    @NotNull
    private Long requestUserId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
