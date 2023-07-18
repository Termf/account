package com.binance.account.domain.bo;

import com.binance.master.commons.ToString;

public class CapitalWithdrawRedisVerify extends ToString {
    private String amount;
    private String address;
    private String addressTag;

    public String getAddressTag() {
        return addressTag;
    }

    public void setAddressTag(String addressTag) {
        this.addressTag = addressTag;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
