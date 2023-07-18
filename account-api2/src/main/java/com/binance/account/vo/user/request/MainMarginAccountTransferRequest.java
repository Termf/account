package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@ApiModel("MainMarginAccountTransferRequest")
@Data
public class MainMarginAccountTransferRequest {
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;
	
	@ApiModelProperty(required = true, notes = "子账户email")
    @NotBlank
    private String subEmail;

    @ApiModelProperty(required = true, notes = "1:main2margin 2:margin2main")
    @NotNull
    @Min(1)
    @Max(2)
    private Integer type;

    @ApiModelProperty(required = true, notes = "资产名字(例如BTC)")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "划转数量")
    @NotNull
    private BigDecimal amount;
}
