package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserMobileIndexMapper {
    UserMobileIndex selectByPrimaryKey(@Param("mobile") String mobile, @Param("country") String country);

    int deleteByPrimaryKey(@Param("mobile") String mobile, @Param("country") String country);

    int insert(UserMobileIndex record);

    int insertSelective(UserMobileIndex record);

    int insertIgnore(UserMobileIndex record);

    int updateSelective(UserMobileIndex record);

    UserMobileIndex selectByMobile(@Param("mobile") String mobile);
}
