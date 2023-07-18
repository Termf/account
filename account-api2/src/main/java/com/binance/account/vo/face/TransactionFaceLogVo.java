package com.binance.account.vo.face;

import com.binance.account.common.enums.TransFaceLogStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-12-21 17:38
 */
@Setter
@Getter
public class TransactionFaceLogVo implements Serializable {

    private static final long serialVersionUID = -133693256774915282L;

    private Long id;

    private Long userId;

    private String email;

    private String userRemark;

    private String transId;

    private String transType;

    private TransFaceLogStatus status;

    private Date createTime;

    private Date updateTime;

    /** 1-个人认证 2-企业认证 */
    private Integer certificateType;

    /** 个人认证或者企业认证的业务ID */
    private Long certificateId;

    private String faceStatus;

    private String faceRemark;

    private String failReason;

    /**
     * 提现业务标识
     */
    private String withdrawId;

    /**
     * 是否为锁定具体某一个用户的KYC认证人脸识别（相当于KYC认证不是多流程的情况:参考US的kyc认证）
     */
    private boolean kycLockOne;
}
