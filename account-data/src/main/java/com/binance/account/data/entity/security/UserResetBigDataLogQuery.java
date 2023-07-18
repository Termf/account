package com.binance.account.data.entity.security;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResetBigDataLogQuery {
	private Long userId;
	private String transId;
	private Date startTime;
	private Date endTime;
}
