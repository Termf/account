package com.binance.account.data.mapper.datamigration;

import com.binance.account.data.entity.datamigration.Synchron;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface SynchronMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(Synchron record);

    int insertSelective(Synchron record);

    Synchron selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(Synchron record);

    int updateByPrimaryKey(Synchron record);

    Long getMaxUserId(@Param("minId") Long minId, @Param("maxId") Long maxId);
}
