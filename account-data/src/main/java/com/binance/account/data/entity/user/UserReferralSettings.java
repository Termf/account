package com.binance.account.data.entity.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户返佣设置
 * 
 * @author zhenlei sun
 */
@Data
public class UserReferralSettings implements Serializable {

    private static final long serialVersionUID = -3339683541337422695L;

    private Long id;

    private Long userId;

    private String regionState;

    private String city;

    private String postalCode;

    private String address;

    private String taxInfo;

    private String promotionMethods;

    private String outlets;

    private String notes;

    private Date createTime;

    private Date updateTime;
}
