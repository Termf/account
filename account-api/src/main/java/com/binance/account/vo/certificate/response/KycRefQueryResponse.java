package com.binance.account.vo.certificate.response;

import com.binance.account.util.IdNumberMaskUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class KycRefQueryResponse implements Serializable {

    private Long userId;

    private Integer kycLevel;

    private String kycType;

    private String firstName;

    private String middleName;

    private String lastName;

    private String countryCode;

    private String companyName;

    private String idNumber;

    private Date createTime;

    private Date updateTime;

    /**
     * 身份证号掩码
     * @param idNumber
     */
    public void setIdNumber(String idNumber) {
        this.idNumber = IdNumberMaskUtil.getIdNumberMark(idNumber);
    }

}
