package com.binance.account.common.query;

import com.binance.master.commons.Pagination;
import lombok.Data;

@Data
public class UserChannelRiskRatingQuery extends Pagination {

    private static final long serialVersionUID = 8727833252024626683L;

    private Long userId;

    private String email;

    private String channelCode;

    private String tierLevel;

    private String name;

    private String status;

    private String failReason;

    private String worldCheckStatus;

    private String worldCheckFailReason;

    private String regularReviewStatus;

    private String auditorName;

    private String auditRemark;

    private String riskRatingLevel;

    private String citizenshipCountry;

    private String residenceCountry;

    private String cardCountry;

    private String startAuditTime;

    private String endAuditTime;

    private String startCreateTime;

    private String endCreateTime;
}
