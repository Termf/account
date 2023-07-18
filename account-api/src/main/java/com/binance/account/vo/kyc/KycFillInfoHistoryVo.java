package com.binance.account.vo.kyc;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class KycFillInfoHistoryVo implements Serializable {
    private static final long serialVersionUID = -4310539349313844783L;

    private Long id;

    private Long userId;

    private String fillType;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String firstName;

    private String middleName;

    private String lastName;

    private Byte gender;

    private String birthday;

    private String taxId;

    private String country;

    private String regionState;

    private String city;

    private String address;

    private String postalCode;

    private String nationality;

    private String billFile;

    private String companyName;

    private String companyAddress;

    private String contactNumber;

    private String registerName;

    private String registerEmail;

    private String source;

    private String idmTid;

    private String refType;

    private String refId;

    private String issuingAuthority;

    private String expiryDate;

}
