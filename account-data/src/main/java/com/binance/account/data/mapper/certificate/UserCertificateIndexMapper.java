package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserCertificateIndexMapper {
    int deleteByPrimaryKey(@Param("number") String number, @Param("country") String country, @Param("type") String type,@Param("userId") Long userId);

    int deleteConsiderTypeNull(@Param("number") String number, @Param("country") String country, @Param("type") String type,@Param("userId") Long userId);

    int insertIgnore(UserCertificateIndex record);

    @Deprecated
    UserCertificateIndex selectByPrimaryKey(@Param("number") String number, @Param("country") String country, @Param("type") String type);
    @Deprecated
    int updateByPrimaryKeySelective(UserCertificateIndex record);

    List<UserCertificateIndex> selectCertificate(@Param("number") String number, @Param("country") String country, @Param("type") String type);

    List<UserCertificateIndex> selectCertificateByNumber(@Param("number") String number);

    int updateCertificateType(UserCertificateIndex record);


}
