package com.binance.account.vo.face.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2018-12-07 18:34
 */
@ApiModel("人脸识别初始化结果")
@Setter
@Getter
public class FaceInitResponse extends ToString {

    /** SDK 端初始化生产的二维码 */
    private String qrCode;

    /** PC 端初始化生产的人脸识别页面 */
    private String livenessUrl;

    /** PC 端初始化后生产的人脸识别业务编号  */
    private String bizNo;

    /** 主要做日志用 */
    private Long userId;

    /** 主要做日志用 */
    private String transId;
    
    /**
     * 是否为锁定具体某一个用户的KYC认证人脸识别（相当于KYC认证不是多流程的情况:参考US的kyc认证）
     */
    private boolean kycLockOne;
    
    /**二维码有效周期（秒）*/
    private long qrCodeValidSeconds;

}
