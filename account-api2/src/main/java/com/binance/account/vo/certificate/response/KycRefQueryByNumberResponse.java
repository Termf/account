package com.binance.account.vo.certificate.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class KycRefQueryByNumberResponse implements Serializable {

    private static final long serialVersionUID = -928898506568698327L;

    private String number;

    private String country;

    private Long userId;

    private String type;

    private Integer certificateType;

    private Date createTime;


}
