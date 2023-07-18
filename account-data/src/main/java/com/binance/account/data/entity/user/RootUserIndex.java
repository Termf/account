package com.binance.account.data.entity.user;

import java.util.Date;

public class RootUserIndex {
    private Long userId;

    private Long rootUserId;

    private String accountType;

    private Date insertTime;

    private Date updateTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRootUserId() {
        return rootUserId;
    }

    public void setRootUserId(Long rootUserId) {
        this.rootUserId = rootUserId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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