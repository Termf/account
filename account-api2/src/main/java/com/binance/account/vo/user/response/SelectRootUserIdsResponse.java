package com.binance.account.vo.user.response;

import lombok.Data;

import java.util.Date;

/**
 * Created by yangyang on 2019/11/7.
 */
@Data
public class SelectRootUserIdsResponse {

    private Long userId;

    private Long rootUserId;

    private Long brokerSubAcountId;

}
