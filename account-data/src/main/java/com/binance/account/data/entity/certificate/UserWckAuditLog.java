package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;
import lombok.NoArgsConstructor;

/**
 * world-check one 审核记录
 */
@NoArgsConstructor
@Data
public class UserWckAuditLog {
    private Long id;

    private Long kycId;

    private Integer auditorSeq;

    private Long auditorId;

    private Boolean isValid;

    private String memo;

    private Date createTime;
    
    private Long isPep;
    
    private Long isAdverse;

    public UserWckAuditLog(Long kycId, Integer auditorSeq, Long auditorId, Boolean isValid, String memo, Long isPep, Long isAdverse) {
        this.kycId = kycId;
        this.auditorSeq = auditorSeq;
        this.auditorId = auditorId;
        this.isValid = isValid;
        this.memo = memo;
        this.isPep = isPep;
        this.isAdverse = isAdverse;
    }
}