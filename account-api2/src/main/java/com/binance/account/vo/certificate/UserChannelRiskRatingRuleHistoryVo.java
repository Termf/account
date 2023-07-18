package com.binance.account.vo.certificate;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.Date;

@Data
public class UserChannelRiskRatingRuleHistoryVo extends ToString {

    private static final long serialVersionUID = 3765453771078842530L;

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
