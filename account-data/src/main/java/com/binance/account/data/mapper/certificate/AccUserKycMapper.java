package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.AccUserKyc;
import com.binance.account.data.entity.certificate.AccUserKycExample;
import java.util.List;

import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface AccUserKycMapper {
    long countByExample(AccUserKycExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AccUserKyc record);

    int insertSelective(AccUserKyc record);

    List<AccUserKyc> selectByExample(AccUserKycExample example);

    AccUserKyc selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AccUserKyc record, @Param("example") AccUserKycExample example);

    int updateByExample(@Param("record") AccUserKyc record, @Param("example") AccUserKycExample example);

    int updateByPrimaryKeySelective(AccUserKyc record);

    int updateByPrimaryKey(AccUserKyc record);
}