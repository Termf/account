package com.binance.account.vo.reset.response;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2019-01-23 17:19
 */
@Setter
@Getter
public class ResetApplyTimesRet extends ToString {

    private static final long serialVersionUID = -7478601779961465137L;

    private Long applyTimes = 0L;

    private Long refuseTimes = 0L;

    private Long successTimes = 0L;

}
