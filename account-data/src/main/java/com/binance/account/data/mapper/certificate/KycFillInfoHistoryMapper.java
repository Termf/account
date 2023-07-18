package com.binance.account.data.mapper.certificate;

import com.binance.account.data.entity.certificate.KycFillInfoHistory;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@DefaultDB
public interface KycFillInfoHistoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(KycFillInfoHistory record);

    int insertSelective(KycFillInfoHistory record);

    KycFillInfoHistory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(KycFillInfoHistory record);

    int updateByPrimaryKey(KycFillInfoHistory record);

    /**
     * 计算某一时间范围内请求的次数
     * @param userId
     * @param fillType
     * @param startTime
     * @param endTime
     * @return
     */
    int getHistoryCount(@Param("userId") Long userId,
                        @Param("fillType") String fillType,
                        @Param("startTime")Date startTime,
                        @Param("endTime") Date endTime);

    /**
     * 获取某一用户的提交历史
     * @param userId
     * @param fillType
     * @return
     */
    List<KycFillInfoHistory> getHistories(@Param("userId") Long userId,
                                          @Param("fillType") String fillType);
}