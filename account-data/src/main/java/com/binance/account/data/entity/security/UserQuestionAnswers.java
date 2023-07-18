package com.binance.account.data.entity.security;

import java.io.Serializable;
import java.util.Date;

import com.binance.account.common.enums.ResetAnswerStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserQuestionAnswers implements Serializable {
	private static final long serialVersionUID = -250517954432701407L;
	private Long id;
	private Long userId;
	private Long answerId; // 每一次答题标识
	private String flowId;// 当前重置请求记录id
	private String flowType;// 重置请求类型 如google mobile enable
	private Long questionId;// 问题表id
	private String questionType;// 问题类型对应risk_type
	private String options;// 选项
	/**
	 * 答案json串
	 */
	private String answers;
	/**
	 * 风控给出的正确答案
	 */
	private String correctAnswer;
	/**
	 * 加权分数，加权分数*100存储
	 */
	private Integer score;
	/**
	 * 本题得分，实际分数*100存储
	 */
	private Integer point;
	private ResetAnswerStatus status;
	private Date updateTime;
	private Date createTime;

	/**
	 * 是否新设备
	 */
	private Boolean newDevice;
	/**
	 * 答题是否通过
	 */
	private Boolean pass;
}
