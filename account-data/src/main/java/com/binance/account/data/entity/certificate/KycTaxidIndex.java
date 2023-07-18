package com.binance.account.data.entity.certificate;

public class KycTaxidIndex {
    private String taxId;

    private Long userId;

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId == null ? null : taxId.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}