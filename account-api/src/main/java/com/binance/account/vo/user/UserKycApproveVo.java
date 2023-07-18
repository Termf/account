package com.binance.account.vo.user;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lw
 *         <p>
 *         2018/04/28
 */
@Data
public class UserKycApproveVo{

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户填写的信息
     */
    private BaseInfo baseInfo;

    /**
     * 检查通过的信息
     */
    private CheckInfo checkInfo;

    /**
     * 认证时间
     */
    private Date approveTime;

    /**
     * JUMIO 的唯一标识
     */
    private String scanReference;

    /** 1-个人认证 2-企业认证 */
    private Integer certificateType;

    /**
     * KYC对应记录的ID
     */
    private Long certificateId;


    @Setter
    @Getter
    public static class BaseInfo implements Serializable {

        /**
         * 名
         */
        String firstName;

        /**
         * 姓
         */
        String lastName;

        /**
         * 中间名
         */
        String middleName;

        /**
         * 生日
         */
        Date dob;

        /**
         * 地址
         */
        String address;

        /**
         * 邮编
         */
        String postalCode;

        /**
         * 城市
         */
        String city;

        /**
         * 国家
         */
        String country;

    }

    @Setter
    @Getter
    public static class CheckInfo {

        /**
         * 类型
         */
        String documentType;

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
         * 证件正面照
         */
        private String front;

        /**
         * 证件正面照链接
         */
        private String frontUrl;

        /**
         * 证件背面照
         */
        private String back;

        /**
         * 证件背面照链接
         */
        private String backUrl;

        /**
         * 脸部照
         */
        private String face;

        /**
         * 脸部照链接
         */
        private String faceUrl;

    }
}