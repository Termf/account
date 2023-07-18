package com.binance.account.data.entity.sso;


import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户实体类
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
public class User implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1910839305321196944L;
	private Long userId;
    private String email;
    private String password;
    private String salt;
    private String agentId;
    private Integer agentLevel;
    private Integer emailVerified;
    private String registerChannel;
    private Double agentRewardRatio;
    private Double takerCommission;
    private Double makerCommission;
    private Double buyerCommission;
    private Double sellerCommission;
    private String appActiveCode;
}
