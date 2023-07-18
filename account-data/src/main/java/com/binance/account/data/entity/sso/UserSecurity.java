package com.binance.account.data.entity.sso;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户安全信息实体类
 * <p>
 * <p>
 * Copyright (C) 上海比捷网络科技有限公司.
 * </p>
 *
 * @author YueYouqian
 * @since 1.0
 */
@Getter
@Setter
public class UserSecurity {
    private int id;
    private Long userId;
    private Date lastLoginTime;
    private int loginFailedNum;
    private Date loginFailedTime;
    private String type;
    private String status;
    private String secretKey;
    private boolean mobileSecurity;
}
