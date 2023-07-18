package com.binance.account.service.security.filter;

import com.binance.account.common.enums.UserSecurityResetStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostResult {
	UserSecurityResetStatus status;
	String msgKey;
}
