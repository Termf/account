package com.binance.account.data.mapper.certificate;

import java.util.List;

import com.binance.account.common.query.UserRiskRatingQuery;
import com.binance.account.data.entity.certificate.UserRiskRating;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface UserRiskRatingMapper {

    int insertSelective(UserRiskRating record);

    List<UserRiskRating> selectByPage(UserRiskRatingQuery record);
    
    int getListCount(UserRiskRatingQuery record);

}