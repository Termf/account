package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface KycCertificateMapper {

    int insert(KycCertificate record);


    KycCertificate selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(KycCertificate record);

    int updateStatus(KycCertificate record);

    int updateJumioStatus(KycCertificate kycCertificate);

    int updateJumioAndBaseStatus(KycCertificate kycCertificate);

    int updateFaceStatus(KycCertificate kycCertificate);

    int updateFaceOcrStatus(KycCertificate kycCertificate);

    int updateFiatPtStatus(KycCertificate kycCertificate);

    long getListCount(KycCertificateQuery query);

    List<KycCertificate> getList(KycCertificateQuery query);

    int updateKycType(KycCertificate record);

    int updateJumioStatusWithFace(KycCertificate kycCertificate);

    int updateLockOne(KycCertificate kycCertificate);

    int deleteByPk(@Param("userId") Long userId);

    List<KycCertificate> queryByIdList(@Param("userIds") List<Long> userIds);
    
    int updateFaceOcrPassStatus(KycCertificate record);
}
