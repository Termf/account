package com.binance.account.vo.certificate;

import com.binance.account.common.enums.KycSubStatus;
import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2019-03-04 17:00
 */
@Setter
@Getter
public class KycDetailResponse extends ToString {

    private static final long serialVersionUID = -3394140914672068347L;

    /**
     * 当前KYC的认证状态 -1:未验证 0-正在审核流程 1-已通过 2-已拒绝
     */
    private Integer kycStatus;

    /**
     * KYC认证提示语信息
     */
    private String kycMessage;

    /**
     * KYC类型 user:个人认证 company:企业认证
     */
    private String type;

    /**
     * KYC认证流程标识
     */
    private String transId;
    
    /**
     * qrCode二维码用于sdk face验证
     */
    private String qrCode;

    /**
     * JUMIO: 待上传
     * FACE_PENDING: 待人脸识别
     * AUDITING: 等待审核
     */
    private KycSubStatus kycSubStatus;

    private FillInfo fillInfo;

    /**
     * 是否为不合规国籍审核通过
     */
    private Boolean forbidCountryPassed;
    
    /**
     * kycLevel
     */
    private Integer kycLevel;

    @Getter
    @Setter
    public static class FillInfo extends ToString {

        private static final long serialVersionUID = 2591248626099010962L;

        private String firstName;
        private String middleName;
        private String lastName;
        private String companyName;
    }


}
