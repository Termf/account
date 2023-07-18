package com.binance.account.data.entity.certificate;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class UserChannelRiskCountry implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4702758443939286827L;
	
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