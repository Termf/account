package com.binance.account.vo.subuser.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by zhao chenkai on 2019/10/25.
 */
@ApiModel(description = "查询子账户相应币种的可用余额Response", value = "查询子账户相应币种的可用余额Response")
@Data
public class SubUserCurrencyBalanceResp extends ToString {

    private static final long serialVersionUID = 931562146648669608L;

    private Long userId;

    private String email;

    private String asset;

    private BigDecimal free;
}
