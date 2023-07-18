package com.binance.account.data.mapper.tradelevel;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.tradelevel.TradeLevel;
import com.binance.master.annotations.DefaultDB;

/**
 * @author lufei
 * @date 2018/11/16
 */
@DefaultDB
public interface TradeLevelMapper {

    List<TradeLevel> selectList();

    TradeLevel selectById(@Param("id") Long id);

    Integer save(TradeLevel level);

    Integer update(TradeLevel level);

    Integer delete(@Param("id") Long id);

    TradeLevel selectByLevel(@Param("level") Integer level);
}
