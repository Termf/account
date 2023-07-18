package com.binance.account.vo.kyc.request;

import com.binance.master.commons.ToString;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdditionalInfoRequest extends ToString {

    private static final long serialVersionUID = -5603679679109399144L;

    @NotNull
    private Long userId;

    private String postalCode; // 邮编

    private String issuingAuthority; // 发行机构

    private String expiryDate; // 证件有效期
}
