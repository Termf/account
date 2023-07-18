package com.binance.account.vo.reset.request;

import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-23 17:21
 */
@Getter
@Setter
public class ResetApplyTimesArg extends ToString {
    private static final long serialVersionUID = 2290613376789667359L;

    @NotNull
    private Long userId;

    @NotNull
    private UserSecurityResetType type;
}
