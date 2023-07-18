package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.WckStatus;
import lombok.Data;

import java.util.Date;

/**
 * world-check one审核进度
 */
@Data
public class UserWckAudit {
    private Long kycId;

    private Long userId;

    private String caseSystemId;

    private WckStatus status;

    private Date createTime;

    private Date updateTime;

    private Boolean isDel;
    
    private String issuingCountry;
}