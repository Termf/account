package com.binance.account.data.mapper.certificate;

import java.util.List;

import com.binance.account.common.query.UserCertificateListRequest;
import com.binance.account.data.entity.certificate.UserCertificate;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserCertificateMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserCertificate record);

    int insertSelective(UserCertificate record);

    UserCertificate selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserCertificate record);

    int updateByPrimaryKey(UserCertificate record);

    int insertIgnore(UserCertificate record);
    
    int getListCount(UserCertificateListRequest request);
    
    List<UserCertificate> selectByPage(UserCertificateListRequest request);
}
