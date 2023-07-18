package com.binance.account.vo.certificate;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserChannelRiskRatingVo extends ToString {

    private static final long serialVersionUID = 4186190417531925759L;

    private Integer id;

    private Long userId;

    private String channelCode;

    private String tierLevel;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;

    private BigDecimal yearlyLimit;

    private BigDecimal totalLimit;

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

    private String addressStatus;
}
