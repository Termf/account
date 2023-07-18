package com.binance.account.data.entity.country;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Country extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 8961596206886172667L;

    private String code;

    private String code2;

    private String en;

    private String cn;
    
    private String nationality;

    private String mobileCode;

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public void setCode2(String code2) {
        this.code2 = code2 == null ? null : code2.trim();
    }

    public void setEn(String en) {
        this.en = en == null ? null : en.trim();
    }

    public void setCn(String cn) {
        this.cn = cn == null ? null : cn.trim();
    }

    public void setMobileCode(String mobileCode) {
        this.mobileCode = mobileCode == null ? null : mobileCode.trim();
    }
}
