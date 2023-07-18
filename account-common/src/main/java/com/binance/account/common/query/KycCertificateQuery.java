package com.binance.account.common.query;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class KycCertificateQuery extends Pagination {

    private static final long serialVersionUID = -4081543026128818334L;

    private Long userId;

    private String email;

    private KycCertificateKycType kycType;

    private Integer kycLevel;

    private KycCertificateStatus baseFillStatus;

    private KycCertificateStatus addressStatus;

    private String bindMobile;

    private KycCertificateStatus jumioStatus;

    private KycCertificateStatus faceStatus;

    private String fiatPtStatus;

    private KycCertificateStatus faceOcrStatus;

    private Date startTime;

    private Date endTime;
    
    private Date upStartTime;

    private Date upEndTime;

    public Integer getKycTypeValue() {
        if (this.kycType == null) {
            return null;
        }else {
            return this.kycType.getCode();
        }
    }

}
