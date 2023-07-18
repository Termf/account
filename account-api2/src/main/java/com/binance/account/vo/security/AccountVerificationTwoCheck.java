package com.binance.account.vo.security;

import com.binance.account.vo.security.enums.AccountVerificationTwoEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author: mingming.sheng
 * @Date: 2020/4/16 1:08 下午
 */
@Data
public class AccountVerificationTwoCheck implements Serializable {
    private static final long serialVersionUID = 8609802597091574263L;

    /**
     * 2fa验证类型
     */
    private AccountVerificationTwoEnum verifyType;

    /**
     * 2fa验证对象（手机号/邮箱）掩码
     */
    private String verifyTargetMask;

    /**
     * 是否可选(0-可选，1-必选)
     */
    private Integer option;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountVerificationTwoCheck check = (AccountVerificationTwoCheck) o;
        return verifyType == check.verifyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(verifyType);
    }

    public AccountVerificationTwoCheck() {

    }

    public AccountVerificationTwoCheck(AccountVerificationTwoEnum verifyType, Integer option) {
        this.verifyType = verifyType;
        this.option = option;
    }
}
