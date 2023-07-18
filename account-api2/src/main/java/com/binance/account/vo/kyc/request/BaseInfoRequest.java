package com.binance.account.vo.kyc.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import com.binance.account.common.enums.KycFillInfoGender;

import io.swagger.annotations.ApiModel;

@ApiModel("基础信息")
@Getter
@Setter
public class BaseInfoRequest extends KycFlowRequest {

    private static final long serialVersionUID = -7795645724514533988L;

    private String fillType;

    private String status;

    private Date createTime;

    private Date updateTime;

    private String firstName;

    private String middleName;

    private String lastName;

    private KycFillInfoGender gender;

    private String birthday;

    private String taxId;

    private String country;
    
    private String residenceCountry;

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
    
    private boolean oldApi = true;
    
    private String flowDefine;
    
    private String idcardNumber;

    private String tin;
    // 证件类型
    private String idType;
    // 证件发行者
    private String issuer;
    // 住所街道地址
    private String suburb;
    // 签证国家
    private String countryOfIssue;

    
}
