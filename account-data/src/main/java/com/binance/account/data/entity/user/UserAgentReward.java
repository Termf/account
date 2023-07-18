package com.binance.account.data.entity.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用户分佣比例信息
 */
@Getter
@Setter
public class UserAgentReward implements Serializable{

    private static final long serialVersionUID = -5973527496237357411L;
    private Long id;
    
    private Long userId;

    private String batchId;

    private String email;

    private BigDecimal oldAgentRewardRatio;

    private BigDecimal newAgentRewardRatio;

    private BigDecimal agentRewardRatio;

    private String reason;

    private String isRestore;

    private Date expectRestoreTime;

    private Date actualRestoreTime;

    private String applyId;

    private String applyName;

    private Date applyTime;

    private String operatorId;

    private Date operatorTime;

    private Date updateTime;

    private Date createTime;

    private Byte status;

    private Long agentId;

    private String trackSource;

    //--vo
    private String expectRestoreTimeStr;
    
    private String actualRestoreTimeStr;
    
    private String applyTimeStr;
    
    private String operatorTimeStr;

    private String updateTimeStr;
}