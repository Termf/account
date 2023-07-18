package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.JumioStatus;
import com.binance.account.common.enums.JumioType;
import com.binance.master.commons.ToString;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class Jumio extends ToString {

    public static final String NAME_NA = "N/A";

    /**
     * ID
     */
    private String id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户ID
     */
    private JumioType type;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 证件类型
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
     * 自定义的流水号
     */
    private String merchantReference;

    public static Jumio toJumio(UserCertificate certificate){
        Jumio jumio = new Jumio();
        jumio.setUserId(certificate.getUserId());
        jumio.setType(JumioType.user);
        jumio.setLastName(certificate.getLastName());
        jumio.setFirstName(certificate.getFirstName());
        jumio.setIssuingCountry(certificate.getCountry());
        jumio.setNumber(certificate.getNumber());
        jumio.setDocumentType(certificate.getType()==1 ? "ID_CARD" : "PASSPORT");
        jumio.setFront(certificate.getFront());
        jumio.setBack(certificate.getBack());
        jumio.setFace(certificate.getHand());
        jumio.setCreateTime(DateUtils.getNewUTCDate());
        jumio.setUpdateTime(DateUtils.getNewUTCDate());
        jumio.setSource("old_kyc");
        return jumio;
    }

    /**
     * 获取jumio返回的名称，并过滤 N/A
     */
    public String getName(){
        return StringUtils.join(firstName, " ", lastName).replaceAll(NAME_NA, "").trim();
    }
}
