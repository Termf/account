package com.binance.account.data.entity.agent;

import java.math.BigDecimal;

public class UserAgentRate {
    private Long id;

    private String agentCode;

    private String label;

    private Long userId;

    private Integer agentLevel;

    private BigDecimal referralRate;

    private Integer selectShare;

    private Integer agentChannel;

    private Integer isDelete;


    public Integer getAgentChannel() {
        return agentChannel;
    }

    public void setAgentChannel(Integer agentChannel) {
        this.agentChannel = agentChannel;
    }

    public Integer getSelectShare() {
        return selectShare;
    }

    public void setSelectShare(Integer selectShare) {
        this.selectShare = selectShare;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAgentLevel() {
        return agentLevel;
    }

    public void setAgentLevel(Integer agentLevel) {
        this.agentLevel = agentLevel;
    }

    public BigDecimal getReferralRate() {
        return referralRate;
    }

    public void setReferralRate(BigDecimal referralRate) {
        this.referralRate = referralRate;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}