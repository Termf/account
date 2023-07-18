package com.binance.account.vo.user;

import com.binance.account.common.enums.JumioStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.WckStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lw
 *         <p>
 *         2018/04/28
 */
@Data
public class UserKycVo{


    private static final long serialVersionUID = -3611921170806368269L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 审核状态
     */
    private KycStatus status;

    /**
     * 用户填写的信息
     */
    private BaseInfo baseInfo;

    /**
     * Jumio返回的信息
     */
    private CheckInfo checkInfo;

    /**
     * 日提交次数
     */
    private int submitCountDay;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 备注（审核人填写）
     */
    private String memo;

    /** 人脸识别状态 */
    private String faceStatus;

    /** 人脸识别备注信息 */
    private String faceRemark;

    /**
     * JUMIO 的 scanReference
     */
    private String scanReference;

    /**
     * JumioInfo的status
     */
    private String checkStatus;

    /**
     * 拒绝原因描述
     */
    private String failReasonDesc;
    private String failReasonEn;
    private String failReasonCn;

    /**
     * 当前的World Check 状态
     */
    private WckStatus wckStatus;

    private String transFaceLogId;

    /**
     * face idcard ocr status
     */
    private String faceOcrStatus;

    /**
     * face idcard ocr remark
     */
    private String faceOcrRemark;

    @Setter
    @Getter
    public static class BaseInfo implements Serializable {

        private static final long serialVersionUID = 8238447094678805885L;
        /**
         * 名
         */
        private String firstName;

        /**
         * 姓
         */
        private String lastName;

        /**
         * 中间名
         */
        private String middleName;

        /**
         * 曾用名 - 名
         */
        private String formerFirstName;

        /**
         * 曾用名 - 姓
         */
        private String formerLastName;

        /**
         * 曾用名 - 中间名
         */
        private String formerMiddleName;

        /**
         * 生日
         */
        private Date dob;

        /**
         * 国籍
         */
        private String nationality;

        /**
         * 地址
         */
        private String address;

        /**
         * 邮编
         */
        private String postalCode;

        /**
         * 城市
         */
        private String city;

        /**
         * 居住国家
         */
        private String country;

    }

    @Setter
    @Getter
    public static class CheckInfo {

        private String id;


        /**
         * 类型
         */
        private String documentType;

        /**
         * 审核状态
         */
        private JumioStatus status;

        /**
         * 发行国家
         */
        private String issuingCountry;

        /**
         * 过期时间
         */
        private String expiryDate;

        /**
         * 名
         */
        private String firstName;

        /**
         * 姓
         */
        private String lastName;

        /**
         * 生日
         */
        private String dob;

        /**
         * 证件号码
         */
        private String number;

        /**
         * 邮编
         */
        private String postalCode;

        /**
         * 城市
         */
        private String city;

        /**
         * 证件地址
         */
        private String address;

        /**
         * 来源
         */
        private String source;

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