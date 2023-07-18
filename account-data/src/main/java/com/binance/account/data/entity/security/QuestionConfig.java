package com.binance.account.data.entity.security;

import java.util.Date;
import java.util.List;

import com.binance.account.common.enums.QuestionSceneEnum;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuestionConfig {
	private Integer id;// 自增ID
	private QuestionSceneEnum scene;//场景
	private String group; // 那套题
	private List<String> rules;//规则列表
	private Date createTime;// 创建时间
	private Date updateTime;// 更新时间
	private String operator; // 操作人
}
