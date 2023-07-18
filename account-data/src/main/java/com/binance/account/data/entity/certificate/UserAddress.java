package com.binance.account.data.entity.certificate;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户地址信息
 * 
 * @author alex
 */
@Data
public class UserAddress implements Serializable {

    private static final long serialVersionUID = 329349123984182210L;

    public enum Status {

        /**
         * 0 未审核
         */
        PENDING,

        /**
         * 1 后台审核通过
         */
        PASSED,

        /**
         * 2 后台审核拒绝
         */
        REFUSED,

        /**
         * 3 已过期
         */
        EXPIRED,
        /**
         * 4 已取消
         */
        CANCELLED,
        /**
         * 5 未审核
         */
        WAITING,

    }

    private Long id;

    private Long userId;

    private UserAddress.Status status;

    private Date createTime;

    private Date updateTime;

    private String approver;

    private Date approveTime;

    private Integer daySubmitCount;

    private String checkFirstName;

    private String checkLastName;

    private String streetAddress;

    private String postalCode;

    private String city;

    private String country;

    private String addressFile;

    private String failReason;

    private String sourceOfFund;

    private String estimatedTradeVolume;

    @JSONField(serialize = false)
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(country)) {
            sb.append(country);
            sb.append(" ");
        }
        if (StringUtils.isNotBlank(city)) {
            sb.append(city);
            sb.append(" ");
        }
        if (StringUtils.isNotBlank(streetAddress)) {
            sb.append(streetAddress);
        }
        return sb.toString().trim();
    }

}