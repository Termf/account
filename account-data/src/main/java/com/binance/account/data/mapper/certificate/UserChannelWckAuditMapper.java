package com.binance.account.data.mapper.certificate;

import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.data.entity.certificate.UserChannelWckAudit;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mikiya.chen
 * @date 2020/3/3 4:09 下午
 */
@DefaultDB
public interface UserChannelWckAuditMapper {

    int insertSelective(UserChannelWckAudit record);

    List<Map<String, Object>> selectChannelWckAuditInfoByPage(@Param("userId") Long userId, @Param("status") WckChannelStatus status,
           @Param("firstAuditorId") Long firstAuditorId, @Param("secondAuditorId") Long secondAuditorId, @Param("auditorSeq") Integer auditorSeq,
           @Param("country") String country, @Param("start") Integer start, @Param("offset") Integer offset);

    Integer countChannelWckAuditInfo(@Param("userId") Long userId, @Param("status") WckChannelStatus status,
           @Param("firstAuditorId") Long firstAuditorId, @Param("secondAuditorId") Long secondAuditorId, @Param("auditorSeq") Integer auditorSeq,
              @Param("country") String country);

    List<UserChannelWckAudit> selectByUserId(@Param("userId") Long userId);

    UserChannelWckAudit selectByCaseId(@Param("caseId") String caseId);

    List<UserChannelWckAudit> selectInitialRows(@Param("createTimeStart") Date createTimeStart, @Param("createTimeEnd")Date createTimeEnd);

    List<UserChannelWckAudit> selectByUserIdAndStatusInPage(@Param("userId") Long userId, @Param("status") WckChannelStatus status, @Param("start") Integer start, @Param("offset") Integer offset);

    Integer countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") WckChannelStatus status);

    int updateByPrimaryKeySelective(UserChannelWckAudit record);
}
