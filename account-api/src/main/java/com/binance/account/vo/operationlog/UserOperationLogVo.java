package com.binance.account.vo.operationlog;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserOperationLogVo implements Serializable {

    private static final long serialVersionUID = 2177389459345422674L;

    private String id;

    private String operation;

    private Long userId;

    private String email;

    private String clientType;

    private String versionCode;

    private String realIp;

    private String location;

    private String fullIp;

    private String apikey;

    private String userAgent;

    private Date requestTime;

    private Date responseTime;

    private String request;

    private String responseStatus;

    private String failReason;

    private String response;

    private String uuid;

}
