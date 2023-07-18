package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.SnapShotShareConfig;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface SnapShotShareConfigMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SnapShotShareConfig record);

    int insertSelective(SnapShotShareConfig record);

    SnapShotShareConfig selectByPrimaryKey(Integer id);

    List<SnapShotShareConfig> selectAllSnapShotConfig(@Param("language") String language,@Param("type") Integer type);

    int updateByPrimaryKeySelective(SnapShotShareConfig record);

    int updateByPrimaryKey(SnapShotShareConfig record);
}