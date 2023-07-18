package com.binance.account.data.entity.certificate;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserCertificateIndex extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 3099739958060685638L;

    private String number;

    private String country;

    private Long userId;

    private String type;

    private Integer certificateType;

    private Date createTime;

    public void setNumber(String number) {
        this.number = number == null ? null : number.trim();
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

}
