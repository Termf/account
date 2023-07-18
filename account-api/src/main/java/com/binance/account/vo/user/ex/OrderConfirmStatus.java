package com.binance.account.vo.user.ex;

import com.binance.account.common.enums.OrderConfirmType;
import com.binance.master.utils.BitUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "用户下单确认状态")
@Getter
@Setter
@NoArgsConstructor
public class OrderConfirmStatus {
    public static final Long DEFAULT_STATUS = 28L;
    @ApiModelProperty(name = "限价单，默认关闭")
    private boolean limitOrder;
    @ApiModelProperty(name = "市价单，默认关闭")
    private boolean marketOrder;
    @ApiModelProperty(name = "止损单，默认开启")
    private boolean stopLossOrder;
    @ApiModelProperty(name = "margin借入，默认开启")
    private boolean marginAutoBorrow;
    @ApiModelProperty(name = "margin偿付，默认开启")
    private boolean marginAutoRepay;
    @ApiModelProperty(name = "oco，默认关闭")
    private boolean oco;

    public static OrderConfirmStatus build(Long status) {
        if (status == null) {
            status = DEFAULT_STATUS;
        }
        OrderConfirmStatus orderConfirmStatus = new OrderConfirmStatus();
        orderConfirmStatus.limitOrder = BitUtils.isTrue(status, OrderConfirmType.limitOrder.getBitVal());
        orderConfirmStatus.marketOrder = BitUtils.isTrue(status, OrderConfirmType.marketOrder.getBitVal());
        orderConfirmStatus.stopLossOrder = BitUtils.isTrue(status, OrderConfirmType.stopLossOrder.getBitVal());
        orderConfirmStatus.marginAutoBorrow = BitUtils.isTrue(status, OrderConfirmType.marginAutoBorrow.getBitVal());
        orderConfirmStatus.marginAutoRepay = BitUtils.isTrue(status, OrderConfirmType.marginAutoRepay.getBitVal());
        orderConfirmStatus.oco = BitUtils.isTrue(status, OrderConfirmType.oco.getBitVal());
        return orderConfirmStatus;
    }
}
