package com.binance.account.vo.subuser.response;

/**
 * Created by Fei.Huang on 2018/11/27.
 */
public class SubAccountResp {

    // 子账户邮箱
    String email;
    // 子账户被母账户启用状态
    SubAccountStatus status;
    // 子账户是否激活
    boolean activated;
    // 子账户手机号
    String mobile;
    // 子账户是否开启google验证
    boolean gAuth;
    // 子账户创建时间
    Long createTime;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SubAccountStatus getStatus() {
        return status;
    }

    public void setStatus(SubAccountStatus status) {
        this.status = status;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isgAuth() {
        return gAuth;
    }

    public void setgAuth(boolean gAuth) {
        this.gAuth = gAuth;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public enum SubAccountStatus {
        enabled,
        disabled
    }
}