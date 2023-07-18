package com.binance.account.vo.user.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by yangyang on 2019/11/7.
 */
@Data
public class SelectIfHasAgenUserRequest {

    @NotNull
    private Date startTime;

    @NotNull
    private Date endTime;

    @NotNull
    private Long userId;
}
