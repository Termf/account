package com.binance.account.vo.user.request;

import com.binance.account.vo.user.UserAddressVo;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "地址人工审核", value = "地址人工审核")
@Getter
@Setter
public class AddressAuditRequest extends ToString {

	private static final long serialVersionUID = 2882013418273481202L;

    @ApiModelProperty(required = true, notes = "id")
    @NotNull
    private Long id;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "审核状态")
    @NotNull
    private UserAddressVo.Status status;

    @ApiModelProperty(required = false, notes = "失败原因")
    private String failReason;

    @ApiModelProperty(required = false, notes = "审核者")
    private String approver;

}
