package com.binance.account.vo.user.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by yangyang on 2019/11/7.
 */
@Data
public class SelectUserRegisterTimeResponse {

    private Long userId;

    private Date registerTime;

    private Date futureRegisterTime;

    private Date marginRegisterTime;

}
