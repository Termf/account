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
public class AccountVerificationTwoBind implements Serializable {
    private static final long serialVersionUID = -8760014623290617157L;

    /**
     * 2fa验证类型
     */
    private AccountVerificationTwoEnum verifyType;

    /**
     * 是否可选(0-可选，1-必选)
     */
    private Integer option;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountVerificationTwoBind that = (AccountVerificationTwoBind) o;
        return verifyType == that.verifyType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(verifyType);
    }
}
