package com.binance.account.data.entity.certificate;

import java.io.Serializable;

import lombok.Data;

@Data
public class JumioToken implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3589745111547354229L;

    private String merchantIdScanReference;

    private String successUrl;

    private String errorUrl;

    private String captureMethod;
}
