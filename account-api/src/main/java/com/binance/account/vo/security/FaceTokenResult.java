package com.binance.account.vo.security;

import com.alibaba.fastjson.JSON;
import com.binance.master.utils.LogMaskUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-09-13 17:58
 */
@Setter
@Getter
public class FaceTokenResult implements Serializable {
    private static final long serialVersionUID = -6525053023432131456L;

    /** 本次token是否能获取成功 */
    private boolean success;

    /** 提示语和错误信息描述 */
    private String message;

    /**
     * 用于标识一些特殊的错误信息, 确认是否方便引入其他处理(目前不是所有错误都设置有errorCode)
     * eg:NO_FACE_FOUND 没有识别到人脸, MULTIPLE_FACES 识别到多张人脸
     */
    private String errorCode;

    /** 获取到的token */
    private String token;

    /** 做活体的操作的URL链接 */
    private String livenessUrl;

    /** token过期时间 */
    private Date expireTime;

    /** 本次请求的业务流水号 */
    private String bizNo;

    /** 用户标志加密后的标识, 可以用于face++的控制台查询 */
    private String uuid;

    /** 本次业务Face++验证结果查询的业务标识(目前使用PC端的不会有返回值，需要在回调信息中获取) */
    private String bizId;

    /** 本次请求face++的httpCode值 */
    private Integer httpCode;

    /** 只有在启用图片放缩时返回 图片放缩倍数 */
    private Double imageScaleMultiple;

    /** 只有在启用图片放缩时返回 对比图片 */
    private byte[] contrastImage;

    /** 原始图片，主要用于多人脸时进行截取功能时用 */
    private byte[] sourceImage;
    /** 原始图片地址，主要用于多人脸截取时使用 */
    private String sourceImagePath;
    /** 原始图片名称，主要用于多人脸截取时使用 */
    private String sourceImageName;


    @Override
    public String toString() {
        return LogMaskUtils.maskJsonString(JSON.toJSONString(this), "contrastImage", "sourceImage");
    }
}
