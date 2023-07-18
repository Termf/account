package com.binance.account.vo.security;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OldWithdrawDailyLimitModifyVo extends ToString {
    private static final long serialVersionUID = -4089888012728181023L;

    private String userId;
    private BigDecimal withdrawDaliyLimitLast;
    private String modifyCause;
    private Short autoRestore;
    private Date restoreTimePlan;
    private Date restoreTimeActual;
    private String forbidReason;
    private Date forbidRestoreTime;
    private Date forbidRestoreTimeActual;
    private Short forbidAutoRestore;
    private String applyInfo;

}
