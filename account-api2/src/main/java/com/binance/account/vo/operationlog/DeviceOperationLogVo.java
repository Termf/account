package com.binance.account.vo.operationlog;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class DeviceOperationLogVo implements Serializable {

    private static final long serialVersionUID = 2177389459345422674L;

    private String id;

    private String operation;

    private Long devicePk;

    private Long userId;

    private String email;

    private String ip;

    private Date time;

    private String note;

    private String score;

    private String deviceInfo;

}
