package com.binance.account.data.entity.subuser;

import java.util.Date;

public class SubUserBindingDelete {
    private Long subUserId;

    private Long parentUserId;

    private String remark;

    private Date insertTime;

    private Date updateTime;

    private Long brokerSubAccountId;

    public Long getSubUserId() {
        return subUserId;
    }

    public void setSubUserId(Long subUserId) {
        this.subUserId = subUserId;
    }

    public Long getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(Long parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public Long getBrokerSubAccountId() {
        return brokerSubAccountId;
    }

    public void setBrokerSubAccountId(Long brokerSubAccountId) {
        this.brokerSubAccountId = brokerSubAccountId;
    }
}