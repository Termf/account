package com.binance.account.data.entity.broker;

import java.math.BigDecimal;
import java.util.Date;

public class BrokerCommissionUpdateBak {
    private Long id;

    private Long userId;

    private Long tradingAccount;

    private BigDecimal makerCommiss;

    private BigDecimal takerCommiss;

    private String symbol;

    private Integer source;

    private Date insertTime;

    private Date updateTime;

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

    public Long getTradingAccount() {
        return tradingAccount;
    }

    public void setTradingAccount(Long tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public BigDecimal getMakerCommiss() {
        return makerCommiss;
    }

    public void setMakerCommiss(BigDecimal makerCommiss) {
        this.makerCommiss = makerCommiss;
    }

    public BigDecimal getTakerCommiss() {
        return takerCommiss;
    }

    public void setTakerCommiss(BigDecimal takerCommiss) {
        this.takerCommiss = takerCommiss;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol == null ? null : symbol.trim();
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
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