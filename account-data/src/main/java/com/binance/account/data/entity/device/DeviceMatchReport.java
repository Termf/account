package com.binance.account.data.entity.device;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceMatchReport {

    private Long id;

    private String candidateDeviceInfo;

    @Deprecated
    private Long targetDevicePk;

    private String v1MatchedDeviceInfo;

    private String v2MatchedDeviceInfo;

    private Double v1Score;

    private Double v2Score;

    private Integer version;

    private Date insertTime;

}
