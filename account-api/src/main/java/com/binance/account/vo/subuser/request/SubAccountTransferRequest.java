package com.binance.account.vo.subuser.request;

import com.binance.master.commons.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@ApiModel("子母账户划转Request")
@Getter
@Setter
public class SubAccountTransferRequest implements Serializable{
    private static final long serialVersionUID = -4736694365131883094L;
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;
	
	@ApiModelProperty(required = true, notes = "转出方userid")
    @NotNull
    private Long senderUserId;

    @ApiModelProperty(required = true, notes = "转入方userid")
    @NotNull
    private Long recipientUserId;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;
}
