package com.binance.account.vo.certificate;

import java.util.Date;

import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserChannelRiskRatingRuleVo extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = 778043137661905465L;
	
	private Integer id;

    private Integer riskRatingId;

    private Long userId;

    private String channelCode;

    private String ruleNo;

    private String ruleName;

    private String ruleValue;

    private String ruleLevel;

    private String ruleScore;

    private String auditor;

    private Date auditTime;

    private Date createTime;

    private Date updateTime;

}
