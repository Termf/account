package com.binance.account.domain.bo;


import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author szlong
 */
@Data
public class UserOperationForBigDataDto {

    private String userId;

    private String email;

    private String clientType;

    private String versionCode;

    private String realIp;

    private String fullIp;

    private String userAgent;

    private Date requestTime;

    private Date responseTime;

    private String apikey;

    private String request;

    private String response;

    private String responseStatus;

    private String failReason;

    private String operation;

    private Map<String, String> deviceInfo;

    private String sessionId;

    private String referer;

    private String uuid;

}
