package com.binance.account.data.entity.user;

import java.util.Date;

public class UserCommonPermission {
    private String userType;//用户类型

    private Boolean enableDeposit;//是否可以充值(0:禁止;1:允许)

    private Boolean enableWithdraw;//是否可以提币(0:禁止;1:允许)

    private Boolean enableTrade;//是否可以交易(0:禁止;1:允许)

    private Boolean enableTransfer;//是否可以划转(0:禁止;1:允许)

    private Boolean enableSubTransfer;//是否可以子账号划转(0:禁止;1:允许)

    private Boolean enableCreateApikey;//是否可以创建apikey(0:禁止;1:允许)

    private Boolean enableLogin;//是否可以登录(0:禁止;1:允许)

    private Boolean enableCreateMargin;//是否可以创建margin账号(0:禁止;1:允许)

    private Boolean enableCreateFutures;//是否可以创建期货账号(0:禁止;1:允许)

    private Boolean enableCreateFiat;//是否可以创建法币账号(0:禁止;1:允许)

    private Boolean enableCreateIsolatedMargin;//是否可以创建逐仓margin账号(0:禁止;1:允许)

    private Boolean enableCreateSubAccount;//是否可以创建子账号(0:禁止;1:允许)

    private Boolean enableParentAccount;//是否可以成为母账号(0:禁止;1:允许)

    private Boolean enableBrokerParentAccount;//是否可以broker母账号(0:禁止;1:允许)

    private Boolean enableCreateBrokerSubAccount;//是否可以创建broker子账号(0:禁止;1:允许)

    private Date insertTime;

    private Date updateTime;

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public Boolean getEnableDeposit() {
        return enableDeposit;
    }

    public void setEnableDeposit(Boolean enableDeposit) {
        this.enableDeposit = enableDeposit;
    }

    public Boolean getEnableWithdraw() {
        return enableWithdraw;
    }

    public void setEnableWithdraw(Boolean enableWithdraw) {
        this.enableWithdraw = enableWithdraw;
    }

    public Boolean getEnableTrade() {
        return enableTrade;
    }

    public void setEnableTrade(Boolean enableTrade) {
        this.enableTrade = enableTrade;
    }

    public Boolean getEnableTransfer() {
        return enableTransfer;
    }

    public void setEnableTransfer(Boolean enableTransfer) {
        this.enableTransfer = enableTransfer;
    }

    public Boolean getEnableSubTransfer() {
        return enableSubTransfer;
    }

    public void setEnableSubTransfer(Boolean enableSubTransfer) {
        this.enableSubTransfer = enableSubTransfer;
    }

    public Boolean getEnableCreateApikey() {
        return enableCreateApikey;
    }

    public void setEnableCreateApikey(Boolean enableCreateApikey) {
        this.enableCreateApikey = enableCreateApikey;
    }

    public Boolean getEnableLogin() {
        return enableLogin;
    }

    public void setEnableLogin(Boolean enableLogin) {
        this.enableLogin = enableLogin;
    }

    public Boolean getEnableCreateMargin() {
        return enableCreateMargin;
    }

    public void setEnableCreateMargin(Boolean enableCreateMargin) {
        this.enableCreateMargin = enableCreateMargin;
    }

    public Boolean getEnableCreateFutures() {
        return enableCreateFutures;
    }

    public void setEnableCreateFutures(Boolean enableCreateFutures) {
        this.enableCreateFutures = enableCreateFutures;
    }

    public Boolean getEnableCreateFiat() {
        return enableCreateFiat;
    }

    public void setEnableCreateFiat(Boolean enableCreateFiat) {
        this.enableCreateFiat = enableCreateFiat;
    }

    public Boolean getEnableCreateIsolatedMargin() {
        return enableCreateIsolatedMargin;
    }

    public void setEnableCreateIsolatedMargin(Boolean enableCreateIsolatedMargin) {
        this.enableCreateIsolatedMargin = enableCreateIsolatedMargin;
    }

    public Boolean getEnableCreateSubAccount() {
        return enableCreateSubAccount;
    }

    public void setEnableCreateSubAccount(Boolean enableCreateSubAccount) {
        this.enableCreateSubAccount = enableCreateSubAccount;
    }

    public Boolean getEnableParentAccount() {
        return enableParentAccount;
    }

    public void setEnableParentAccount(Boolean enableParentAccount) {
        this.enableParentAccount = enableParentAccount;
    }

    public Boolean getEnableBrokerParentAccount() {
        return enableBrokerParentAccount;
    }

    public void setEnableBrokerParentAccount(Boolean enableBrokerParentAccount) {
        this.enableBrokerParentAccount = enableBrokerParentAccount;
    }

    public Boolean getEnableCreateBrokerSubAccount() {
        return enableCreateBrokerSubAccount;
    }

    public void setEnableCreateBrokerSubAccount(Boolean enableCreateBrokerSubAccount) {
        this.enableCreateBrokerSubAccount = enableCreateBrokerSubAccount;
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