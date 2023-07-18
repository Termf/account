package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangyang on 2019/7/18.
 */
@ApiModel("获取禁止提现的时间")
@Getter
@Setter
public class WithdrawTimeForTradeResponse {
    /**
     * 解绑谷歌手机禁止提币时间
     */
    private Long withdrawRestrictedUnbind2fa;

    /**
     * 解绑youbikey-禁止提币时间
     */
    private Long withdrawUnbindYoubikey;


    /**
     * 忘记修改密码(未登录)
     */
    private Long withdrawForgetPassword;

    /**
     * 更新/未登录重置密码-禁止提币时间
     */
    private Long withdrawRestrictedPassword;

    /**
     * 重置谷歌手机验证/解禁账户导致禁止提币时间
     */
    private Long withdrawAccountRestrictedReset2fa;

    /**
     * 最后一次登录禁止提币时间
     */
    private Long withdrawLoginDelay;

    /**
     * 更换email-禁止提币时间
     */
    private Long withdrawRestrictedEmail;


}
