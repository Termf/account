package com.binance.account.vo.withdraw.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zhenleisun
 */
@ApiModel(description = "提现地址审核Request", value = "提现地址审核Request")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawAddressCheckRequest implements Serializable {
    private static final long serialVersionUID = 2011614025244561908L;

    @ApiModelProperty(required = true, notes = "用户ID")
    @NotNull
    private String userId;

    @ApiModelProperty(required = true, notes = "withdraw资产/币种")
    @NotNull
    private String asset;

    @ApiModelProperty(required = true, notes = "withdraw数量")
    @NotNull
    private BigDecimal amount;

    @ApiModelProperty(required = true, notes = "withdraw地址")
    @NotNull
    private String address;

}
