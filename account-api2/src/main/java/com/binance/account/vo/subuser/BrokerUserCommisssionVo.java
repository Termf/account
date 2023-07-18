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

    private String fiatRequestIp;

    private String fiatSupportedAsset;

    private Integer dayWithdrawLimit;

    private BigDecimal dayWithdrawPer;

    private Integer dayWithdrawSwitch;

    private BigDecimal maxDeliveryMakerCommiss;

    private BigDecimal minDeliveryMakerCommiss;

    private BigDecimal maxDeliveryTakerCommiss;

    private BigDecimal minDeliveryTakerCommiss;





    public String getFiatSupportedAsset() {
        return fiatSupportedAsset;
    }

    public void setFiatSupportedAsset(String fiatSupportedAsset) {
        this.fiatSupportedAsset = fiatSupportedAsset;
    }

    public String getFiatRequestIp() {
        return fiatRequestIp;
    }

    public void setFiatRequestIp(String fiatRequestIp) {
        this.fiatRequestIp = fiatRequestIp;
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

    public Integer getDayWithdrawLimit() {
        return dayWithdrawLimit;
    }

    public void setDayWithdrawLimit(Integer dayWithdrawLimit) {
        this.dayWithdrawLimit = dayWithdrawLimit;
    }

    public BigDecimal getDayWithdrawPer() {
        return dayWithdrawPer;
    }

    public void setDayWithdrawPer(BigDecimal dayWithdrawPer) {
        this.dayWithdrawPer = dayWithdrawPer;
    }

    public Integer getDayWithdrawSwitch() {
        return dayWithdrawSwitch;
    }

    public void setDayWithdrawSwitch(Integer dayWithdrawSwitch) {
        this.dayWithdrawSwitch = dayWithdrawSwitch;
    }

    public BigDecimal getMaxDeliveryMakerCommiss() {
        return maxDeliveryMakerCommiss;
    }

    public void setMaxDeliveryMakerCommiss(BigDecimal maxDeliveryMakerCommiss) {
        this.maxDeliveryMakerCommiss = maxDeliveryMakerCommiss;
    }

    public BigDecimal getMinDeliveryMakerCommiss() {
        return minDeliveryMakerCommiss;
    }

    public void setMinDeliveryMakerCommiss(BigDecimal minDeliveryMakerCommiss) {
        this.minDeliveryMakerCommiss = minDeliveryMakerCommiss;
    }

    public BigDecimal getMaxDeliveryTakerCommiss() {
        return maxDeliveryTakerCommiss;
    }

    public void setMaxDeliveryTakerCommiss(BigDecimal maxDeliveryTakerCommiss) {
        this.maxDeliveryTakerCommiss = maxDeliveryTakerCommiss;
    }

    public BigDecimal getMinDeliveryTakerCommiss() {
        return minDeliveryTakerCommiss;
    }

    public void setMinDeliveryTakerCommiss(BigDecimal minDeliveryTakerCommiss) {
        this.minDeliveryTakerCommiss = minDeliveryTakerCommiss;
    }
}