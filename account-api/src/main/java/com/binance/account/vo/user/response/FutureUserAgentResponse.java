package com.binance.account.vo.user.response;


import lombok.Data;

@Data
public class FutureUserAgentResponse {

    private String agentCode;

    private Long userId;

    private Long futureUserId;
}