package com.binance.account.data.entity.certificate;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-12-04 18:17
 */
@Setter
@Getter
public class KycCertificateResult implements Serializable {
    private static final long serialVersionUID = 8264649552539603627L;

    public static final int STATUS_REVIEW = 0;
    public static final int STATUS_PASS = 1;
    public static final int STATUS_REFUSED = 2;
    public static final int TYPE_USER = 1;
    public static final int TYPE_COMPANY = 2;


    /** 0:审核中 , 1:通过 2: 拒绝 null:未验证 */
    private Integer certificateStatus;
    /** 错误原因标识语 (需要转换) */
    private String certificateMessage;
    /** 1-个人认证 2-企业认证 */
    private Integer certificateType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String companyName;
    private String country;
    private String city;
    private String address;
    private Date dob;
    private String postalCode;

    /** 企业认证或者个人认证记录ID */
    private Long certificateId;

    /** 是否为 不合规国籍认证通过的数据 */
    private boolean forbidPassed;
    
    private Date updateTime;
    
    /** 标记是否是新流程*/
    private boolean newVersion = false;
    
}
