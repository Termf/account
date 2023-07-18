package com.binance.account.common.enums;

/**
 * @author liliang1
 * @date 2019-03-04 17:11
 */
public enum KycSubStatus {

    /** 需要上传 JUMIO */
    JUMIO,

    /**
     * 等待做人脸识别
     */
    FACE_PENDING,

    /**
     * 正在审核
     */
    AUDITING,

    /**
     * 人脸识别OCR
     */
    FACE_OCR,

    /**
     * 基础信息提交
     */
    BASIC,
    ;

}
