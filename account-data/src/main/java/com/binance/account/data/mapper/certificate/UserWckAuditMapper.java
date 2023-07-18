package com.binance.account.data.mapper.certificate;

import com.binance.account.common.enums.WckStatus;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.master.annotations.DefaultDB;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserWckAuditMapper {

    int insertSelective(UserWckAudit record);

    UserWckAudit selectByPrimaryKey(Long kycId);

    /**
     * 查询初始状态的数据，用于job处理
     */
    List<UserWckAudit> selectInitialRows(@Param("createTimeStart")Date createTimeStart, @Param("createTimeEnd")Date createTimeEnd);

    List<Map<String, Object>> selectByAdmin(@Param("userId") Long userId, @Param("status")WckStatus status,
                                            @Param("auditorId") Long auditorId, @Param("auditorSeq") Integer auditorSeq);

    int updateByPrimaryKeySelective(UserWckAudit record);

    int deleteByKycId(@Param("kycId") Long kycId);

    /**
     * 根据KYC ID LIST 查询
     * @param kycIds
     * @return
     */
    List<UserWckAudit> selectByKycIds(@Param("kycIds") Collection<Long> kycIds);
    
    List<Map<String, Object>> selectWckAuditInfo(@Param("userId") Long userId, @Param("status") WckStatus status,
            @Param("auditorId") Long auditorId, @Param("auditorSeq") Integer auditorSeq,
            @Param("country") String country);

    List<Map<String, Object>> selectWckAuditInfoByPage(@Param("userId") Long userId, @Param("status") WckStatus status,
            @Param("auditorId") Long auditorId, @Param("auditorSeq") Integer auditorSeq,
            @Param("country") String country, @Param("start") Integer start, @Param("offset") Integer offset);

    Integer countWckAuditInfo(@Param("userId") Long userId, @Param("status") WckStatus status,
            @Param("auditorId") Long auditorId, @Param("auditorSeq") Integer auditorSeq,
            @Param("country") String country);
    
    Integer selectCountsByStatus(@Param("status")WckStatus status, @Param("auditorId") Long auditorId, 
            @Param("auditorSeq") Integer auditorSeq, @Param("country") String country);
}