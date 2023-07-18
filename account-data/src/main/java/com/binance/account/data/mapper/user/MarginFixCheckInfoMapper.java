package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.MarginFixCheckInfo;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface MarginFixCheckInfoMapper {
    int deleteByPrimaryKey(Long rootUserId);

    int insert(MarginFixCheckInfo record);

    int insertSelective(MarginFixCheckInfo record);

    MarginFixCheckInfo selectByPrimaryKey(Long rootUserId);

    int updateByPrimaryKeySelective(MarginFixCheckInfo record);

    int updateByPrimaryKey(MarginFixCheckInfo record);


    MarginFixCheckInfo selectByOldMarginUserId(@Param("oldMarginUserId") Long oldMarginUserId);

}