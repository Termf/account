package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.TaxIdBlacklistQuery;
import com.binance.account.data.entity.certificate.TaxIdBlacklist;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface TaxIdBlacklistMapper {

    int insertSelective(TaxIdBlacklist record);

    List<TaxIdBlacklist> queryList(TaxIdBlacklistQuery query);

    int queryCount(TaxIdBlacklistQuery query);

    TaxIdBlacklist getBlacklistByTaxId(String taxId);

    int deleteByTaxId(String taxId);

}