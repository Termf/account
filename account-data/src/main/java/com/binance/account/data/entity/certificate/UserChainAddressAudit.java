package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;

@Data
public class UserChainAddressAudit {
    private Long id;

    private Long userId;

    private String email;

    private Integer type;

    private Integer status;

    private String coin;

    private String address;

    private String refundAddress;

    private String refundAddressTag;

    private String chainalysisResult;

    private String comment;

    private String channel;

    private Date createTime;

    private Date updateTime;

    private String createdBy;

    private String updatedBy;
    
    private String bizId;

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public enum Status {

        /**
         * 0 待处理
         */
        PENDING,

        /**
         * 1 已停止服务
         */
        STOP_SERVICE,

        /**
         * 2 已豁免
         */
        EXEMPTED,

        /**
         * 3 已拒绝
         */
        REFUSED,

        /**
         * 4 已退款
         */
        REFUNDED
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin == null ? null : coin.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getChainalysisResult() {
        return chainalysisResult;
    }

    public void setChainalysisResult(String chainalysisResult) {
        this.chainalysisResult = chainalysisResult == null ? null : chainalysisResult.trim();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? null : comment.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }
}
