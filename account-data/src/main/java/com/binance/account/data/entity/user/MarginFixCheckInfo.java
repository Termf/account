package com.binance.account.data.entity.user;

import java.util.Date;

public class MarginFixCheckInfo {
    private Long rootUserId;

    private Long rootAccountId;

    private Long oldMarginUserId;

    private Long oldMarginAccountId;

    private Long newMarginUserId;

    private Long newMarginAccountId;

    private Date insertTime;

    private Date updateTime;

    public Long getRootUserId() {
        return rootUserId;
    }

    public void setRootUserId(Long rootUserId) {
        this.rootUserId = rootUserId;
    }

    public Long getRootAccountId() {
        return rootAccountId;
    }

    public void setRootAccountId(Long rootAccountId) {
        this.rootAccountId = rootAccountId;
    }

    public Long getOldMarginUserId() {
        return oldMarginUserId;
    }

    public void setOldMarginUserId(Long oldMarginUserId) {
        this.oldMarginUserId = oldMarginUserId;
    }

    public Long getOldMarginAccountId() {
        return oldMarginAccountId;
    }

    public void setOldMarginAccountId(Long oldMarginAccountId) {
        this.oldMarginAccountId = oldMarginAccountId;
    }

    public Long getNewMarginUserId() {
        return newMarginUserId;
    }

    public void setNewMarginUserId(Long newMarginUserId) {
        this.newMarginUserId = newMarginUserId;
    }

    public Long getNewMarginAccountId() {
        return newMarginAccountId;
    }

    public void setNewMarginAccountId(Long newMarginAccountId) {
        this.newMarginAccountId = newMarginAccountId;
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