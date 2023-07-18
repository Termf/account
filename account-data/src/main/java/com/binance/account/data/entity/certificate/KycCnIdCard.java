package com.binance.account.data.entity.certificate;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
public class KycCnIdCard implements Serializable {
    private Long userId;

    private String firstName;

    private String middleName;

    private String lastName;

    private String number;

    private String status;

    private String failReason;

    private Date createTime;

    private Date approveTime;

    private Integer flagUser;

    private String fiatStatus;

    private String fiatRemark;

    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}