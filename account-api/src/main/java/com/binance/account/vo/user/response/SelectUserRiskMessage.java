package com.binance.account.vo.user.response;

import lombok.Data;

@Data
public class SelectUserRiskMessage {

    private String userEmail;

    private String name;

    private String phone;

    private String userId;

    private String ip;

    private Boolean changedPassword;

    private String deviceInfo;
}
