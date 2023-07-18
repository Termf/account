package com.binance.account.data.entity.certificate;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * world-check 渠道用户 审核记录
 * @author mikiya.chen
 * @date 2020/3/3 4:47 下午
 */
@NoArgsConstructor
@Data
public class UserChannelWckAuditLog {

    private Long id;

    private String caseId;

    private Integer auditorSeq;

    private Long auditorId;

    private Boolean isValid;

    private String memo;

    private Date createTime;

    private Long isPep;

    private Long sanctionsHits;

    private String failReason;

    public UserChannelWckAuditLog(String caseId, Integer auditorSeq, Long auditorId, Boolean isValid, String memo, Long isPep, Long sanctionsHits, String failReason) {
        this.caseId = caseId;
        this.auditorSeq = auditorSeq;
        this.auditorId = auditorId;
        this.isValid = isValid;
        this.memo = memo;
        this.isPep = isPep;
        this.sanctionsHits = sanctionsHits;
        this.failReason = failReason;
    }
}
