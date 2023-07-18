package com.binance.account.data.entity.certificate;

import lombok.Data;

@Data
public class JumioUpload{

    private String merchantIdScanReference;

    private String frontsideImage;

    private String backsideImage;

    private String faceImage;

    private String country;

    private String idType;
}
