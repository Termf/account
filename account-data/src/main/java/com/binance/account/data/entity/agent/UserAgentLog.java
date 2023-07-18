package com.binance.account.data.entity.agent;

import java.util.Date;

public class UserAgentLog {
    private Long id;

    private String agentCode;

    private Long userId;

    private Integer userType;

    private Long referralUser;

    private String referralEmail;

    private String client;

    private Date insertTime;

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

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

    public Long getReferralUser() {
        return referralUser;
    }

    public void setReferralUser(Long referralUser) {
        this.referralUser = referralUser;
    }

    public String getReferralEmail() {
        return referralEmail;
    }

    public void setReferralEmail(String referralEmail) {
        this.referralEmail = referralEmail == null ? null : referralEmail.trim();
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client == null ? null : client.trim();
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }
}