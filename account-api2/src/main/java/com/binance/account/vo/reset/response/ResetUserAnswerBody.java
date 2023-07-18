package com.binance.account.vo.reset.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.binance.account.common.enums.ResetAnswerStatus;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户2FA重置,答题记录返回实体")
@Getter
@Setter
public class ResetUserAnswerBody extends ToString {
	private static final long serialVersionUID = -5481980980468775848L;

	@ApiModelProperty("问题和答案")
	private List<ResetAnswersInfo> answers;
	@ApiModelProperty("用户答题次数")
	private Integer protectedCounts;
	@ApiModelProperty("配置的答题失败最大阈值")
	private Integer configCounts;
	
	
	@ApiModel("用户2FA重置,问题与答案实体")
	@Getter
	@Setter
	public static class ResetAnswersInfo extends ToString {
		private static final long serialVersionUID = -8843087278972022982L;
		private Long id;
		private Long userId;
		private String flowId;// 当前重置请求记录id
		private String flowType;// 重置请求类型 如google auth
		private Long questionId;// 问题表id
		private String questionType;// 问题类型对应risk_type,不用存数据库
		private String remark; // 题库问题描述信息
		private String options;
		/**
		 * 答案json串
		 */
		private String answers;
		/**
		 * 风控给出的正确答案
		 */
		private String correctAnswer;
		/**
		 * 加权分数
		 */
		private BigDecimal score;
		/**
		 * 实际分数
		 */
		private BigDecimal point;
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
}