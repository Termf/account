package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserChainAddressAudit;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserChainAddressAuditMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserChainAddressAudit record);

    int insertSelective(UserChainAddressAudit record);

    UserChainAddressAudit selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserChainAddressAudit record);

    List<UserChainAddressAudit> getAuditPage(Map<String, Object> query);

    int getAuditCount(Map<String, Object> query);

    int updateAllRecordStatus(UserChainAddressAudit userChainAddressAudit);

    int resetToPending(UserChainAddressAudit userChainAddressAudit);

    List<Map<String, Object>> getProcessingCount(@Param("userId") Long userId);

    List<UserChainAddressAudit> selectRejectRecByAddress(String address);
}