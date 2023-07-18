package com.binance.account.data.entity.security;

import com.binance.master.constant.CacheKeys;
import com.binance.master.utils.RedisCacheUtils;

public class VerificationsTwo {

    private String mobileKey;

    public VerificationsTwo() {
    }

    public VerificationsTwo(String mobileKey) {
        super();
        this.mobileKey = mobileKey;
    }

    public void delMobileCode() {
        RedisCacheUtils.del(mobileKey, CacheKeys.MOBILE_AUTH_TIME);
    }
}

