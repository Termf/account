package com.binance.account.data.mapper.datamigration;

import com.binance.account.data.entity.datamigration.SynchronTask;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@DefaultDB
public interface SynchronTaskMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SynchronTask record);

    int insertSelective(SynchronTask record);

    SynchronTask selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SynchronTask record);

    int updateByPrimaryKey(SynchronTask record);

    SynchronTask getFrontSynchronTask(@Param("minute") Long minute);

    int updateHeartbeat(@Param("id") Long id, @Param("updateTime") Date updateTime);

    long queryCount();

    int deleteAll();
}
