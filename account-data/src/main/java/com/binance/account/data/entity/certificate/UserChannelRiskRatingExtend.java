package com.binance.account.data.entity.certificate;

import lombok.Data;

import java.util.Date;

@Data
public class UserChannelRiskRatingExtend {

    private Long id;

    private Long userId;

    private String channelCode;

    private String attrKey;

    private String attrValue;

    private Date createTime;

    private Date updateTime;

}