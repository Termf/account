package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserReferralSettings;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhenlei sun
 */
@DefaultDB
public interface UserReferralSettingsMapper {

    /**
     * 新增记录
     *
     * @param record
     * @return
     */
    int insert(UserReferralSettings record);

    /**
     * 新增记录
     *
     * @param userId
     * @return
     */
    UserReferralSettings queryByUserId(@Param("userId") Long userId);
}
