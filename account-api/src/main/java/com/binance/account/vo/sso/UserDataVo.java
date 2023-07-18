package com.binance.account.vo.sso;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户数据实体类
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
public class UserDataVo {
    private Long userId;
    private String afsSecurityLevel;
    private String afsSecurityScore;
    private Integer commissionStatus;
}
