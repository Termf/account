package com.binance.account.vo.sso;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户登录日志实体类
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
public class UserLogVo {
    private int id;
    private Long userId;
    private Date loginTime;
    private String loginResult;
    private String ipAddress;
    private String ipLocation;
    private String resInfo;
    private String clientType;
    private String versionCode;
    private String browser;
}
