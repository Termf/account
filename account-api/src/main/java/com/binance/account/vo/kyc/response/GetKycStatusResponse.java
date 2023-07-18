package com.binance.account.vo.kyc.response;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateNextStep;
import com.binance.account.vo.kyc.KycFillInfoVo;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
@ApiModel("获取kyc状态")
@Setter
@Getter
public class GetKycStatusResponse extends KycFlowResponse {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5799070175073147944L;

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
    
    private String googleFormStatus;

    private String googleFormTips;
    
    private boolean kycFaceSwitch;
    
    private KycCertificateNextStep nextStep;
    
    private KycFillInfoVo base;
    
    private String qrCode;

}
