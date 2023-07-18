package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.master.utils.DateUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author lw
 *         <p>
 *         2018/04/28
 */
@Data
public class UserKycApprove implements Serializable {


    private static final long serialVersionUID = 4219735148370374408L;

    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 用户填写的信息
     */
    private BaseInfo baseInfo;

    /**
     * 认证时间
     */
    private Date approveTime;

    /**
     * 备注（审核人填写）
     */
    private String memo;

    /**
     * jumio_id
     */
    private String jumioId;

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

    /**
     * 名(证件信息)
     */
    private String certificateFirstName;

    /**
     * 姓(证件信息)
     */
    private String certificateLastName;

    /**
     * 生日(证件信息)
     */
    private String certificateDob;

    /**
     * 国家(证件信息)
     */
    private String certificateCountry;
    
    /**
	 * 证件发布日期
	 */
	private String certificateIssuingDate;

    private Set<Long> userIds;

    private Date startTime;

    private Date endTime;

    private String moveMsg;

    /**
     * 用户检查
     */
    private String faceCheck;

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

    /**
     * 个人的KYC_APPROVE
     * @param userKyc
     * @return
     */
    public static UserKycApprove toKycApprove(UserKyc userKyc){
        UserKycApprove kycApprove = new UserKycApprove();
        kycApprove.setCertificateType(KycCertificateResult.TYPE_USER);
        kycApprove.setCertificateId(userKyc.getId());
        kycApprove.setApproveTime(new Date());
        kycApprove.setBaseInfo(new UserKycApprove.BaseInfo());
        BeanUtils.copyProperties(userKyc, kycApprove);
        if(userKyc.getBaseInfo()!=null){
            BeanUtils.copyProperties(userKyc.getBaseInfo(), kycApprove.getBaseInfo());
        }
        return kycApprove;
    }

    /**
     * 企业的KYC_APPROVE
     * @param companyCertificate
     * @return
     */
    public static UserKycApprove toKycApprove(CompanyCertificate companyCertificate) {
        UserKycApprove kycApprove = new UserKycApprove();
        kycApprove.setCertificateType(KycCertificateResult.TYPE_COMPANY);
        kycApprove.setCertificateId(companyCertificate.getId());
        kycApprove.setApproveTime(DateUtils.getNewUTCDate());
        kycApprove.setUserId(companyCertificate.getUserId());
        kycApprove.setJumioId(companyCertificate.getJumioId());
        kycApprove.setScanReference(companyCertificate.getScanReference());
        UserKycApprove.BaseInfo baseInfo = new UserKycApprove.BaseInfo();
        baseInfo.setCountry(companyCertificate.getCompanyCountry());
        kycApprove.setBaseInfo(baseInfo);
        return kycApprove;
    }


    /**
     * 企业的KYC_APPROVE
     * @return
     */
    public static UserKycApprove toKycApprove(KycCertificate kycCertificate ,KycFillInfo kycFillInfo,String scanReference) {
        UserKycApprove kycApprove = new UserKycApprove();
        kycApprove.setUserId(kycCertificate.getUserId());
        UserKycApprove.BaseInfo baseInfo = new UserKycApprove.BaseInfo();
        if(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
        	kycApprove.setCertificateType(KycCertificateResult.TYPE_COMPANY);
        	baseInfo.setCountry(kycFillInfo.getCountry());
        }else {
        	kycApprove.setCertificateType(KycCertificateResult.TYPE_USER);
        	baseInfo.setFirstName(kycFillInfo.getFirstName());
        	baseInfo.setLastName(kycFillInfo.getLastName());
        	baseInfo.setMiddleName(kycFillInfo.getMiddleName());
        	try {
        		Date dob = StringUtils.isBlank(kycFillInfo.getBirthday())?null:DateUtils.formatter(kycFillInfo.getBirthday(), "yyyy-MM-dd");
        		baseInfo.setDob(dob);
        	}catch(Exception e) {
        		baseInfo.setDob(null);
        	}
        	baseInfo.setAddress(kycFillInfo.getAddress());
        	baseInfo.setPostalCode(kycFillInfo.getPostalCode());
        	baseInfo.setCity(kycFillInfo.getCity());
        	baseInfo.setCountry(kycFillInfo.getCountry());
        }
        kycApprove.setBaseInfo(baseInfo);
        kycApprove.setCertificateId(kycFillInfo.getId());
        kycApprove.setApproveTime(new Date());
        kycApprove.setScanReference(scanReference);
        kycApprove.setJumioId(null);

        return kycApprove;
    }


    /**
     * 老版本的KYC 已经不再使用
     * @param certificate
     * @return
     */
    @Deprecated
    public static UserKycApprove toKycApprove(UserCertificate certificate){
        UserKycApprove kycApprove = new UserKycApprove();
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.setFirstName(certificate.getFirstName());
        baseInfo.setLastName(certificate.getLastName());
        baseInfo.setCountry(certificate.getCountry());
        kycApprove.setBaseInfo(baseInfo);
        kycApprove.setUserId(certificate.getUserId());
        kycApprove.setApproveTime(new Date());
        return kycApprove;
    }
}
