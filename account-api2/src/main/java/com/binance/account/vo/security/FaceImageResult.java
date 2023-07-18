package com.binance.account.vo.security;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-09-14 17:28
 */
@Setter
@Getter
public class FaceImageResult implements Serializable {
    private static final long serialVersionUID = -8553880583264096825L;

    /** 用户ID */
    private Long userId;
    /** 补充信息(原样返回，做日志) */
    private String extra;
    /** 图片1 地址 */
    private String faceAction1;
    /** 图片2 地址 */
    private String faceAction2;
    /** 图片3 地址 */
    private String faceAction3;
    /** 最佳图片 地址 */
    private String faceBest;

    public boolean hasImage() {
        return !StringUtils.isAllBlank(faceAction1, faceAction2, faceAction3, faceBest);
    }

}
