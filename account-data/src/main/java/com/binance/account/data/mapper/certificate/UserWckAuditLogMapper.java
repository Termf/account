package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserWckAuditLog;
import com.binance.master.annotations.DefaultDB;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserWckAuditLogMapper {

    int insertSelective(UserWckAuditLog record);

    UserWckAuditLog selectByPrimaryKey(Long id);

    List<UserWckAuditLog> selectByKycIds(@Param("kycIds") List<Long> kycIds);

    List<UserWckAuditLog> selectByAuditorId(@Param("auditorId") Long auditorId,@Param("auditorSeq") Integer auditorSeq);

    int updateByPrimaryKeySelective(UserWckAuditLog record);

    int deleteByKycId(@Param("kycId") Long kycId);
}