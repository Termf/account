package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.UserChannelRiskCountryQuery;
import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.certificate.UserChannelRiskCountry;
import com.binance.master.annotations.DefaultDB;
import java.util.List;

@DefaultDB
public interface UserChannelRiskCountryMapper {
    int deleteByPrimaryKey(@Param("countryCode") String countryCode,@Param("channelCode") String channelCode);

    int insert(UserChannelRiskCountry record);

    int insertSelective(UserChannelRiskCountry record);

    UserChannelRiskCountry selectByPrimaryKey(@Param("countryCode") String countryCode,@Param("channelCode") String channelCode);

    int updateByPrimaryKeySelective(UserChannelRiskCountry record);

    int updateByPrimaryKey(UserChannelRiskCountry record);

    long queryCount(UserChannelRiskCountryQuery query);

    List<UserChannelRiskCountry> query(UserChannelRiskCountryQuery query);
}