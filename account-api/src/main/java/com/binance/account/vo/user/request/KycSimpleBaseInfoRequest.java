package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.Date;

@Data
public class KycSimpleBaseInfoRequest extends ToString {
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nationality;
    private String idNo;
    private String idType;
    private Date dob;
}

