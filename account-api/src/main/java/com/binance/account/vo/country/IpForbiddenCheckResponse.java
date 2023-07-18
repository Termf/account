package com.binance.account.vo.country;

import lombok.Data;

/**
 * @author freeman
 * For US
 */
@Data
public class IpForbiddenCheckResponse {
    /**
     * 国家是否在黑名单
     */
    private Boolean isCountryForbidden;
    /**
     * 国家某个地区是否被禁止
     */
    private Boolean isRegionForbidden;
    /**
     * 提醒内容
     */
    private String message;
}
