package com.binance.account.vo.country;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2018-11-05 18:23
 */
@Setter
@Getter
public class CountryVo extends ToString {

    private static final long serialVersionUID = -7442831037539461732L;

    private String code;

    private String code2;

    private String en;

    private String cn;

    private String mobileCode;

    private Boolean kycForbid; //是否为kyc不合规国籍

    private String countryImageUrl;

}
