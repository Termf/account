package com.binance.account.vo.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by mengjuan on 2018/9/26.
 */
@Getter
@Setter
public class UserAgentRewardVo implements Serializable{
    private static final long serialVersionUID = 1210923512044072509L;

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

    private String insertTimeStr;
}
