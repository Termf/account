package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangyang on 2019/7/18.
 */
@ApiModel("获取account-email,password更新时间")
@Getter
@Setter
public class AccountUpdateTimeForTrade {

    private Long updateEmailTime;

    private Long updatePasswordTime;
}
