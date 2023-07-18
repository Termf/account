package com.binance.account.data.entity.agent;

import java.math.BigDecimal;
import java.util.Date;

public class UserAgentConfig {
    private Long id;

    private Long userId;

    private Integer maxLink;

    private BigDecimal maxAgentRate;

    private Date insertTime;

    private Date updateTime;

    private Integer isDelete;

    private String createUser;

    private String updateUser;

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

    public Integer getMaxLink() {
        return maxLink;
    }

    public void setMaxLink(Integer maxLink) {
        this.maxLink = maxLink;
    }

    public BigDecimal getMaxAgentRate() {
        return maxAgentRate;
    }

    public void setMaxAgentRate(BigDecimal maxAgentRate) {
        this.maxAgentRate = maxAgentRate;
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }
}