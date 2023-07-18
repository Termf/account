package com.binance.account.common.enums;

/**
 * @author liliang1
 * @date 2019-04-25 20:14
 */
public enum CertificateType {

    USER(1),
    COMPANY(2),
    UNVERIFIED(-1);

    private int code;

    CertificateType(int code) {
        this.code = code;
    }

    public static CertificateType getByCode(Integer code) {
        if (code == null) {
            return UNVERIFIED;
        }
        switch (code) {
            case 1:
                return USER;
            case 2:
                return COMPANY;
            default:
                return UNVERIFIED;
        }
    }

    public int getCode() {
        return code;
    }
}
