package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserChannelRiskRating implements Serializable {
    private static final long serialVersionUID = 1915041342888947449L;

    private Integer id;

    private Long userId;

    private String channelCode;

    private String tierLevel;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;
    
    private BigDecimal totalLimit;

    private BigDecimal yearlyLimit;

    private BigDecimal withdrawDailyLimit;

    private BigDecimal withdrawMonthlyLimit;

    private BigDecimal withdrawYearlyLimit;

    private BigDecimal withdrawTotalLimit;

    private String limitUnit;
    
    private String applyAmount;

    private String name;
    
    private String birthday;

    private String status;
    
    private String failReason;

    private String worldCheckStatus;
    
    private String worldCheckFailReason;

    private String regularReviewStatus;

    private String auditorName;

    private Date auditTime;

    private String auditRemark;

    private String riskRatingLevel;

    private BigDecimal riskRatingScore;

    private String citizenshipCountry;

    private String residenceCountry;

    private String cardCountry;

    private String ipAddress;

    private Date createTime;

    private Date updateTime;

}
