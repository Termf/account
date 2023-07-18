package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.UserChannelRiskRatingRuleHistory;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface UserChannelRiskRatingRuleHistoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserChannelRiskRatingRuleHistory record);

    int insertBatch(@Param("histories") List<UserChannelRiskRatingRuleHistory> record);

    UserChannelRiskRatingRuleHistory selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserChannelRiskRatingRuleHistory record);

    int updateByPrimaryKey(UserChannelRiskRatingRuleHistory record);
}
