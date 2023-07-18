package com.binance.account.vo.reset.request;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-01-28 18:57
 */
@Setter
@Getter
public class ResetUserIdArg extends ToString {
    private static final long serialVersionUID = -5699098908159588574L;

    @NotNull
    private Long userId;
}
