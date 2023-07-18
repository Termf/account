package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.Map;

@Data
public class ChannelRiskRatingRuleAuditRequest extends ToString {
    private static final long serialVersionUID = -4833729706708330630L;

    private Long userId;

    private Integer riskRatingId;

    private String auditor;

    private Map<String, String> rulesValue;

}
