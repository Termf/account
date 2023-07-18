package com.binance.account.domain.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FutureRes implements Serializable {

    public static final String SUCCESS = "success";

    private String code;

    private String mgs;
}
