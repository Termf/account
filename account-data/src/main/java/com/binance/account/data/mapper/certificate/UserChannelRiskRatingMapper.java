package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserChannelRiskRatingMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserChannelRiskRating record);

    UserChannelRiskRating selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserChannelRiskRating record);
    
    void resetTierLevel(UserChannelRiskRating record);

    List<UserChannelRiskRating> getPageList(UserChannelRiskRatingQuery query);

    long getPageCount(UserChannelRiskRatingQuery query);

    UserChannelRiskRating selectByUk(@Param("userId")Long userId,@Param("channelCode")String channelCode);
    
    int auditWckPass(UserChannelRiskRating record);
    
    int updateRiskLevelScore(UserChannelRiskRating record);
    
    List<UserChannelRiskRating> selectByUserId(@Param("userId")Long userId);
    
    int updateTierLevel(UserChannelRiskRating record);
    
    int updateLimit(UserChannelRiskRating record);

    void resetWckStatusByUserId(@Param("userId")Long userId);
    
}
