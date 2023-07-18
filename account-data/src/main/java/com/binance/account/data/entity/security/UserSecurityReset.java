package com.binance.account.data.entity.security;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 重置流程的实体类
 * @author liliang1
 */
@Setter
@Getter
public class UserSecurityReset implements Serializable {
    private static final long serialVersionUID = -3289223347342799472L;

    private String id;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private UserSecurityResetType type;

    private UserSecurityResetStatus status;

    private Integer certificateType;

    private String front;

    private String back;

    private String hand;

    private Date auditTime;

    private String auditMsg;

    private Integer questionFailTimes;

    private Integer questionSeq;

    private Integer questionScore;

    private String scanReference;

    private String jumioToken;

    private String jumioStatus;

    private String jumioRemark;

    private String issuingCountry;

    private String idNumber;

    private String documentType;

    private String applyIp;

    private String jumioIp;

    private String faceIp;

    private String failReason;

    private String faceStatus;

    private String faceRemark;

    /** 用于辅助检查KYC信息时的状态 */
    private Long certificateId;

    /** 用于辅助检查kyc信息时的信息值 */
    private boolean certificateIsForbidPassed;
    
    /** 用于辅助检查kyc是否是新流程 */
    private boolean newVersion;
    
    /** 用于辅助检查kyc是否是新流程 */
    private String certificateStatus;
    
    /** 认证来源*/
    private String certificateSource;
    
}