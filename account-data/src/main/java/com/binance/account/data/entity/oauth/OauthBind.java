package com.binance.account.data.entity.oauth;

import lombok.Data;

@Data
public class OauthBind {
    /** PK */
    private Integer id;

    /** 用户id */
    private Long userId;

    /** 第三方ID */
    private String clientId;

    /** 第三方userId */
    private String oauthUserId;

    /** 第三方userId */
    private String status;
}
