package com.binance.account.data.mapper.tradelevel;

import com.binance.account.data.entity.tradelevel.TradeLevel;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

/**
 * @author lufei
 * @date 2018/11/16
 */
@DefaultDB
public interface TradeLevelFuturesMapper {

    List<TradeLevel> selectFuturesList();
}
