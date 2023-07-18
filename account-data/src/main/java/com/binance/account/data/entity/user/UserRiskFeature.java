package com.binance.account.data.entity.user;

import java.util.Date;

import com.binance.account.common.enums.UserRiskStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserRiskFeature {
	private Long id;// 自增id
	private Long userId;// 用户id
	private String flowId;// 流程id
	private String ip;// 用户发起流程ip
	private UserRiskStatus status;// 0新增 1处理中 2处理完
	private Boolean riskResult;// 0未命中规则 1命中规则
	private String features;// 命中风控规则信息
	private Date updateTime;// 更新时间
	private Date createTime;// 创建时间
}
