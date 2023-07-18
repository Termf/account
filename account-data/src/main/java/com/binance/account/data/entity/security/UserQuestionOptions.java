package com.binance.account.data.entity.security;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserQuestionOptions {
	private Long id;//
	private Long userId;// 用户id
	private String riskType;// 风控问题类型
	private String options;// '风控问题选项
	private Date createTime;// 创建时间
}
