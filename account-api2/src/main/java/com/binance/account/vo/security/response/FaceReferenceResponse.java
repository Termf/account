package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-09-17 18:02
 */
@ApiModel("人脸对比照结果")
@Setter
@Getter
public class FaceReferenceResponse implements Serializable {

    private static final long serialVersionUID = -7547061154351331231L;

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
