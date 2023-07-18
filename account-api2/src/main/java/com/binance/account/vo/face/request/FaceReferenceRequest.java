package com.binance.account.vo.face.request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-12-18 14:08
 */
@Setter
@Getter
public class FaceReferenceRequest implements Serializable {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("来源类型")
    private String sourceType;

    @ApiModelProperty("来源照片")
    private String sourceImage;

    @ApiModelProperty("来源照片压缩倍数")
    private Double sourceScale;

    @ApiModelProperty("业务正常通过后的对照照片")
    private String refImage;

    @ApiModelProperty("待业务验证的临时检测照片")
    private String checkImage;

    @ApiModelProperty("检测照片的质量")
    private Double refQuality;

    @ApiModelProperty("检测照片的质量阈值")
    private Double qualityThreshold;

    @ApiModelProperty("照片的旋转角度")
    private Integer orientation;

    @ApiModelProperty("备注信息")
    private String remark;

    /**
     * 检查对比照片的内容
     */
    @ApiModelProperty("待业务验证的临时检测照片数据")
    private byte[] checkImageData;

}
