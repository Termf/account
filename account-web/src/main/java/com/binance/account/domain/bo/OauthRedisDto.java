package com.binance.account.domain.bo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class OauthRedisDto implements Serializable {
    private static final long serialVersionUID = 8148858037405478066L;
    private String clientId;
    private String oauthUserId;
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private String scope;
    private int expiresIn;
    private String callback;
    // 邮件模板
    private String template;
    // 邮件中的连接verifyCode
    private String verifyCode;
    // 最后一次发送邮件的时间
    private Date time;
}
