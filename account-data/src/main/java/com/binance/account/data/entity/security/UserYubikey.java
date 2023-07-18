package com.binance.account.data.entity.security;

import lombok.Data;

import java.util.Date;

@Data
public class UserYubikey {
    private Long id;

    private Long userId;

    private String origin;

    private String nickName;

    private String credentialId;

    private String userHandle;

    private String publicKey;

    private Long signatureCount;

    private Boolean isLegacy;

    private Date createTime;

    private Date updateTime;

}