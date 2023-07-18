package com.binance.account.vo.user.response;

import lombok.Data;

@Data
public class UserEmailChangeInitResponse {

    private String flowId;

    private String type;

    private int flowStatus;

    private String flowStatusMsg;

}
