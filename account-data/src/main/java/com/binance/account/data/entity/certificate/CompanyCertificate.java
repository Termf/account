package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CompanyCertificate extends ToString {

    private static final long serialVersionUID = 5975053744616160946L;

    private Long id;

    private Long userId;

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

    private String jumioId;

    private Integer redoJumio;

    /** JUMIO 的唯一标识 */
    private String scanReference;

    /** JUMIO 的状态 */
    private String jumioStatus;

    /** 人脸识别状态 */
    private String faceStatus;

    /** 人脸识别备注信息 */
    private String faceRemark;

    /**
     * 初始化记录的时候，检查当前是否存在有提币人脸识别标识，如果存在，把最后一笔提币人脸识别的提币ID关联上
     */
    private String transFaceLogId;

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
