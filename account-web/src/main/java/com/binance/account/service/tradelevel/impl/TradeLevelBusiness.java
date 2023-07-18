package com.binance.account.service.tradelevel.impl;

import java.util.ArrayList;
import java.util.List;

import com.binance.account.data.mapper.tradelevel.TradeLevelFuturesMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.tradelevel.TradeLevel;
import com.binance.account.data.mapper.tradelevel.TradeLevelMapper;
import com.binance.account.service.tradelevel.ITradeLevel;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.account.vo.user.TradeLevelVo;
import com.binance.account.vo.user.request.TradeLevelRequest;
import com.binance.account.vo.user.request.TradeSingleLevelRequest;
import com.binance.master.error.BusinessException;

/**
 * @author lufei
 * @date 2018/11/16
 */
@Service
public class TradeLevelBusiness implements ITradeLevel {

    @Autowired
    private TradeLevelMapper levelMapper;
    @Autowired
    private TradeLevelFuturesMapper tradeLevelFuturesMapper;

    @Override
    public List<TradeLevelVo> manageList() {
        List<TradeLevel> models = levelMapper.selectList();
        List<TradeLevelVo> vos = new ArrayList<>(models.size());
        for (TradeLevel level : models) {
            TradeLevelVo vo = new TradeLevelVo();
            BeanUtils.copyProperties(level, vo);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public List<TradeLevelVo> futuresManageList() {
        List<TradeLevel> models = tradeLevelFuturesMapper.selectFuturesList();
        List<TradeLevelVo> vos = new ArrayList<>(models.size());
        for (TradeLevel level : models) {
            TradeLevelVo vo = new TradeLevelVo();
            BeanUtils.copyProperties(level, vo);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public TradeLevelVo manageInfo(IdLongRequest request) {
        TradeLevel level = levelMapper.selectById(request.getId());
        TradeLevelVo vo = new TradeLevelVo();
        BeanUtils.copyProperties(level, vo);
        return vo;
    }

    @Override
    public void manageAdd(TradeLevelRequest request) {
        TradeLevelVo vo = request.getVo();
        TradeLevel level = new TradeLevel();
        BeanUtils.copyProperties(vo, level);
        levelMapper.save(level);
    }

    @Override
    public void manageUpdate(TradeLevelRequest request) {
        TradeLevelVo vo = request.getVo();
        TradeLevel level = levelMapper.selectById(vo.getId());
        if (level == null) {
            throw new BusinessException("不存在记录");
        }
        TradeLevel newLevel = new TradeLevel();
        BeanUtils.copyProperties(vo, newLevel);
        levelMapper.update(newLevel);
    }

    @Override
    public void manageDelete(IdLongRequest request) {
        levelMapper.delete(request.getId());
    }

    @Override
    public TradeLevelVo selectByLevel(TradeSingleLevelRequest request) {
        TradeLevel level = levelMapper.selectByLevel(request.getLevel());
        if (level == null) {
            return null;
        }
        TradeLevelVo newLevel = new TradeLevelVo();
        BeanUtils.copyProperties(level, newLevel);
        return newLevel;
    }

}
