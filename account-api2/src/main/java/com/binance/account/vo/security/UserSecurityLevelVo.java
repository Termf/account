package com.binance.account.vo.security;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/10/24
 */
@Getter
@Setter
public class UserSecurityLevelVo implements Serializable {

    private static final long serialVersionUID = -3837743879104845589L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户交易等级
     */
    private String securityLevel;
}
