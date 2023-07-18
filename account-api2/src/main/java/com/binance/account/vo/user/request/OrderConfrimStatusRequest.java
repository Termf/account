package com.binance.account.vo.user.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.binance.account.common.enums.OrderConfirmType;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户下单确认状态Request")
@Getter
@Setter
public class OrderConfrimStatusRequest implements Serializable {

    private static final long serialVersionUID = 810734512800046973L;
    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("类型")
    @NotNull
    private OrderConfirmType orderConfirmType;

    @ApiModelProperty("true:启用 false 停用")
    private boolean status;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
