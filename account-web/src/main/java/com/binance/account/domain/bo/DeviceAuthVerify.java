package com.binance.account.domain.bo;

import lombok.Data;

@Data
public class DeviceAuthVerify {
    private String code;
    private Integer errorCount = 0;
    private Long errorTime;
    private Long createTime;
    private String loginToken;
}
