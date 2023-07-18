package com.binance.account.vo.withdraw.response;

import java.math.BigDecimal;
import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "提现锁定日志Request", value = "提现锁定日志Request")
@Getter
@Setter
public class UserWithdrawLockLogResponse extends ToString {

    private Long id;

    private Long userId;

    private Long tranId;

    private String type;

    private String isManual;

    private BigDecimal amount;

    private Date insertTime;

    private String operator;

}
