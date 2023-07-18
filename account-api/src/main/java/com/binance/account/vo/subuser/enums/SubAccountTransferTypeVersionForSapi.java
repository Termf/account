package com.binance.account.vo.subuser.enums;

public enum SubAccountTransferTypeVersionForSapi {

    TRANSFER_IN(1, "转入"),
    TRANSFER_OUT(2, "转出");
    private int code;
    private String desc;

    SubAccountTransferTypeVersionForSapi(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public static SubAccountTransferTypeVersionForSapi getByCode(int code) {
        for (SubAccountTransferTypeVersionForSapi status : SubAccountTransferTypeVersionForSapi.values()) {
            if (code == status.getCode()) {
                return status;
            }
        }
        return null;
    }
}
