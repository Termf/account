package com.binance.account.vo.security;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-09-14 14:28
 */
@Setter
@Getter
public class FaceResultVo implements Serializable {
    private static final long serialVersionUID = 3239450573068982454L;

    /** 人脸识别是否通过 */
    private boolean success;

    /** 错误描述信息 */
    private String message;

    /** 错误编码 (只是一些特殊的信息进行了配置, 方便提示语或者错误信息的判断) */
    private String messageCode;

    /** 补充数据 */
    private String extraData;

    /** 本次face++结果查询编号 */
    private String bizId;

    /** 本次业务的流水号 */
    private String bizNo;

    /** 本次人脸识别的验证请ID (可以用在face++的控制台查询) */
    private String verifyRequestId;

    /** 本次人脸识别的对比分数 */
    private Double confidence;

    /** 疑似合成脸 分值 */
    private Double syntheticFaceConfidence;
    /** 疑似合成脸 阈值 */
    private Double syntheticFaceThreshold;

    /** 疑似面具 分值 */
    private Double maskConfidence;
    /** 疑似面具 阈值 */
    private Double maskThreshold;

    /** 疑似屏幕翻拍 分值 */
    private Double screenReplayConfidence;

    /** 疑似屏幕翻拍 阈值 */
    private Double screenReplayThreshold;

}
