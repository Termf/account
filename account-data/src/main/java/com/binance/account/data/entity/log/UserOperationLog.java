package com.binance.account.data.entity.log;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserOperationLog implements Serializable {

    private static final long serialVersionUID = 7658532087341303453L;

    private Long id;

    private String email;

    private String operation;

    private String uuid;

    private Long userId;

    private String clientType;

    private String versionCode;

    private String realIp;

    private String fullIp;

    private String apikey;

    private String userAgent;

    private Date requestTime;

    private Date responseTime;

    private String request;

    private String responseStatus;

    private String response;

}
