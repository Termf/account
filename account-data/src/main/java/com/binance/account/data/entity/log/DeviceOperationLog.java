package com.binance.account.data.entity.log;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class DeviceOperationLog implements Serializable {

    private static final long serialVersionUID = 6545355654413290348L;

    private Long id;

    private Long userId;

    private Long devicePk;

    private Date time;

    private String operation;

    private String ip;

    private String note;

    private String score;

    private String deviceInfo;

    private String userOperationLogUuid;
}
