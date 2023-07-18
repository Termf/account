package com.binance.account.vo.subuser.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by zhao chenkai on 2019/10/25.
 */
@ApiModel("查询子账户相应币种的可用余额request")
@Data
public class SubUserCurrencyBalanceReq extends ToString {

    private static final long serialVersionUID = -372857959034029316L;

    @ApiModelProperty(required = true, notes = "母账号id")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "币种")
    @NotEmpty
    private String coin;

    @ApiModelProperty(required = false, notes = "子账号邮箱")
    private String email;
}
