package com.binance.account.vo.user.request;

import com.binance.account.common.enums.KycStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel(description = "kyc人工审核", value = "kyc人工审核")
@Getter
@Setter
public class KycAuditRequest extends ToString {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6461531779888220587L;

    @ApiModelProperty(required = true, notes = "id")
    @NotNull
    private Long id;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "审核状态")
    @NotNull
    private KycStatus status;

    @ApiModelProperty(notes = "失败原因")
    private String failReason;

    @ApiModelProperty(notes = "证件号码")
    private String number;

    @ApiModelProperty(notes = "备注")
    private String memo;

}
