package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserChannelRiskRatingExtend;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserChannelRiskRatingExtendMapper {

    int insertSelective(UserChannelRiskRatingExtend record);

    UserChannelRiskRatingExtend selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserChannelRiskRatingExtend record);

}