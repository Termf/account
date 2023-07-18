package com.binance.account.data.entity.security;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResetBigDataLog {
	private Long id;
	private Long userId;
	private String transId;
	private Integer score; // 放大100倍存储
	private Date batchTime;
	private Date createTime;
	// ---扩展字段----
	private String resetType;
	private String email;
	private String protectedMode;
}