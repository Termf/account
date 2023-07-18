package com.binance.account.data.mapper.device;

import com.binance.account.data.entity.device.UserDeviceRelation;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface UserDeviceRelationMapper {

    int insert(UserDeviceRelation record);

    int insertIgnoreSelective(UserDeviceRelation record);

    int updateByPrimaryKeySelective(UserDeviceRelation record);

    int updateByPrimaryKey(UserDeviceRelation record);

    List<UserDeviceRelation> selectRelation(@Param("devicePk") Long devicePk);
}
