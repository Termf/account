package com.binance.account.data.entity.futureagent;

import java.util.Date;

public class FutureUserAgent {
    private Long id;

    private String agentCode;

    private Long userId;

    private Long futureUserId;

    private Date insertTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode == null ? null : agentCode.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFutureUserId() {
        return futureUserId;
    }

    public void setFutureUserId(Long futureUserId) {
        this.futureUserId = futureUserId;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}