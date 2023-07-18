package com.binance.account.vo.subuser;

import java.math.BigDecimal;
import java.util.Date;

public class BrokerUserCommisssionVo {
    private Long id;

    private Long userId;

    private BigDecimal maxMakerCommiss;

    private BigDecimal minMakerCommiss;

    private BigDecimal maxTakerCommiss;

    private BigDecimal minTakerCommiss;

    private Integer maxSubAccount;

    private Integer dayMaxSubAccount;

    private Date insertTime;

    private Date updateTime;

    private Integer isDelete;


    private BigDecimal maxFuturesMakerCommiss;

    private BigDecimal minFuturesMakerCommiss;

    private BigDecimal maxFuturesTakerCommiss;

    private BigDecimal minFuturesTakerCommiss;




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

    public BigDecimal getMaxMakerCommiss() {
        return maxMakerCommiss;
    }

    public void setMaxMakerCommiss(BigDecimal maxMakerCommiss) {
        this.maxMakerCommiss = maxMakerCommiss;
    }

    public BigDecimal getMinMakerCommiss() {
        return minMakerCommiss;
    }

    public void setMinMakerCommiss(BigDecimal minMakerCommiss) {
        this.minMakerCommiss = minMakerCommiss;
    }

    public BigDecimal getMaxTakerCommiss() {
        return maxTakerCommiss;
    }

    public void setMaxTakerCommiss(BigDecimal maxTakerCommiss) {
        this.maxTakerCommiss = maxTakerCommiss;
    }

    public BigDecimal getMinTakerCommiss() {
        return minTakerCommiss;
    }

    public void setMinTakerCommiss(BigDecimal minTakerCommiss) {
        this.minTakerCommiss = minTakerCommiss;
    }

    public Integer getMaxSubAccount() {
        return maxSubAccount;
    }

    public void setMaxSubAccount(Integer maxSubAccount) {
        this.maxSubAccount = maxSubAccount;
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

    public Integer getDayMaxSubAccount() {
        return dayMaxSubAccount;
    }

    public void setDayMaxSubAccount(Integer dayMaxSubAccount) {
        this.dayMaxSubAccount = dayMaxSubAccount;
    }


    public BigDecimal getMaxFuturesMakerCommiss() {
        return maxFuturesMakerCommiss;
    }

    public void setMaxFuturesMakerCommiss(BigDecimal maxFuturesMakerCommiss) {
        this.maxFuturesMakerCommiss = maxFuturesMakerCommiss;
    }

    public BigDecimal getMinFuturesMakerCommiss() {
        return minFuturesMakerCommiss;
    }

    public void setMinFuturesMakerCommiss(BigDecimal minFuturesMakerCommiss) {
        this.minFuturesMakerCommiss = minFuturesMakerCommiss;
    }

    public BigDecimal getMaxFuturesTakerCommiss() {
        return maxFuturesTakerCommiss;
    }

    public void setMaxFuturesTakerCommiss(BigDecimal maxFuturesTakerCommiss) {
        this.maxFuturesTakerCommiss = maxFuturesTakerCommiss;
    }

    public BigDecimal getMinFuturesTakerCommiss() {
        return minFuturesTakerCommiss;
    }

    public void setMinFuturesTakerCommiss(BigDecimal minFuturesTakerCommiss) {
        this.minFuturesTakerCommiss = minFuturesTakerCommiss;
    }
}