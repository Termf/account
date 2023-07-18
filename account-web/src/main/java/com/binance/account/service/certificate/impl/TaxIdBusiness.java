package com.binance.account.service.certificate.impl;

import com.binance.account.common.query.TaxIdBlacklistQuery;
import com.binance.account.data.entity.certificate.TaxIdBlacklist;
import com.binance.account.data.mapper.certificate.TaxIdBlacklistMapper;
import com.binance.account.service.certificate.ITaxId;
import com.binance.account.vo.certificate.TaxIdBlacklistVo;
import com.binance.master.commons.SearchResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaxIdBusiness implements ITaxId {

    @Resource
    private TaxIdBlacklistMapper taxIdBlacklistMapper;

    @Override
    public SearchResult<TaxIdBlacklistVo> queryTaxIdBlacklist(TaxIdBlacklistQuery query) {
        int count = taxIdBlacklistMapper.queryCount(query);
        if (count == 0) {
            return new SearchResult<>(new ArrayList<>(), 0);
        }
        List<TaxIdBlacklist> dataList = taxIdBlacklistMapper.queryList(query);
        List<TaxIdBlacklistVo> blacklist = dataList.stream().map( t -> {
            TaxIdBlacklistVo vo = new TaxIdBlacklistVo();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
        return new SearchResult<>(blacklist, count);
    }

    /**
     * 把taxId添加到黑名单里
     * @param taxId taxId
     * @param creator 操作者
     * @param remark 备注
     */
    @Override
    public int pushBlacklist(String taxId, String creator, String remark) {
        TaxIdBlacklist blacklist = new TaxIdBlacklist();
        blacklist.setTaxId(taxId);
        blacklist.setCreator(creator);
        blacklist.setRemark(remark);
        return taxIdBlacklistMapper.insertSelective(blacklist);
    }

    /**
     * 根据taxId查询
     * @param taxId taxId
     */
    @Override
    public TaxIdBlacklist getBlacklistByTaxId(String taxId) {
        return taxIdBlacklistMapper.getBlacklistByTaxId(taxId);
    }

    /**
     * 把taxId从黑名单列表移除
     * @param taxId
     * @return
     */
    @Override
    public int removeBlacklist(String taxId) {
        return taxIdBlacklistMapper.deleteByTaxId(taxId);
    }
}
