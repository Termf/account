package com.binance.account.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author alex
 */
@Data
public class UserAddressVo implements Serializable {


    private static final long serialVersionUID = 124124923492018347L;

    public enum Status {

        /**
         * 等待上传
         */
        PENDING,

        /**
         * 后台审核通过
         */
        PASSED,

        /**
         * 后台审核拒绝
         */
        REFUSED,

        /**
         * 已过期
         */
        EXPIRED,

        /**
         * 已过期
         */
        CANCELLED,
        
        /**
         * 5 未审核
         */
        WAITING,
    }

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
     * 名
     */
    private String firstName;

    /**
     * 姓
     */
    private String lastName;

    /**
     * 地址
     */
    private String streetAddress;

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

    /**
     * 审核状态
     */
    private UserAddressVo.Status status;

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
     * 地址文件
     */
    private String addressFile;

    /**
     * 资金来源
     */
    private String sourceOfFund;

    /**
     * 预计每月交易额
     */
    private String estimatedTradeVolume;

    /**
     * 审核者
     */
    private String approver;

    /**
     * 审核时间
     */
    private Date approveTime;

}