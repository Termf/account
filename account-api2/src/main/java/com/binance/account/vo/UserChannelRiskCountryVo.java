package com.binance.account.vo;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.Date;

@Data
public class UserChannelRiskCountryVo extends ToString {

    private static final long serialVersionUID = -605126505379514564L;

    private String countryCode;

    private String channelCode;

    private String riskLevel;

    private String riskScore;

    private String memo;

    private String auditor;

    private Date auditTime;

    private Date createTime;

    private Date updateTime;
}
