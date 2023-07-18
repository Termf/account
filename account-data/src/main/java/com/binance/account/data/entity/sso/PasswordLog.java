package com.binance.account.data.entity.sso;

import lombok.Getter;
import lombok.Setter;

/**
 * 密码更新日志实体类
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
public class PasswordLog {
    private Long userId;
    private String ipAddress;
    private Integer type;
    private Integer result;
}
