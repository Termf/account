package com.binance.account.common.query;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Date;

@Data
public class KycRefQuery extends Pagination {

    private KycRefQueryType queryType;

    private String firstName;

    private String lastName;

    private String countryCode;

    private String companyName;

    private String idNumber;

    private Date startTime;

    private Date endTime;

    private String taxId;

    private String regionState;

    private String birthday;

    public enum KycRefQueryType {
        BASIC,
        JUMIO,
        FACE_OCR
    }

    public String getFirstName() {
        return StringUtils.isEmpty(this.firstName) ? null : this.firstName.trim().toLowerCase();
    }

    public String getLastName() {
        return StringUtils.isEmpty(this.lastName) ? null : this.lastName.trim().toLowerCase();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCompanyName() {
        return StringUtils.isEmpty(companyName) ? null : this.companyName.trim().toLowerCase();
    }

    public String getIdNumber() {
        return StringUtils.isEmpty(this.idNumber) ? null : this.idNumber.trim().toLowerCase();
    }
}
