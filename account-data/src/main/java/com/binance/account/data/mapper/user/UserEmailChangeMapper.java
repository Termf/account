package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserEmailChange;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserEmailChangeMapper {

    void insertSelective(UserEmailChange record);

    /**
     * 查找未完成的流程
     *
     * @param userId
     * @return
     */
    List<UserEmailChange> findUndoneByUserId(@Param("userId")Long userId);

    List<UserEmailChange> findUndoneWithHours(@Param("hour") int hour);

    void updateUserEmailChangeByFlowId(UserEmailChange record);

    UserEmailChange findByFlowId(@Param("flowId") String flowId);

    Integer countByUserIdAndStatus(@Param("userId") Long userId,@Param("status") Byte status);

    void updateStatusCancelByHour(@Param("updatedAt") Date updatedAt,@Param("hour") int hour);

    Integer totalCount(Map<String,Object> map);

    List<UserEmailChange> findList(Map<String,Object> map);
}
