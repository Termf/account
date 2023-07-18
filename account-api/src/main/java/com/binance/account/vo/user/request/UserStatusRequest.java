package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("用户状态Request")
@Getter
@Setter
public class UserStatusRequest implements Serializable {

    private static final long serialVersionUID = 1514981546951464979L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("状态码")
    @NotNull
    private Long status;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
