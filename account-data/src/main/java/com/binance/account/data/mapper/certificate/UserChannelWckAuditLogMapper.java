package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserChannelWckAuditLog;
import com.binance.account.data.entity.certificate.UserWckAuditLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mikiya.chen
 * @date 2020/3/3 4:50 下午
 */
@DefaultDB
public interface UserChannelWckAuditLogMapper {

    int insertSelective(UserChannelWckAuditLog record);

    UserWckAuditLog selectByPrimaryKey(Long id);

    List<UserChannelWckAuditLog> selectByCaseIds(@Param("caseIds") List<String> caseIds);

    List<UserChannelWckAuditLog> selectByAuditorId(@Param("auditorId") Long auditorId,@Param("auditorSeq") Integer auditorSeq);

    int updateByPrimaryKeySelective(UserChannelWckAuditLog record);

    int deleteByCaseId(@Param("caseId")String caseId);
}
