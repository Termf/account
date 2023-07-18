package com.binance.account.vo.kyc;

import com.binance.account.common.enums.KycCertificateKycType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class KycCertificateVo implements Serializable {

    private static final long serialVersionUID = -7716452077834081877L;

    private Long userId;

    private KycCertificateKycType kycType;

    private Integer kycLevel;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String messageTips;

    private String baseFillStatus;

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
    
    private KycFillInfoVo baseInfo;
    
    private KycFillInfoVo addressInfo;
    
    private JumioVo jumioVo;

    private String googleFormStatus;

    private String googleFormTips;

    private String fiatPtStatus;

    private String fiatPtTips;
    
    private String faceOcrStatus;

    private String faceOcrTips;
}
