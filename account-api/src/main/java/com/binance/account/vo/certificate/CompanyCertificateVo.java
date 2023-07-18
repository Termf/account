package com.binance.account.vo.certificate;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.JumioStatus;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CompanyCertificateVo{
    private static final long serialVersionUID = 5975053744616160946L;

    private Long id;

    private Long userId;

    private String email;

    private String companyName;

    private String companyAddress;

    private String companyCountry;

    private String applyerName;

    private String applyerEmail;

    private CompanyCertificateStatus status;

    private String info;

    private String contactNumber;

    private Date insertTime;

    private Date updateTime;

    private Integer redoJumio;

    /** JUMIO 的唯一标识 */
    private String scanReference;

    /** JUMIO 的状态 */
    private String jumioStatus;

    /** 人脸识别状态 */
    private String faceStatus;

    /** 人脸识别备注信息 */
    private String faceRemark;

    private String transFaceLogId;

    /**
     * Jumio返回的信息
     */
    private CompanyCertificateVo.CheckInfo checkInfo;

    @Setter
    @Getter
    public static class CheckInfo {

        String id;

        /**
         * 类型
         */
        String documentType;

        /**
         * 审核状态
         */
        JumioStatus status;

        /**
         * 发行国家
         */
        String issuingCountry;

        /**
         * 过期时间
         */
        String expiryDate;

        /**
         * 名
         */
        String firstName;

        /**
         * 姓
         */
        String lastName;

        /**
         * 生日
         */
        String dob;

        /**
         * 证件号码
         */
        String number;

        /**
         * 邮编
         */
        String postalCode;

        /**
         * 城市
         */
        String city;

        /**
         * 证件地址
         */
        String address;

        /**
         * 来源
         */
        String source;

        /**
         * jumio Token 用于查询状态
         */
        private String scanReference;

        /**
         * jumio AuthToken 调用上传控件
         */
        private String authToken;

        /**
         * 证件正面照
         */
        private String front;

        /**
         * 证件背面照
         */
        private String back;

        /**
         * 脸部照
         */
        private String face;

    }


    public void setCompanyName(String companyName) {
        this.companyName = companyName == null ? null : companyName.trim();
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress == null ? null : companyAddress.trim();
    }

    public void setApplyerName(String applyerName) {
        this.applyerName = applyerName == null ? null : applyerName.trim();
    }

    public void setApplyerEmail(String applyerEmail) {
        this.applyerEmail = applyerEmail == null ? null : applyerEmail.trim();
    }

    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }


    public void setCompanyCountry(String companyCountry) {
        this.companyCountry = companyCountry == null ? null : companyCountry.trim();
    }
}
