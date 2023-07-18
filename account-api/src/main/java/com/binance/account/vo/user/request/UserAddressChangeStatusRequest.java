package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-04-25 14:33
 */
@Setter
@Getter
public class UserAddressChangeStatusRequest extends ToString {

    private static final long serialVersionUID = -6089950843441339229L;

    @ApiModelProperty("userId")
    @NotNull
    private Long userId;

    @ApiModelProperty("原因")
    private String failReason;

    @ApiModelProperty("审核者")
    private String approver;


}
