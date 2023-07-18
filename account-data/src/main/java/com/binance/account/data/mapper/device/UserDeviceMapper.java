package com.binance.account.data.mapper.device;

import com.binance.account.data.entity.device.UserDevice;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.javasimon.aop.Monitored;

import java.util.List;

@DefaultDB
@Monitored
public interface UserDeviceMapper {
    int deleteByPrimaryKey(Long id);

    int insertSelective(UserDevice record);

    UserDevice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDevice record);

    List<UserDevice> selectByUserIdAndAgentType(@Param("userId") Long userId, @Param("agentType") String agentType,
                                                @Param("status") Integer status, @Param("source") String source,
                                                @Param("excludeSource") String excludeSource, @Param("isDel") Byte isDel,
                                                @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countByUserIdAndAgentType(@Param("userId") Long userId, @Param("agentType") String agentType,
                                   @Param("status") Integer status, @Param("source") String source,
                                   @Param("excludeSource") String excludeSource, @Param("isDel") Byte isDel);
    /**
     * 查询已授权的设备
     */
    List<UserDevice> selectAuthorizedDevices(@Param("userId") Long userId, @Param("agentType") String agentType);

    UserDevice selectById(@Param("userId") Long userId, @Param("id") Long id);

    UserDevice selectUserLastLoginDevice(@Param("userId")Long userId);
}