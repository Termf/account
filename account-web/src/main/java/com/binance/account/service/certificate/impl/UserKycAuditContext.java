package com.binance.account.service.certificate.impl;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserKycAuditContext {

    private Long userId;

    private KycStatus auditStatus;

    private String failReason;

    private String memo;

    private UserKyc userKyc;

    private boolean isOcrFlow;

    private String number;

    private String country;

    private String documentType;

    private String jumioId;

    private String scanReference;

    private JumioStatus jumioStatus;

    private IdCardOcrStatus ocrStatus;

    private String jumioSource;
    
    private boolean isForbidCountry;
    
    private UserKycApprove userKycApprove;
    
    private String firstName;
    
    private String lastName;
    
    private String birthday;
    
    public Jumio buildJumio() {
    	Jumio jumio = new Jumio();
    	jumio.setFirstName(firstName);
    	jumio.setLastName(lastName);
    	jumio.setDob(birthday);
    	jumio.setIssuingCountry(country);
    	return jumio;
    }

}
