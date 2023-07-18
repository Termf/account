package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.KycRefQuery;
import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface KycFillInfoMapper {
    int deleteByUk(@Param("userId") Long userId,@Param("fillType") String fillType);

    int insert(KycFillInfo record);

    int insertSelective(KycFillInfo record);

    KycFillInfo selectByUserIdFillType(@Param("userId") Long userId,@Param("fillType") String fillType);

    int updateByUkSelective(KycFillInfo record);

    int updateNameByUk(KycFillInfo record);

    long kycRefQueryCount(KycRefQuery kycRefQuery);

    List<KycFillInfo> kycRefQueryList(KycRefQuery kycRefQuery);

    int updateFillName(KycFillInfo record);

    int updateAdditionalByUk(KycFillInfo kycFillInfo);
}
