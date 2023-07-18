package com.binance.account.data.entity.security;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-09-14
 */
@Setter
@Getter
public class UserFaceReference implements Serializable {
    private static final long serialVersionUID = -4146599673340319536L;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    private String sourceType;

    private String sourceImage;

    private Double sourceScale;

    private Boolean needScale;

    private Double refScale;

    private String refImage;

    private String checkImage;

    private String refImageName;

    private Double refQuality;

    private Double qualityThreshold;

    private Integer orientation;

    private String remark;

}