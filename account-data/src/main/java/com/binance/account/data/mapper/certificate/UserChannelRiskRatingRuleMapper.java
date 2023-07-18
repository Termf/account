package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserChannelRiskRatingRule;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface UserChannelRiskRatingRuleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserChannelRiskRatingRule record);

    int insertSelective(UserChannelRiskRatingRule record);

    UserChannelRiskRatingRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserChannelRiskRatingRule record);

    int updateByPrimaryKey(UserChannelRiskRatingRule record);

    List<UserChannelRiskRatingRule> selectByUserIdAndChannelCode(@Param("userId") Long userId, @Param("channelCode") String channelCode);

    int updateRiskLevelScore(UserChannelRiskRatingRule record);

    List<UserChannelRiskRatingRule> selectByUserIdAndRiskRatingId(@Param("userId") Long userId, @Param("riskRatingId") Integer riskRatingId);
}
