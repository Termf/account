package com.binance.account.vo.question;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.binance.account.common.enums.ResetAnswerStatus;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel("问答模块-用户问答记录响应体")
@Setter
@Getter
@ToString
public class QueryLogResponseBody { 
	private List<Body> body; 
	
	@Setter
	@Getter
	@ToString
	public static class Body{
		private Long userId;
		private Long answerId; // 每一次答题标识
		private String flowId;// 当前重置请求记录id
		private String flowType;// 重置请求类型 如google mobile enable
		private Long questionId;// 问题表id
		private String questionType;// 问题类型对应risk_type
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
		 * 加权分数，加权分数*100存储
		 */
		private BigDecimal score;
		/**
		 * 本题得分，实际分数*100存储
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
		
		/**
		 * 用户是否处于保护模式
		 */
		private Boolean inProtectedMode;
		
		/**
		 * 风控后置结果
		 */
		private Boolean riskPostResult;
		/**
		 * 风控后置处理命中规则
		 */
		private String riskPostFeatures;
		/**
		 * 风控后置处理时间戳
		 */
		private Date riskPostTime;
	}
}
