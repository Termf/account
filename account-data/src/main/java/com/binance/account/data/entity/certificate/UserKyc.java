package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.KycStatus;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
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
public class UserKyc implements Serializable {


    private static final long serialVersionUID = -3611921170806368269L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 审核状态
     */
    private KycStatus status;

    /**
     * 用户填写的信息
     */
    private BaseInfo baseInfo;

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

    /**
     * jumio_id
     */
    private String jumioId;

    /**
     * JUMIO 的scanReference
     */
    private String scanReference;

    /**
     * JUMIO认证的状态
     */
    private String checkStatus;

    /** 人脸识别状态 */
    private String faceStatus;

    /** 人脸识别备注信息 */
    private String faceRemark;

    /**
     * 用于在初始化的时候检查是否当前打开了提币人脸，如果打开了，查询最后一笔提币人脸的信息把提币数据关联起来
     */
    private String transFaceLogId;

    /**
     * face idcard ocr status
     */
    private String faceOcrStatus;

    /**
     * face idcard ocr remark
     */
    private String faceOcrRemark;


    public static boolean validateBaseInfo(UserKyc.BaseInfo baseInfo) {
        if (StringUtils.isBlank(baseInfo.getFirstName())) {
            return false;
        }
        if (StringUtils.isBlank(baseInfo.getLastName())) {
            return false;
        }
        if (baseInfo.getDob() == null) {
            return false;
        }
        if (StringUtils.isBlank(baseInfo.getAddress())) {
            return false;
        }
        if (StringUtils.isBlank(baseInfo.getCity())) {
            return false;
        }
        if (StringUtils.isBlank(baseInfo.getCountry())) {
            return false;
        }
        return true;
    }

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
         * 国家
         */
        private String country;

    }

    /**
     * 添加备注，多条以分号间隔
     */
    public void appendMemo(String memo){
        if (StringUtils.isBlank(this.memo)){
            this.memo = memo;
        }else if (StringUtils.isNotBlank(memo)){
            this.memo = this.memo + ";" + memo;
        }
        this.memo = StringUtils.left(this.memo, 512);
    }

    /**
     * 获取填写的名字(lastName + firstName)
     */
    public String getFillName(){
        return StringUtils.join(baseInfo.firstName, " ", baseInfo.lastName).trim();
    }


    /**
     * 获取jumio返回的名字(lastName + firstName)
     */
    public String getCheckName(Jumio jumio){
        if (jumio != null){
            return StringUtils.join(jumio.getFirstName(), " ", jumio.getLastName()).replaceAll("N/A", "").trim();
        }
        return null;
    }

    /**
     * 尝试获取出生日期
     */
    public String tryGetDob(Jumio jumio){
        String birthDate = jumio==null ? null : jumio.getDob();
        if (StringUtils.isBlank(birthDate) && baseInfo!=null && baseInfo.getDob()!=null){
            birthDate = DateUtils.formatter(baseInfo.getDob(), DateUtils.SIMPLE_PATTERN);
        }
        return birthDate;
    }
    
    public boolean isOcrFlow() {
    	return StringUtils.isNoneBlank(faceOcrStatus);
    }
}