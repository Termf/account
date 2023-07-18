package com.binance.account.data.entity.certificate;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserChannelRiskRatingRule implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -561720333813464334L;

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