package com.binance.account.data.entity.certificate;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import com.binance.master.commons.ToString;

@Setter
@Getter
public class KycCertificate extends ToString{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1174706023705055472L;

	private Long userId;

    private Integer kycType;

    private Integer kycLevel;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String messageTips;

    private String baseFillStatus;
    
    private String baseSubStatus;

    private String baseFillTips;

    private String addressStatus;

    private String addressTips;

    private String bindMobile;

    private String jumioStatus;

    private String jumioTips;

    private String faceStatus;

    private String faceTips;

    private String remark;
    
    private String mobileCode;
    
    private String googleFormStatus;

    private String googleFormTips;

    private String fiatPtStatus;

    private String fiatPtTips;

    private String faceOcrStatus;

    private String faceOcrTips;
    
    private boolean lockOne;
    
    private String flowDefine;

    private String operator;
}