package com.binance.account.service.tradelevel;

import java.util.List;

import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.TradeLevelVo;
import com.binance.account.vo.user.request.TradeLevelRequest;
import com.binance.account.vo.user.request.TradeSingleLevelRequest;

/**
 * @author lufei
 * @date 2018/11/16
 */
public interface ITradeLevel {

    List<TradeLevelVo> manageList();

    List<TradeLevelVo> futuresManageList();

    TradeLevelVo manageInfo(IdLongRequest request);

    void manageAdd(TradeLevelRequest request);

    void manageUpdate(TradeLevelRequest request);

    void manageDelete(IdLongRequest request);

    TradeLevelVo selectByLevel(TradeSingleLevelRequest request);
}
