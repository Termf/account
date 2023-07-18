package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.KycTaxidIndex;
import com.binance.master.annotations.DefaultDB;
@DefaultDB
public interface KycTaxidIndexMapper {
    int deleteByPrimaryKey(String taxId);

    int insert(KycTaxidIndex record);

    int insertSelective(KycTaxidIndex record);

    KycTaxidIndex selectByPrimaryKey(String taxId);

    int updateByPrimaryKeySelective(KycTaxidIndex record);

    int updateByPrimaryKey(KycTaxidIndex record);
}